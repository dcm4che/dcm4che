/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2015
 */
public class BufferedImageUtils {

    private BufferedImageUtils() {}

    public static BufferedImage convertToIntRGB(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof DirectColorModel)
            return bi;

        if (cm.getNumComponents() != 3)
            throw new IllegalArgumentException("ColorModel: " + cm);

        WritableRaster raster = bi.getRaster();
        if (cm instanceof PaletteColorModel)
            return ((PaletteColorModel) cm).convertToIntDiscrete(raster);

        BufferedImage intRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = intRGB.getGraphics();
        try {
            graphics.drawImage(bi, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return intRGB;
    }
    
    public static BufferedImage convertYBRtoRGB(BufferedImage src, BufferedImage dst) {
        if (src.getColorModel().getTransferType() != DataBuffer.TYPE_BYTE) {
            throw new UnsupportedOperationException(
                "Cannot convert color model to RGB: unsupported transferType" + src.getColorModel().getTransferType());
        }
        if (src.getColorModel().getNumComponents() != 3) {
            throw new IllegalArgumentException("Unsupported colorModel: " + src.getColorModel());
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8},
                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        ColorSpace cs = src.getColorModel().getColorSpace();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] ba = (byte[]) raster.getDataElements(j, i, null);
                float[] fba = new float[] { (ba[0] & 0xFF) / 255f, (ba[1] & 0xFF) / 255f, (ba[2] & 0xFF) / 255f };
                float[] rgb = cs.toRGB(fba);
                ba[0] = (byte) (rgb[0] * 255);
                ba[1] = (byte) (rgb[1] * 255);
                ba[2] = (byte) (rgb[2] * 255);
                rasterDst.setDataElements(j, i, ba);
            }
        }
        return dst;
    }

    public static BufferedImage convertPalettetoRGB(BufferedImage src, BufferedImage dst) {
        ColorModel pcm = src.getColorModel();
        if (!(pcm instanceof PaletteColorModel || pcm instanceof IndexColorModel)) {
            throw new UnsupportedOperationException(
                "Cannot convert " + pcm.getClass().getName() + " to RGB");
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8},
                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] b = convertTo3Bytes(pcm , raster.getDataElements(j, i, null));
                rasterDst.setDataElements(j, i, b);
            }
        }
        return dst;
    }

    private static byte[] convertTo3Bytes(ColorModel pm, Object data) {
        byte[] b = new byte[3];
        int pix;
        if (data instanceof byte[]) {
            byte[] pixels = (byte[]) data;
            pix = pm.getRGB(pixels[0]);

        } else {
            short[] pixels = (short[]) data;
            pix = pm.getRGB(pixels[0]);
        }
        b[0] = (byte) ((pix >> 16) & 0xff);
        b[1] = (byte) ((pix >> 8) & 0xff);
        b[2] = (byte) (pix & 0xff);
        return b;
    }

    /**
     * Set Image Pixel Module Attributes from Buffered Image. Supports Buffered Images with ColorSpace GRAY or RGB
     * and with a DataBuffer containing one bank of unsigned byte data.
     *
     * @param bi Buffered Image
     * @param attrs Data Set to supplement with Image Pixel Module Attributes or {@code null}
     * @return Data Set with included Image Pixel Module Attributes
     * @throws UnsupportedOperationException if the ColorSpace or DataBuffer of the Buffered Image is not supported
     */
    public static Attributes toImagePixelModule(BufferedImage bi, Attributes attrs) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof IndexColorModel || cm instanceof PaletteColorModel) {
            bi = convertPalettetoRGB(bi, null);
            cm = bi.getColorModel();
        }
        ColorSpace cs = cm.getColorSpace();
        Raster raster = bi.getRaster();
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        switch (cs.getType()) {
            case ColorSpace.TYPE_GRAY:
                return toImagePixelModule(1, "MONOCHROME2", 1, rows, columns,
                        toMonochrome2PixelData(raster), attrs);
            case ColorSpace.TYPE_RGB:
                return toImagePixelModule(3, "RGB", 1, rows, columns,
                        toRGBPixelData(raster), attrs);
            default:
                throw new UnsupportedOperationException(toString(cs));
        }
    }

    /**
     * Set Image Pixel Module Attributes from animated GIF.
     *
     * @param animatedGIF animated GIF
     * @param attrs Data Set to supplement with Image Pixel Module Attributes or {@code null}
     * @return Data Set with included Image Pixel Module Attributes
     * @throws IIOInvalidTreeException if animatedGIF does not contain an animated GIF
     */
    public static Attributes toImagePixelModule(Iterator<IIOImage> animatedGIF, Attributes attrs)
            throws IIOInvalidTreeException {
        BufferedImage bi = (BufferedImage) animatedGIF.next().getRenderedImage();
        if (!animatedGIF.hasNext()) {
            return toImagePixelModule(bi, attrs);
        }
        BufferedImage rgb = convertPalettetoRGB(bi, null);
        Raster raster = rgb.getRaster();
        List<IIOImage> list = new ArrayList<>();
        while (animatedGIF.hasNext()) {
            list.add(animatedGIF.next());
        }
        int numFrames = list.size() + 1;
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        int frameLength = rows * columns * 3;
        byte[] pixelData = new byte[numFrames * frameLength];
        int offset = 0;
        copyRGBPixelDataTo(raster, pixelData, offset);
        Graphics graphics = bi.getGraphics();
        for (IIOImage frame : list) {
            Node imageDescriptor = getImageDescriptor(frame.getMetadata());
            NamedNodeMap imageDescriptorAttrs = imageDescriptor.getAttributes();
            graphics.drawImage((BufferedImage) frame.getRenderedImage(),
                    getIntAttribute(imageDescriptorAttrs, "imageLeftPosition", imageDescriptor),
                    getIntAttribute(imageDescriptorAttrs, "imageTopPosition", imageDescriptor),
                    null);
            convertPalettetoRGB(bi, rgb);
            copyRGBPixelDataTo(raster, pixelData, offset += frameLength);
        }
        graphics.dispose();
        return toImagePixelModule(3, "RGB", numFrames, rows, columns, pixelData, attrs);
    }

    private static int getIntAttribute(NamedNodeMap attrs, String name, Node node) throws IIOInvalidTreeException {
        Node attr = attrs.getNamedItem(name);
        if (attr == null) {
            throw new IIOInvalidTreeException("Required attribute " + name + " not present!", node);
        }
        try {
            return Integer.parseInt(attr.getNodeValue());
        } catch (NumberFormatException e) {
            throw new IIOInvalidTreeException("Bad value for " + node.getNodeName() + " attribute " + name + "!", node);
        }
    }

    private static Node getImageDescriptor(IIOMetadata metadata) throws IIOInvalidTreeException {
        Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node child = root.getFirstChild();
        while (child != null) {
            if ("ImageDescriptor".equals(child.getLocalName())) {
                return child;
            }
            child = child.getNextSibling();
        }
        throw new IIOInvalidTreeException("Required child ImageDescriptor not present!", root);
    }

    private static String toString(ColorSpace cs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = cs.getNumComponents(); i < n; i++) {
            sb.append(i > 0 ? ", 0" : "ColorSpace[").append(cs.getName(i));
        }
        return sb.append(']').toString();
    }

    private static byte[] toMonochrome2PixelData(Raster raster) {
        ComponentSampleModel csb = getComponentSampleModel(raster);
        int pixelStride = csb.getPixelStride();
        int scanlineStride = csb.getScanlineStride();
        int h = csb.getHeight();
        int w = csb.getWidth();
        int offset = csb.getOffset(0, 0);
        byte[] src = getData(raster);
        byte[] dest = new byte[h * w];
        for (int y = 0, j = 0; y < h; y++) {
            for (int x = 0, i = y * scanlineStride; x < w; x++, i += pixelStride) {
                dest[j++] = src[i + offset];
            }
        }
        return dest;
    }

    private static byte[] toRGBPixelData(Raster raster) {
        byte[] dest = new byte[raster.getHeight() * raster.getWidth() * 3];
        copyRGBPixelDataTo(raster, dest, 0);
        return dest;
    }

    private static void copyRGBPixelDataTo(Raster raster, byte[] dest, int offset) {
        ComponentSampleModel csb = getComponentSampleModel(raster);
        int pixelStride = csb.getPixelStride();
        int scanlineStride = csb.getScanlineStride();
        int h = csb.getHeight();
        int w = csb.getWidth();
        int r = csb.getOffset(0, 0, 0);
        int g = csb.getOffset(0, 0, 1);
        int b = csb.getOffset(0, 0, 2);
        byte[] src = getData(raster);
        for (int y = 0, j = offset; y < h; y++) {
            for (int x = 0, i = y * scanlineStride; x < w; x++, i += pixelStride) {
                dest[j++] = src[i + r];
                dest[j++] = src[i + g];
                dest[j++] = src[i + b];
            }
        }
    }

    private static byte[] getData(Raster raster) {
        DataBuffer dataBuffer = raster.getDataBuffer();
        if (!(dataBuffer instanceof DataBufferByte) || dataBuffer.getNumBanks() > 1) {
            throw new UnsupportedOperationException(raster.toString());
        }
        return ((DataBufferByte) dataBuffer).getData();
    }

    private static ComponentSampleModel getComponentSampleModel(Raster raster) {
        SampleModel sb = raster.getSampleModel();
        if (!(sb instanceof ComponentSampleModel)) {
            throw new UnsupportedOperationException(sb.toString());
        }
        return (ComponentSampleModel) sb;
    }

    private static Attributes toImagePixelModule(int samples, String pmi, int numberOfFrames, int rows,  int columns,
            byte[] pixelData, Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(11);
        }
        attrs.setInt(Tag.SamplesPerPixel, VR.US, samples);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, pmi);
        if (samples > 1) {
            attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        }
        if (numberOfFrames > 1) {
            attrs.setInt(Tag.NumberOfFrames, VR.IS, numberOfFrames);
        }
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        attrs.setInt(Tag.BitsAllocated, VR.US, 8);
        attrs.setInt(Tag.BitsStored, VR.US, 8);
        attrs.setInt(Tag.HighBit, VR.US, 7);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        attrs.setBytes(Tag.PixelData, VR.OB, pixelData);
        return attrs;
    }

}
