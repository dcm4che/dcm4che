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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ImageReaderFactory {

    public static class ImageReaderParam {
        public final String formatName;
        public final String className;
        public final String colorPMI;

        public ImageReaderParam(String formatName, String className,
                String colorPMI) {
            this.formatName = formatName;
            this.className = className;
            this.colorPMI = colorPMI;
        }
    }

    private static ImageReaderFactory defaultFactory;
    private final HashMap<String, ImageReaderParam> map = 
            new HashMap<String, ImageReaderParam>();

    public static ImageReaderFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = initDefault();

        return defaultFactory;
    }

    public static void setDefault(ImageReaderFactory factory) {
        if (factory == null)
            throw new NullPointerException();

        defaultFactory = factory;
    }

    private static ImageReaderFactory initDefault() {
        ImageReaderFactory factory = new ImageReaderFactory();
        InputStream in = ImageReaderFactory.class
                .getResourceAsStream("ImageReaderFactory.properties");
        try {
            factory.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            SafeClose.close(in);
        }
        return factory ;
    }

    public void load(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String[] ss = StringUtils.split((String) entry.getValue(), ':');
            map.put((String) entry.getKey(),
                    new ImageReaderParam(ss[0], ss[1], ss[2]));
        }
    }

    public ImageReaderParam get(String tsuid) {
        return map.get(tsuid);
    }

    public ImageReaderParam put(String tsuid,
            ImageReaderParam param) {
        return map.put(tsuid, param);
    }

    public ImageReaderParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    public void clear() {
        map.clear();
    }

    public static ImageReaderParam getImageReaderParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    public static ImageReader getImageReader(ImageReaderParam param) {
        Iterator<ImageReader> iter =
                ImageIO.getImageReadersByFormatName(param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Image Reader for format: "
                    + param.formatName + " registered");

        String className = param.className;
        if (className == null || className.isEmpty() || className.equals("*"))
            return iter.next();

        do {
            ImageReader reader = iter.next();
            if (reader.getClass().getName().equals(className))
                return reader;
        } while (iter.hasNext());

        throw new RuntimeException("Image Reader: " +className
                + " not registered");
    }

}
