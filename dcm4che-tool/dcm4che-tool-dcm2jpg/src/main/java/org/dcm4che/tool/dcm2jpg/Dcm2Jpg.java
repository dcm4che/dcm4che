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

package org.dcm4che.tool.dcm2jpg;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.dcm4che.data.Attributes;
import org.dcm4che.image.PaletteColorModel;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.tool.common.CLIUtils;
import org.dcm4che.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Dcm2Jpg {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.dcm2jpg.messages");

    private String suffix;
    private int frame = 1;
    private float windowCenter;
    private float windowWidth;
    private boolean autoWindowing;
    private Attributes prState;
    private short[] pval2gray;
    private final ImageReader imageReader =
            ImageIO.getImageReadersByFormatName("DICOM").next();
    private ImageWriter imageWriter;
    private ImageWriteParam imageWriteParam;

    public void initImageWriter(String formatName, String suffix,
            String clazz, String compressionType, Number quality) {
        Iterator<ImageWriter> imageWriters =
                ImageIO.getImageWritersByFormatName(formatName);
        if (!imageWriters.hasNext())
            throw new IllegalArgumentException(
                    MessageFormat.format(rb.getString("formatNotSupported"),
                            formatName));
        this.suffix = suffix != null ? suffix : formatName.toLowerCase();
        imageWriter = imageWriters.next();
        if (clazz != null)
            while (!clazz.equals(imageWriter.getClass().getName()))
                if (imageWriters.hasNext())
                    imageWriter = imageWriters.next();
                else
                    throw new IllegalArgumentException(
                            MessageFormat.format(rb.getString("noSuchImageWriter"),
                                    clazz, formatName));
        imageWriteParam = imageWriter.getDefaultWriteParam();
        if (compressionType != null || quality != null) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (compressionType != null)
                imageWriteParam.setCompressionType(compressionType);
            if (quality != null)
                imageWriteParam.setCompressionQuality(quality.floatValue());
        }
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

    public final void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    public final void setPresentationState(Attributes prState) {
        this.prState = prState;
    }

    public final void setPval2gray(short[] pval2gray) {
        this.pval2gray = pval2gray;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("format")
                .withDescription(rb.getString("format"))
                .create("F"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("class")
                .withDescription(rb.getString("encoder"))
                .create("E"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("type")
                .withDescription(rb.getString("compression"))
                .create("C"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("quality")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("quality"))
                .create("q"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("suffix")
                .withDescription(rb.getString("suffix"))
                .withLongOpt("suffix")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("number")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("frame"))
                .withLongOpt("frame")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("center")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("windowCenter"))
                .withLongOpt("windowCenter")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("width")
                .withType(PatternOptionBuilder.NUMBER_VALUE)
                .withDescription(rb.getString("windowWidth"))
                .withLongOpt("windowWidth")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file")
                .withType(PatternOptionBuilder.EXISTING_FILE_VALUE)
                .withDescription(rb.getString("ps"))
                .withLongOpt("ps")
                .create());
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file")
                .withType(PatternOptionBuilder.EXISTING_FILE_VALUE)
                .withDescription(rb.getString("pv2gray"))
                .withLongOpt("pv2gray")
                .create());
        opts.addOption(null, "noauto", false, rb.getString("noauto"));
        opts.addOption(null, "lsE", false, rb.getString("lsencoders"));
        opts.addOption(null, "lsF", false, rb.getString("lsformats"));

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
                    cl.getOptionValue("E"),
                    cl.getOptionValue("C"),
                    (Number) cl.getParsedOptionValue("q"));
            if (cl.hasOption("frame"))
                main.setFrame(
                        ((Number) cl.getParsedOptionValue("frame")).intValue());
            if (cl.hasOption("windowCenter"))
                main.setWindowCenter(
                        ((Number) cl.getParsedOptionValue("windowCenter")).floatValue());
            if (cl.hasOption("windowWidth"))
                main.setWindowWidth(
                        ((Number) cl.getParsedOptionValue("windowWidth")).floatValue());
            main.setAutoWindowing(!cl.hasOption("noauto"));
            main.setPresentationState(
                    loadDicomObject((File) cl.getParsedOptionValue("ps")));
            main.setPval2gray(
                    loadPVal2Gray((File) cl.getParsedOptionValue("pval2gray")));
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
        }
    }

    public void convert(File src, File dest) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(src);
        try {
            BufferedImage bi = readImage(iis);
            bi = convert(bi);
            dest.delete();
            ImageOutputStream ios = ImageIO.createImageOutputStream(dest);
            try {
                writeImage(ios, bi);
            } finally {
                SafeClose.close(ios);
            }
        } finally {
            SafeClose.close(iis);
        }
    }

    private BufferedImage convert(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof PaletteColorModel)
            return ((PaletteColorModel) cm).convertToIntDiscrete(bi.getData());
        return bi;
    }

    private BufferedImage readImage(ImageInputStream iis) throws IOException {
        imageReader.setInput(iis);
        return imageReader.read(frame-1);
    }

    private void writeImage(ImageOutputStream ios, BufferedImage bi)
            throws IOException {
        imageWriter.setOutput(ios);
        imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
    }


    private String suffix(File src) {
        return src.getName() + '.' + suffix;
    }

    @SuppressWarnings("resource")
    private static short[] loadPVal2Gray(File f) throws IOException {
        if (f == null)
            return null;
        BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f)));
        try {
            short[] pval2gray = new short[256];
            int n = 0;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#')
                    continue;
                if (n == pval2gray.length) {
                    if (n != pval2gray.length)
                        throw new IOException("Number of entries in " + f 
                                + " > 2^16");
                    pval2gray = Arrays.copyOf(pval2gray, n<<1);
                }
                try {
                    pval2gray[n++] = (short) Integer.parseInt(line.trim());
                } catch (NumberFormatException nfe) {
                    throw new IOException("Invalid entry in " + f + ": " + line);
                }
            }
            if (n != pval2gray.length)
                throw new IOException("Number of entries in " + f + ": " + n
                        + " != 2^[8..16]");
            return pval2gray;
           
        } finally {
            SafeClose.close(r);
        }
    }

    private static Attributes loadDicomObject(File f) throws IOException {
        if (f == null)
            return null;
        DicomInputStream dis = new DicomInputStream(f);
        try {
            return dis.readDataset(-1, -1);
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
