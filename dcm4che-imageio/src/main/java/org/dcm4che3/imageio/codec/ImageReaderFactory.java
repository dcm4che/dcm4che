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

package org.dcm4che3.imageio.codec;

import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ImageReaderFactory implements Serializable {

    private static final long serialVersionUID = -2881173333124498212L;

    private static final Logger LOG = LoggerFactory.getLogger(ImageReaderFactory.class);

    public static class ImageReaderParam implements Serializable {

        private static final long serialVersionUID = 6593724836340684578L;

        public final String formatName;
        public final String className;
        public final PatchJPEGLS patchJPEGLS;
        public final Property[] imageReadParams;

        public ImageReaderParam(String formatName, String className,
                                PatchJPEGLS patchJPEGLS, Property[] imageReadParams) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageReadParams = imageReadParams;
        }

        public ImageReaderParam(String formatName, String className,
                                String patchJPEGLS, String... imageWriteParams) {
            this(formatName, className,
                    patchJPEGLS != null  && !patchJPEGLS.isEmpty()
                            ? PatchJPEGLS.valueOf(patchJPEGLS)
                            : null,
                    Property.valueOf(imageWriteParams));
        }

        public Property[] getImageReadParams() {
            return imageReadParams;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageReaderParam that = (ImageReaderParam) o;

            if (!formatName.equals(that.formatName)) return false;
            if (className != null ? !className.equals(that.className) : that.className != null) return false;
            if (patchJPEGLS != that.patchJPEGLS) return false;
            return Arrays.equals(imageReadParams, that.imageReadParams);

        }

        @Override
        public int hashCode() {
            int result = formatName.hashCode();
            result = 31 * result + (className != null ? className.hashCode() : 0);
            result = 31 * result + (patchJPEGLS != null ? patchJPEGLS.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(imageReadParams);
            return result;
        }

        @Override
        public String toString() {
            return "ImageReaderParam{" +
                    "formatName='" + formatName + '\'' +
                    ", className='" + className + '\'' +
                    ", patchJPEGLS=" + patchJPEGLS +
                    ", imageReadParams=" + Arrays.toString(imageReadParams) +
                    '}';
        }
    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }

    private static volatile ImageReaderFactory defaultFactory;

    private final TreeMap<String, ImageReaderParam> map = new TreeMap<>();

    public static ImageReaderFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = initDefault();

        return defaultFactory;
    }

    public static void resetDefault() {
        defaultFactory = null;
    }

    public static void setDefault(ImageReaderFactory factory) {
        if (factory == null)
            throw new NullPointerException();

        defaultFactory = factory;
    }

    private static ImageReaderFactory initDefault() {
        ImageReaderFactory factory = new ImageReaderFactory();
        String name = System.getProperty(ImageReaderFactory.class.getName(),
                "org/dcm4che3/imageio/codec/ImageReaderFactory.properties");
        try {
            factory.load(name);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load Image Reader Factory configuration from: "
                            + name, e);
        }
        return factory;
    }

    public void load(String name) throws IOException {
        URL url;
        try {
            url = new URL(name);
        } catch (MalformedURLException e) {
            url = ResourceLocator.getResourceURL(name, this.getClass());
            if (url == null)
                throw new IOException("No such resource: " + name);
        }
        InputStream in = url.openStream();
        try {
            load(in);
        } finally {
            SafeClose.close(in);
        }
    }

    public void load(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String[] ss = StringUtils.split((String) entry.getValue(), ':');
            map.put((String) entry.getKey(), new ImageReaderParam(ss[0], ss[1], ss[2],
                    ss.length > 3 ? StringUtils.split(ss[3], ';') : StringUtils.EMPTY_STRING));
        }
    }

    public ImageReaderParam get(String tsuid) {
        return map.get(tsuid);
    }

    public boolean contains(String tsuid) {
        return map.containsKey(tsuid);
    }

    public ImageReaderParam put(String tsuid, ImageReaderParam param) {
        return map.put(tsuid, param);
    }

    public ImageReaderParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    public Set<Entry<String, ImageReaderParam>> getEntries() {
        return Collections.unmodifiableMap(map).entrySet();
    }

    public void clear() {
        map.clear();
    }

    public static ImageReaderParam getImageReaderParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    public static boolean canDecompress(String tsuid) {
        return getDefault().contains(tsuid);
    }

    public static ImageReader getImageReader(ImageReaderParam param) {
        return Boolean.getBoolean("org.dcm4che3.imageio.codec.useServiceLoader")
                ? getImageReaderFromServiceLoader(param)
                : getImageReaderFromImageIOServiceRegistry(param);
    }

    public static ImageReader getImageReaderFromImageIOServiceRegistry(ImageReaderParam param) {
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName(param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Reader for format: " + param.formatName + " registered");

        ImageReader reader = iter.next();
        if (param.className != null) {
            while (!param.className.equals(reader.getClass().getName())) {
                if (iter.hasNext())
                    reader = iter.next();
                else {
                    LOG.warn("No preferred Reader {} for format: {} - use {}",
                            param.className, param.formatName, reader.getClass().getName());
                    break;
                }
            }
        }
        return reader;
    }

    public static ImageReader getImageReaderFromServiceLoader(ImageReaderParam param) {
        try {
            return getImageReaderSpi(param).createReaderInstance();
        } catch (IOException e) {
            throw new RuntimeException("Error instantiating Reader for format: "  + param.formatName, e);
        }
    }

    private static ImageReaderSpi getImageReaderSpi(ImageReaderParam param) {
        Iterator<ImageReaderSpi> iter = new FormatNameFilterIterator<ImageReaderSpi>(
                ServiceLoader.load(ImageReaderSpi.class).iterator(), param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Reader for format: " + param.formatName + " registered");

        ImageReaderSpi spi = iter.next();
        if (param.className != null) {
            while (!param.className.equals(spi.getPluginClassName())) {
                if (iter.hasNext())
                    spi = iter.next();
                else {
                    LOG.warn("No preferred Reader {} for format: {} - use {}",
                            param.className, param.formatName, spi.getPluginClassName());
                    break;
                }
            }
        }
        return spi;
    }
}
