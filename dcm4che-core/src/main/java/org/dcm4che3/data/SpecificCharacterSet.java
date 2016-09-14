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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che3.data;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SpecificCharacterSet {
    
    public static final SpecificCharacterSet DICOM_DEFAULT =
            new SpecificCharacterSet(new Codec[]{Codec.ISO_646}, new String[] {null});
    
    public static SpecificCharacterSet DEFAULT = DICOM_DEFAULT;

    private static ThreadLocal<SoftReference<Encoder>> cachedEncoder1 = 
            new ThreadLocal<SoftReference<Encoder>>();

    private static ThreadLocal<SoftReference<Encoder>> cachedEncoder2 = 
            new ThreadLocal<SoftReference<Encoder>>();

    protected final Codec[] codecs;
    protected final String[] dicomCodes;

    private enum Codec {
        ISO_646("US-ASCII", 0x2842, 0),
        ISO_8859_1("ISO-8859-1", 0x2842, 0x2d41),
        ISO_8859_2("ISO-8859-2", 0x2842, 0x2d42),
        ISO_8859_3("ISO-8859-3", 0x2842, 0x2d43),
        ISO_8859_4("ISO-8859-4", 0x2842, 0x2d44),
        ISO_8859_5("ISO-8859-5", 0x2842, 0x2d4c),
        ISO_8859_6("ISO-8859-6", 0x2842, 0x2d47),
        ISO_8859_7("ISO-8859-7", 0x2842, 0x2d46),
        ISO_8859_8("ISO-8859-8", 0x2842, 0x2d48),
        ISO_8859_9("ISO-8859-9", 0x2842, 0x2d4d),
        JIS_X_201("JIS_X0201", 0x284a, 0x2949),
        TIS_620("TIS-620", 0x2842, 0x2d54),
        JIS_X_208("x-JIS0208", -1, 0x2442),
        JIS_X_212("JIS_X0212-1990", -1, 0x242844),
        KS_X_1001("EUC-KR", 0, 0x242943),
        GB2312("GB2312", 0x2842, 0x242941),
        UTF_8("UTF-8", 0, 0),
        GB18030("GB18030", 0, 0);

        private final String charsetName;
        private final int escSeq0;
        private final int escSeq1;

        private Codec(String charsetName, int escSeq0, int escSeq1) {
            this.charsetName = charsetName;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
        }

        public static Codec forCode(String code) {
            if (code == null)
                return ISO_646;

            switch(last2digits(code)) {
            case 0:
                if (code.equals("ISO_IR 100") || code.equals("ISO 2022 IR 100"))
                    return Codec.ISO_8859_1;
                break;
            case 1:
                if (code.equals("ISO_IR 101") || code.equals("ISO 2022 IR 101"))
                    return Codec.ISO_8859_2;
                break;
            case 6:
                if (code.equals("ISO 2022 IR 6"))
                    return Codec.ISO_646;
                break;
            case 9:
                if (code.equals("ISO_IR 109") || code.equals("ISO 2022 IR 109"))
                    return Codec.ISO_8859_3;
                break;
            case 10:
                if (code.equals("ISO_IR 110") || code.equals("ISO 2022 IR 110"))
                    return Codec.ISO_8859_4;
                break;
            case 13:
                if (code.equals("ISO_IR 13") || code.equals("ISO 2022 IR 13"))
                    return Codec.JIS_X_201;
                break;
            case 26:
                if (code.equals("ISO_IR 126") || code.equals("ISO 2022 IR 126"))
                    return Codec.ISO_8859_7;
                break;
            case 27:
                if (code.equals("ISO_IR 127") || code.equals("ISO 2022 IR 127"))
                    return Codec.ISO_8859_6;
                break;
            case 30:
                if (code.equals("GB18030"))
                    return Codec.GB18030;
                break;
            case 31:
                if (code.equals("GBK"))
                    return Codec.GB18030;
                break;
            case 38:
                if (code.equals("ISO_IR 138") || code.equals("ISO 2022 IR 138"))
                    return Codec.ISO_8859_8;
                break;
            case 44:
                if (code.equals("ISO_IR 144") || code.equals("ISO 2022 IR 144"))
                    return Codec.ISO_8859_5;
                break;
            case 48:
                if (code.equals("ISO_IR 148") || code.equals("ISO 2022 IR 148"))
                    return Codec.ISO_8859_9;
                break;
            case 49:
                if (code.equals("ISO 2022 IR 149"))
                    return Codec.KS_X_1001;
                break;
            case 58:
                if (code.equals("ISO 2022 IR 58"))
                    return Codec.GB2312;
                break;
            case 59:
                if (code.equals("ISO 2022 IR 159"))
                    return Codec.JIS_X_212;
                break;
            case 66:
                if (code.equals("ISO_IR 166") || code.equals("ISO 2022 IR 166"))
                    return Codec.TIS_620;
                break;
            case 87:
                if (code.equals("ISO 2022 IR 87"))
                    return Codec.JIS_X_208;
                break;
            case 92:
                if (code.equals("ISO_IR 192"))
                    return Codec.UTF_8;
                break;
            }
            return ISO_646;
        }

        private static int last2digits(String code) {
            int len = code.length();
            if (len < 2)
                return -1;
            char ch1 = code.charAt(len-1);
            char ch2 = code.charAt(len-2);
            return (ch2 & 15) * 10 + (ch1 & 15);
        }

        public byte[] encode(String val) {
            try {
                return val.getBytes(charsetName);
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        public String decode(byte[] b, int off, int len) {
            try {
                return new String(b, off, len, charsetName);
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        public boolean containsASCII() {
            return escSeq0 >= 0;
        }

        public int getEscSeq0() {
            return escSeq0;
        }

        public int getEscSeq1() {
            return escSeq1;
        }
    }

    private static final class Encoder {
        final Codec codec;
        final CharsetEncoder encoder;
 
        public Encoder(Codec codec) {
            this.codec = codec;
            this.encoder = Charset.forName(codec.charsetName).newEncoder();
        }

        public boolean encode(CharBuffer cb, ByteBuffer bb, boolean escSeq,
                CodingErrorAction errorAction) {
            encoder.onMalformedInput(errorAction)
                    .onUnmappableCharacter(errorAction)
                    .reset();
            int cbmark = cb.position();
            int bbmark = bb.position();
            try {
                if (escSeq)
                    escSeq(bb, codec.getEscSeq1());
                CoderResult cr = encoder.encode(cb, bb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = encoder.flush(bb);
                if (!cr.isUnderflow())
                    cr.throwException();
            } catch (CharacterCodingException x) {
                cb.position(cbmark);
                bb.position(bbmark);
                return false;
            }
            return true;
        }

        private static void escSeq(ByteBuffer bb, int seq) {
            bb.put((byte) 0x1b);
            int b1 = seq >> 16;
            if (b1 != 0)
                bb.put((byte) b1);
            bb.put((byte) (seq >> 8));
            bb.put((byte) seq);
        }
    }

    private static final class ISO2022 extends SpecificCharacterSet {

        private ISO2022(Codec[] charsetInfos, String... codes) {
            super(charsetInfos, codes);
        }

        @Override
        public byte[] encode(String val, String delimiters) {
            int strlen = val.length();
            CharBuffer cb = CharBuffer.wrap(val.toCharArray());
            Encoder enc1 = encoder(cachedEncoder1, codecs[0]);
            byte[] buf = new byte[strlen];
            ByteBuffer bb = ByteBuffer.wrap(buf);
            // try to encode whole string value with character set specified
            // by value1 of (0008,0005) Specific Character Set
            if (!enc1.encode(cb, bb, false, CodingErrorAction.REPORT)) {
                // split whole string value according VR specific delimiters
                // and try to encode each component separately
                Encoder[] encs = new Encoder[codecs.length];
                encs[0] = enc1;
                encs[1] = encoder(cachedEncoder2, codecs[1]);
                StringTokenizer comps =
                        new StringTokenizer(val, delimiters, true);
                buf = new byte[2 * strlen + 4 * (comps.countTokens() + 1)];
                bb = ByteBuffer.wrap(buf);
                int cur = 0;
                while (comps.hasMoreTokens()) {
                    String comp = comps.nextToken();
                    if (comp.length() == 1 // if delimiter
                            && delimiters.indexOf(comp.charAt(0)) >= 0) {
                        // switch to initial character set, if current active
                        // character set does not contain ASCII
                        if (!codecs[cur].containsASCII())
                            Encoder.escSeq(bb, codecs[0].getEscSeq0());
                        bb.put((byte) comp.charAt(0));
                        cur = 0;
                        continue;
                    }
                    cb = CharBuffer.wrap(comp.toCharArray());
                    // try to encode component with current active character set
                    if (encs[cur].encode(cb, bb, false,
                            CodingErrorAction.REPORT))
                        continue;
                    int next = cur;
                    // try to encode component with other character sets
                    // specified by values of (0008,0005) Specific Character Set
                    do {
                        next = (next + 1) % encs.length;
                        if (next == cur) {
                            // component could not be encoded with any of the
                            // specified character sets, encode it with the
                            // initial character set, using the default
                            // replacement of the character set decoder
                            // for characters which cannot be encoded
                            if (!codecs[cur].containsASCII())
                                Encoder.escSeq(bb, codecs[0].getEscSeq0());
                            encs[0].encode(cb, bb, false,
                                    CodingErrorAction.REPLACE);
                            next = 0;
                            break;
                        }
                        if (encs[next] == null)
                            encs[next] = new Encoder(codecs[next]);
                    } while (!encs[next].encode(cb, bb, true, 
                            CodingErrorAction.REPORT));
                    cur = next;
                }
                if (!codecs[cur].containsASCII())
                    Encoder.escSeq(bb, codecs[0].getEscSeq0());
            }
            return Arrays.copyOf(buf, bb.position());
        }

        @Override
        public String decode(byte[] b) {
            Codec codec = codecs[0];
            int off = 0;
            int cur = 0;
            int step = 1;
            StringBuffer sb = new StringBuffer(b.length);
            while (cur < b.length) {
                if (b[cur] == 0x1b) { // ESC
                    if (off < cur) {
                        sb.append(codec.decode(b, off, cur - off));
                    }
                    cur += 3;
                    switch (((b[cur - 2] & 255) << 8) + (b[cur - 1] & 255)) {
                    case 0x2428:
                        if (b[cur++] == 0x44) {
                            codec = Codec.JIS_X_212;
                            step = 2;
                        } else { // decode invalid ESC sequence as chars
                            sb.append(codec.decode(b, cur - 4, 4));
                        }
                        break;
                    case 0x2429:
                        switch (b[cur++]) {
                            case 0x41:
                                codec = Codec.GB2312;
                                step = -1;
                                break;
                            case 0x43:
                                codec = Codec.KS_X_1001;
                                step = -1;
                                break;
                            default: // decode invalid ESC sequence as chars
                                sb.append(codec.decode(b, cur - 4, 4));
                        }
                        break;
                    case 0x2442:
                        codec = Codec.JIS_X_208;
                        step = 2;
                        break;
                    case 0x2842:
                        codec = Codec.ISO_646;
                        step = 1;
                        break;
                    case 0x284a:
                    case 0x2949:
                        codec = Codec.JIS_X_201;
                        step = 1;
                        break;
                    case 0x2d41:
                        codec = Codec.ISO_8859_1;
                        step = 1;
                        break;
                    case 0x2d42:
                        codec = Codec.ISO_8859_2;
                        step = 1;
                        break;
                    case 0x2d43:
                        codec = Codec.ISO_8859_3;
                        step = 1;
                        break;
                    case 0x2d44:
                        codec = Codec.ISO_8859_4;
                        step = 1;
                        break;
                    case 0x2d46:
                        codec = Codec.ISO_8859_7;
                        step = 1;
                        break;
                    case 0x2d47:
                        codec = Codec.ISO_8859_6;
                        step = 1;
                        break;
                    case 0x2d48:
                        codec = Codec.ISO_8859_8;
                        step = 1;
                        break;
                    case 0x2d4c:
                        codec = Codec.ISO_8859_5;
                        step = 1;
                        break;
                    case 0x2d4d:
                        codec = Codec.ISO_8859_9;
                        step = 1;
                        break;
                    case 0x2d54:
                        codec = Codec.TIS_620;
                        step = 1;
                        break;
                    default: // decode invalid ESC sequence as chars
                        sb.append(codec.decode(b, cur - 3, 3));
                    }
                    off = cur;
                } else {
                    cur += step > 0 ? step : b[cur] < 0 ? 2 : 1;
                }
            }
            if (off < cur) {
                sb.append(codec.decode(b, off, cur - off));
            }
            return sb.toString();
        }
    }

    public static final void setDefaultCharacterSet(String characterSet) {
        DEFAULT = characterSet == null ? DICOM_DEFAULT : valueOf(characterSet);
    }

    public static SpecificCharacterSet valueOf(String... codes) {
        if (codes == null || codes.length == 0)
            return DICOM_DEFAULT;

        Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; i++)
            infos[i] = Codec.forCode(codes[i]);

        if (codes.length == 1 && infos[0] == Codec.ISO_646 && DEFAULT.containsASCII())
            return new SpecificCharacterSet(DEFAULT.codecs, codes);

        return codes.length > 1 ? new ISO2022(infos,codes)
                : new SpecificCharacterSet(infos, codes);
    }
    
    public String[] toCodes () {
        
        return dicomCodes;
    }

    private static Encoder encoder(ThreadLocal<SoftReference<Encoder>> tl,
            Codec codec) {
        SoftReference<Encoder> sr;
        Encoder enc;
        if ((sr = tl.get()) == null || (enc = sr.get()) == null
                || enc.codec != codec)
            tl.set(new SoftReference<Encoder>(enc = new Encoder(codec)));
        return enc;
    }

    protected SpecificCharacterSet(Codec[] codecs, String... codes) {
        this.codecs = codecs;
        this.dicomCodes = codes;
    }

    public byte[] encode(String val, String delimiters) {
        return codecs[0].encode(val);
    }

    public String decode(byte[] val) {
        return codecs[0].decode(val, 0, val.length);
    }

    public boolean isUTF8() {
        return codecs[0].equals(Codec.UTF_8);
    }
    
    public boolean isASCII() {
        return codecs[0].equals(Codec.ISO_646);
    }

    public boolean containsASCII() {
        return codecs[0].containsASCII();
    }
    
    @Override public boolean equals(Object other) {
        
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final SpecificCharacterSet othercs = (SpecificCharacterSet) other;
        return Arrays.equals(this.codecs,othercs.codecs);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.codecs);
    }

}
