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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Image Readers for different DICOM transfer syntaxes and MIME types.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@LDAP(objectClasses = "dcmImageReaderFactory")
@ConfigurableClass
public class ImageReaderFactory implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(ImageReaderFactory.class);

    /** Optional flag that */
    public static final String IMAGE_TYPE_SPECIFIER_REQUIRED = "IMAGE_TYPE_SPECIFIER_REQUIRED";

    private static final long serialVersionUID = -2881173333124498212L;

    @LDAP(objectClasses = "dcmImageReader")
    @ConfigurableClass
    public static class ImageReaderParam implements Serializable {

        private static final long serialVersionUID = 6593724836340684578L;

        @ConfigurableProperty(name = "dcmIIOFormatName")
        public String formatName;

        @ConfigurableProperty(name = "dcmJavaClassName")
        public String className;

        @ConfigurableProperty(name = "dcmPatchJPEGLS")
        public PatchJPEGLS patchJPEGLS;

        @ConfigurableProperty(name = "dcmImageTypeSpecifierRequired", defaultValue = "false")
        private boolean imageTypeSpecifierRequired;

        public ImageReaderParam() {
        }

        public ImageReaderParam(String formatName, String className) {
            this.formatName = formatName;
            this.className = nullify(className);
        }

        public ImageReaderParam(String formatName, String className, String patchJPEGLS) {
            this(formatName, className);
            setPatchJPEGLS(patchJPEGLS);
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

        public void setPatchJPEGLS(String patchJPEGLS) {
            if(patchJPEGLS != null && !patchJPEGLS.isEmpty()) {
                setPatchJPEGLS( PatchJPEGLS .valueOf(patchJPEGLS));
            }
        }

        public boolean isImageTypeSpecifierRequired() {
            return imageTypeSpecifierRequired;
        }

        public void setImageTypeSpecifierRequired(boolean imageTypeSpecifierRequired) {
            this.imageTypeSpecifierRequired = imageTypeSpecifierRequired;
        }
    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }

    private static ImageReaderFactory defaultFactory;

    @LDAP(distinguishingField = "dicomTransferSyntax", noContainerNode = true)
    @ConfigurableProperty(
            name="dicomImageReaderMap",
            label = "Image Readers by Transfer Syntax",
            description = "Image readers by Transfer Syntax"
    )
    private Map<String, ImageReaderParam> mapTransferSyntaxUIDs = new TreeMap<String, ImageReaderParam>();
    
    @ConfigurableProperty(
            name="dicomImageReaderMapMime",
            label = "Image Readers by MIME type",
            description = "Image readers by MIME type"
    )
    private Map<String, ImageReaderParam> mapMimeTypes = new TreeMap<String, ImageReaderParam>();

    public Map<String, ImageReaderParam> getMapTransferSyntaxUIDs() {
        return mapTransferSyntaxUIDs;
    }

    public void setMapTransferSyntaxUIDs(Map<String, ImageReaderParam> mapTransferSyntaxUIDs) {
        this.mapTransferSyntaxUIDs = mapTransferSyntaxUIDs;
    }

    public Map<String, ImageReaderParam> getMapMimeTypes() {
        return mapMimeTypes;
    }

    public void setMapMimeTypes(Map<String, ImageReaderParam> mapMimeTypes) {
        this.mapMimeTypes = mapMimeTypes;
    }

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

        factory.init();

        return factory;
    }

    public void init() {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Image Readers:\n");
            for (Entry<String, ImageReaderParam> entry : mapTransferSyntaxUIDs.entrySet()) {
                String tsUid = entry.getKey();
                sb.append(' ').append(tsUid);
                sb.append(" (").append(UID.nameOf(tsUid)).append("): ");
                sb.append(getImageReaderName(entry.getValue())).append('\n');
            }
            for (Entry<String, ImageReaderParam> entry : mapMimeTypes.entrySet()) {
                sb.append(' ').append(entry.getKey()).append(": ");
                sb.append(getImageReaderName(entry.getValue())).append('\n');
            }
            LOG.debug(sb.toString());
        }
    }

    private String getImageReaderName(ImageReaderParam imageReaderParam) {
        ImageReader imageReader = null;
        try {
            imageReader = getImageReader(imageReaderParam);
        } catch (RuntimeException e) {
            // none found
        }
        return imageReader != null ? imageReader.getClass().getName() : "null";
    }

    public void load(String name) throws IOException {
        URL url;
        try {
            url = new URL(name);
            LOG.debug("Loading {} image reader factory", url);
        } catch (MalformedURLException e) {
            url = ResourceLocator.getResourceURL(name, this.getClass());            
            if (url == null) {
                File f = new File(name);
                if(f.exists() && f.isFile()) {
                    url = f.toURI().toURL();
                    LOG.debug("Loading file {}", f.getAbsoluteFile());
                } else {
                    throw new IOException("No such resource: " + name);
                }
            } else {
                LOG.debug("Loading {} resource", url);
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

            ImageReaderParam readerParam = new ImageReaderParam(formatName, className);

            // parse the optional flags at the end of the settings
            for(String flag : StringUtils.split(ss[2], ',')) {
                flag = flag.trim();

                if(IMAGE_TYPE_SPECIFIER_REQUIRED.equals(flag)) {
                    readerParam.setImageTypeSpecifierRequired(true);
                }
                else {
                    try {
                        PatchJPEGLS patch = PatchJPEGLS.valueOf(flag);
                        if (patch != null) {
                            readerParam.setPatchJPEGLS(patch);
                        }
                    }
                    catch(IllegalArgumentException e) {
                        LOG.warn("Invalid ImageReader flag ignored: {}", flag);
                    }
                }
            }

            if (key.contains("/")) { // mime type
                mapMimeTypes.put(key, readerParam);
            } else { // transfer syntax uid
                mapTransferSyntaxUIDs.put(key, readerParam);
            }
        }
    }

    private ImageReaderParam getForTransferSyntaxUID(String tsuid) {
        return mapTransferSyntaxUIDs.get(tsuid);
    }

    private ImageReaderParam getForMimeType(String mimeType) {
        return mapMimeTypes.get(mimeType);
    }

    private boolean containsTransferSyntaxUID(String tsuid) {
        return mapTransferSyntaxUIDs.containsKey(tsuid);
    }

    public static ImageReaderParam getImageReaderParam(String tsuid) {
        return getDefault().getForTransferSyntaxUID(tsuid);
    }

    public static boolean canDecompress(String tsuid) {
        return getDefault().containsTransferSyntaxUID(tsuid);
    }

    public static ImageReader getImageReader(ImageReaderParam param) {

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(param.formatName);

        while (readers.hasNext()) {
            ImageReader reader = readers.next();

            if (param.className == null || param.className.equals(reader.getClass().getName())) {
                LOG.debug("Using Image Reader {}", reader.getClass());
                return reader;
            }
        }

        throw new RuntimeException("No matching Image Reader for format: " + param.formatName + " (Class: " + ((param.className == null) ? "*" : param.className) + ") registered");
    }

    public static ImageReader getImageReaderForMimeType(String mimeType) {
        ImageReaderParam imageReaderParam = getDefault().getForMimeType(mimeType);

        if (imageReaderParam != null) {
            // configured mime type
            return getImageReader(imageReaderParam);
        } else {
            // not configured mime type, fallback to first ImageIO reader for this mime type
            ImageReader reader = ImageIO.getImageReadersByMIMEType(mimeType).next();
            LOG.debug("Using Image Reader {}", reader.getClass());
            return reader;
        }
    }

}
