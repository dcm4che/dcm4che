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

package org.dcm4che3.hl7;

import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.ArrayList;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Message extends ArrayList<HL7Segment> {

    private static final long serialVersionUID = 5000743858306540891L;

    public HL7Message() {
    }

    public HL7Message(int initialCapacity) {
        super(initialCapacity);
    }

    public HL7Segment getSegment(String name) {
        for (HL7Segment seg : this)
            if (name.equals(seg.getField(0, null)))
                return seg;
        return null;
    }

    @Override
    public String toString() {
        return toString('\r');
    }

    public String toString(char segdelim) {
        int len = size();
        for (HL7Segment seg : this) {
            int segSize = seg.size();
            len += segSize - 1;
            for (int i = 0; i < segSize; i++) {
                String s = seg.getField(i, null);
                if (s != null)
                    len += s.length();
            }
        }
        char[] cs = new char[len];
        int off = 0;
        for (HL7Segment seg : this) {
            char delim = seg.getFieldSeparator();
            int segSize = seg.size();
            for (int i = 0; i < segSize; i++) {
                String s = seg.getField(i, null);
                if (s != null) {
                    int l = s.length();
                    s.getChars(0, l, cs, off);
                    off += l;
                }
                cs[off++] = delim;
            }
            cs[off-1] = segdelim;
        }
        return new String(cs);
    }

    public byte[] getBytes(String defCharset) {
        try {
            return toString().getBytes(HL7Charset.toCharsetName(get(0).getField(17, defCharset)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static HL7Message parse(byte[] b, String defCharset) {
        return parse(b, b.length, defCharset);
    }

    public static HL7Message parse(byte[] b, int size, String defCharset) {
        ParsePosition pos = new ParsePosition(0);
        HL7Message msg = new HL7Message();
        HL7Segment seg = HL7Segment.parseMSH(b, size, pos);
        char fieldSeparator = seg.getFieldSeparator();
        String encodingCharacters = seg.getEncodingCharacters();
        String charsetName = HL7Charset.toCharsetName(seg.getField(17, defCharset));
        msg.add(seg);
        while ((seg = HL7Segment.parse(
                b, size, pos, fieldSeparator, encodingCharacters, charsetName)) != null)
            msg.add(seg);
        msg.trimToSize();
        return msg;
    }

    public static HL7Message makeACK(HL7Segment msh, String ackCode, String text) {
        int size = msh.size();
        HL7Segment ackmsh = HL7Segment.makeMSH(size, msh.getFieldSeparator(),
                msh.getEncodingCharacters());
        ackmsh.setField(2, msh.getField(4, null));
        ackmsh.setField(3, msh.getField(5, null));
        ackmsh.setField(4, msh.getField(2, null));
        ackmsh.setField(5, msh.getField(3, null));
        ackmsh.setField(8, "ACK");
        for (int i = 10; i < size; i++)
            ackmsh.setField(i, msh.getField(i, null));
        HL7Segment msa = new HL7Segment(4, msh.getFieldSeparator(),
                msh.getEncodingCharacters());
        msa.setField(0, "MSA");
        msa.setField(1, ackCode);
        msa.setField(2, msh.getField(9, null));
        msa.setField(3, text != null && text.length() > 80 ? text.substring(0, 80) : text);
        HL7Message ack = new HL7Message(2);
        ack.add(ackmsh);
        ack.add(msa);
        return ack;
    }

    public static HL7Message makePixQuery(String pid, String... domains) {
        HL7Segment msh = HL7Segment.makeMSH();
        msh.setField(8, "QBP^Q23^QBP_Q21");
        HL7Segment qpd = new HL7Segment(5);
        qpd.setField(0, "QPD");
        qpd.setField(1, "IHE PIX Query");
        qpd.setField(2, "QRY" + msh.getField(9, ""));
        qpd.setField(3, pid);
        qpd.setField(4, HL7Segment.concat(domains, '~'));
        HL7Segment rcp = new HL7Segment(8);
        rcp.setField(0, "RCP");
        rcp.setField(1, "I");
        HL7Message qbp = new HL7Message(3);
        qbp.add(msh);
        qbp.add(qpd);
        qbp.add(rcp);
        return qbp;
    }
}
