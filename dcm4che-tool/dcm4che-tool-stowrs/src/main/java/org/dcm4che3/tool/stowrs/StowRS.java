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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2017
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
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

package org.dcm4che3.tool.stowrs;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Apr 2017
 */
public class StowRS {
    static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private Attributes keys;
    private static Options opts;
    private String URL;
    private boolean noAppn;
    private boolean pixelHeader;
    private JPEGHeader jpegHeader;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");
    private BulkData defaultBulkdata = new BulkData(null, "bulk", false);

    private static final String boundary = "myboundary";

    public StowRS() {
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs(2).withArgName("[seq/]attr=value")
                .withValueSeparator().withDescription(rb.getString("metadata"))
                .create("m"));
        opts.addOption("f", "file", true, rb.getString("file"));
        opts.addOption("u", "url", true, rb.getString("url"));
        opts.addOption("t", "metadata-type", true,
                rb.getString("metadata-type"));
        opts.addOption("ph", "pixel-header", true, rb.getString("pixel-header"));
        opts.addOption("na","no-appn", true, rb.getString("no-appn"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRS.class);
    }

    private static Attributes configureKeys(StowRS main, CommandLine cl) {
        Attributes temp = new Attributes();
        CLIUtils.addAttributes(temp, cl.getOptionValues("m"));
        return temp;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            cl = parseCommandLine(args);
            StowRS instance = new StowRS();
            instance.keys = configureKeys(instance, cl);
            LOG.info("added keys for coercion: \n" + instance.keys.toString());
            List<String> files = cl.getArgList();
            if (files.isEmpty())
                throw new MissingArgumentException("No pixel data files or dicom files specified");
            if ((instance.URL = cl.getOptionValue("u")) == null)
                throw new MissingOptionException("Missing url");
            if (!cl.hasOption("t")) {
                ObjectType ot = new ObjectType(null, "application/dicom");
                stow(instance, null, files, ot);
                return;
            }
            stowMetadataAndBulkdata(cl, instance, files);
        } catch (Exception e) {
            LOG.error("Error: \n", e);
            e.printStackTrace();
        }
    }

    private static void stowMetadataAndBulkdata(CommandLine cl, StowRS instance, List<String> files) throws Exception {
        String metadataType;
        Attributes metadata = defaultMetadata();

        if ((metadataType = cl.getOptionValue("t")) == null)
            metadataType = "xml";
        if (!(metadataType.equalsIgnoreCase("json") || metadataType.equalsIgnoreCase("xml")))
            throw new IllegalArgumentException("Bad Type specified for metadata, specify either XML or JSON");
        if (cl.getOptionValue("f") != null) {
            Path filePath = Paths.get(cl.getOptionValue("f"));
            JSONReader reader = new JSONReader(Json.createParser(new FileReader(filePath.toString())));
            metadata = metadataType.equalsIgnoreCase("json")
                    ? reader.readDataset(null)
                    : SAXReader.parse(filePath.toString());
        }

        coerceattributes(metadata, instance);

        if (cl.hasOption("pixel-header"))
            instance.pixelHeader = Boolean.valueOf(cl.getOptionValue("pixel-header"));
        if (cl.hasOption("no-appn"))
            instance.noAppn = Boolean.valueOf(cl.getOptionValue("no-appn"));

        String contentType = "application/dicom+" + metadataType.toLowerCase();
        Path bulkdataFile = Paths.get(files.get(0));
        String bulkdataFileName = bulkdataFile.toFile().getName();
        String bulkdataType;
        String extension = bulkdataFileName.substring(bulkdataFileName.lastIndexOf(".")+1).toLowerCase();
        switch (extension) {
            case "pdf":
                bulkdataType = "application/pdf";
                setPDFAttributes(bulkdataFile, metadata);
                if (metadata.getValue(Tag.EncapsulatedDocument) == null)
                    metadata.setValue(Tag.EncapsulatedDocument, VR.OB, instance.defaultBulkdata);
                break;
            case "mpg":
            case "mpg2":
            case "mpeg":
                bulkdataType = "video/mpeg";
                readPixelHeader(instance, metadata, bulkdataFile, true);
                if (metadata.getString(Tag.SOPClassUID) == null)
                    metadata.setString(Tag.SOPClassUID, VR.UI, UID.VideoPhotographicImageStorage);
                break;
            case "jpeg":
            case "jpg":
                bulkdataType = "image/jpeg; transfer-syntax: " + UID.JPEGBaseline1;
                readPixelHeader(instance, metadata, bulkdataFile, false);
                if (metadata.getString(Tag.SOPClassUID) == null)
                    metadata.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
                break;
            default:
                throw new IllegalArgumentException("unsupported extension for bulkdata");
        }
        ObjectType ot = new ObjectType(bulkdataType, contentType);
        stow(instance, metadata, files, ot);
    }

    private static Attributes defaultMetadata() throws ParserConfigurationException, SAXException, IOException {
        LOG.info("Read first always from default metadata file : etc/stowrs/metadata.xml");
        //Path filePath = new File("../etc/stowrs/metadata.xml").getAbsoluteFile().toPath();
        Path filePath = new File("/home/vrinda/dcm4che/etc/stowrs/metadata.xml").toPath();
        Attributes metadata = SAXReader.parse(filePath.toString());
        metadata.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        return metadata;
    }

    private static void readPixelHeader(StowRS instance, Attributes metadata, Path bulkdataFile, boolean isMpeg)
            throws IOException {
        if (metadata.getValue(Tag.PixelData) == null)
            metadata.setValue(Tag.PixelData, VR.OB, instance.defaultBulkdata);
        if (!instance.pixelHeader)
            return;
        BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(bulkdataFile));
        byte[] b16384 = new byte[16384];
        StreamUtils.readAvailable(bis, b16384, 0, 16384);
        if (isMpeg) {
            MPEGHeader mpegHeader = new MPEGHeader(b16384);
            mpegHeader.toAttributes(metadata, Files.size(bulkdataFile));
            return;
        }
        instance.jpegHeader = new JPEGHeader(b16384, JPEG.SOS);
        instance.jpegHeader.toAttributes(metadata);
    }

    private static void stow(StowRS instance, Attributes metadata, List<String> files, ObjectType ot)
            throws Exception {
        try {
            URL newUrl = new URL(instance.URL);
            HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "multipart/related; type=" + ot.contentType + "; boundary=" + boundary);
            OutputStream out = connection.getOutputStream();
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + ot.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            writeData(instance, metadata, files, ot, out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            String response = connection.getResponseMessage();
            LOG.info("response: " + response);
            connection.disconnect();
            LOG.info("STOW successful!");
        } catch (Exception e) {
            LOG.error("Exception : " + e.getMessage());
        }
    }

    private static void writeData(StowRS instance, Attributes metadata, List<String> files, ObjectType ot,
                                  OutputStream out) throws Exception {
        if (!ot.contentType.equals("application/dicom")) {
            writeMetdataAndBulkData(instance, metadata, files, ot, out);
            return;
        }
        String[] filesArray = files.toArray(new String[files.size()]);
        Files.copy(Paths.get(filesArray[0]), out);
        for (int i = 1; i < filesArray.length; i++) {
            out.write(("\r\n--"+boundary+"\r\n").getBytes());
            out.write(("Content-Type: " + ot.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            Files.copy(Paths.get(filesArray[i]), out);
        }
    }

    private static void writeMetdataAndBulkData(StowRS instance,
                                                Attributes metadata, List<String> files, ObjectType ot, OutputStream out)
            throws Exception {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        if (ot.contentType.equals("application/dicom+xml"))
            SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
        else
            try (JsonGenerator gen = Json.createGenerator(bOut)) {
                new JSONWriter(gen).write(metadata);
            }
        out.write(bOut.toByteArray());
        out.write(("\r\n--"+boundary+"\r\n").getBytes());
        out.write(("Content-Type: " + ot.bulkdataType + "\r\n").getBytes());
        String contentLoc = ot.bulkdataType.equals("application/pdf")
                ? ((BulkData) metadata.getValue(Tag.EncapsulatedDocument)).getURI()
                : ((BulkData) metadata.getValue(Tag.PixelData)).getURI();
        out.write(("Content-Location: " + contentLoc + "\r\n").getBytes());
        out.write("\r\n".getBytes());
        Path file = Paths.get(files.get(0));
        byte[] b = Files.readAllBytes(file);
        if (instance.jpegHeader != null && instance.noAppn) {
            int i = instance.jpegHeader.offsetAfterAPP();
            out.write(-1);
            out.write((byte)JPEG.SOI);
            out.write(-1);
            out.write(b, i, (int) Files.size(file) - i);
        } else
            out.write(b);
    }

    private static void coerceattributes(Attributes metadata, StowRS instance) {
        if (instance.keys.tags().length > 0)
            LOG.info("Coercing the following keys from specified attributes to metadata:");
        metadata.update(Attributes.UpdatePolicy.OVERWRITE, instance.keys, new Attributes());
        LOG.info(instance.keys.toString());
    }

    private static void setPDFAttributes(Path bulkDataFile, Attributes metadata) {
        metadata.setString(Tag.SOPClassUID, VR.UI, UID.EncapsulatedPDFStorage);
        metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
        metadata.setString(Tag.ContentDate, VR.DA,
                DateUtils.formatDA(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.ContentTime, VR.TM,
                DateUtils.formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.AcquisitionDateTime, VR.DT,
                DateUtils.formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.BurnedInAnnotation, VR.CS, "YES");
        metadata.setNull(Tag.DocumentTitle, VR.ST);
        metadata.setNull(Tag.ConceptNameCodeSequence, VR.SQ);
        metadata.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO,
                "application/pdf");

    }

    static class ObjectType {
        private String bulkdataType;
        private String contentType;

        ObjectType(String bulkdataType, String contentType) {
            this.bulkdataType = bulkdataType;
            this.contentType = contentType;
        }
    }
}
