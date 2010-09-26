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
            Tag.OverlayData + 0x00020000,
            0x00080000,
            0x00290010,
            0x00291008,
            0x00080002
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
            VR.OW,
            VR.UL,
            VR.LO,
            VR.UN,
            VR.UN
    };

    private static final String[] NAMES = {
            "Command Group Length",
            "Data Set Type",
            "File Meta Information Group Length",
            "Transfer Syntax UID",
            "Specific Character Set",
            "Waveform Data",
            "Pixel Data",
            "Overlay Data",
            "Overlay Data",
            "Group Length",
            "Private Creator",
            "Private Attribute",
            "Unknown Attribute"
    };
 
    private static final String PRIVATE_CREATOR = "SIEMENS CSA HEADER";

    private static final String[] PRIVATE_KEYWORDS = {
            "CSAImageHeaderType",
            "CSAImageHeaderVersion",
            "CSAImageHeaderInfo",
            "CSASeriesHeaderType",
            "CSASeriesHeaderVersion",
            "CSASeriesHeaderInfo"
    };

    private static final int[] PRIVATE_TAGS = {
            0x00290008,
            0x00290009,
            0x00290010,
            0x00290018,
            0x00290019,
            0x00290020,
            0x00290021,
    };

    private static final VR[] PRIVATE_VRS = {
            VR.CS,
            VR.LO,
            VR.OB,
            VR.CS,
            VR.LO,
            VR.OB,
            VR.UN,
    };

    private static final String[] PRIVATE_NAMES = {
            "CSA Image Header Type",
            "CSA Image Header Version",
            "CSA Image Header Info",
            "CSA Series Header Type",
            "CSA Series Header Version",
            "CSA Series Header Info",
            "Unknown Attribute"
    };

    private static final String[] KEYWORDS = {
            "CommandGroupLength",
            "CommandDataSetType",
            "FileMetaInformationGroupLength",
            "TransferSyntaxUID",
            "SpecificCharacterSet",
            "WaveformData",
            "PixelData",
            "OverlayData"
    };

    @Test
    public void testVrOf() {
        for (int i = 0; i < TAGS.length; i++)
            assertEquals(VRS[i],
                    ElementDictionary.vrOf(TAGS[i], null));
    }

    @Test
    public void testNameOf() {
        for (int i = 0; i < TAGS.length; i++)
            assertEquals(NAMES[i],
                    ElementDictionary.nameOf(TAGS[i], null));
    }

    @Test
    public void tagForName() {
        for (int i = 0; i < KEYWORDS.length; i++)
            assertEquals(TAGS[i],
                    ElementDictionary.tagForName(KEYWORDS[i], null));
    }

    @Test
    public void testPrivateVrOf() {
        for (int i = 0; i < PRIVATE_TAGS.length; i++)
            assertEquals(PRIVATE_VRS[i],
                    ElementDictionary.vrOf(PRIVATE_TAGS[i], PRIVATE_CREATOR));
    }

    @Test
    public void testPrivateNameOf() {
        for (int i = 0; i < PRIVATE_TAGS.length; i++)
            assertEquals(PRIVATE_NAMES[i],
                    ElementDictionary.nameOf(PRIVATE_TAGS[i], PRIVATE_CREATOR));
    }

    @Test
    public void tagPrivateForName() {
        for (int i = 0; i < PRIVATE_KEYWORDS.length; i++)
            assertEquals(PRIVATE_TAGS[i],
                    ElementDictionary.tagForName(PRIVATE_KEYWORDS[i], PRIVATE_CREATOR));
    }
}
