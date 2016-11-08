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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SpecificCharacterSet {
    
    public static final SpecificCharacterSet DICOM_DEFAULT =
            new SpecificCharacterSet(new Codec[] {Codec.ISO_646}, new String[] {null});
    
    public static SpecificCharacterSet DEFAULT = DICOM_DEFAULT;
    
    protected final Codec[] codecs;
    protected final String[] dicomCodes;

    private enum Codec {
        ISO_646("US-ASCII", new byte[] {0x1b, 0x28, 0x42}, null, 1),
        ISO_8859_1("ISO-8859-1", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x41}, 1),
        ISO_8859_2("ISO-8859-2", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x42}, 1),
        ISO_8859_3("ISO-8859-3", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x43}, 1),
        ISO_8859_4("ISO-8859-4", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x44}, 1),
        ISO_8859_5("ISO-8859-5", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x4c}, 1),
        ISO_8859_6("ISO-8859-6", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x47}, 1),
        ISO_8859_7("ISO-8859-7", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x46}, 1),
        ISO_8859_8("ISO-8859-8", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x48}, 1),
        ISO_8859_9("ISO-8859-9", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x4d}, 1),
        JIS_X_201("JIS_X0201", new byte[] {0x1b, 0x28, 0x4a}, new byte[] {0x1b, 0x29, 0x49}, 1),
        TIS_620("TIS-620", new byte[] {0x1b, 0x28, 0x42}, new byte[] {0x1b, 0x2d, 0x54}, 1),
        JIS_X_208("x-JIS0208", new byte[] {0x1b, 0x24, 0x42}, null, 2),
        JIS_X_212("JIS_X0212-1990", new byte[] {0x1b, 0x24, 0x28, 0x44}, null, 2),
        KS_X_1001("EUC-KR", null, new byte[] {0x1b, 0x24, 0x29, 0x43}, 2),
        GB2312("GB2312", null, new byte[] {0x1b, 0x24, 0x29, 0x41}, 2),
        UTF_8("UTF-8", null, null, 0),
        GB18030("GB18030", null, null, 0),
        GBK("GBK", null, null, 0);

        private final String charsetName;
        private final byte[] escSeq0;
        private final byte[] escSeq1;
        private final int bytesPerChar;

        private Codec(final String charsetName, final byte[] escSeq0, final byte[] escSeq1, final int bytesPerChar) {
            this.charsetName = charsetName;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
            this.bytesPerChar = bytesPerChar;
        }

        public static Codec forCode(final String code) {
            if (code == null) {
                return ISO_646;
            }

            switch (last2digits(code)) {
                case 0:
                    if (code.equals("ISO_IR 100") || code.equals("ISO 2022 IR 100")) {
                        return Codec.ISO_8859_1;
                    }
                    break;
                case 1:
                    if (code.equals("ISO_IR 101") || code.equals("ISO 2022 IR 101")) {
                        return Codec.ISO_8859_2;
                    }
                    break;
                case 6:
                    if (code.equals("ISO 2022 IR 6")) {
                        return Codec.ISO_646;
                    }
                    break;
                case 9:
                    if (code.equals("ISO_IR 109") || code.equals("ISO 2022 IR 109")) {
                        return Codec.ISO_8859_3;
                    }
                    break;
                case 10:
                    if (code.equals("ISO_IR 110") || code.equals("ISO 2022 IR 110")) {
                        return Codec.ISO_8859_4;
                    }
                    break;
                case 13:
                    if (code.equals("ISO_IR 13") || code.equals("ISO 2022 IR 13")) {
                        return Codec.JIS_X_201;
                    }
                    break;
                case 26:
                    if (code.equals("ISO_IR 126") || code.equals("ISO 2022 IR 126")) {
                        return Codec.ISO_8859_7;
                    }
                    break;
                case 27:
                    if (code.equals("ISO_IR 127") || code.equals("ISO 2022 IR 127")) {
                        return Codec.ISO_8859_6;
                    }
                    break;
                case 30:
                    if (code.equals("GB18030")) {
                        return Codec.GB18030;
                    }
                    break;
                case 31:
                    if (code.equals("GBK")) {
                        return Codec.GBK;
                    }
                    break;
                case 38:
                    if (code.equals("ISO_IR 138") || code.equals("ISO 2022 IR 138")) {
                        return Codec.ISO_8859_8;
                    }
                    break;
                case 44:
                    if (code.equals("ISO_IR 144") || code.equals("ISO 2022 IR 144")) {
                        return Codec.ISO_8859_5;
                    }
                    break;
                case 48:
                    if (code.equals("ISO_IR 148") || code.equals("ISO 2022 IR 148")) {
                        return Codec.ISO_8859_9;
                    }
                    break;
                case 49:
                    if (code.equals("ISO 2022 IR 149")) {
                        return Codec.KS_X_1001;
                    }
                    break;
                case 58:
                    if (code.equals("ISO 2022 IR 58")) {
                        return Codec.GB2312;
                    }
                    break;
                case 59:
                    if (code.equals("ISO 2022 IR 159")) {
                        return Codec.JIS_X_212;
                    }
                    break;
                case 66:
                    if (code.equals("ISO_IR 166") || code.equals("ISO 2022 IR 166")) {
                        return Codec.TIS_620;
                    }
                    break;
                case 87:
                    if (code.equals("ISO 2022 IR 87")) {
                        return Codec.JIS_X_208;
                    }
                    break;
                case 92:
                    if (code.equals("ISO_IR 192")) {
                        return Codec.UTF_8;
                    }
                    break;
            }
            return ISO_646;
        }

        private static int last2digits(final String code) {
            final int len = code.length();
            if (len < 2) {
                return -1;
            }
            final char ch1 = code.charAt(len - 1);
            final char ch2 = code.charAt(len - 2);
            return (ch2 & 15) * 10 + (ch1 & 15);
        }

        public boolean canEncode(final String val) {
            return Charset.forName(charsetName).newEncoder().canEncode(val);
        }

        public boolean canEncode(final char c) {
            return canEncode(Character.toString(c));
        }

        public byte[] encode(final String val) {
            try {
                return val.getBytes(charsetName);
            } catch (final UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        public byte[] encode(final char c) {
            return encode(Character.toString(c));
        }

        public String decode(final byte[] b) {
            try {
                return new String(b, charsetName);
            } catch (final UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        public boolean containsASCII() {
            return hasEscSeq0();
        }

        public int getBytesPerChar() {
            return bytesPerChar;
        }

        public byte[] getEscSeq0() {
            return escSeq0;
        }

        public boolean hasEscSeq0() {
            return escSeq0 != null;
        }

        public boolean containsEscSeq0(final ByteBuffer bb) {
            return containsEscSeq(bb, escSeq0);
        }

        public byte[] getEscSeq1() {
            return escSeq1;
        }

        public boolean hasEscSeq1() {
            return escSeq1 != null;
        }

        public boolean containsEscSeq1(final ByteBuffer bb) {
            return containsEscSeq(bb, escSeq1);
        }

        private boolean containsEscSeq(final ByteBuffer bb, final byte[] escSeqBytes) {
            bb.mark();
            for (final byte escSeqByte : escSeqBytes) {
                if (bb.get() != escSeqByte) {
                    bb.reset();
                    return false;
                }
            }
            return true;
        }
    }

    private static final class ISO2022 extends SpecificCharacterSet {

        private ISO2022(final Codec[] charsetInfos, final String... codes) {
            super(charsetInfos, codes);
        }

        @Override
        public byte[] encode(final String val, final StringValueType type) {
            Codec codecG0 = getInitialCodecG0();
            Codec codecG1 = getInitialCodecG1();

            // in the simplest case, codec G0 can encode the whole string value
            if (codecG0.canEncode(val)) {
                return codecG0.encode(val);
            }

            /*
             * Calculate maximum capacity of the ByteBuffer:
             * In the worst case, each character has a preceding escape sequence of four bytes and the
             * character itself takes up two bytes.
             */
            final int maxCapacity = 4 * (val.length() * 2);
            final ByteBuffer bb = ByteBuffer.allocate(maxCapacity);

            // try to encode each character of the string value consecutively
            final char[] chars = val.toCharArray();
            for (final char c : chars) {
                /*
                 * Before encoding the current character -> perform checks if the initial character set
                 * shall be active (see DICOM PS3.5 2016c, 6.1.2.5.3 Requirements)
                 */
                final byte firstByte = getInitialCodecG0().encode(c)[0];
                boolean delimiterPresent = false;

                /*
                 * Check 1: if first byte codes for a DICOM control character and therefore the initial
                 * character set shall be active
                 */
                if (firstByte >= 0x09 && firstByte <= 0x0D) {
                    delimiterPresent = true;
                }

                /*
                 * Check 2: if VR is of type PN and therefore the initial character set shall be active
                 * before "^" and "=" delimiters
                 */
                if (type == StringValueType.PN && (firstByte == 0x5E || firstByte == 0x3D)) {
                    delimiterPresent = true;
                }

                /*
                 * Check 3: if data element may have multiple values and therefore the initial character set
                 * shall be active if the first byte codes for backslash character
                 */
                if (type.multipleValues && firstByte == 0x5C && (codecG0 != Codec.JIS_X_201 || codecG0 == getInitialCodecG0())) {
                    delimiterPresent = true;
                }

                // always encode delimiters with the initial character set
                if (delimiterPresent) {
                    if (codecG0 != getInitialCodecG0()) {
                        bb.put(getInitialCodecG0().getEscSeq0());
                    }
                    codecG0 = getInitialCodecG0();
                    codecG1 = getInitialCodecG1();
                    bb.put(codecG0.encode(c));
                    continue;
                }

                if (codecG0.canEncode(c)) {
                    final byte[] encodedChar = codecG0.encode(c);
                    if (!(encodedChar[0] < 0)) {
                        bb.put(encodedChar);
                        continue;
                    }
                }

                if (codecG1 != null && codecG1.canEncode(c)) {
                    final byte[] encodedChar = codecG1.encode(c);
                    if (encodedChar[0] < 0) {
                        bb.put(encodedChar);
                        continue;
                    }
                }

                boolean couldEncode = false;
                for (final Codec cd : codecs) {
                    if (cd.canEncode(c)) {
                        final byte[] encodedChar = cd.encode(c);
                        if (cd.hasEscSeq0() && !(encodedChar[0] < 0)) {
                            codecG0 = cd;
                            bb.put(codecG0.getEscSeq0());
                            bb.put(encodedChar);
                            couldEncode = true;
                            break;
                        }
                        if (cd.hasEscSeq1() && (encodedChar[0] < 0)) {
                            codecG1 = cd;
                            bb.put(codecG1.getEscSeq1());
                            bb.put(encodedChar);
                            couldEncode = true;
                            break;
                        }
                    }
                }

                /*
                 * Could not encode character with any of the Specific Character Sets.
                 * Switch to the initial codec G0 and append the unknown character as four characters "\nnn",
                 * where "nnn" is the three digit octal representation of the character (see DICOM PS3.5 2016c,
                 * 6.1.2.3 Encoding of Character Repertoires).
                 */
                if (!couldEncode) {
                    if (codecG0 != getInitialCodecG0()) {
                        codecG0 = getInitialCodecG0();
                        bb.put(codecG0.getEscSeq0());
                    }
                    bb.put(codecG0.encode('\\'));
                    bb.put(codecG0.encode(Integer.toString(bb.get() & 0xFF, 8)));
                }
            }

            final byte[] encodedString = new byte[bb.position()];
            bb.flip();
            bb.get(encodedString);
            return encodedString;
        }

        @Override
        public String decode(final byte[] b, final StringValueType type) {
            final ByteBuffer bb = ByteBuffer.wrap(b);
            final StringBuilder sb = new StringBuilder(b.length);

            Codec codecG0 = getInitialCodecG0();
            Codec codecG1 = getInitialCodecG1();

            while (bb.hasRemaining()) {
                // check if current byte initiates an escape sequence
                if (bb.get(bb.position()) == 0x1b) { // ESC
                    boolean switched = false;
                    for (final Codec c : codecs) {
                        if (c.hasEscSeq0() && c.containsEscSeq0(bb)) {
                            switched = true;
                            codecG0 = c;
                            break;
                        }
                        if (c.hasEscSeq1() && c.containsEscSeq1(bb)) {
                            switched = true;
                            codecG1 = c;
                            break;
                        }
                    }
                    if (!switched) {
                        /*
                         * Unknown/invalid escape sequence detected.
                         * Append all remaining bytes with the four characters "\nnn", where "nnn" is the
                         * three digit octal representation of each byte (see DICOM PS3.5 2016c, 6.1.2.3
                         * Encoding of Character Repertoires).
                         */
                        while (bb.hasRemaining()) {
                            sb.append("\\");
                            sb.append(Integer.toString(bb.get() & 0xFF, 8));
                        }
                    }
                } else {
                    /*
                     * Before decoding the current character -> perform checks if the initial character set
                     * shall be active (see DICOM PS3.5 2016c, 6.1.2.5.3 Requirements). These checks are only
                     * necessary if codec G0 is a single-byte character set.
                     */
                    if (codecG0.getBytesPerChar() == 1) {
                        final byte firstByte = bb.get(bb.position());

                        /*
                         * Check 1: if first byte codes for a DICOM control character and therefore the initial
                         * character set shall be active
                         */
                        if (firstByte >= 0x09 && firstByte <= 0x0D) {
                            codecG0 = getInitialCodecG0();
                            codecG1 = getInitialCodecG1();
                        }

                        /*
                         * Check 2: if VR is of type PN and therefore the initial character set shall be active
                         * before "^" and "=" delimiters
                         */
                        if (type == StringValueType.PN && (firstByte == 0x5E || firstByte == 0x3D)) {
                            codecG0 = getInitialCodecG0();
                            codecG1 = getInitialCodecG1();
                        }

                        /*
                         * Check 3: if data element may have multiple values and therefore the initial character set
                         * shall be active if the first byte codes for backslash character
                         */
                        if (type.multipleValues && firstByte == 0x5C &&
                                (codecG0 != Codec.JIS_X_201 || codecG0 == getInitialCodecG0())) {
                            codecG0 = getInitialCodecG0();
                            codecG1 = getInitialCodecG1();
                        }
                    }

                    // decode the current character
                    final Codec applicableCodec = bb.get(bb.position()) < 0 ? codecG1 : codecG0;
                    final byte[] currentChar = new byte[applicableCodec.getBytesPerChar()];
                    bb.get(currentChar);
                    String s = applicableCodec.decode(currentChar);

                    if (applicableCodec == Codec.JIS_X_201 && applicableCodec != getInitialCodecG0()) {
                        // replace backslash character with Yen symbol
                        s = s.replace("\\", "\u00A5");
                        // replace tilde character with over-line
                        s = s.replace("~", "\u203E");
                    }

                    sb.append(s);
                }
            }

            return sb.toString();
        }

        private Codec getInitialCodecG0() {
            return codecs[0];
        }

        private Codec getInitialCodecG1() {
            return codecs[0].hasEscSeq1() ? codecs[0] : null;
        }
    }

    public static final void setDefaultCharacterSet(final String characterSet) {
        DEFAULT = characterSet == null ? DICOM_DEFAULT : valueOf(characterSet);
    }

    public static SpecificCharacterSet valueOf(final String... codes) {
        if (codes == null || codes.length == 0) {
            return DICOM_DEFAULT;
        }

        final Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; i++) {
            infos[i] = Codec.forCode(codes[i]);
        }

        if (codes.length == 1 && infos[0] == Codec.ISO_646 && DEFAULT.containsASCII()) {
            return new SpecificCharacterSet(DEFAULT.codecs, codes);
        }

        return codes.length > 1 ? new ISO2022(infos, codes) : new SpecificCharacterSet(infos, codes);
    }

    public String[] toCodes() {
        return dicomCodes;
    }

    protected SpecificCharacterSet(final Codec[] codecs, final String... codes) {
        this.codecs = codecs;
        this.dicomCodes = codes;
    }

    public byte[] encode(final String val, final StringValueType type) {
        return codecs[0].encode(val);
    }

    public String decode(final byte[] val, final StringValueType type) {
        return codecs[0].decode(val);
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

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final SpecificCharacterSet othercs = (SpecificCharacterSet) other;
        return Arrays.equals(this.codecs, othercs.codecs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.codecs);
    }

}
