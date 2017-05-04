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

package org.dcm4che3.imageio.codec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

        private String name;

        public ImageWriterParam() {
        }

        public ImageWriterParam(String formatName, String className, PatchJPEGLS patchJPEGLS,
            Property[] imageWriteParams, Property[] iioMetadata, String name) {
            if (formatName == null || name == null)
                throw new IllegalArgumentException();
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageWriteParams = imageWriteParams;
            this.iioMetadata = iioMetadata;
            this.name = name;
        }

        public ImageWriterParam(String formatName, String className, String patchJPEGLS, String[] imageWriteParams,
            String[] iioMetadata, String name) {
            this(formatName, className, patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                .valueOf(patchJPEGLS) : null, Property.valueOf(imageWriteParams), Property.valueOf(iioMetadata), name);
        }

        public ImageWriterParam(String formatName, String className, String patchJPEGLS, String[] imageWriteParams,
            String name) {
            this(formatName, className, patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                .valueOf(patchJPEGLS) : null, Property.valueOf(imageWriteParams), null, name);
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

        public String tosString() {
            return name;
        }
    }

    public static class ImageWriterItem {

        private final ImageWriter imageWriter;
        private final ImageWriterParam imageWriterParam;

        public ImageWriterItem(ImageWriter imageReader, ImageWriterParam imageReaderParam) {
            this.imageWriter = imageReader;
            this.imageWriterParam = imageReaderParam;
        }

        public ImageWriter getImageWriter() {
            return imageWriter;
        }

        public ImageWriterParam getImageWriterParam() {
            return imageWriterParam;
        }
    }

    private static volatile ImageWriterFactory defaultFactory;

    @LDAP(distinguishingField = "dicomTransferSyntax", noContainerNode = true)
    @ConfigurableProperty(
        name="dicomImageWriterMap",
        label = "Image Writers by transfer syntax",
        description = "Image writers by transfer syntax"
    )

    private Map<String, List<ImageWriterParam>> mapTransferSyntaxUIDs = new LinkedHashMap<String, List<ImageWriterParam>>();

    
    @ConfigurableProperty(
            name="dicomImageWriterMapMime",
            label = "Image Writers by MIME type",
            description = "Image writers by MIME type"
    )
    private Map<String, List<ImageWriterParam>> mapMimeTypes = new TreeMap<String, List<ImageWriterParam>>();

    public Map<String, List<ImageWriterParam>> getMapTransferSyntaxUIDs() {
        return mapTransferSyntaxUIDs;
    }

    public void setMapTransferSyntaxUIDs(Map<String, List<ImageWriterParam>> mapTransferSyntaxUIDs) {
        this.mapTransferSyntaxUIDs = mapTransferSyntaxUIDs;
    }

    public Map<String, List<ImageWriterParam>> getMapMimeTypes() {
        return mapMimeTypes;
    }

    public void setMapMimeTypes(Map<String, List<ImageWriterParam>> mapMimeTypes) {
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
        String name =
            System.getProperty(ImageWriterFactory.class.getName(), "org/dcm4che3/imageio/codec/ImageWriterFactory.xml");
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
            for (Entry<String, List<ImageWriterParam>> entry : mapTransferSyntaxUIDs.entrySet()) {
                String tsUid = entry.getKey();
                sb.append(' ').append(tsUid);
                sb.append(" (").append(UID.nameOf(tsUid)).append("): ");
                for(ImageWriterParam reader : entry.getValue()){
                    sb.append(reader.name);
                    sb.append(' ');
                }
                sb.append('\n');
            }
            for (Entry<String, List<ImageWriterParam>> entry : mapMimeTypes.entrySet()) {
                sb.append(' ').append(entry.getKey()).append(": ");
                for(ImageWriterParam reader : entry.getValue()){
                    sb.append(reader.name);
                    sb.append(' ');
                }
                sb.append('\n');
            }
            LOG.debug(sb.toString());
        }
    }

    public void load(InputStream stream) throws IOException {
        XMLStreamReader xmler = null;
        try {
            String sys = ImageReaderFactory.getNativeSystemSpecification();
            
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            xmler = xmlif.createXMLStreamReader(stream);

            int eventType;
            while (xmler.hasNext()) {
                eventType = xmler.next();
                switch (eventType) {
                    case XMLStreamConstants.START_ELEMENT:
                        String key = xmler.getName().getLocalPart();
                        if ("ImageWriterFactory".equals(key)) {
                            while (xmler.hasNext()) {
                                eventType = xmler.next();
                                switch (eventType) {
                                    case XMLStreamConstants.START_ELEMENT:
                                        key = xmler.getName().getLocalPart();
                                        if ("element".equals(key)) {
                                            String tsuid = xmler.getAttributeValue(null, "tsuid");
                                            String mime = xmler.getAttributeValue(null, "mime");

                                            boolean state = true;
                                            while (xmler.hasNext() && state) {
                                                eventType = xmler.next();
                                                switch (eventType) {
                                                    case XMLStreamConstants.START_ELEMENT:
                                                        key = xmler.getName().getLocalPart();
                                                        if ("writer".equals(key)) {
                                                            String s = xmler.getAttributeValue(null, "sys");
                                                            String[] systems = s == null ? null : s.split(",");
                                                            if (systems == null
                                                                || (sys != null && Arrays.binarySearch(systems, sys) >= 0)) {
                                                                // Only add readers that can run on the current system
                                                                ImageWriterParam param =
                                                                    new ImageWriterParam(xmler.getAttributeValue(null,
                                                                        "format"), xmler.getAttributeValue(null,
                                                                        "class"), xmler.getAttributeValue(null,
                                                                        "patchJPEGLS"), StringUtils.split(
                                                                        xmler.getAttributeValue(null, "params"), ';'),
                                                                        xmler.getAttributeValue(null, "name"));
                                                                if(tsuid != null){
                                                                	putForTransferSyntaxUID(tsuid, param);
                                                                }
                                                                if(mime != null){
                                                                	putForMimeType(mime, param);
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    case XMLStreamConstants.END_ELEMENT:
                                                        if ("element".equals(xmler.getName().getLocalPart())) {
                                                            state = false;
                                                        }
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        catch (XMLStreamException e) {
            LOG.error("Cannot read DICOM Writers! " + e.getMessage());
        } finally {
            if (xmler != null) {
                try {
                    xmler.close();
                } catch (XMLStreamException e) {
                    LOG.debug(e.getMessage());
                }
            }
            SafeClose.close(stream);
        }
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

    public List<ImageWriterParam> getForTransferSyntaxUID(String tsuid) {
        return mapTransferSyntaxUIDs.get(tsuid);
    }

    public boolean putForTransferSyntaxUID(String tsuid, ImageWriterParam param) {
        List<ImageWriterParam> writerSet = getForTransferSyntaxUID(tsuid);
        if (writerSet == null) {
            writerSet = new ArrayList<ImageWriterParam>();
            mapTransferSyntaxUIDs.put(tsuid, writerSet);
        }
        return writerSet.add(param);
    }
    
    public List<ImageWriterParam> getForMimeType(String mimeType) {
        return mapMimeTypes.get(mimeType);
    }
    
    public boolean putForMimeType(String mimeType, ImageWriterParam param) {
        List<ImageWriterParam> writerSet = getForMimeType(mimeType);
        if (writerSet == null) {
            writerSet = new ArrayList<ImageWriterParam>();
            mapMimeTypes.put(mimeType, writerSet);
        }
        return writerSet.add(param);
    }

    public static ImageWriterItem getImageWriterParam(String tsuid) {
        List<ImageWriterParam> list = getDefault().getForTransferSyntaxUID(tsuid);
        if (list != null) {
            synchronized (list) {
                for (Iterator<ImageWriterParam> it = list.iterator(); it.hasNext();) {
                    ImageWriterParam imageParam = it.next();
                    String cl = imageParam.getClassName();
                    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(imageParam.getFormatName());
                    while (iter.hasNext()) {
                        ImageWriter writer = iter.next();
                        if (cl == null || writer.getClass().getName().equals(cl)) {
                            return new ImageWriterItem(writer, imageParam);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static ImageWriterItem getImageWriterParamByMimeType(String mimeType) {
        List<ImageWriterParam> list = getDefault().getForMimeType(mimeType);
        if (list != null) {
            synchronized (list) {
                for (Iterator<ImageWriterParam> it = list.iterator(); it.hasNext();) {
                    ImageWriterParam imageParam = it.next();
                    String cl = imageParam.getClassName();
                    Iterator<ImageWriter> iter = ImageIO.getImageWritersByMIMEType(mimeType);
                    while (iter.hasNext()) {
                        ImageWriter writer = iter.next();
                        if (cl == null || writer.getClass().getName().equals(cl)) {
                            return new ImageWriterItem(writer, imageParam);
                        }
                    }
                }
            }
        }
        return null;
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
        ImageWriterItem imageWriter = getImageWriterParamByMimeType(mimeType);

        if (imageWriter != null) {
        	// configured mime type
            return imageWriter.getImageWriter();
        } else {
            // not configured mime type, fallback to first ImageIO writer for this mime type
            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            LOG.debug("Using Image Writer {}", writer.getClass());
            return writer;
        }
    }
}
