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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.util.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class IOD extends ArrayList<IOD.DataElement> {

    private static final long serialVersionUID = -5065822488885801576L;

    public enum DataElementType {
        TYPE_0, TYPE_1, TYPE_1C, TYPE_2, TYPE_2C, TYPE_3
    }

    public static class DataElement implements Serializable {

        private static final long serialVersionUID = -7460474415381086525L;

        public final int tag;
        public final VR vr;
        public final DataElementType type;
        public final int minVM;
        public final int maxVM;
        public final boolean singleItem;
        private Object values;

        public DataElement(int tag, VR vr, DataElementType type,
                int minVM, int maxVM, boolean singleItem) {
            this.tag = tag;
            this.vr = vr;
            this.type = type;
            this.minVM = minVM;
            this.maxVM = maxVM;
            this.singleItem = singleItem;
        }

        public DataElement setValues(String[][] values) {
            this.values = values;
            return this;
        }

        public DataElement setValues(String... values) {
            this.values = values;
            return this;
        }

        public DataElement setValues(int[][] values) {
            this.values = values;
            return this;
        }

        public DataElement setValues(int... values) {
            this.values = values;
            return this;
        }

        public DataElement setItemIOD(IOD iod) {
            this.values = iod;
            return this;
        }

        public Object getValues() {
            return values;
        }
   }

    @Override
    public void trimToSize() {
        super.trimToSize();
        for (DataElement el : this) {
            if (el.values instanceof IOD)
                ((IOD) el.values).trimToSize();
        }
    }

    public void parse(String uri) throws IOException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser parser = f.newSAXParser();
            parser.parse(uri, new SAXHandler(this));
        } catch (SAXException e) {
            throw new IOException("Failed to parse " + uri, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SAXHandler extends DefaultHandler {

        private DataElement el;
        private StringBuilder sb = new StringBuilder();
        private boolean processCharacters;
        private String valueNumber;
        private Map<String, List<String>> values = 
                new HashMap<String, List<String>>();
        private LinkedList<IOD> iods = new LinkedList<IOD>();
        private Map<String, IOD> refs = new HashMap<String, IOD>();

        public SAXHandler(IOD iod) {
            iods.add(iod);
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                org.xml.sax.Attributes atts) throws SAXException {
            switch (qName.charAt(0)) {
            case 'D':
                if (qName.equals("DataElement"))
                    startDataElement(
                            atts.getValue("tag"),
                            atts.getValue("vr"),
                            atts.getValue("type"),
                            atts.getValue("vm"),
                            atts.getValue("items"));
                break;
            case 'I':
                if (qName.equals("Item"))
                    startItem(atts.getValue("id"), atts.getValue("idref"));
                break;
            case 'V':
                if (qName.equals("Value"))
                    startValue(atts.getValue("number"));
                break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            switch (qName.charAt(0)) {
            case 'D':
                if (qName.equals("DataElement"))
                    endDataElement();
                break;
            case 'I':
                if (qName.equals("Item"))
                    endItem();
                break;
            case 'V':
                if (qName.equals("Value"))
                    endValue();
                break;
            }
            processCharacters = false;
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if (processCharacters)
                sb.append(ch, start, length);
        }

        private void startDataElement(String tagStr, String vrStr,
                String typeStr, String vm, String items) throws SAXException {
            IOD iod = iods.getLast();
            if (iod == null)
                throw new SAXException("Item with idref must be empty");
            int tag = (int) Long.parseLong(tagStr, 16);
            VR vr = VR.valueOf(vrStr);
            DataElementType type = DataElementType.valueOf("TYPE_" + typeStr);
            int minVM = -1;
            int maxVM = -1;
            if (vm != null) {
                String[] ss = StringUtils.split(vm, '-');
                if (ss[0].charAt(0) != 'n') {
                    minVM = Integer.parseInt(ss[0]);
                    if (ss.length > 1) {
                        if (ss[1].charAt(0) != 'n')
                            maxVM = Integer.parseInt(ss[1]);
                    } else {
                        maxVM = minVM;
                    }
                }
            }
            boolean singleItem = items != null && items.endsWith("1");
            el = new DataElement(tag, vr, type, minVM, maxVM, singleItem);
            iod.add(el);
        }

        private void endDataElement() {
            if (values.isEmpty())
                return;

            List<String> list = values.get(null);
            if (list != null) {
                if (el.vr.isIntType())
                    el.setValues(parseInts(list));
                else
                    el.setValues(list.toArray(new String[list.size()]));
            } else {
                int size = Integer.parseInt(Collections.max(values.keySet()));
                if (el.vr.isIntType())
                    setValues(new int[size][]);
                else
                    setValues(new String[size][]);
            }
            values.clear();
        }

        private int[] parseInts(List<String> list) {
            int[] is = new int[list.size()];
            for (int i = 0; i < is.length; i++)
                is[i] = Integer.parseInt(list.get(i));
            return is;
        }

        private void setValues(int[][] dest) {
            for (Entry<String, List<String>> entry : values.entrySet()) {
                int i = Integer.parseInt(entry.getKey()) - 1;
                List<String> list = entry.getValue();
                dest[i] = parseInts(list);
            }
            el.setValues(dest);
        }

        private void setValues(String[][] dest) {
            for (Entry<String, List<String>> entry : values.entrySet()) {
                int i = Integer.parseInt(entry.getKey()) - 1;
                List<String> list = entry.getValue();
                dest[i] = list.toArray(new String[list.size()]);
            }
            el.setValues(dest);
        }

        private void startValue(String number) {
            this.valueNumber = number;
            sb.setLength(0);
            processCharacters = true;
        }

        private void endValue() {
            List<String> list = values.get(valueNumber);
            if (list == null) {
                list = new ArrayList<String>();
                values.put(valueNumber, list);
            }
            list.add(sb.toString());
        }

        private void startItem(String id, String idref) throws SAXException {
            IOD iod;
            if (idref != null) {
                iod = refs.get(idref);
                if (iod == null)
                    throw new SAXException("undefined idref:" + idref);
                iods.add(null);
            } else { 
                iod = new IOD();
                iods.add(iod);
            }
            el.setItemIOD(iod);
            if (id != null)
                refs.put(id, iod);
        }

        private void endItem() {
            iods.removeLast();
        }

    }

    public static IOD load(String uri) throws IOException {
        if (uri.startsWith("resource:")) {
            try {
                uri = Thread.currentThread().getContextClassLoader()
                        .getResource(uri.substring(9)).toString();
            } catch (NullPointerException npe) {
                throw new FileNotFoundException(uri);
            }
        } else if (uri.indexOf(':') < 2) {
            uri = new File(uri).toURI().toString();
        }
        IOD iod = new IOD();
        iod.parse(uri);
        iod.trimToSize();
        return iod;
    }

}
