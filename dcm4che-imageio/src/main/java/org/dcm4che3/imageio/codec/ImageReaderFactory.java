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
import javax.imageio.ImageReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.SafeClose;
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
        
        private String name;

        public ImageReaderParam() {
        }

        public ImageReaderParam(String formatName, String className,
                String patchJPEGLS, String name) {
            if (formatName == null || name == null)
                throw new IllegalArgumentException();
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS
                    .valueOf(patchJPEGLS) : null;
            this.name = name;
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
        
        public String toString() {
            return name;
        }
    }

    private static String nullify(String s) {
        return s == null || s.isEmpty() || s.equals("*") ? null : s;
    }
    
    public static class ImageReaderItem {

        private final ImageReader imageReader;
        private final ImageReaderParam imageReaderParam;

        public ImageReaderItem(ImageReader imageReader, ImageReaderParam imageReaderParam) {
            this.imageReader = imageReader;
            this.imageReaderParam = imageReaderParam;
        }

        public ImageReader getImageReader() {
            return imageReader;
        }

        public ImageReaderParam getImageReaderParam() {
            return imageReaderParam;
        }

    }

    private static volatile ImageReaderFactory defaultFactory;

    @LDAP(distinguishingField = "dicomTransferSyntax", noContainerNode = true)
    @ConfigurableProperty(
            name="dicomImageReaderMap",
            label = "Image Readers by Transfer Syntax",
            description = "Image readers by Transfer Syntax"
    )
    private Map<String, List<ImageReaderParam>> mapTransferSyntaxUIDs = new LinkedHashMap<String, List<ImageReaderParam>>();

    @ConfigurableProperty(
            name="dicomImageReaderMapMime",
            label = "Image Readers by MIME type",
            description = "Image readers by MIME type"
    )
    private Map<String, List<ImageReaderParam>> mapMimeTypes = new TreeMap<String, List<ImageReaderParam>>();
    
    public Map<String, List<ImageReaderParam>> getMapTransferSyntaxUIDs() {
        return mapTransferSyntaxUIDs;
    }

    public void setMapTransferSyntaxUIDs(Map<String, List<ImageReaderParam>> mapTransferSyntaxUIDs) {
        this.mapTransferSyntaxUIDs = mapTransferSyntaxUIDs;
    }
    
    public Map<String, List<ImageReaderParam>> getMapMimeTypes() {
        return mapMimeTypes;
    }

    public void setMapMimeTypes(Map<String, List<ImageReaderParam>> mapMimeTypes) {
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
        String name =
            System.getProperty(ImageReaderFactory.class.getName(), "org/dcm4che3/imageio/codec/ImageReaderFactory.xml");
        try {
            factory.load(name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Image Reader Factory configuration from: " + name, e);
        }

        factory.init();

        return factory;
    }

    public void init() {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Image Readers:\n");
            for (Entry<String, List<ImageReaderParam>> entry : mapTransferSyntaxUIDs.entrySet()) {
                String tsUid = entry.getKey();
                sb.append(' ').append(tsUid);
                sb.append(" (").append(UID.nameOf(tsUid)).append("): ");
                int k =1;
                for(ImageReaderParam reader : entry.getValue()){
                	sb.append(k++);
                	sb.append(") ");
                    sb.append(reader.name);
                    sb.append(' ');
                }
                sb.append('\n');
            }
            for (Entry<String, List<ImageReaderParam>> entry : mapMimeTypes.entrySet()) {
                sb.append(' ').append(entry.getKey()).append(": ");
                int k =1;
                for(ImageReaderParam reader : entry.getValue()){
                  	sb.append(k++);
                	sb.append(") ");
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
            String sys = getNativeSystemSpecification();

            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            xmler = xmlif.createXMLStreamReader(stream);

            int eventType;
            while (xmler.hasNext()) {
                eventType = xmler.next();
                switch (eventType) {
                    case XMLStreamConstants.START_ELEMENT:
                        String key = xmler.getName().getLocalPart();
                        if ("ImageReaderFactory".equals(key)) {
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
                                                        if ("reader".equals(key)) {
                                                            String s = xmler.getAttributeValue(null, "sys");
                                                            String[] systems = s == null ? null : s.split(",");
                                                            if (systems == null
                                                                || (sys != null && Arrays.asList(systems).contains(sys))) {
                                                                // Only add readers that can run on the current system
                                                                ImageReaderParam param =
                                                                    new ImageReaderParam(xmler.getAttributeValue(null,
                                                                        "format"), xmler.getAttributeValue(null,
                                                                        "class"), xmler.getAttributeValue(null,
                                                                        "patchJPEGLS"), xmler.getAttributeValue(null,
                                                                        "name"));
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
            LOG.error("Cannot read DICOM Readers! " + e.getMessage());
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

    public List<ImageReaderParam> getForTransferSyntaxUID(String tsuid) {
        return mapTransferSyntaxUIDs.get(tsuid);
    }
    
    private boolean putForTransferSyntaxUID(String tsuid, ImageReaderParam param) {
        List<ImageReaderParam> readerSet = getForTransferSyntaxUID(tsuid);
        if (readerSet == null) {
            readerSet = new ArrayList<ImageReaderParam>();
            mapTransferSyntaxUIDs.put(tsuid, readerSet);
        }
        return readerSet.add(param);
    }
    
    public List<ImageReaderParam> getForMimeType(String mimeType) {
        return mapMimeTypes.get(mimeType);
    }

    public boolean putForMimeType(String mimeType, ImageReaderParam param) {
        List<ImageReaderParam> readerSet = getForMimeType(mimeType);
        if (readerSet == null) {
            readerSet = new ArrayList<ImageReaderParam>();
            mapMimeTypes.put(mimeType, readerSet);
        }
        return readerSet.add(param);
    }

    public static ImageReaderItem getImageReader(String tsuid) {
        List<ImageReaderParam> list = getDefault().getForTransferSyntaxUID(tsuid);        
        if (list != null) {
            synchronized (list) {
                for (Iterator<ImageReaderParam> it = list.iterator(); it.hasNext();) {
                    ImageReaderParam imageParam = it.next();
                    String cl = imageParam.getClassName();
                    Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName(imageParam.getFormatName());
                    while (iter.hasNext()) {
                        ImageReader reader = iter.next();
                        if (cl == null || reader.getClass().getName().equals(cl)) {
                            return new ImageReaderItem(reader, imageParam);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static ImageReaderItem getImageReaderByMimeType(String mimeType) {
        List<ImageReaderParam> list = getDefault().getForMimeType(mimeType);        
        if (list != null) {
            synchronized (list) {
                for (Iterator<ImageReaderParam> it = list.iterator(); it.hasNext();) {
                    ImageReaderParam imageParam = it.next();
                    String cl = imageParam.getClassName();
                    Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType(imageParam.getFormatName());
                    while (iter.hasNext()) {
                        ImageReader reader = iter.next();
                        if (cl == null || reader.getClass().getName().equals(cl)) {
                            return new ImageReaderItem(reader, imageParam);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static ImageReader getImageReaderForMimeType(String mimeType) {
        ImageReaderItem imageReader = getImageReaderByMimeType(mimeType);

        if (imageReader != null) {
            // configured mime type
            return imageReader.getImageReader();
        } else {
            // not configured mime type, fallback to first ImageIO reader for this mime type
            ImageReader reader = ImageIO.getImageReadersByMIMEType(mimeType).next();
            LOG.debug("Using Image Reader {}", reader.getClass());
            return reader;
        }
    }

    public static String getNativeSystemSpecification() {
        String val = System.getProperty("native.library.spec");

        if (val == null) {
            // Follows the OSGI specification for naming the operating system and processor architecture
            // http://www.osgi.org/Specifications/Reference
            String osName = System.getProperty("os.name"); 
            String osArch = System.getProperty("os.arch"); 
            if (osName != null && !osName.trim().equals("") && osArch != null && !osArch.trim().equals("")) {  
                if (osName.toLowerCase().startsWith("win")) { 
                    // All Windows versions are grouped under windows.
                    osName = "windows"; 
                } else if (osName.equals("Mac OS X")) { 
                    osName = "macosx"; 
                } else if (osName.equals("SymbianOS")) { 
                    osName = "epoc32"; 
                } else if (osName.equals("hp-ux")) { 
                    osName = "hpux"; 
                } else if (osName.equals("Mac OS")) { 
                    osName = "macos"; 
                } else if (osName.equals("OS/2")) { 
                    osName = "os2"; 
                } else if (osName.equals("procnto")) { 
                    osName = "qnx"; 
                } else {
                    osName = osName.toLowerCase();
                }

                if (osArch.equals("pentium") || osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586")
                    || osArch.equals("i686")) { 
                    osArch = "x86"; 
                } else if (osArch.equals("amd64") || osArch.equals("em64t") || osArch.equals("x86_64")) {
                    osArch = "x86-64"; 
                } else if (osArch.equals("power ppc")) { 
                    osArch = "powerpc"; 
                } else if (osArch.equals("psc1k")) { 
                    osArch = "ignite"; 
                } else {
                    osArch = osArch.toLowerCase();
                }
                val = osName + "-" + osArch;
                System.setProperty("native.library.spec", val);  
            }
        }
        return val;
    }
}
