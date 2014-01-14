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

package org.dcm4che.imageio.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

import org.dcm4che.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class ImageReaderFactory implements Serializable {

    private static final long serialVersionUID = -2881173333124498212L;

    public static class ImageReaderParam implements Serializable {

        private static final long serialVersionUID = 6593724836340684578L;

        public final String formatName;
        public final String className;
        public final PatchJPEGLS patchJPEGLS;

        public ImageReaderParam(String formatName, String className,
                String patchJPEGLS) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                    .valueOf(patchJPEGLS) : null;
        }

    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }

    private static ImageReaderFactory defaultFactory;
    private final HashMap<String, ImageReaderParam> map = new HashMap<String, ImageReaderParam>();

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
                "org/dcm4che/imageio/codec/ImageReaderFactory.properties");
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
            url = StringUtils.getResourceURL(name, this.getClass());
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
            map.put((String) entry.getKey(), new ImageReaderParam(ss[0], ss[1],
                    ss[2]));
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

        // ImageReaderSpi are laoded through the java ServiceLoader,
        // istead of imageio ServiceRegistry
        Iterator<ImageReaderSpi> iter = ServiceLoader
                .load(ImageReaderSpi.class).iterator();

        try {

            if (iter != null && iter.hasNext()) {

                String className = param.className;
                if (className == null)
                    return iter.next().createReaderInstance();

                do {
                    ImageReaderSpi readerspi = iter.next();
                    if (supportsFormat(readerspi.getFormatNames(),
                            param.formatName)) {

                        ImageReader reader = readerspi.createReaderInstance();

                        if (reader.getClass().getName().equals(className))
                            return reader;
                    }
                } while (iter.hasNext());
            }

            throw new RuntimeException("No Image Reader for format: "
                    + param.formatName + " registered");

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error instantiating Reader for format: "
                            + param.formatName);
        }
    }

    private static boolean supportsFormat(String[] supportedFormats,
            String format) {
        boolean supported = false;

        if (format != null && supportedFormats != null) {

            for (int i = 0; i < supportedFormats.length; i++)
                if (supportedFormats[i] != null
                        && supportedFormats[i].trim().equalsIgnoreCase(
                                format.trim()))
                    supported = true;
        }

        return supported;
    }

}
