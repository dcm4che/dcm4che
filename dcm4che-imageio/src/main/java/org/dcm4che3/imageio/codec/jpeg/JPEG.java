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

package org.dcm4che3.imageio.codec.jpeg;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JPEG {

    /** For temporary use in arithmetic coding */
    public static final int TEM = 0x01;

    // Codes 0x02 - 0xBF are reserved

    // SOF markers for Nondifferential Huffman coding
    /** Baseline DCT */
    public static final int SOF0 = 0xC0;
    /** Extended Sequential DCT */
    public static final int SOF1 = 0xC1;
    /** Progressive DCT */
    public static final int SOF2 = 0xC2;
    /** Lossless Sequential */
    public static final int SOF3 = 0xC3;

    /** Define Huffman Tables */
    public static final int DHT = 0xC4;

    // SOF markers for Differential Huffman coding
    /** Differential Sequential DCT */
    public static final int SOF5 = 0xC5;
    /** Differential Progressive DCT */
    public static final int SOF6 = 0xC6;
    /** Differential Lossless */
    public static final int SOF7 = 0xC7;

    /** Reserved for JPEG extensions */
    public static final int JPG = 0xC8;

    // SOF markers for Nondifferential arithmetic coding
    /** Extended Sequential DCT, Arithmetic coding */
    public static final int SOF9 = 0xC9;
    /** Progressive DCT, Arithmetic coding */
    public static final int SOF10 = 0xCA;
    /** Lossless Sequential, Arithmetic coding */
    public static final int SOF11 = 0xCB;

    /** Define Arithmetic conditioning tables */
    public static final int DAC = 0xCC;

    // SOF markers for Differential arithmetic coding
    /** Differential Sequential DCT, Arithmetic coding */
    public static final int SOF13 = 0xCD;
    /** Differential Progressive DCT, Arithmetic coding */
    public static final int SOF14 = 0xCE;
    /** Differential Lossless, Arithmetic coding */
    public static final int SOF15 = 0xCF;

    // Restart Markers
    public static final int RST0 = 0xD0;
    public static final int RST1 = 0xD1;
    public static final int RST2 = 0xD2;
    public static final int RST3 = 0xD3;
    public static final int RST4 = 0xD4;
    public static final int RST5 = 0xD5;
    public static final int RST6 = 0xD6;
    public static final int RST7 = 0xD7;
    /** Number of restart markers */
    public static final int RESTART_RANGE = 8;

    /** Start of Image */
    public static final int SOI = 0xD8;
    /** End of Image */
    public static final int EOI = 0xD9;
    /** Start of Scan */
    public static final int SOS = 0xDA;

    /** Define Quantization Tables */
    public static final int DQT = 0xDB;

    /** Define Number of lines */
    public static final int DNL = 0xDC;

    /** Define Restart Interval */
    public static final int DRI = 0xDD;

    /** Define Hierarchical progression */
    public static final int DHP = 0xDE;

    /** Expand reference image(s) */
    public static final int EXP = 0xDF;

    // Application markers
    /** APP0 used by JFIF */
    public static final int APP0 = 0xE0;
    public static final int APP1 = 0xE1;
    public static final int APP2 = 0xE2;
    public static final int APP3 = 0xE3;
    public static final int APP4 = 0xE4;
    public static final int APP5 = 0xE5;
    public static final int APP6 = 0xE6;
    public static final int APP7 = 0xE7;
    public static final int APP8 = 0xE8;
    public static final int APP9 = 0xE9;
    public static final int APP10 = 0xEA;
    public static final int APP11 = 0xEB;
    public static final int APP12 = 0xEC;
    public static final int APP13 = 0xED;
    /** APP14 used by Adobe */
    public static final int APP14 = 0xEE;
    public static final int APP15 = 0xEF;

    // codes 0xF0 to 0xFD are reserved
    /** JPEG-LS coding */
    public static final int SOF55 = 0xF7;
    /** JPEG-LS parameters */
    public static final int LSE =   0xF8;

    /** Comment marker */
    public static final int COM = 0xFE;

    public static boolean isStandalone(int marker) {
        switch(marker) {
        case TEM:
        case RST0:
        case RST1:
        case RST2:
        case RST3:
        case RST4:
        case RST5:
        case RST6:
        case RST7:
        case SOI:
        case EOI:
            return true;
        }
        return false;
    }

    public static boolean isSOF(int marker) {
        switch(marker) {
            case SOF0:
            case SOF1:
            case SOF2:
            case SOF3:
            case SOF5:
            case SOF6:
            case SOF7:
            case SOF9:
            case SOF10:
            case SOF11:
            case SOF13:
            case SOF14:
            case SOF15:
            case SOF55:
                return true;
        }
        return false;
    }

    public static boolean isAPP(int marker) {
        return (marker & APP0) == APP0;
    }
}
