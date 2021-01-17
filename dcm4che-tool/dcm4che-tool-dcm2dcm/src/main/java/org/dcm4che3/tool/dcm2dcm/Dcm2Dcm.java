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
 * Java(TM), hosted at https://github.com/dcm4che.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Compressor;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.imageio.codec.Transcoder;
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
    private boolean legacy;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private final List<Property> params = new ArrayList<Property>();
    private int maxThreads = 1;

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

    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public void addCompressionParam(String name, Object value) {
        params.add(new Property(name, value));
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads <= 0)
            throw new IllegalArgumentException("max-threads: " + maxThreads);
        this.maxThreads = maxThreads;
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
        tsGroup.addOption(Option.builder("t")
                .longOpt("transfer-syntax")
                .hasArg()
                .argName("uid")
                .desc(rb.getString("transfer-syntax"))
                .build());
        tsGroup.addOption(Option.builder()
                .longOpt("jpeg")
                .desc(rb.getString("jpeg"))
                .build());
        tsGroup.addOption(Option.builder()
                .longOpt("jpll")
                .desc(rb.getString("jpll"))
                .build());
        tsGroup.addOption(Option.builder()
                .longOpt("jlsl")
                .desc(rb.getString("jlsl"))
                .build());
        tsGroup.addOption(Option.builder()
                .longOpt("jlsn")
                .desc(rb.getString("jlsn"))
                .build());
        tsGroup.addOption(Option.builder()
                .longOpt("j2kr")
                .desc(rb.getString("j2kr"))
                .build());
        tsGroup.addOption(Option.builder()
                .longOpt("j2ki")
                .desc(rb.getString("j2ki"))
                .build());
        opts.addOptionGroup(tsGroup);
        OptionGroup fmiGroup = new OptionGroup();
        fmiGroup.addOption(Option.builder("F")
                .longOpt("no-fmi")
                .desc(rb.getString("no-fmi"))
                .build());
        fmiGroup.addOption(Option.builder("f")
                .longOpt("retain-fmi")
                .desc(rb.getString("retain-fmi"))
                .build());
        opts.addOptionGroup(fmiGroup);
        opts.addOption(Option.builder()
                .hasArg()
                .argName("N")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("max-threads"))
                .longOpt("max-threads")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("max-error")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("verify"))
                .longOpt("verify")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("size")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("verify-block"))
                .longOpt("verify-block")
                .build());
        opts.addOption(Option.builder("q")
                .hasArg()
                .argName("quality")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("quality"))
                .build());
        opts.addOption(Option.builder("Q")
                .hasArg()
                .argName("compression")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("compression"))
                .build());
        opts.addOption(Option.builder("N")
                .hasArg()
                .argName("near-lossless")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("near-lossless"))
                .build());
        opts.addOption(Option.builder("C")
                .hasArgs()
                .argName("name=value")
                .valueSeparator()
                .desc(rb.getString("compression-param"))
                .build());
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
            main.setLegacy(cl.hasOption("legacy"));

            if (cl.hasOption("max-threads"))
                main.setMaxThreads(((Number) cl.getParsedOptionValue("max-threads")).intValue());

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
                main.addCompressionParam("compressionRatiofactor",
                        cl.getParsedOptionValue("Q"));

            if (cl.hasOption("N"))
                main.addCompressionParam("nearLossless",
                        cl.getParsedOptionValue("N"));

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
            main.mtranscode(argList.subList(0, argc - 1), dest);
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
                : cl.hasOption("evrbe") ? UID.ExplicitVRBigEndian
                : cl.hasOption("defl") ? UID.DeflatedExplicitVRLittleEndian
                : cl.hasOption("jpeg") ? UID.JPEGBaseline8Bit
                : cl.hasOption("jpll") ? UID.JPEGLosslessSV1
                : cl.hasOption("jlsl") ? UID.JPEGLSLossless
                : cl.hasOption("jlsn") ? UID.JPEGLSNearLossless
                : cl.hasOption("j2kr") ? UID.JPEG2000Lossless
                : cl.hasOption("j2ki") ? UID.JPEG2000
                : cl.getOptionValue("t", def);
    }

    private void mtranscode(List<String> srcList, File dest) throws InterruptedException {
        ExecutorService executorService = maxThreads > 1 ? Executors.newFixedThreadPool(maxThreads) : null;
        for (String src : srcList) {
            mtranscode(new File(src), dest, executorService);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void mtranscode(final File src, File dest, Executor executer) {
         if (src.isDirectory()) {
             dest.mkdir();
             for (File file : src.listFiles())
                 mtranscode(file, new File(dest, file.getName()), executer);
             return;
         }
         final File finalDest = dest.isDirectory() ? new File(dest, src.getName()) : dest;
         if (executer != null) {
             executer.execute(() -> transcode(src, finalDest));
         } else {
             transcode(src, finalDest);
         }
    }

    private void transcode(File src, File dest) {
        try {
            if (legacy)
                transcodeLegacy(src, dest);
            else
                transcodeWithTranscoder(src, dest);

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

    public void transcodeLegacy(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        DicomInputStream dis = new DicomInputStream(src);
        try {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset();
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
                    compressor = new Compressor(dataset, dis.getTransferSyntax());
                    compressor.compress(tsuid, params.toArray(new Property[params.size()]));
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

    public void transcodeWithTranscoder(File src, final File dest) throws IOException {
        try (Transcoder transcoder = new Transcoder(src)) {
            transcoder.setIncludeFileMetaInformation(!nofmi);
            transcoder.setRetainFileMetaInformation(retainfmi);
            transcoder.setEncodingOptions(encOpts);
            transcoder.setDestinationTransferSyntax(tsuid);
            transcoder.setCompressParams(params.toArray(new Property[params.size()]));
            transcoder.transcode((transcoder1, dataset) -> new FileOutputStream(dest));
        } catch (Exception e) {
            Files.deleteIfExists(dest.toPath());
            throw e;
        }
    }

    private String adjustTransferSyntax(String tsuid, int bitsStored) {
        switch (tstype) {
        case JPEG_BASELINE:
            if (bitsStored > 8)
                return UID.JPEGExtended12Bit;
            break;
        case JPEG_EXTENDED:
            if (bitsStored <= 8)
                return UID.JPEGBaseline8Bit;
            break;
        default:
        }
        return tsuid;
    }

}
