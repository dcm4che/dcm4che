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

package org.dcm4che3.hl7;

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7ContentHandler extends DefaultHandler {

    private final Writer writer;
    private char[] delimiters = Delimiter.DEFAULT.toCharArray();
    private final char[] escape = { '\\', 0, '\\' };
    private boolean ignoreCharacters = true;

    public HL7ContentHandler(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        try {
            switch (qName.charAt(0)) {
            case 'f':
                if (qName.equals("field")) {
                    writer.write(delimiters[0]);
                    ignoreCharacters = false;
                    return;
                }
                break;
            case 'c':
                if (qName.equals("component")) {
                    writer.write(delimiters[1]);
                    ignoreCharacters = false;
                    return;
                }
                break;
            case 'r':
                if (qName.equals("repeat")) {
                    writer.write(delimiters[2]);
                    ignoreCharacters = false;
                    return;
                }
                break;
            case 'e':
                if (qName.equals("escape")) {
                    writer.write(delimiters[3]);
                    ignoreCharacters = false;
                    return;
                }
                break;
            case 's':
                if (qName.equals("subcomponent")) {
                    writer.write(delimiters[4]);
                    ignoreCharacters = false;
                    return;
                }
                break;
            case 'M':
                if (qName.equals("MSH")) {
                    startHeaderSegment(qName, atts);
                    return;
                }
                break;
            case 'B':
                if (qName.equals("BHS")) {
                    startHeaderSegment(qName, atts);
                    return;
                }
                break;
            case 'F':
                if (qName.equals("FHS")) {
                    startHeaderSegment(qName, atts);
                    return;
                }
                break;
            case 'h':
                if (qName.equals("hl7"))
                    return;
            }
            writer.write(qName);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    private void startHeaderSegment(String seg, Attributes atts) throws IOException {
        Delimiter[] values = Delimiter.values();
        for (int i = 0; i < values.length; i++) {
            String value = atts.getValue(values[i].attribute());
            if (value != null)
                delimiters[i] = value.charAt(0);
        }
        this.escape[0] = this.escape[2] = delimiters[3];
        writer.write(seg);
        writer.write(delimiters);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        ignoreCharacters = true;
        try {
            switch (qName.charAt(0)) {
            case 'f':
                if (qName.equals("field")) return;
                break;
            case 'c':
                if (qName.equals("component")) return;
                break;
            case 'r':
                if (qName.equals("repeat")) return;
                break;
            case 'e':
                if (qName.equals("escape")) {
                    writer.write(delimiters[3]);
                    ignoreCharacters = false;
                    return;
                }
                break;
            case 's':
                if (qName.equals("subcomponent")) return;
                break;
            case 'h':
                if (qName.equals("hl7")) {
                    writer.flush();
                    return;
                }
            }
            writer.write('\r');
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] cbuf, int start, int length)
            throws SAXException {
        if (ignoreCharacters)
            return;

        try {
            int off = start;
            int end = start + length;
            char c;
            char[] delims = delimiters;
            for (int i = start; i < end; i++) {
                c = cbuf[i];
                for (int j = 0; j < delims.length; j++) {
                    if (c == delims[j]) {
                        writer.write(cbuf, off, i - off);
                        off = i + 1;
                        escape(j);
                        break;
                    }
                }
            }
            writer.write(cbuf, off, end - off);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    private void escape(int delimIndex) throws IOException {
        escape[1] = Delimiter.ESCAPE.charAt(delimIndex);
        writer.write(escape);
    }
}
