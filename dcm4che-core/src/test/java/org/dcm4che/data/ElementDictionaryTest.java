package org.dcm4che.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ElementDictionaryTest {

    private static final int[] TAGS = {
        Tag.CommandGroupLength,
        Tag.CommandDataSetType,
        Tag.FileMetaInformationGroupLength,
        Tag.TransferSyntaxUID,
        Tag.SpecificCharacterSet,
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
        "WaveformData",
        "PixelData",
        "OverlayData",
        "OverlayData"
    };

    private static final String PRIVATE_CREATOR = "SIEMENS CSA HEADER";

    private static final int[] PRIVATE_TAGS = {
        0x00290008,
        0x00290009,
        0x00290010,
        0x00290018,
        0x00290019,
        0x00290020
    };

    private static final VR[] PRIVATE_VRS = {
        VR.CS,
        VR.LO,
        VR.OB,
        VR.CS,
        VR.LO,
        VR.OB
    };

    private static final String[] PRIVATE_KEYWORDS = {
        "CSAImageHeaderType",
        "CSAImageHeaderVersion",
        "CSAImageHeaderInfo",
        "CSASeriesHeaderType",
        "CSASeriesHeaderVersion",
        "CSASeriesHeaderInfo"
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
        for (int i = 0; i < PRIVATE_TAGS.length; i++)
            assertEquals(PRIVATE_VRS[i],
                    ElementDictionary.vrOf(PRIVATE_TAGS[i], PRIVATE_CREATOR));
    }

    @Test
    public void testPrivateKeywordOf() {
        for (int i = 0; i < PRIVATE_TAGS.length; i++)
            assertEquals(PRIVATE_KEYWORDS[i],
                         ElementDictionary.keywordOf(PRIVATE_TAGS[i], 
                                                     PRIVATE_CREATOR));
    }

    @Test
    public void tagPrivateForKeyword() {
        for (int i = 0; i < PRIVATE_KEYWORDS.length; i++)
            assertEquals(PRIVATE_TAGS[i],
                         ElementDictionary.tagForKeyword(PRIVATE_KEYWORDS[i],
                                                         PRIVATE_CREATOR));
    }
}
