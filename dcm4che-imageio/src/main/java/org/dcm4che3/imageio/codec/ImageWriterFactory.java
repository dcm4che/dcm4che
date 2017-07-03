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

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Provides Image Writers for different DICOM transfer syntaxes and MIME types.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@LDAP(objectClasses = "dcmImageWriterFactory")
@ConfigurableClass
public class ImageWriterFactory implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(ImageWriterFactory.class);

    private static final long serialVersionUID = 6328126996969794374L;

    @LDAP(objectClasses = "dcmImageWriter")
    @ConfigurableClass
    public static class ImageWriterParam implements Serializable {

        private static final long serialVersionUID = 3521737269113651910L;

        @ConfigurableProperty(name="dcmIIOFormatName")
        public String formatName;

        @ConfigurableProperty(name="dcmJavaClassName")
        public String className;

        @ConfigurableProperty(name="dcmPatchJPEGLS")
        public PatchJPEGLS patchJPEGLS;

        @ConfigurableProperty(name = "dcmImageWriteParam")
        public Property[] imageWriteParams;

        @ConfigurableProperty(name = "dcmWriteIIOMetadata")
        public Property[] iioMetadata;

        public ImageWriterParam() {
        }

        public ImageWriterParam(String formatName, String className,
                PatchJPEGLS patchJPEGLS, Property[] imageWriteParams, Property[] iioMetadata) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageWriteParams = imageWriteParams;
            this.iioMetadata = iioMetadata;
        }

        public ImageWriterParam(String formatName, String className,
                String patchJPEGLS, String[] imageWriteParams, String[] iioMetadata) {
            this(formatName, className, patchJPEGLS != null
                    && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                    .valueOf(patchJPEGLS) : null, Property
                    .valueOf(imageWriteParams), Property.valueOf(iioMetadata));
        }

        public ImageWriterParam(String formatName, String className,
                String patchJPEGLS, String[] imageWriteParams) {
            this(formatName, className, patchJPEGLS != null
                    && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                    .valueOf(patchJPEGLS) : null, Property
                    .valueOf(imageWriteParams), null);
        }


        public Property[] getImageWriteParams() {
            return imageWriteParams;
        }

        public Property[] getIIOMetadata() {
            return iioMetadata;
        }

        public String getFormatName() {
            return formatName;
        }

        public void setFormatName(String formatName) {
            this.formatName = formatName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public PatchJPEGLS getPatchJPEGLS() {
            return patchJPEGLS;
        }

        public void setPatchJPEGLS(PatchJPEGLS patchJPEGLS) {
            this.patchJPEGLS = patchJPEGLS;
        }

        public void setImageWriteParams(Property[] imageWriteParams) {
            this.imageWriteParams = imageWriteParams;
        }

        public Property[] getIioMetadata() {
            return iioMetadata;
        }

        public void setIioMetadata(Property[] iioMetadata) {
            this.iioMetadata = iioMetadata;
        }
    }

    private static ImageWriterFactory defaultFactory;

    @LDAP(distinguishingField = "dicomTransferSyntax", noContainerNode = true)
    @ConfigurableProperty(
        name="dicomImageWriterMap",
        label = "Image Writers by transfer syntax",
        description = "Image writers by transfer syntax"
    )
    private Map<String, ImageWriterParam> mapTransferSyntaxUIDs = new TreeMap<String, ImageWriterParam>();
    
    @ConfigurableProperty(
            name="dicomImageWriterMapMime",
            label = "Image Writers by MIME type",
            description = "Image writers by MIME type"
    )
    private Map<String, ImageWriterParam> mapMimeTypes = new TreeMap<String, ImageWriterParam>();

    public Map<String, ImageWriterParam> getMapTransferSyntaxUIDs() {
        return mapTransferSyntaxUIDs;
    }

    public void setMapTransferSyntaxUIDs(Map<String, ImageWriterParam> mapTransferSyntaxUIDs) {
        this.mapTransferSyntaxUIDs = mapTransferSyntaxUIDs;
    }

    public Map<String, ImageWriterParam> getMapMimeTypes() {
        return mapMimeTypes;
    }

    public void setMapMimeTypes(Map<String, ImageWriterParam> mapMimeTypes) {
        this.mapMimeTypes = mapMimeTypes;
    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }

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

        factory.init();

        return factory;
    }

    public void init() {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Image Writers:\n");
            for (Entry<String, ImageWriterParam> entry : mapTransferSyntaxUIDs.entrySet()) {
                String tsUid = entry.getKey();
                sb.append(' ').append(tsUid);
                sb.append(" (").append(UID.nameOf(tsUid)).append("): ");
                sb.append(getImageWriterName(entry.getValue())).append('\n');
            }
            for (Entry<String, ImageWriterParam> entry : mapMimeTypes.entrySet()) {
                sb.append(' ').append(entry.getKey()).append(": ");
                sb.append(getImageWriterName(entry.getValue())).append('\n');
            }
            LOG.debug(sb.toString());
        }
    }

    private String getImageWriterName(ImageWriterParam imageWriterParam) {
        ImageWriter imageWriter = null;
        try {
            imageWriter = getImageWriter(imageWriterParam);
        } catch (RuntimeException e) {
            // none found
        }
        return imageWriter != null ? imageWriter.getClass().getName() : "null";
    }

    public void load(String name) throws IOException {
        URL url;
        try {
            url = new URL(name);
        } catch (MalformedURLException e) {
            url = ResourceLocator.getResourceURL(name, this.getClass());
            if (url == null) {
                File f = new File(name);
                if(f.exists() && f.isFile()) {
                    url = f.toURI().toURL();
                } else {
                    throw new IOException("No such resource: " + name);
                }
            }
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
            String key = (String) entry.getKey();

            String[] ss = StringUtils.split((String) entry.getValue(), ':');
            String formatName = ss[0];
            String className = ss[1];
            String patchJPEGLS = ss[2];
            String[] imageWriteParams = StringUtils.split(ss[3], ';');

            if (key.contains("/")) { // mime type
                mapMimeTypes.put(key, new ImageWriterParam(formatName, className, patchJPEGLS, imageWriteParams));
            } else { // transfer syntax uid
                mapTransferSyntaxUIDs.put(key, new ImageWriterParam(formatName, className, patchJPEGLS, imageWriteParams));
            }

        }
    }

    public ImageWriterParam getForTransferSyntaxUID(String tsuid) {
        return mapTransferSyntaxUIDs.get(tsuid);
    }

    public ImageWriterParam getForMimeType(String mimeType) {
        return mapMimeTypes.get(mimeType);
    }

    public static ImageWriterParam getImageWriterParam(String tsuid) {
        return getDefault().getForTransferSyntaxUID(tsuid);
    }

    public static ImageWriter getImageWriter(ImageWriterParam param) {

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(param.formatName);

        while (writers.hasNext()) {
            ImageWriter writer = writers.next();

            if (param.className == null || param.className.equals(writer.getClass().getName())) {
                LOG.debug("Using Image Writer {}", writer.getClass());
                return writer;
            }
        }

        throw new RuntimeException("No matching Image Writer for format: " + param.formatName + " (Class: " + ((param.className == null) ? "*" : param.className) + ") registered");
    }

    public static ImageWriter getImageWriterForMimeType(String mimeType) {
        ImageWriterParam imageWriterParam = getDefault().getForMimeType(mimeType);

        if (imageWriterParam != null) {
            // configured mime type
            return getImageWriter(imageWriterParam);
        } else {
            // not configured mime type, fallback to first ImageIO writer for this mime type
            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            LOG.debug("Using Image Writer {}", writer.getClass());
            return writer;
        }
    }

}
