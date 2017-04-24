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
 * Portions created by the Initial Developer are Copyright (C) 2012
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 */
public class StowRS {
    private static byte[] buffer = new byte[8192];
    static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private Attributes keys;
    private static Options opts;
    private String URL;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");

    private static final String boundary = "myboundary";

    public StowRS() {
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs(2).withArgName("[seq/]attr=value")
                .withValueSeparator().withDescription(rb.getString("metadata"))
                .create("m"));
        opts.addOption("f", "file", true, rb.getString("file"));
        opts.addOption("u", "url", true, rb.getString("url"));
        opts.addOption("t", "metadata-type", true,
                rb.getString("metadata-type"));
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
            Attributes metadata;
            cl = parseComandLine(args);
            StowRS instance = new StowRS();
            instance.keys = configureKeys(instance, cl);
            LOG.info("added keys for coercion: \n" + instance.keys.toString());
            List<String> files = cl.getArgList();
            if (files == null)
                throw new MissingArgumentException("No pixel data files or dicom files specified");
            if (!cl.hasOption("u"))
                throw new MissingOptionException("Missing url");
            else
                instance.URL = cl.getOptionValue("u");
            if (!cl.hasOption("t")) {
                ObjectType ot = new ObjectType(null, null, "application/dicom");
                stow(instance.URL, null, files, ot);
                return;
            }
            stowNonDicom(cl, instance, files);
        } catch (Exception e) {
            LOG.error("Error: \n", e);
        }
    }

    private static void stowNonDicom(CommandLine cl, StowRS instance, List<String> files) throws Exception {
        Attributes metadata;
        String metadataType = cl.getOptionValue("t").toLowerCase();
        if (!(metadataType.equals("json") || metadataType.equals("xml")))
            throw new IllegalArgumentException("Bad Type specified for metadata, specify either XML or JSON");
        Path filePath;
        if (!cl.hasOption("f")) {
            LOG.info("No metadata file specified, using default metadata from etc/stowrs/metadata.xml");
            //filePath = new File("../etc/stowrs/metadata.xml").getAbsoluteFile().toPath();
            filePath = new File("\\metadata.xml").getAbsoluteFile().toPath();
            metadataType = "xml";
            metadata = SAXReader.parse(filePath.toString());
            metadata.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        } else {
            filePath = Paths.get(cl.getOptionValue("f"));
            JSONReader reader = new JSONReader(Json.createParser(new FileReader(filePath.toString())));
            metadata = metadataType.equals("json")
                    ? reader.readDataset(null)
                    : SAXReader.parse(filePath.toString());
        }
        String contentType = "application/dicom+" + metadataType;
        Path bulkdataFile = Paths.get(files.get(0));
        BulkData defaultBulkdata = new BulkData(null, "bulk", false);
        String bulkdataType;
        String extension = bulkdataFile.getFileName().toString().substring(bulkdataFile.getFileName().toString().lastIndexOf(".")+1).toLowerCase();
        if (!extension.equals("pdf")) {
            if (extension.equals("jpeg") || extension.equals("jpg")) {
                BufferedInputStream bio = new BufferedInputStream(Files.newInputStream(bulkdataFile));
                JPEGHeader header = new JPEGHeader(firstBytesOf(bio), JPEG.SOS);
                bulkdataType = "image/jpeg; transfer-syntax: " + UID.JPEGBaseline1;
                header.toAttributes(metadata);
            } else if (extension.equals("mpeg") || extension.equals("mp4")) {
                bulkdataType = "video/" + extension;
                setVideoAttributes(metadata);
            } else
                throw new IllegalArgumentException("unsupported extension for bulkdata");
            if (metadata.getValue(Tag.PixelData) == null)
                metadata.setValue(Tag.PixelData, VR.OB, defaultBulkdata);
        } else {
            bulkdataType = "application/pdf";
            setPDFAttributes(bulkdataFile, metadata);
            if (metadata.getValue(Tag.EncapsulatedDocument) == null)
                metadata.setValue(Tag.EncapsulatedDocument, VR.OB, defaultBulkdata);
        }
        ObjectType ot = new ObjectType(metadataType, bulkdataType, contentType);
        coerceattributes(metadata, instance);
        stow(instance.URL, metadata, files, ot);
    }

    private static void stow(String url, Attributes metadata, List<String> files, ObjectType ot) throws Exception {
        try {
            URL newUrl = new URL(url);
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
            if (ot.contentType.equals("application/dicom"))
                writeDicomFiles(files, ot, out);
            else
                writeMetdataAndBulkData(metadata, files, ot, out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            String response = connection.getResponseMessage();
            LOG.info("response: " + response);
            connection.disconnect();
            LOG.info("STOW successful!");
        } catch (Exception e) {
            LOG.error("Exception : " + e.getMessage());
        }
    }

    private static void writeDicomFiles(List<String> files, ObjectType ot, OutputStream out) throws IOException {
        String[] filesArray = files.toArray(new String[files.size()]);
        Files.copy(Paths.get(filesArray[0]), out);
        for (int i = 1; i < filesArray.length; i++) {
            out.write(("\r\n--"+boundary+"\r\n").getBytes());
            out.write(("Content-Type: " + ot.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            Files.copy(Paths.get(filesArray[i]), out);
        }
    }

    private static void writeMetdataAndBulkData(Attributes metadata, List<String> files, ObjectType ot, OutputStream out)
            throws Exception {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        if (ot.metadataType.equals("xml"))
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
        Files.copy(Paths.get(files.get(0)), out);
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

    private static void setVideoAttributes(Attributes metadata) {
        metadata.setString(Tag.SOPClassUID, VR.UI, UID.VideoEndoscopicImageStorage);
        metadata.setInt(Tag.NumberOfFrames, VR.IS, 9999);
        metadata.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
    }

    private static byte[] firstBytesOf(BufferedInputStream bis) throws IOException {
        int n, off = 0, len = buffer.length;
        bis.mark(8192);
        while (len > 0 && (n = bis.read(buffer, off, len)) > 0) {
            off += n;
            len -= n;
        }
        bis.reset();
        return len > 0 ? Arrays.copyOf(buffer, buffer.length - len) : buffer;
    }

    static class ObjectType {
        private String metadataType;
        private String bulkdataType;
        private String contentType;

        ObjectType(String metadataType, String bulkdataType, String contentType) {
            this.metadataType = metadataType;
            this.bulkdataType = bulkdataType;
            this.contentType = contentType;
        }
    }
}
