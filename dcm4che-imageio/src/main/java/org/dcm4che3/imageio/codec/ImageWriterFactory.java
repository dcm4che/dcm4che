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

import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
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
public class ImageWriterFactory implements Serializable {

    private static final long serialVersionUID = 6328126996969794374L;

    private static final Logger LOG = LoggerFactory.getLogger(ImageWriterFactory.class);

    public static class ImageWriterParam implements Serializable {

        private static final long serialVersionUID = 3521737269113651910L;

        public final String formatName;
        public final String className;
        public final PatchJPEGLS patchJPEGLS;
        public final Property[] imageWriteParams;

        public ImageWriterParam(String formatName, String className,
                PatchJPEGLS patchJPEGLS, Property[] imageWriteParams) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageWriteParams = imageWriteParams;
        }

        public ImageWriterParam(String formatName, String className,
                String patchJPEGLS, String[] imageWriteParams) {
            this(formatName, className, patchJPEGLS != null
                    && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                    .valueOf(patchJPEGLS) : null, Property
                    .valueOf(imageWriteParams));
        }


        public Property[] getImageWriteParams() {
            return imageWriteParams;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageWriterParam that = (ImageWriterParam) o;

            if (!formatName.equals(that.formatName)) return false;
            if (className != null ? !className.equals(that.className) : that.className != null) return false;
            if (patchJPEGLS != that.patchJPEGLS) return false;
            return Arrays.equals(imageWriteParams, that.imageWriteParams);

        }

        @Override
        public int hashCode() {
            int result = formatName.hashCode();
            result = 31 * result + (className != null ? className.hashCode() : 0);
            result = 31 * result + (patchJPEGLS != null ? patchJPEGLS.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(imageWriteParams);
            return result;
        }

        @Override
        public String toString() {
            return "ImageWriterParam{" +
                    "formatName='" + formatName + '\'' +
                    ", className='" + className + '\'' +
                    ", patchJPEGLS=" + patchJPEGLS +
                    ", imageWriteParams=" + Arrays.toString(imageWriteParams) +
                    '}';
        }
    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }

    private static volatile ImageWriterFactory defaultFactory;

    private PatchJPEGLS patchJPEGLS;
    private final TreeMap<String, ImageWriterParam> map = new TreeMap<>();

    public static ImageWriterFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = initDefault();

        return defaultFactory;
    }

    public static void resetDefault() {
        defaultFactory = null;
    }

    public static void setDefault(ImageWriterFactory factory) {
        if (factory == null)
            throw new NullPointerException();

        defaultFactory = factory;
    }

    private static ImageWriterFactory initDefault() {
        ImageWriterFactory factory = new ImageWriterFactory();
        String name = System.getProperty(ImageWriterFactory.class.getName(),
                "org/dcm4che3/imageio/codec/ImageWriterFactory.properties");
        try {
            factory.load(name);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load Image Writer Factory configuration from: "
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
            map.put((String) entry.getKey(), new ImageWriterParam(ss[0], ss[1],
                    ss[2], StringUtils.split(ss[3], ';')));
        }
    }

    public final PatchJPEGLS getPatchJPEGLS() {
        return patchJPEGLS;
    }

    public final void setPatchJPEGLS(PatchJPEGLS patchJPEGLS) {
        this.patchJPEGLS = patchJPEGLS;
    }

    public ImageWriterParam get(String tsuid) {
        return map.get(tsuid);
    }

    public ImageWriterParam put(String tsuid, ImageWriterParam param) {
        return map.put(tsuid, param);
    }

    public ImageWriterParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    public Set<Entry<String, ImageWriterParam>> getEntries() {
        return Collections.unmodifiableMap(map).entrySet();
    }

    public void clear() {
        map.clear();
    }

    public static ImageWriterParam getImageWriterParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    public static ImageWriter getImageWriter(ImageWriterParam param) {
        return Boolean.getBoolean("org.dcm4che3.imageio.codec.useServiceLoader")
                ? getImageWriterFromServiceLoader(param)
                : getImageWriterFromImageIOServiceRegistry(param);
    }

    public static ImageWriter getImageWriterFromImageIOServiceRegistry(ImageWriterParam param) {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Writer for format: " + param.formatName + " registered");

        ImageWriter writer = iter.next();
        if (param.className != null) {
            while (!param.className.equals(writer.getClass().getName())) {
                if (iter.hasNext())
                    writer = iter.next();
                else {
                    LOG.warn("No preferred Writer {} for format: {} - use {}",
                            param.className, param.formatName, writer.getClass().getName());
                    break;
                }
            }
        }
        return writer;
    }

    public static ImageWriter getImageWriterFromServiceLoader(ImageWriterParam param) {
        try {
            return getImageWriterSpi(param).createWriterInstance();
        } catch (IOException e) {
            throw new RuntimeException("Error instantiating Writer for format: "  + param.formatName, e);
        }
    }

    private static ImageWriterSpi getImageWriterSpi(ImageWriterParam param) {
        Iterator<ImageWriterSpi> iter = new FormatNameFilterIterator<ImageWriterSpi>(
                ServiceLoader.load(ImageWriterSpi.class).iterator(), param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Writer for format: " + param.formatName + " registered");

        ImageWriterSpi spi = iter.next();
        if (param.className != null) {
            while (!param.className.equals(spi.getPluginClassName())) {
                if (iter.hasNext())
                    spi = iter.next();
                else {
                    LOG.warn("No preferred Writer {} for format: {} - use {}",
                            param.className, param.formatName, spi.getPluginClassName());
                    break;
                }
            }
        }
        return spi;
    }

}
