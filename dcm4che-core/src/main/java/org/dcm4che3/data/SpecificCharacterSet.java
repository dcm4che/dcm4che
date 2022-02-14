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
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @author Itr Tert (itr.tert@gmail.com)
 */
public class SpecificCharacterSet {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificCharacterSet.class);

    public static final SpecificCharacterSet ASCII = new SpecificCharacterSet(new Codec[]{Codec.ISO_646});

    private static SpecificCharacterSet DEFAULT = ASCII;
    private static final ThreadLocal<SoftReference<Encoder>> cachedEncoder1 = new ThreadLocal<SoftReference<Encoder>>();
    private static final ThreadLocal<SoftReference<Encoder>> cachedEncoder2 = new ThreadLocal<SoftReference<Encoder>>();

    protected final Codec[] codecs;
    protected final String[] dicomCodes;

    private enum Codec {
        ISO_646(true, 0x2842, 0, 1),
        ISO_8859_1(true, 0x2842, 0x2d41, 1),
        ISO_8859_2(true, 0x2842, 0x2d42, 1),
        ISO_8859_3(true, 0x2842, 0x2d43, 1),
        ISO_8859_4(true, 0x2842, 0x2d44, 1),
        ISO_8859_5(true, 0x2842, 0x2d4c, 1),
        ISO_8859_6(true, 0x2842, 0x2d47, 1),
        ISO_8859_7(true, 0x2842, 0x2d46, 1),
        ISO_8859_8(true, 0x2842, 0x2d48, 1),
        ISO_8859_9(true, 0x2842, 0x2d4d, 1),
        JIS_X_201(true, 0x284a, 0x2949, 1) {
            @Override
            public String toText(String s) {
                return s.replace('\\', '¥');
            }
        },
        TIS_620(true, 0x2842, 0x2d54, 1),
        JIS_X_208(false, 0x2442, 0, 1),
        JIS_X_212(false, 0x242844, 0, 2),
        KS_X_1001(false, 0, 0x242943, -1),
        GB2312(false, 0, 0x242941, -1),
        UTF_8(true, 0, 0, -1),
        GB18030(false, 0, 0, -1);

        private static final String[] charsetNames = resetCharsetNames(new String[18]);
        private final boolean containsASCII;
        private final int escSeq0;
        private final int escSeq1;
        private final int bytesPerChar;

        Codec(boolean containsASCII, int escSeq0, int escSeq1, int bytesPerChar) {
            this.containsASCII = containsASCII;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
            this.bytesPerChar = bytesPerChar;
        }

        private static void resetCharsetNames() {
            resetCharsetNames(charsetNames);
        }

        private static String[] resetCharsetNames(String[] charsetNames) {
            charsetNames[0] = "US-ASCII";
            charsetNames[1] = "ISO-8859-1";
            charsetNames[2] = "ISO-8859-2";
            charsetNames[3] = "ISO-8859-3";
            charsetNames[4] = "ISO-8859-4";
            charsetNames[5] = "ISO-8859-5";
            charsetNames[6] = "ISO-8859-6";
            charsetNames[7] = "ISO-8859-7";
            charsetNames[8] = "ISO-8859-8";
            charsetNames[9] = "ISO-8859-9";
            charsetNames[10] = "JIS_X0201";
            charsetNames[11] = "TIS-620";
            charsetNames[12] = "x-JIS0208";
            charsetNames[13] = "JIS_X0212-1990";
            charsetNames[14] = "EUC-KR";
            charsetNames[15] = "GB2312";
            charsetNames[16] = "UTF-8";
            charsetNames[17] = "GB18030";
            return charsetNames;
        }

        public static Codec forCode(String code) {
            return forCode(code, true);
        }

        private static Codec forCode(String code, boolean lenient) {
            return forCode(code, lenient, SpecificCharacterSet.DEFAULT.codecs[0]);
        }

        private static Codec forCode(String code, boolean lenient, Codec defCodec) {
            switch(code != null ? code : "") {
                case "":
                case "ISO 2022 IR 6":
                    return defCodec;
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
            if (!lenient)
                throw new IllegalArgumentException("No such Specific Character Set Code: " + code);
            return defCodec;
        }

        public byte[] encode(String val) {
            try {
                return val.getBytes(charsetName());
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        private String charsetName() {
            return charsetNames[ordinal()];
        }

        private void setCharsetName(String charsetName) {
            charsetNames[ordinal()] = charsetName;
        }

        public String decode(byte[] b, int off, int len) {
            try {
                return new String(b, off, len, charsetName());
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

    private enum G0G1 {
        G0, G1, Both
    }

    private static final class Encoder {
        final Codec codec;
        final CharsetEncoder encoder;
 
        public Encoder(Codec codec) {
            this.codec = codec;
            this.encoder = Charset.forName(codec.charsetName()).newEncoder();
        }

        public boolean encode(CharBuffer cb, ByteBuffer bb, int escSeq,
                G0G1 useRange, CodingErrorAction errorAction) {
            encoder.onMalformedInput(errorAction)
                    .onUnmappableCharacter(errorAction)
                    .reset();
            int cbmark = cb.position();
            int bbmark = bb.position();
            try {
                escSeq(bb, escSeq);
                int graphicCharStart = bb.position();
                CoderResult cr = encoder.encode(cb, bb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = encoder.flush(bb);
                if (!cr.isUnderflow())
                    cr.throwException();

                if (useRange == G0G1.G0) {
                    for (int i = graphicCharStart, end = bb.position(); i < end; ++i) {
                        if (0 > bb.get(i)) {
                            throw new CharacterCodingException();
                        }
                    }
                } else if (useRange == G0G1.G1) {
                    for (int i = graphicCharStart, end = bb.position(); i < end; ++i) {
                        if (0 <= bb.get(i)) {
                            throw new CharacterCodingException();
                        }
                    }
                }
                // if useRange == G0G1.Both, then do nothing
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
            if (!enc1.encode(cb, bb, 0, G0G1.Both, CodingErrorAction.REPORT)) {
                // split whole string value according VR specific delimiters
                // and try to encode each component separately
                Encoder[] encs = new Encoder[codecs.length];
                encs[0] = enc1;
                encs[1] = encoder(cachedEncoder2, codecs[1]);
                StringTokenizer comps = new StringTokenizer(val, delimiters, true);
                buf = new byte[(2 + 4) * strlen];
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
            if (codecs[cur[1]].getEscSeq1() != 0 && encs[cur[1]].encode(cb, bb, 0, G0G1.G1, CodingErrorAction.REPORT))
                return;

            // try to encode component with current active character set of G0, if different to G1
            if ((codecs[cur[1]].getEscSeq1() == 0 || codecs[cur[1]].getEscSeq0() != codecs[cur[0]].getEscSeq0())
                    && encs[cur[0]].encode(cb, bb, 0, G0G1.G0, CodingErrorAction.REPORT))
                return;

            int next = encs.length;
            while (--next >= 0) {
                if (encs[next] == null)
                    encs[next] = new Encoder(codecs[next]);
                if (codecs[next].getEscSeq1() != 0) {
                    if (encs[next].encode(cb, bb, codecs[next].getEscSeq1(), G0G1.G1, CodingErrorAction.REPORT)) {
                        cur[1] = next;
                        break;
                    }
                }
                if (codecs[next].getEscSeq0() != 0) {
                    if (encs[next].encode(cb, bb, codecs[next].getEscSeq0(), G0G1.G0, CodingErrorAction.REPORT)) {
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

    /**
     * Overwrites mapping of value of DICOM Specific Character Set (0008,0005) to named charset.
     *
     * For example, {@code SpecificCharacterSet.setCharsetNameMapping("ISO_IR 100", "ISO-8859-15")} associates
     * ISO-8859-15 (Latin-9), {@code SpecificCharacterSet.setCharsetNameMapping("ISO_IR 100", "windows-1252")}
     * Windows-1252 (CP-1252), with DICOM Specific Character Set (0008,0005) code value {@code ISO_IR 100} -
     * replacing the default mapping to ISO-8859-1 (Latin-1) - were both (ISO-8859-15 and Windows-1252)
     * containing characters Š/š and Ž/ž not included in Latin-1, but used in Estonian and Finnish for
     * transcribing foreign names.
     *
     * @param  code
     *         value of DICOM Specific Character Set (0008,0005)
     * @param  charsetName
     *         The name of the mapped charset
     *
     * @throws  IllegalCharsetNameException
     *          If the given code or charset name is illegal
     *
     * @throws  IllegalArgumentException
     *          If the given {@code charsetName} is null
     *
     * @throws  UnsupportedCharsetException
     *          If no support for the named charset is available
     *          in this instance of the Java virtual machine
     */
    public static void setCharsetNameMapping(String code, String charsetName) {
        Codec.forCode(code, false).setCharsetName(checkCharsetName(charsetName));
    }

    /**
     * Reset mapping of DICOM Specific Character Set (0008,0005) values to named charsets as specified by
     * <a href="http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.12.html#table_C.12-2">
     * DICOM PS 3.3 Table C.12-2</a>.
     *
     * <table>
     * <tr>
     * <th>Code String</th>
     * <th>Charset Name</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr><td>_empty_</td>
     *     <td>{@code US-ASCII}</td></tr>
     * <tr><td>{@code ISO_IR 100}</td>
     *     <td>{@code ISO-8859-1}</td></tr>
     * <tr><td>{@code ISO_IR 101}</td>
     *     <td>{@code ISO-8859-2}</td></tr>
     * <tr><td>{@code ISO_IR 109}</td>
     *     <td>{@code ISO-8859-3}</td></tr>
     * <tr><td>{@code ISO_IR 110}</td>
     *     <td>{@code ISO-8859-4}</td></tr>
     * <tr><td>{@code ISO_IR 144}</td>
     *     <td>{@code ISO-8859-5}</td></tr>
     * <tr><td>{@code ISO_IR 127}</td>
     *     <td>{@code ISO-8859-6}</td></tr>
     * <tr><td>{@code ISO_IR 126}</td>
     *     <td>{@code ISO-8859-7}</td></tr>
     * <tr><td>{@code ISO_IR 138}</td>
     *     <td>{@code ISO-8859-8}</td></tr>
     * <tr><td>{@code ISO_IR 148}</td>
     *     <td>{@code ISO-8859-9}</td></tr>
     * <tr><td>{@code ISO_IR 13}</td>
     *     <td>{@code JIS_X0201}</td></tr>
     * <tr><td>{@code ISO_IR 166}</td>
     *     <td>{@code TIS-620}</td></tr>
     * <tr><td>{@code ISO 2022 IR 87}</td>
     *     <td>{@code x-JIS0208}</td></tr>
     * <tr><td>{@code ISO 2022 IR 159}</td>
     *     <td>{@code JIS_X0212-1990}</td></tr>
     * <tr><td>{@codeISO 2022 IR 149 }</td>
     *     <td>{@code EUC-KR}</td></tr>
     * <tr><td>{@code ISO 2022 IR 58}</td>
     *     <td>{@code GB2312}</td></tr>
     * <tr><td>{@code ISO_IR 192}</td>
     *     <td>{@code UTF-8}</td></tr>
     * <tr><td>{@code GB18030}</td>
     *     <td>{@code GB18030}</td></tr>
     * </tbody>
     * </table>
     */
    public static void resetCharsetNameMappings() {
        Codec.resetCharsetNames();
    }

    public static String checkSpecificCharacterSet(String code) {
        Codec.forCode(code, false);
        return code;
    }

    public static String checkCharsetName(String charsetName) {
        if (!Charset.isSupported(charsetName))
            throw new UnsupportedCharsetException(charsetName);
        return charsetName;
    }

    public static SpecificCharacterSet valueOf(String... codes) {
        if (codes == null || codes.length == 0)
            return DEFAULT;

        boolean iso2022 = codes.length > 1;
        Codec defCodec = SpecificCharacterSet.DEFAULT.codecs[0];
        if (iso2022) {
            codes = checkISO2022(codes);
            if (defCodec == Codec.UTF_8) {
                defCodec = Codec.ISO_646;
            }
        }

        Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; i++) {
            infos[i] = Codec.forCode(codes[i], true, defCodec);
        }

        return iso2022 ? new ISO2022(infos, codes)
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
        String[] results = codes;
        for (int i = 0; i < codes.length; i++) {
            String code = codes[i];
            if (code != null && !code.isEmpty() && !code.startsWith("ISO 2022")) {
                switch (code) {
                    case "ISO_IR 100":
                    case "ISO_IR 101":
                    case "ISO_IR 109":
                    case "ISO_IR 110":
                    case "ISO_IR 144":
                    case "ISO_IR 127":
                    case "ISO_IR 126":
                    case "ISO_IR 138":
                    case "ISO_IR 148":
                    case "ISO_IR 13":
                    case "ISO_IR 166":
                        if (results == codes) results = codes.clone();
                        results[i] = "ISO 2022 " + code.substring(4);
                        continue;
                }
                LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]",
                        StringUtils.concat(codes, '\\'), StringUtils.maskNull(codes[0], ""));
                return new String[]{codes[0]};
            }
        }
        if (codes != results) {
            LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]",
                    StringUtils.concat(codes, '\\'), StringUtils.concat(results, '\\'));
        }
        return ensureFirstContainsASCII(results);
    }

    private static String[] ensureFirstContainsASCII(String[] codes) {
        for (int i = 0; i < codes.length; i++) {
            if (Codec.forCode(codes[i]).containsASCII()) {
                if (i == 0) return codes;
                String[] clone = codes.clone();
                clone[0] = codes[i];
                clone[i] = codes[0];
                LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]",
                        StringUtils.concat(codes, '\\'), StringUtils.concat(clone, '\\'));
                return clone;
            }
        }
        String[] withASCII = new String[1 + codes.length];
        withASCII[0] = "";
        System.arraycopy(codes, 0, withASCII, 1, codes.length);
        LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]",
                StringUtils.concat(codes, '\\'), StringUtils.concat(withASCII, '\\'));
        return withASCII;
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

    public boolean contains(SpecificCharacterSet other) {
        return Arrays.equals(codecs, other.codecs) || (other.isASCII() || other == ASCII) && containsASCII();
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
