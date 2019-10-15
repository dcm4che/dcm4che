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
 * Java(TM), hosted at https://github.com/dcm4che.
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

import org.dcm4che3.util.SafeBuffer;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SpecificCharacterSet {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificCharacterSet.class);

    public static final SpecificCharacterSet ASCII = new SpecificCharacterSet(new Codec[]{Codec.ISO_646});

    private static SpecificCharacterSet DEFAULT = ASCII;
    private static ThreadLocal<SoftReference<Encoder>> cachedEncoder1 = new ThreadLocal<SoftReference<Encoder>>();
    private static ThreadLocal<SoftReference<Encoder>> cachedEncoder2 = new ThreadLocal<SoftReference<Encoder>>();

    protected final Codec[] codecs;
    protected final String[] dicomCodes;

    private enum Codec {
        ISO_646("US-ASCII", true, 0x2842, 0, 1),
        ISO_8859_1("ISO-8859-1", true, 0x2842, 0x2d41, 1),
        ISO_8859_2("ISO-8859-2", true, 0x2842, 0x2d42, 1),
        ISO_8859_3("ISO-8859-3", true, 0x2842, 0x2d43, 1),
        ISO_8859_4("ISO-8859-4", true, 0x2842, 0x2d44, 1),
        ISO_8859_5("ISO-8859-5", true, 0x2842, 0x2d4c, 1),
        ISO_8859_6("ISO-8859-6", true, 0x2842, 0x2d47, 1),
        ISO_8859_7("ISO-8859-7", true, 0x2842, 0x2d46, 1),
        ISO_8859_8("ISO-8859-8", true, 0x2842, 0x2d48, 1),
        ISO_8859_9("ISO-8859-9", true, 0x2842, 0x2d4d, 1),
        JIS_X_201("JIS_X0201", true, 0x284a, 0x2949, 1) {
            @Override
            public String toText(String s) {
                return s.replace('\\', '¥');
            }
        },
        TIS_620("TIS-620", true, 0x2842, 0x2d54, 1),
        JIS_X_208("x-JIS0208", false, 0x2442, 0, 1),
        JIS_X_212("JIS_X0212-1990", false, 0x242844, 0, 2),
        KS_X_1001("EUC-KR", false, 0x2842, 0x242943, -1),
        GB2312("GB2312", false, 0x2842, 0x242941, -1),
        UTF_8("UTF-8", true, 0, 0, -1),
        GB18030("GB18030", false, 0, 0, -1);

        private final String charsetName;
        private final boolean containsASCII;
        private final int escSeq0;
        private final int escSeq1;
        private final int bytesPerChar;

        Codec(String charsetName, boolean containsASCII, int escSeq0, int escSeq1, int bytesPerChar) {
            this.charsetName = charsetName;
            this.containsASCII = containsASCII;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
            this.bytesPerChar = bytesPerChar;
        }

        public static Codec forCode(String code) {
            if (code == null)
                return SpecificCharacterSet.DEFAULT.codecs[0];

            switch(code) {
                case "ISO 2022 IR 6":
                    return SpecificCharacterSet.DEFAULT.codecs[0];
                case "ISO_IR 100":
                case "ISO 2022 IR 100":
                    return Codec.ISO_8859_1;
                case "ISO_IR 101":
                case "ISO 2022 IR 101":
                    return Codec.ISO_8859_2;
                case "ISO_IR 109":
                case "ISO 2022 IR 109":
                    return Codec.ISO_8859_3;
                case "ISO_IR 110":
                case "ISO 2022 IR 110":
                    return Codec.ISO_8859_4;
                case "ISO_IR 144":
                case "ISO 2022 IR 144":
                    return Codec.ISO_8859_5;
                case "ISO_IR 127":
                case "ISO 2022 IR 127":
                    return Codec.ISO_8859_6;
                case "ISO_IR 126":
                case "ISO 2022 IR 126":
                    return Codec.ISO_8859_7;
                case "ISO_IR 138":
                case "ISO 2022 IR 138":
                    return Codec.ISO_8859_8;
                case "ISO_IR 148":
                case "ISO 2022 IR 148":
                    return Codec.ISO_8859_9;
                case "ISO_IR 13":
                case "ISO 2022 IR 13":
                    return Codec.JIS_X_201;
                case "ISO_IR 166":
                case "ISO 2022 IR 166":
                    return Codec.TIS_620;
                case "ISO 2022 IR 87":
                    return Codec.JIS_X_208;
                case "ISO 2022 IR 159":
                    return Codec.JIS_X_212;
                case "ISO 2022 IR 149":
                    return Codec.KS_X_1001;
                case "ISO 2022 IR 58":
                    return Codec.GB2312;
                case "ISO_IR 192":
                    return Codec.UTF_8;
                case "GB18030":
                case "GBK":
                    return Codec.GB18030;
            }
            return SpecificCharacterSet.DEFAULT.codecs[0];
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
            return containsASCII;
        }

        public int getEscSeq0() {
            return escSeq0;
        }

        public int getEscSeq1() {
            return escSeq1;
        }

        public int getBytesPerChar() {
            return bytesPerChar;
        }

        public String toText(String s) {
            return s;
        }
    }

    private static final class Encoder {
        final Codec codec;
        final CharsetEncoder encoder;
 
        public Encoder(Codec codec) {
            this.codec = codec;
            this.encoder = Charset.forName(codec.charsetName).newEncoder();
        }

        public boolean encode(CharBuffer cb, ByteBuffer bb, int escSeq,
                CodingErrorAction errorAction) {
            encoder.onMalformedInput(errorAction)
                    .onUnmappableCharacter(errorAction)
                    .reset();
            int cbmark = cb.position();
            int bbmark = bb.position();
            try {
                escSeq(bb, escSeq);
                CoderResult cr = encoder.encode(cb, bb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = encoder.flush(bb);
                if (!cr.isUnderflow())
                    cr.throwException();
            } catch (CharacterCodingException x) {
                SafeBuffer.position(cb, cbmark);
                SafeBuffer.position(bb, bbmark);
                return false;
            }
            return true;
        }

        private static void escSeq(ByteBuffer bb, int seq) {
            if (seq == 0)
                return;

            bb.put((byte) 0x1b);
            int b1 = seq >> 16;
            if (b1 != 0)
                bb.put((byte) b1);
            bb.put((byte) (seq >> 8));
            bb.put((byte) seq);
        }

        public byte[] replacement() {
            return encoder.replacement();
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
            if (!enc1.encode(cb, bb, 0, CodingErrorAction.REPORT)) {
                // split whole string value according VR specific delimiters
                // and try to encode each component separately
                Encoder[] encs = new Encoder[codecs.length];
                encs[0] = enc1;
                encs[1] = encoder(cachedEncoder2, codecs[1]);
                StringTokenizer comps = new StringTokenizer(val, delimiters, true);
                buf = new byte[2 * strlen + 4 * (comps.countTokens() + 1)];
                bb = ByteBuffer.wrap(buf);
                int[] cur = { 0, 0 };
                while (comps.hasMoreTokens()) {
                    String comp = comps.nextToken();
                    if (comp.length() == 1 && delimiters.indexOf(comp.charAt(0)) >= 0) { // if delimiter
                        activateInitialCharacterSet(bb, cur);
                        bb.put((byte) comp.charAt(0));
                        continue;
                    }
                    cb = CharBuffer.wrap(comp.toCharArray());
                    encodeComponent(encs, cb, bb, cur);
                }
                activateInitialCharacterSet(bb, cur);
            }
            return Arrays.copyOf(buf, bb.position());
        }

        private void encodeComponent(Encoder[] encs, CharBuffer cb, ByteBuffer bb, int[] cur) {
            // try to encode component with current active character of G1
            if (codecs[cur[1]].getEscSeq1() != 0 && encs[cur[1]].encode(cb, bb, 0, CodingErrorAction.REPORT))
                return;

            // try to encode component with current active character set of G0, if different to G1
            if ((codecs[cur[1]].getEscSeq1() == 0 || codecs[cur[1]].getEscSeq0() != codecs[cur[0]].getEscSeq0())
                    && encs[cur[0]].encode(cb, bb, 0, CodingErrorAction.REPORT))
                return;

            int next = encs.length;
            while (--next >= 0) {
                if (encs[next] == null)
                    encs[next] = new Encoder(codecs[next]);
                if (codecs[next].getEscSeq1() != 0) {
                    if (encs[next].encode(cb, bb, codecs[next].getEscSeq1(), CodingErrorAction.REPORT)) {
                        cur[1] = next;
                        break;
                    }
                } else {
                    if (encs[next].encode(cb, bb, codecs[next].getEscSeq0(), CodingErrorAction.REPORT)) {
                        cur[0] = next;
                        break;
                    }
                }
            }
            if (next < 0) {
                if (cb.length() > 1) {
                    for (int i = 0; i < cb.length(); i++) {
                        encodeComponent(encs, cb.subSequence(i, i + 1), bb, cur);
                    }
                } else {
                    // character could not be encoded with any of the
                    // specified character sets, encode it with the
                    // current character set of G0, using the default
                    // replacement of the character set decoder
                    // for characters which cannot be encoded
                    bb.put(encs[cur[0]].replacement());
                }
            }
        }

        private void activateInitialCharacterSet(ByteBuffer bb, int[] cur) {
            if (cur[0] != 0) {
                Encoder.escSeq(bb, codecs[0].getEscSeq0());
                cur[0] = 0;
            }
            if (cur[1] != 0) {
                Encoder.escSeq(bb, codecs[0].getEscSeq1());
                cur[1] = 0;
            }
        }

        @Override
        public String decode(byte[] b) {
            Codec[] codec = { codecs[0], codecs[0] };
            int g = 0;
            int off = 0;
            int cur = 0;
            StringBuilder sb = new StringBuilder(b.length);
            while (cur < b.length) {
                if ( b[ cur ] == 0x1b && cur + 2 < b.length ) { // ESC
                    if (off < cur) {
                        sb.append(codec[g].decode(b, off, cur - off));
                    }
                    int esc0 = cur++;
                    int esc1 = cur++;
                    int esc2 = cur++;
                    switch (((b[esc1] & 255) << 8) + (b[esc2] & 255)) {
                        case 0x2428:
                            if (cur < b.length && b[cur++] == 0x44) {
                                codec[0] = Codec.JIS_X_212;
                            } else { // decode invalid ESC sequence as chars
                                sb.append(codec[0].decode(b, esc0, cur - esc0));
                            }
                            break;
                        case 0x2429:
                            switch (cur < b.length ? b[cur++] : -1) {
                                case 0x41:
                                    switchCodec(codec, 1, Codec.GB2312);
                                    break;
                                case 0x43:
                                    switchCodec(codec, 1, Codec.KS_X_1001);
                                    break;
                                default: // decode invalid ESC sequence as chars
                                    sb.append(codec[0].decode(b, esc0, cur - esc0));
                            }
                            break;
                        case 0x2442:
                            codec[0] = Codec.JIS_X_208;
                            break;
                        case 0x2842:
                            switchCodec(codec, 0, Codec.ISO_646);
                            break;
                        case 0x284a:
                            codec[0] = Codec.JIS_X_201;
                            if (codec[1].getEscSeq1() == 0)
                                codec[1] = codec[0];
                            break;
                        case 0x2949:
                            codec[1] = Codec.JIS_X_201;
                            break;
                        case 0x2d41:
                            switchCodec(codec, 1, Codec.ISO_8859_1);
                            break;
                        case 0x2d42:
                            switchCodec(codec, 1, Codec.ISO_8859_2);
                            break;
                        case 0x2d43:
                            switchCodec(codec, 1, Codec.ISO_8859_3);
                            break;
                        case 0x2d44:
                            switchCodec(codec, 1, Codec.ISO_8859_4);
                            break;
                        case 0x2d46:
                            switchCodec(codec, 1, Codec.ISO_8859_7);
                            break;
                        case 0x2d47:
                            switchCodec(codec, 1, Codec.ISO_8859_6);
                            break;
                        case 0x2d48:
                            switchCodec(codec, 1, Codec.ISO_8859_8);
                            break;
                        case 0x2d4c:
                            switchCodec(codec, 1, Codec.ISO_8859_5);
                            break;
                        case 0x2d4d:
                            switchCodec(codec, 1, Codec.ISO_8859_9);
                            break;
                        case 0x2d54:
                            switchCodec(codec, 1, Codec.TIS_620);
                            break;
                        default: // decode invalid ESC sequence as chars
                            sb.append(codec[0].decode(b, esc0, cur - esc0));
                    }
                    off = cur;
                } else {
                    if (codec[0] != codec[1] && g == (b[cur] < 0 ? 0 : 1)) {
                        if (off < cur) {
                            sb.append(codec[g].decode(b, off, cur - off));
                        }
                        off = cur;
                        g = 1 - g;
                    }
                    int bytesPerChar = codec[g].getBytesPerChar();
                    cur += bytesPerChar > 0 ? bytesPerChar : b[cur] < 0 ? 2 : 1;
                }
            }
            if (off < cur) {
                sb.append(codec[g].decode(b, off, Math.min( cur, b.length) - off ));
            }
            return sb.toString();
        }

        private void switchCodec(Codec[] codecs, int i, Codec codec) {
            codecs[i] = codec;
            if (codecs[0].getEscSeq0() == codecs[1].getEscSeq0())
                codecs[0] = codecs[1];
        }

    }

    public static SpecificCharacterSet getDefaultCharacterSet() {
        return DEFAULT;
    }

    public static void setDefaultCharacterSet(String code) {
        SpecificCharacterSet cs = code != null ? valueOf(code) : ASCII;
        if (!cs.containsASCII())
            throw new IllegalArgumentException("Default Character Set must contain ASCII - " + code);
        DEFAULT = cs;
    }

    public static SpecificCharacterSet valueOf(String... codes) {
        if (codes == null || codes.length == 0)
            return DEFAULT;

        if (codes.length > 1)
            codes = checkISO2022(codes);

        Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; i++)
            infos[i] = Codec.forCode(codes[i]);

        return codes.length > 1 ? new ISO2022(infos,codes)
                : new SpecificCharacterSet(infos, codes);
    }

    /**
     * Replace single code for Single-Byte Character Sets with Code Extensions by code for Single-Byte Character Sets
     * without Code Extensions.
     *
     * @param codes the codes
     * @return {@code true} if the code was replaced.
     */
    public static boolean trimISO2022(String[] codes) {
        if (codes != null && codes.length == 1 && codes[0].startsWith("ISO 2022")) {
            switch (codes[0]) {
                case "ISO 2022 IR 6":
                    codes[0] = "";
                    return true;
                case "ISO 2022 IR 100":
                    codes[0] = "ISO_IR 100";
                    return true;
                case "ISO 2022 IR 101":
                    codes[0] = "ISO_IR 101";
                    return true;
                case "ISO 2022 IR 109":
                    codes[0] = "ISO_IR 109";
                    return true;
                case "ISO 2022 IR 110":
                    codes[0] = "ISO_IR 110";
                    return true;
                case "ISO 2022 IR 144":
                    codes[0] = "ISO_IR 144";
                    return true;
                case "ISO 2022 IR 127":
                    codes[0] = "ISO_IR 127";
                    return true;
                case "ISO 2022 IR 126":
                    codes[0] = "ISO_IR 126";
                    return true;
                case "ISO 2022 IR 138":
                    codes[0] = "ISO_IR 138";
                    return true;
                case "ISO 2022 IR 148":
                    codes[0] = "ISO_IR 148";
                    return true;
                case "ISO 2022 IR 13":
                    codes[0] = "ISO_IR 13";
                    return true;
                case "ISO 2022 IR 166":
                    codes[0] = "ISO_IR 166";
                    return true;
            }
        }
        return false;
    }

    private static String[] checkISO2022(String[] codes) {
        for (String code : codes) {
            if (code != null && !code.isEmpty() && !code.startsWith("ISO 2022")) {
                LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]",
                        StringUtils.concat(codes, '\\'), StringUtils.maskNull(codes[0], ""));
                return new String[]{codes[0]};
            }
        }
        return codes;
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

    public String toText(String s) {
        return codecs[0].toText(s);
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
