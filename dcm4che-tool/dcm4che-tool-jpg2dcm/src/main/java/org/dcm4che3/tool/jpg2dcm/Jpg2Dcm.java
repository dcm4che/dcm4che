/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2017
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.tool.jpg2dcm;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

import com.sun.imageio.plugins.jpeg.JPEG;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author gunter zeilinger<gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since May 2017
 */
public class Jpg2Dcm {

    static final Logger LOG = LoggerFactory.getLogger(Jpg2Dcm.class);

    private String charset = "ISO_IR 100";

    private int fileLen;

    private boolean noAPPn;

    private static Options opts;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.jpg2dcm.messages");
    private Attributes keys;
    private String metadataFile;
    private JPEGHeader jpegHeader;
    private File pixelDataFile;
    private byte[] pixelData;

    public Jpg2Dcm() {
    }

    public void convert(Attributes metadata, Jpg2Dcm jpg2Dcm, File dcmFile)
            throws Exception {
        File pixelDataFile = jpg2Dcm.pixelDataFile;
        fileLen = (int) pixelDataFile.length();
        DicomOutputStream dos = null;
        try {
            dos = new DicomOutputStream(dcmFile);
            Date now = new Date();
            metadata.setDate(Tag.InstanceCreationDate, VR.DA, now);
            metadata.setDate(Tag.InstanceCreationTime, VR.TM, now);
            Attributes fmi = metadata.createFileMetaInformation(metadata.getString(Tag.TransferSyntaxUID));
            dos.writeDataset(fmi, metadata);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            if (noAPPn && jpg2Dcm.jpegHeader != null) {
                int off = jpg2Dcm.jpegHeader.offsetAfterAPP();
                dos.writeHeader(Tag.Item, null, fileLen - off + 3);
                dos.write((byte) -1);
                dos.write((byte) JPEG.SOI);
                dos.write((byte) -1);
                dos.write(jpg2Dcm.pixelData);
            } else {
                dos.writeHeader(Tag.Item, null, (fileLen + 1) & ~1);
                dos.write(jpg2Dcm.pixelData);
            }
            if ((fileLen & 1) != 0) {
                dos.write(0);
            }
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        } finally {
            if (dos != null)
                dos.close();
        }
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            Jpg2Dcm jpg2Dcm = new Jpg2Dcm();
            jpg2Dcm.keys = configureKeys(jpg2Dcm, cl);
            LOG.info("added keys for coercion: \n" + jpg2Dcm.keys.toString());
            jpg2Dcm.metadataFile = cl.getOptionValue("f");
            if (cl.getArgs().length < 2)
                throw new MissingArgumentException("Either input pixel data file or output binary file is missing. See example in jpg2dcm help.");
            jpg2Dcm.pixelDataFile = new File(cl.getArgs()[0]);
            Extension ext = getExt(jpg2Dcm.pixelDataFile.getName());
            jpg2Dcm.noAPPn = Boolean.valueOf(cl.getOptionValue("na"));
            Attributes metadata = getMetadata(jpg2Dcm, ext);

            @SuppressWarnings("rawtypes")
            File dcmFile = new File(cl.getArgs()[1]);
            long start = System.currentTimeMillis();
            jpg2Dcm.convert(metadata, jpg2Dcm, dcmFile);
            long fin = System.currentTimeMillis();
            LOG.info("Encapsulated " + jpg2Dcm.pixelDataFile + " to " + dcmFile.getPath() + " in "
                    + (fin - start) + "ms.");
        } catch (ParseException e) {
            System.err.println("jpg2dcm: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            LOG.error("Error: \n", e);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs(2).withArgName("[seq/]attr=value")
                .withValueSeparator().withDescription(rb.getString("metadata"))
                .create("m"));
        opts.addOption("f", "file", true, rb.getString("file"));
        opts.addOption("na","no-appn", true, rb.getString("no-appn"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Jpg2Dcm.class);
    }

    private static Attributes configureKeys(Jpg2Dcm main, CommandLine cl) {
        Attributes temp = new Attributes();
        CLIUtils.addAttributes(temp, cl.getOptionValues("m"));
        return temp;
    }

    private static Attributes getMetadata(Jpg2Dcm jpg2Dcm, Extension ext)
            throws Exception {
        Attributes metadata = defaultMetadata();
        String metadataFile = jpg2Dcm.metadataFile;
        if (metadataFile != null) {
            String metadataFileExt = metadataFile.substring(metadataFile.lastIndexOf(".")+1).toLowerCase();
            if (!metadataFileExt.equals("xml"))
                throw new IllegalArgumentException("Metadata file extension not supported. Read -f option in jpg2dcm help");
            File filePath = new File(metadataFile);
            metadata = SAXReader.parse(filePath.toString());
        }
        coerceAttributes(metadata, jpg2Dcm);
        switch (ext) {
            case jpg:
            case jpeg:
                metadata.setString(Tag.SOPClassUID, VR.UI, metadata.getString(Tag.SOPClassUID, UID.SecondaryCaptureImageStorage));
                metadata.setString(Tag.TransferSyntaxUID, VR.UI, metadata.getString(Tag.TransferSyntaxUID, UID.JPEGBaseline1));
                readPixelHeader(jpg2Dcm, metadata, jpg2Dcm.pixelDataFile, false);
                break;
            case mpg:
            case mpeg:
            case mpg2:
                metadata.setString(Tag.SOPClassUID, VR.UI, metadata.getString(Tag.SOPClassUID, UID.VideoPhotographicImageStorage));
                metadata.setString(Tag.TransferSyntaxUID, VR.UI, metadata.getString(Tag.TransferSyntaxUID, UID.MPEG2));
                readPixelHeader(jpg2Dcm, metadata, jpg2Dcm.pixelDataFile, true);
                break;
        }
        return metadata;
    }

    private static Attributes defaultMetadata() throws ParserConfigurationException, SAXException, IOException {
        LOG.info("Always first set default metadata in the event of required attributes not set/sent by user.");
        Attributes metadata = new Attributes();
        metadata.setString(Tag.PatientName, VR.PN, "JPG2DCM-PatientName");
        metadata.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        metadata.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        metadata.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        return metadata;
    }

    private static void coerceAttributes(Attributes metadata, Jpg2Dcm jpg2Dcm) {
        if (jpg2Dcm.keys.tags().length > 0)
            LOG.info("Coercing the following keys from specified attributes to metadata:");
        metadata.addAll(jpg2Dcm.keys);
        metadata.setString(Tag.SpecificCharacterSet, VR.CS, metadata.getString(Tag.SpecificCharacterSet, jpg2Dcm.charset));
        LOG.info(jpg2Dcm.keys.toString());
    }

    enum Extension {
        mpg, mpg2, mpeg, jpeg, jpg
    }
    private static Extension getExt(String file) {
        String fileExt = file.substring(file.lastIndexOf(".")+1).toLowerCase();
        for (Extension ext : Extension.values())
            if (ext.name().equals(fileExt))
                return ext;
        throw new IllegalArgumentException("Specified file for conversion is not Pixel Data Type. Read jpg2dcm help for supported extension types.");
    }

    private static void readPixelHeader(Jpg2Dcm jpg2Dcm, Attributes metadata, File pixelDataFile, boolean isMpeg)
            throws Exception {
        int fileLen = (int) pixelDataFile.length();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(pixelDataFile);
            bis = new BufferedInputStream(fis);
            byte[] b16384 = new byte[16384];
            StreamUtils.readFully(bis, b16384, 0, 16384);
            jpg2Dcm.pixelData = new byte[fileLen];
            if (isMpeg) {
                MPEGHeader mpegHeader = new MPEGHeader(b16384);
                mpegHeader.toAttributes(metadata, pixelDataFile.length());
                jpg2Dcm.pixelData = Arrays.copyOf(b16384, fileLen);
                StreamUtils.readFully(bis, jpg2Dcm.pixelData, 16384, fileLen-16384);
            } else {
                jpg2Dcm.jpegHeader = new JPEGHeader(b16384, JPEG.SOS);
                jpg2Dcm.jpegHeader.toAttributes(metadata);
                if (jpg2Dcm.noAPPn) {
                    int off = jpg2Dcm.jpegHeader.offsetAfterAPP();
                    byte[] bTemp = Arrays.copyOf(b16384, fileLen);
                    StreamUtils.readFully(bis, bTemp, 16384, fileLen - 16384);
                    jpg2Dcm.pixelData = Arrays.copyOfRange(bTemp, off, fileLen);
                } else {
                    jpg2Dcm.pixelData = Arrays.copyOf(b16384, fileLen);
                    StreamUtils.readFully(bis, jpg2Dcm.pixelData, 16384, fileLen - 16384);
                }
            }
        } finally {
            if (bis != null)
                bis.close();
            if (fis != null)
                fis.close();
        }
    }

}
