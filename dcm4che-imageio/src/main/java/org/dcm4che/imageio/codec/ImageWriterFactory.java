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
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

import org.dcm4che.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che.util.Property;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class ImageWriterFactory implements Serializable {

    private static final long serialVersionUID = 6328126996969794374L;

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
    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }

    private static ImageWriterFactory defaultFactory;

    private PatchJPEGLS patchJPEGLS;
    private final HashMap<String, ImageWriterParam> map = new HashMap<String, ImageWriterParam>();

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
                "org/dcm4che/imageio/codec/ImageWriterFactory.properties");
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

        // ImageWriterSpi are laoded through the java ServiceLoader,
        // istead of imageio ServiceRegistry
        Iterator<ImageWriterSpi> iter = ServiceLoader
                .load(ImageWriterSpi.class).iterator();

        try {

            if (iter != null && iter.hasNext()) {

                String className = param.className;
                if (className == null)
                    return iter.next().createWriterInstance();

                do {
                    ImageWriterSpi writerspi = iter.next();
                    if (supportsFormat(writerspi.getFormatNames(),
                            param.formatName)) {

                        ImageWriter writer = writerspi.createWriterInstance();

                        if (writer.getClass().getName().equals(className))
                            return writer;
                    }
                } while (iter.hasNext());
            }

            throw new RuntimeException("No Image Writer for format: "
                    + param.formatName + " registered");

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error instantiating Writer for format: "
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
