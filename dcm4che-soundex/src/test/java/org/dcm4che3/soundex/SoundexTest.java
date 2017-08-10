/* ***** BEGIN LICENSE BLOCK *****
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * The original code is from the Apache commons-codec-1.5-src package
 * and has been modified for the dcm4che project
 * 
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.soundex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import junit.framework.Assert;

import org.dcm4che3.soundex.Soundex;
import org.junit.Test;

public class SoundexTest {

    public String getSoundexEncoderString(String arg) {
        Soundex inst = new Soundex();
        return inst.toFuzzy(arg);
    }

    protected void checkEncodingVariations(String expected, String data[]) throws Exception {
        for (int i = 0; i < data.length; i++) {
            checkEncoding(expected, data[i]);
        }
    }
    
    public void checkEncoding(String expected, String source) throws Exception {
        assertEquals("Source: " + source, expected, getSoundexEncoderString(source));
    }
    
    @Test
    public void testB650() throws Exception {
        checkEncodingVariations("B650", (new String[]{
            "BARHAM",
            "BARONE",
            "BARRON",
            "BERNA",
            "BIRNEY",
            "BIRNIE",
            "BOOROM",
            "BOREN",
            "BORN",
            "BOURN",
            "BOURNE",
            "BOWRON",
            "BRAIN",
            "BRAME",
            "BRANN",
            "BRAUN",
            "BREEN",
            "BRIEN",
            "BRIM",
            "BRIMM",
            "BRINN",
            "BRION",
            "BROOM",
            "BROOME",
            "BROWN",
            "BROWNE",
            "BRUEN",
            "BRUHN",
            "BRUIN",
            "BRUMM",
            "BRUN",
            "BRUNO",
            "BRYAN",
            "BURIAN",
            "BURN",
            "BURNEY",
            "BYRAM",
            "BYRNE",
            "BYRON",
            "BYRUM"}));
    }

    public void testBadCharacters() {
        Assert.assertEquals("H452", getSoundexEncoderString("HOL>MES"));

    }

    @Test
    public void testEncodeBasic() {
        Assert.assertEquals("T235", getSoundexEncoderString("testing"));
        Assert.assertEquals("T000", getSoundexEncoderString("The"));
        Assert.assertEquals("Q200", getSoundexEncoderString("quick"));
        Assert.assertEquals("B650", getSoundexEncoderString("brown"));
        Assert.assertEquals("F200", getSoundexEncoderString("fox"));
        Assert.assertEquals("J513", getSoundexEncoderString("jumped"));
        Assert.assertEquals("O160", getSoundexEncoderString("over"));
        Assert.assertEquals("T000", getSoundexEncoderString("the"));
        Assert.assertEquals("L200", getSoundexEncoderString("lazy"));
        Assert.assertEquals("D200", getSoundexEncoderString("dogs"));
    }

    /**
     * Examples from http://www.bradandkathy.com/genealogy/overviewofsoundex.html
     */
    @Test
    public void testEncodeBatch2() {
        Assert.assertEquals("A462", getSoundexEncoderString("Allricht"));
        Assert.assertEquals("E166", getSoundexEncoderString("Eberhard"));
        Assert.assertEquals("E521", getSoundexEncoderString("Engebrethson"));
        Assert.assertEquals("H512", getSoundexEncoderString("Heimbach"));
        Assert.assertEquals("H524", getSoundexEncoderString("Hanselmann"));
        Assert.assertEquals("H431", getSoundexEncoderString("Hildebrand"));
        Assert.assertEquals("K152", getSoundexEncoderString("Kavanagh"));
        Assert.assertEquals("L530", getSoundexEncoderString("Lind"));
        Assert.assertEquals("L222", getSoundexEncoderString("Lukaschowsky"));
        Assert.assertEquals("M235", getSoundexEncoderString("McDonnell"));
        Assert.assertEquals("M200", getSoundexEncoderString("McGee"));
        Assert.assertEquals("O155", getSoundexEncoderString("Opnian"));
        Assert.assertEquals("O155", getSoundexEncoderString("Oppenheimer"));
        Assert.assertEquals("R355", getSoundexEncoderString("Riedemanas"));
        Assert.assertEquals("Z300", getSoundexEncoderString("Zita"));
        Assert.assertEquals("Z325", getSoundexEncoderString("Zitzmeinn"));
    }

    /**
     * Examples from http://www.archives.gov/research_room/genealogy/census/soundex.html
     */
    @Test
    public void testEncodeBatch3() {
        Assert.assertEquals("W252", getSoundexEncoderString("Washington"));
        Assert.assertEquals("L000", getSoundexEncoderString("Lee"));
        Assert.assertEquals("G362", getSoundexEncoderString("Gutierrez"));
        Assert.assertEquals("P236", getSoundexEncoderString("Pfister"));
        Assert.assertEquals("J250", getSoundexEncoderString("Jackson"));
        Assert.assertEquals("T522", getSoundexEncoderString("Tymczak"));
        // For VanDeusen: D-250 (D, 2 for the S, 5 for the N, 0 added) is also
        // possible.
        Assert.assertEquals("V532", getSoundexEncoderString("VanDeusen"));
    }

    /**
     * Examples from: http://www.myatt.demon.co.uk/sxalg.htm
     */
    @Test
    public void testEncodeBatch4() {
        Assert.assertEquals("H452", getSoundexEncoderString("HOLMES"));
        Assert.assertEquals("A355", getSoundexEncoderString("ADOMOMI"));
        Assert.assertEquals("V536", getSoundexEncoderString("VONDERLEHR"));
        Assert.assertEquals("B400", getSoundexEncoderString("BALL"));
        Assert.assertEquals("S000", getSoundexEncoderString("SHAW"));
        Assert.assertEquals("J250", getSoundexEncoderString("JACKSON"));
        Assert.assertEquals("S545", getSoundexEncoderString("SCANLON"));
        Assert.assertEquals("S532", getSoundexEncoderString("SAINTJOHN"));

    }

    @Test
    public void testEncodeIgnoreApostrophes() throws Exception {
        checkEncodingVariations("O165", (new String[]{
            "OBrien",
            "'OBrien",
            "O'Brien",
            "OB'rien",
            "OBr'ien",
            "OBri'en",
            "OBrie'n",
            "OBrien'"}));
    }

    @Test
    public void testEncodeIgnoreHyphens() throws Exception {
        checkEncodingVariations("K525", (new String[]{
            "KINGSMITH",
            "-KINGSMITH",
            "K-INGSMITH",
            "KI-NGSMITH",
            "KIN-GSMITH",
            "KINGS-MITH",
            "KINGSM-ITH",
            "KINGSMI-TH",
            "KINGSMIT-H",
            "KINGSMITH-"}));
        assertFalse("Source: KING-SMITH", getSoundexEncoderString("KING-SMITH").equals("K525"));
    }

    @Test
    public void testEncodeIgnoreTrimmable() {
        Assert.assertEquals("W252", getSoundexEncoderString(" \t\n\r Washington \t\n\r "));
    }

    /**
     * Consonants from the same code group separated by W or H are treated as one.
     */
    @Test
    public void testHWRuleEx1() {
        // From
        // http://www.archives.gov/research_room/genealogy/census/soundex.html:
        // Ashcraft is coded A-261 (A, 2 for the S, C ignored, 6 for the R, 1
        // for the F). It is not coded A-226.
        Assert.assertEquals("A261", getSoundexEncoderString("Ashcraft"));
    }

    /**
     * Consonants from the same code group separated by W or H are treated as one.
     * 
     * Test data from http://www.myatt.demon.co.uk/sxalg.htm
     */
    @Test
    public void testHWRuleEx2() {
        Assert.assertEquals("B312", getSoundexEncoderString("BOOTHDAVIS"));
        Assert.assertNotSame("B312", getSoundexEncoderString("BOOTH-DAVIS"));
    }

    /**
     * Consonants from the same code group separated by W or H are treated as one.
     * 
     * @throws Exception
     */
    @Test
    public void testHWRuleEx3() throws Exception {
        Assert.assertEquals("S460", getSoundexEncoderString("Sgler"));
        Assert.assertEquals("S460", getSoundexEncoderString("Swhgler"));
        // Also S460:
        checkEncodingVariations("S460", (new String[]{
            "SAILOR",
            "SALYER",
            "SAYLOR",
            "SCHALLER",
            "SCHELLER",
            "SCHILLER",
            "SCHOOLER",
            "SCHULER",
            "SCHUYLER",
            "SEILER",
            "SEYLER",
            "SHOLAR",
            "SHULER",
            "SILAR",
            "SILER",
            "SILLER"}));
    }

    @Test
    public void testMaxLength() throws Exception {
//        Soundex soundex = new Soundex();
//        soundex.setMaxLength(soundex.getMaxLength());
        Assert.assertEquals("S460", getSoundexEncoderString("Sgler"));
    }

    @Test
    public void testMaxLengthLessThan3Fix() throws Exception {
//        Soundex soundex = new Soundex();
//        soundex.setMaxLength(2);
        Assert.assertEquals("S460", getSoundexEncoderString("SCHELLER"));
    }

    /**
     * Examples for MS SQLServer from
     * http://msdn.microsoft.com/library/default.asp?url=/library/en-us/tsqlref/ts_setu-sus_3o6w.asp
     */
    @Test
    public void testMsSqlServer1() {
        Assert.assertEquals("S530", getSoundexEncoderString("Smith"));
        Assert.assertEquals("S530", getSoundexEncoderString("Smythe"));
    }

    /**
     * Examples for MS SQLServer from
     * http://support.microsoft.com/default.aspx?scid=http://support.microsoft.com:80/support
     * /kb/articles/Q100/3/65.asp&NoWebContent=1
     * 
     * @throws Exception
     */
    @Test
    public void testMsSqlServer2() throws Exception {
        checkEncodingVariations("E625", (new String[]{"Erickson", "Erickson", "Erikson", "Ericson", "Ericksen", "Ericsen"}));
    }

    /**
     * Examples for MS SQLServer from http://databases.about.com/library/weekly/aa042901a.htm
     */
    @Test
    public void testMsSqlServer3() {
        Assert.assertEquals("A500", getSoundexEncoderString("Ann"));
        Assert.assertEquals("A536", getSoundexEncoderString("Andrew"));
        Assert.assertEquals("J530", getSoundexEncoderString("Janet"));
        Assert.assertEquals("M626", getSoundexEncoderString("Margaret"));
        Assert.assertEquals("S315", getSoundexEncoderString("Steven"));
        Assert.assertEquals("M240", getSoundexEncoderString("Michael"));
        Assert.assertEquals("R163", getSoundexEncoderString("Robert"));
        Assert.assertEquals("L600", getSoundexEncoderString("Laura"));
        Assert.assertEquals("A500", getSoundexEncoderString("Anne"));
    }

    /**
     * Fancy characters are not mapped by the default US mapping.
     * 
     * http://issues.apache.org/bugzilla/show_bug.cgi?id=29080
     */
    @Test
    public void testUsMappingEWithAcute() {
        Assert.assertEquals("E000", getSoundexEncoderString("e"));
        if (Character.isLetter('�')) {
            try {
                Assert.assertEquals("�000", getSoundexEncoderString("�"));
                Assert.fail("Expected IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
                // expected
            }
        } else {
            Assert.assertEquals("", getSoundexEncoderString("�"));
        }
    }

    /**
     * Fancy characters are not mapped by the default US mapping.
     * 
     * http://issues.apache.org/bugzilla/show_bug.cgi?id=29080
     */
    @Test
    public void testUsMappingOWithDiaeresis() {
        Assert.assertEquals("O000", getSoundexEncoderString("o"));
        if (Character.isLetter('�')) {
            try {
                Assert.assertEquals("�000", getSoundexEncoderString("�"));
                Assert.fail("Expected IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
                // expected
            }
        } else {
            Assert.assertEquals("", getSoundexEncoderString("�"));
        }
    }

    /**
     * http://www.dcm4che.org/jira/browse/DCMEEREQ-1298
     */
    @Test
    public void testDoubleNames() {
        Assert.assertEquals(getSoundexEncoderString("DA SILVA"), getSoundexEncoderString("DASILVA"));
    }
}
