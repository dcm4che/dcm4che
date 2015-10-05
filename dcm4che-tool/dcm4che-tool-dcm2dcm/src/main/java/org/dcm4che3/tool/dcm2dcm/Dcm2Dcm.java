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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che3.tool.dcm2dcm;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Compressor;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Dcm2Dcm {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.dcm2dcm.messages");

    private String tsuid;
    private TransferSyntaxType tstype;
    private boolean retainfmi;
    private boolean nofmi;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private final List<Property> params = new ArrayList<Property>();

    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
        this.tstype = TransferSyntaxType.forUID(uid);
        if (tstype == null) {
            throw new IllegalArgumentException(
                    "Unsupported Transfer Syntax: " + tsuid);
        }
    }

    public final void setRetainFileMetaInformation(boolean retainfmi) {
        this.retainfmi = retainfmi;
    }

    public final void setWithoutFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public void addCompressionParam(String name, Object value) {
        params.add(new Property(name, value));
    }

    private static Object toValue(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE :
                  s.equalsIgnoreCase("false") ? Boolean.FALSE
                                              : s;
        }
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        CLIUtils.addEncodingOptions(opts);
        OptionGroup tsGroup = new OptionGroup();
        tsGroup.addOption(OptionBuilder
                .withLongOpt("transfer-syntax")
                .hasArg()
                .withArgName("uid")
                .withDescription(rb.getString("transfer-syntax"))
                .create("t"));
        tsGroup.addOption(OptionBuilder
                .withLongOpt("jpeg")
                .withDescription(rb.getString("jpeg"))
                .create());
        tsGroup.addOption(OptionBuilder
                .withLongOpt("jpll")
                .withDescription(rb.getString("jpll"))
                .create());
        tsGroup.addOption(OptionBuilder
                .withLongOpt("jpls")
                .withDescription(rb.getString("jpls"))
                .create());
        tsGroup.addOption(OptionBuilder
                .withLongOpt("j2kr")
                .withDescription(rb.getString("j2kr"))
                .create());
        tsGroup.addOption(OptionBuilder
                .withLongOpt("j2ki")
                .withDescription(rb.getString("j2ki"))
                .create());
        opts.addOptionGroup(tsGroup);
        OptionGroup fmiGroup = new OptionGroup();
        fmiGroup.addOption(OptionBuilder
                .withLongOpt("no-fmi")
                .withDescription(rb.getString("no-fmi"))
                .create("F"));
        fmiGroup.addOption(OptionBuilder
                .withLongOpt("retain-fmi")
                .withDescription(rb.getString("retain-fmi"))
                .create("f"));
        opts.addOptionGroup(fmiGroup);
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("max-error")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("verify"))
                .withLongOpt("verify")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("size")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("verify-block"))
                .withLongOpt("verify-block")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("quality")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("quality"))
                .create("q"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("encoding-rate")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("encoding-rate"))
                .create("Q"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("name=value")
                .withValueSeparator()
                .withDescription(rb.getString("compression-param"))
                .create("C"));
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Dcm2Dcm.class);
        return cl;
    }

     public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Dcm main = new Dcm2Dcm();
            main.setEncodingOptions(CLIUtils.encodingOptionsOf(cl));
            if (cl.hasOption("F")) {
                if (transferSyntaxOf(cl, null) != null)
                    throw new ParseException(rb.getString("transfer-syntax-no-fmi"));
                main.setTransferSyntax(UID.ImplicitVRLittleEndian);
                main.setWithoutFileMetaInformation(true);
            } else {
                main.setTransferSyntax(transferSyntaxOf(cl, UID.ExplicitVRLittleEndian));
                main.setRetainFileMetaInformation(cl.hasOption("f"));
            }

            if (cl.hasOption("verify"))
                main.addCompressionParam("maxPixelValueError",
                        cl.getParsedOptionValue("verify"));

            if (cl.hasOption("verify-block"))
                main.addCompressionParam("avgPixelValueBlockSize",
                        cl.getParsedOptionValue("verify-block"));

            if (cl.hasOption("q"))
                main.addCompressionParam("compressionQuality",
                        cl.getParsedOptionValue("q"));

            if (cl.hasOption("Q"))
                main.addCompressionParam("encodingRate",
                        cl.getParsedOptionValue("Q"));

            String[] cparams = cl.getOptionValues("C");
            if (cparams != null)
                for (int i = 0; i < cparams.length;)
                    main.addCompressionParam(cparams[i++], toValue(cparams[i++]));

            @SuppressWarnings("unchecked")
            final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            for (String src : argList.subList(0, argc-1))
                main.mtranscode(new File(src), dest);
        } catch (ParseException e) {
            System.err.println("dcm2dcm: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2dcm: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String transferSyntaxOf(CommandLine cl, String def) {
        return cl.hasOption("ivrle") ? UID.ImplicitVRLittleEndian
                : cl.hasOption("evrbe") ? UID.ExplicitVRBigEndianRetired
                : cl.hasOption("defl") ? UID.DeflatedExplicitVRLittleEndian
                : cl.hasOption("jpeg") ? UID.JPEGBaseline1
                : cl.hasOption("jpll") ? UID.JPEGLossless
                : cl.hasOption("jpls") ? UID.JPEGLSLossless
                : cl.hasOption("j2kr") ? UID.JPEG2000LosslessOnly
                : cl.hasOption("j2ki") ? UID.JPEG2000
                : cl.getOptionValue("t", def);
    }

    private void mtranscode(File src, File dest) {
         if (src.isDirectory()) {
             dest.mkdir();
             for (File file : src.listFiles())
                 mtranscode(file, new File(dest, file.getName()));
             return;
         }
         if (dest.isDirectory())
             dest = new File(dest, src.getName());
         try {
             transcode(src, dest);
             System.out.println(
                     MessageFormat.format(rb.getString("transcoded"),
                             src, dest));
         } catch (Exception e) {
             System.out.println(
                     MessageFormat.format(rb.getString("failed"),
                             src, e.getMessage()));
             e.printStackTrace(System.out);
         }
     }

     public void transcode(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        DicomInputStream dis = new DicomInputStream(src);
        try {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset(-1, -1);
        } finally {
            dis.close();
        }
        Object pixeldata = dataset.getValue(Tag.PixelData);
        Compressor compressor = null;
        DicomOutputStream dos = null;
        try {
            String tsuid = this.tsuid;
            if (pixeldata != null) {
                if (tstype.isPixeldataEncapsulated()) {
                    tsuid = adjustTransferSyntax(tsuid,
                            dataset.getInt(Tag.BitsStored, 8));
                    compressor = new Compressor(dataset, dis.getTransferSyntax(),
                            tsuid, params.toArray(new Property[params.size()]));
                    compressor.compress();
                } else if (pixeldata instanceof Fragments)
                    Decompressor.decompress(dataset, dis.getTransferSyntax());
            }
            if (nofmi)
                fmi = null;
            else if (retainfmi && fmi != null)
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
            else
                fmi = dataset.createFileMetaInformation(tsuid);
            dos = new DicomOutputStream(dest);
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        } finally {
            SafeClose.close(compressor);
            SafeClose.close(dos);
        }
     }

    private String adjustTransferSyntax(String tsuid, int bitsStored) {
        switch (tstype) {
        case JPEG_BASELINE:
            if (bitsStored > 8)
                return UID.JPEGExtended24;
            break;
        case JPEG_EXTENDED:
            if (bitsStored <= 8)
                return UID.JPEGBaseline1;
            break;
        default:
        }
        return tsuid;
    }

}
