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


import static org.junit.Assert.*;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ElementDictionaryTest {

    private static final int[] TAGS = {
        Tag.CommandGroupLength,
        Tag.CommandDataSetType,
        Tag.FileMetaInformationGroupLength,
        Tag.TransferSyntaxUID,
        Tag.SpecificCharacterSet,
        Tag.SmallestImagePixelValue,
        Tag.WaveformData,
        Tag.PixelData,
        Tag.OverlayData,
        Tag.OverlayData + 0x00020000
    };

    private static final VR[] VRS = {
        VR.UL,
        VR.US,
        VR.UL,
        VR.UI,
        VR.CS,
        VR.SS,
        VR.OW,
        VR.OW,
        VR.OW,
        VR.OW
    };

    private static final String[] KEYWORDS = {
        "CommandGroupLength",
        "CommandDataSetType",
        "FileMetaInformationGroupLength",
        "TransferSyntaxUID",
        "SpecificCharacterSet",
        "SmallestImagePixelValue",
        "WaveformData",
        "PixelData",
        "OverlayData",
        "OverlayData"
    };

    private static final String SIEMENS_CSA_HEADER = "SIEMENS CSA HEADER";

    private static final int[] SIEMENS_CSA_HEADER_TAGS = {
        0x00290008,
        0x00290009,
        0x00290010,
        0x00290018,
        0x00290019,
        0x00290020
    };

    private static final VR[] SIEMENS_CSA_HEADER_VRS = {
        VR.CS,
        VR.LO,
        VR.OB,
        VR.CS,
        VR.LO,
        VR.OB
    };

    private static final String[] SIEMENS_CSA_HEADER_KEYWORDS = {
        "CSAImageHeaderType",
        "CSAImageHeaderVersion",
        "CSAImageHeaderInfo",
        "CSASeriesHeaderType",
        "CSASeriesHeaderVersion",
        "CSASeriesHeaderInfo"
    };

    private static final String SIEMENS_CSA_NON_IMAGE = "SIEMENS CSA NON-IMAGE";

    private static final int[] SIEMENS_CSA_NON_IMAGE_TAGS = {
        0x00290008,
        0x00290009,
        0x00290010,
    };

    private static final VR[] SIEMENS_CSA_NON_IMAGE_VRS = {
        VR.CS,
        VR.LO,
        VR.OB,
    };

    private static final String[] SIEMENS_CSA_NON_IMAGE_KEYWORDS = {
        "CSADataType",
        "CSADataVersion",
        "CSADataInfo",
    };

    @Test
    public void testVrOf() {
        for (int i = 0; i < TAGS.length; i++)
            assertEquals(VRS[i], ElementDictionary.vrOf(TAGS[i], null));
    }

    @Test
    public void testKeywordOf() {
        for (int i = 0; i < TAGS.length; i++)
            assertEquals(KEYWORDS[i],
                         ElementDictionary.keywordOf(TAGS[i], null));
    }

    @Test
    public void tagForKeyword() {
        for (int i = 0; i < KEYWORDS.length-1; i++)
            assertEquals(TAGS[i],
                         ElementDictionary.tagForKeyword(KEYWORDS[i], null));
    }

    @Test
    public void testPrivateVrOf() {
        for (int i = 0; i < SIEMENS_CSA_HEADER_TAGS.length; i++)
            assertEquals(SIEMENS_CSA_HEADER_VRS[i],
                    ElementDictionary.vrOf(SIEMENS_CSA_HEADER_TAGS[i],
                            SIEMENS_CSA_HEADER));
        for (int i = 0; i < SIEMENS_CSA_NON_IMAGE_TAGS.length; i++)
            assertEquals(SIEMENS_CSA_NON_IMAGE_VRS[i],
                    ElementDictionary.vrOf(SIEMENS_CSA_NON_IMAGE_TAGS[i],
                            SIEMENS_CSA_NON_IMAGE));
    }

    @Test
    public void testPrivateKeywordOf() {
        for (int i = 0; i < SIEMENS_CSA_HEADER_TAGS.length; i++)
            assertEquals(SIEMENS_CSA_HEADER_KEYWORDS[i],
                         ElementDictionary.keywordOf(
                                 SIEMENS_CSA_HEADER_TAGS[i], 
                                 SIEMENS_CSA_HEADER));
        for (int i = 0; i < SIEMENS_CSA_NON_IMAGE_TAGS.length; i++)
            assertEquals(SIEMENS_CSA_NON_IMAGE_KEYWORDS[i],
                         ElementDictionary.keywordOf(
                                 SIEMENS_CSA_NON_IMAGE_TAGS[i], 
                                 SIEMENS_CSA_NON_IMAGE));
    }

    @Test
    public void tagPrivateForKeyword() {
        for (int i = 0; i < SIEMENS_CSA_HEADER_KEYWORDS.length; i++)
            assertEquals(SIEMENS_CSA_HEADER_TAGS[i],
                         ElementDictionary.tagForKeyword(
                                 SIEMENS_CSA_HEADER_KEYWORDS[i],
                                 SIEMENS_CSA_HEADER));
        for (int i = 0; i < SIEMENS_CSA_NON_IMAGE_KEYWORDS.length; i++)
            assertEquals(SIEMENS_CSA_NON_IMAGE_TAGS[i],
                         ElementDictionary.tagForKeyword(
                                 SIEMENS_CSA_NON_IMAGE_KEYWORDS[i],
                                 SIEMENS_CSA_NON_IMAGE));
    }
}
