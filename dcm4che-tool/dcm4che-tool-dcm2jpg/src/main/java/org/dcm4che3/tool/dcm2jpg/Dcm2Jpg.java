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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.tool.dcm2jpg;

import org.apache.commons.cli.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.ICCProfile;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.stream.RAFFileImageInputStream;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 */
public class Dcm2Jpg {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.dcm2jpg.messages");

    private interface ReadImage {
        BufferedImage apply(File src) throws IOException;
    }
    private ReadImage readImage;
    private String suffix;
    private int frame = 1;
    private int windowIndex;
    private int voiLUTIndex;
    private boolean preferWindow = true;
    private float windowCenter;
    private float windowWidth;
    private boolean autoWindowing = true;
    private boolean ignorePresentationLUTShape;
    private Attributes prState;
    private final ImageReader imageReader =
            ImageIO.getImageReadersByFormatName("DICOM").next();
    private ImageWriter imageWriter;
    private ImageWriteParam imageWriteParam;
    private int overlayActivationMask = 0xffff;
    private int overlayGrayscaleValue = 0xffff;
    private int overlayRGBValue = 0xffffff;
    private ICCProfile.Option iccProfile = ICCProfile.Option.none;

    public void initImageWriter(String formatName, String suffix,
            String clazz, String compressionType, Number quality) {
        this.suffix = suffix != null ? suffix : formatName.toLowerCase();
        Iterator<ImageWriter> imageWriters =
                ImageIO.getImageWritersByFormatName(formatName);
        if (!imageWriters.hasNext())
            throw new IllegalArgumentException(
                    MessageFormat.format(rb.getString("formatNotSupported"),
                            formatName));
        Iterable<ImageWriter> iterable = () -> imageWriters;
        imageWriter = StreamSupport.stream(iterable.spliterator(), false)
                .filter(matchClassName(clazz))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format(rb.getString("noSuchImageWriter"),
                                clazz, formatName)));
        imageWriteParam = imageWriter.getDefaultWriteParam();
        if (compressionType != null || quality != null) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (compressionType != null)
                imageWriteParam.setCompressionType(compressionType);
            if (quality != null)
                imageWriteParam.setCompressionQuality(quality.floatValue());
        }
    }

    private static Predicate<Object> matchClassName(String clazz) {
        Predicate<String> predicate = clazz.endsWith("*")
                ? startsWith(clazz.substring(0, clazz.length() - 1))
                : clazz::equals;
        return w -> predicate.test(w.getClass().getName());
    }

    private static Predicate<String> startsWith(String prefix) {
        return s -> s.startsWith(prefix);
    }

    public final void setFrame(int frame) {
        this.frame = frame;
    }

    public final void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public final void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public final void setWindowIndex(int windowIndex) {
        this.windowIndex = windowIndex;
    }

    public final void setVOILUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = voiLUTIndex;
    }

    public final void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    public final void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    public boolean isIgnorePresentationLUTShape() {
        return ignorePresentationLUTShape;
    }

    public void setIgnorePresentationLUTShape(boolean ignorePresentationLUTShape) {
        this.ignorePresentationLUTShape = ignorePresentationLUTShape;
    }

    public final void setPresentationState(Attributes prState) {
        this.prState = prState;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    public void setOverlayRGBValue(int overlayRGBValue) {
        this.overlayRGBValue = overlayRGBValue;
    }

    public final void setICCProfile(ICCProfile.Option iccProfile) {
        this.iccProfile = Objects.requireNonNull(iccProfile);
    }

    public final void setReadImage(ReadImage readImage) {
        this.readImage = readImage;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("F")
                .hasArg()
                .argName("format")
                .desc(rb.getString("format"))
                .build());
        opts.addOption(Option.builder("E")
                .hasArg()
                .argName("class")
                .desc(rb.getString("encoder"))
                .build());
        opts.addOption(Option.builder("C")
                .hasArg()
                .argName("type")
                .desc(rb.getString("compression"))
                .build());
        opts.addOption(Option.builder("q")
                .hasArg()
                .argName("quality")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("quality"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("suffix")
                .desc(rb.getString("suffix"))
                .longOpt("suffix")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("number")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("frame"))
                .longOpt("frame")
                .build());
        opts.addOption(Option.builder("c")
                .hasArg()
                .argName("center")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("windowCenter"))
                .longOpt("windowCenter")
                .build());
        opts.addOption(Option.builder("w")
                .hasArg()
                .argName("width")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("windowWidth"))
                .longOpt("windowWidth")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("number")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("window"))
                .longOpt("window")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("number")
                .type(PatternOptionBuilder.NUMBER_VALUE)
                .desc(rb.getString("voilut"))
                .longOpt("voilut")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("profile")
                .desc(rb.getString("iccprofile"))
                .longOpt("iccprofile")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("file")
                .type(PatternOptionBuilder.EXISTING_FILE_VALUE)
                .desc(rb.getString("ps"))
                .longOpt("ps")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("mask")
                .desc(rb.getString("overlays"))
                .longOpt("overlays")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("value")
                .desc(rb.getString("ovlygray"))
                .longOpt("ovlygray")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("value")
                .desc(rb.getString("ovlyrgb"))
                .longOpt("ovlyrgb")
                .build());
        opts.addOption(null, "uselut", false, rb.getString("uselut"));
        opts.addOption(null, "noauto", false, rb.getString("noauto"));
        opts.addOption(null, "noshape", false, rb.getString("noshape"));
        opts.addOption(null, "lsE", false, rb.getString("lsencoders"));
        opts.addOption(null, "lsF", false, rb.getString("lsformats"));
        OptionGroup useGroup = new OptionGroup();
        useGroup.addOption(Option.builder()
                .longOpt("usedis")
                .desc(rb.getString("usedis"))
                .build());
        useGroup.addOption(Option.builder()
                .longOpt("useiis")
                .desc(rb.getString("useiis"))
                .build());
        opts.addOptionGroup(useGroup);
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Dcm2Jpg.class);
        if (cl.hasOption("lsF")) {
            listSupportedFormats();
            System.exit(0);
        }
        if (cl.hasOption("lsE")) {
            listSupportedImageWriters(cl.getOptionValue("F", "JPEG"));
            System.exit(0);
        }
        return cl;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Dcm2Jpg main = new Dcm2Jpg();
            main.initImageWriter(
                    cl.getOptionValue("F", "JPEG"),
                    cl.getOptionValue("suffix"),
                    cl.getOptionValue("E", "com.sun.imageio.plugins.*"),
                    cl.getOptionValue("C"),
                    (Number) cl.getParsedOptionValue("q"));
            if (cl.hasOption("frame"))
                main.setFrame(
                        ((Number) cl.getParsedOptionValue("frame")).intValue());
            if (cl.hasOption("c"))
                main.setWindowCenter(
                        ((Number) cl.getParsedOptionValue("c")).floatValue());
            if (cl.hasOption("w"))
                main.setWindowWidth(
                        ((Number) cl.getParsedOptionValue("w")).floatValue());
            if (cl.hasOption("window"))
                main.setWindowIndex(
                        ((Number) cl.getParsedOptionValue("window")).intValue() - 1);
            if (cl.hasOption("voilut"))
                main.setVOILUTIndex(
                        ((Number) cl.getParsedOptionValue("voilut")).intValue() - 1);
            if (cl.hasOption("overlays"))
                main.setOverlayActivationMask(
                        parseHex(cl.getOptionValue("overlays")));
            if (cl.hasOption("ovlygray"))
                main.setOverlayGrayscaleValue(
                        parseHex(cl.getOptionValue("ovlygray")));
            if (cl.hasOption("ovlyrgb"))
                main.setOverlayRGBValue(
                        parseHex(cl.getOptionValue("ovlyrgb").substring(1)));
            main.setPreferWindow(!cl.hasOption("uselut"));
            main.setAutoWindowing(!cl.hasOption("noauto"));
            main.setIgnorePresentationLUTShape(cl.hasOption("noshape"));
            main.setPresentationState(
                    loadDicomObject((File) cl.getParsedOptionValue("ps")));
            if (cl.hasOption("iccprofile")) {
                try {
                    main.setICCProfile(ICCProfile.Option.valueOf(cl.getOptionValue("iccprofile")));
                } catch (IllegalArgumentException e) {
                    throw new ParseException(e.getMessage());
                }
            }
            main.setReadImage(cl.hasOption("frame")
                    ? (cl.hasOption("usedis")
                        ? main::readImageFromDicomInputStream
                        : main::readImageFromImageInputStream)
                    : (cl.hasOption("useiis")
                        ? main::readImageFromImageInputStream
                        : main::readImageFromDicomInputStream));

            @SuppressWarnings("unchecked")
            final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc-1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())) {
                dest.mkdirs();
                if (!dest.isDirectory())
                    throw new ParseException(
                            MessageFormat.format(rb.getString("nodestdir"), dest));
            }
            for (String src : argList.subList(0, argc-1))
                main.mconvert(new File(src), dest);
        } catch (ParseException e) {
            System.err.println("dcm2jpg: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcm2jpg: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static int parseHex(String s) throws ParseException {
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage());
        }
    }

    private void mconvert(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mconvert(file, new File(dest, 
                        file.isFile() ? suffix(file) : file.getName()));
            return;
        }
        if (dest.isDirectory())
            dest = new File(dest, suffix(src));
        try {
            convert(src, dest);
            System.out.println(
                    MessageFormat.format(rb.getString("converted"),
                            src, dest));
        } catch (Exception e) {
            System.out.println(
                    MessageFormat.format(rb.getString("failed"),
                            src, e.getMessage()));
            e.printStackTrace(System.out);
        }
    }

    public void convert(File src, File dest) throws IOException {
        writeImage(dest, iccProfile.adjust(readImage.apply(src)));
    }

    public BufferedImage readImageFromImageInputStream(File file) throws IOException {
        try (ImageInputStream iis = new RAFFileImageInputStream(file)) {
            imageReader.setInput(iis);
            return imageReader.read(frame - 1, readParam());
        }
    }

    public BufferedImage readImageFromDicomInputStream(File file) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(file)) {
            imageReader.setInput(dis);
            return imageReader.read(frame - 1, readParam());
        }
    }

    private ImageReadParam readParam() {
        DicomImageReadParam param =
                (DicomImageReadParam) imageReader.getDefaultReadParam();
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        param.setAutoWindowing(autoWindowing);
        param.setIgnorePresentationLUTShape(ignorePresentationLUTShape);
        param.setWindowIndex(windowIndex);
        param.setVOILUTIndex(voiLUTIndex);
        param.setPreferWindow(preferWindow);
        param.setPresentationState(prState);
        param.setOverlayActivationMask(overlayActivationMask);
        param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        param.setOverlayRGBValue(overlayRGBValue);
        return param;
    }

    private void writeImage(File dest, BufferedImage bi) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dest, "rw")) {
            raf.setLength(0);
            imageWriter.setOutput(new FileImageOutputStream(raf));
            imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
        }
    }


    private String suffix(File src) {
        return src.getName() + '.' + suffix;
    }

    private static Attributes loadDicomObject(File f) throws IOException {
        if (f == null)
            return null;
        DicomInputStream dis = new DicomInputStream(f);
        try {
            return dis.readDataset();
        } finally {
            SafeClose.close(dis);
        }
    }

    public static void listSupportedImageWriters(String format) {
        System.out.println(MessageFormat.format(rb.getString("writers"), format));
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(format);
        while (it.hasNext()) {
            ImageWriter writer = it.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            System.out.println(MessageFormat.format(rb.getString("writer"),
                    writer.getClass().getName(),
                    param.canWriteCompressed(),
                    param.canWriteProgressive(),
                    param.canWriteTiles(),
                    param.canOffsetTiles(),
                    param.canWriteCompressed()
                        ? Arrays.toString(param.getCompressionTypes())
                        : null));
        }
    }

    public static void listSupportedFormats() {
        System.out.println(
                MessageFormat.format(rb.getString("formats"),
                        Arrays.toString(ImageIO.getWriterFormatNames())));
    }
}
