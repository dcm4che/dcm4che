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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.dcm4che3.soundex.Metaphone;
import org.junit.Test;

public class MetaphoneTest {

    public void assertIsMetaphoneEqual(String source, String[] matches) {
        // match source to all matches
        for (int i = 0; i < matches.length; i++) {
            assertTrue("Source: " + source + " , should have same Metaphone as: " + matches[i],
                       getMetaphone(source).equals(getMetaphone(matches[i])));
        }
        // match to each other
        for (int i = 0; i < matches.length; i++) {
            for (int j = 0; j < matches.length; j++) {
                assertTrue(getMetaphone(matches[i]).equals(getMetaphone(matches[j])));
            }
        }
    }

    public void assertMetaphoneEqual(String[][] pairs) {
        validateFixture(pairs);
        for (int i = 0; i < pairs.length; i++) {
            String name0 = pairs[i][0];
            String name1 = pairs[i][1];
            String failMsg = "Expected match between " + name0 + " and " + name1;
            assertTrue(failMsg, getMetaphone(name0).equals(getMetaphone(name1)));
            assertTrue(failMsg, getMetaphone(name1).equals(getMetaphone(name0)));
        }
    }
    
    /**
     * @return Returns the metaphone.
     */
    private String getMetaphone(String arg) {
        Metaphone inst = new Metaphone();
        return inst.toFuzzy(arg);
    }

    @Test
    public void testIsMetaphoneEqual1() {
        assertMetaphoneEqual(new String[][] { { "Case", "case" }, {
                "CASE", "Case" }, {
                "caSe", "cAsE" }, {
                "quick", "cookie" }
        });
    }

    /**
     * Matches computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqual2() {
        assertMetaphoneEqual(new String[][] { { "Lawrence", "Lorenza" }, {
                "Gary", "Cahra" }, });
    }

    /**
     * Initial AE case.
     * 
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualAero() {
        assertIsMetaphoneEqual("Aero", new String[] { "Eure" });
    }

    /**
     * Initial WH case.
     * 
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualWhite() {
        assertIsMetaphoneEqual(
            "White",
            new String[] { "Wade", "Wait", "Waite", "Wat", "Whit", "Wiatt", "Wit", "Wittie", "Witty", "Wood", "Woodie", "Woody" });
    }

    /**
     * Initial A, not followed by an E case.
     * 
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualAlbert() {
        assertIsMetaphoneEqual("Albert", new String[] { "Ailbert", "Albert", "Alberto"});
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualGary() {
        assertIsMetaphoneEqual(
            "Gary",
            new String[] {
                "Cahra",
                "Cara",
                "Carey",
                "Cari",
                "Caria",
                "Carie",
                "Caro",
                "Carree",
                "Carri",
                "Carrie",
                "Carry",
                "Cary",
                "Cora",
                "Corey",
                "Cori",
                "Corie",
                "Correy",
                "Corri",
                "Corrie",
                "Corry",
                "Cory",
                "Gray",
                "Kara",
                "Kare",
                "Karee",
                "Kari",
                "Karia",
                "Karie",
                "Karrah",
                "Karrie",
                "Karry",
                "Kary",
                "Keri",
                "Kerri",
                "Kerrie",
                "Kerry",
                "Kira",
                "Kiri",
                "Kora",
                "Kore",
                "Kori",
                "Korie",
                "Korrie",
                "Korry" });
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualJohn() {
        assertIsMetaphoneEqual(
            "John",
            new String[] {
                "Gena",
                "Gene",
                "Genia",
                "Genna",
                "Genni",
                "Gennie",
                "Genny",
                "Giana",
                "Gianna",
                "Gina",
                "Ginni",
                "Ginnie",
                "Ginny",
                "Jaine",
                "Jan",
                "Jana",
                "Jane",
                "Janey",
                "Jania",
                "Janie",
                "Janna",
                "Jany",
                "Jayne",
                "Jean",
                "Jeana",
                "Jeane",
                "Jeanie",
                "Jeanna",
                "Jeanne",
                "Jeannie",
                "Jen",
                "Jena",
                "Jeni",
                "Jenn",
                "Jenna",
                "Jennee",
                "Jenni",
                "Jennie",
                "Jenny",
                "Jinny",
                "Jo Ann",
                "Jo-Ann",
                "Jo-Anne",
                "Joan",
                "Joana",
                "Joane",
                "Joanie",
                "Joann",
                "Joanna",
                "Joanne",
                "Joeann",
                "Johna",
                "Johnna",
                "Joni",
                "Jonie",
                "Juana",
                "June",
                "Junia",
                "Junie" });
    }

    /**
     * Initial KN case.
     * 
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualKnight() {
        assertIsMetaphoneEqual(
            "Knight",
            new String[] {
                "Nada",
                "Nadia",
                "Nady",
                "Nat",
                "Nata",
                "Natty",
                "Neda",
                "Nedda",
                "Nedi",
                "Netta",
                "Netti",
                "Nettie",
                "Netty",
                "Nita",
                "Nydia" });
    }
    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualMary() {
        assertIsMetaphoneEqual(
            "Mary",
            new String[] {
                "Mair",
                "Maire",
                "Mara",
                "Mareah",
                "Mari",
                "Maria",
                "Marie",
                "Mary",
                "Maura",
                "Maure",
                "Meara",
                "Merrie",
                "Merry",
                "Mira",
                "Moira",
                "Mora",
                "Moria",
                "Moyra",
                "Muire",
                "Myra",
                "Myrah" });
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualParis() {
        assertIsMetaphoneEqual("Paris", new String[] { "Pearcy", "Perris", "Piercy", "Pierz", "Pryse" });
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualPeter() {
        assertIsMetaphoneEqual(
            "Peter",
            new String[] { "Peadar", "Peder", "Pedro", "Peter", "Petr", "Peyter", "Pieter", "Pietro", "Piotr" });
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualRay() {
        assertIsMetaphoneEqual("Ray", new String[] { "Ray", "Rey", "Roi", "Roy", "Ruy" });
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualSusan() {
        assertIsMetaphoneEqual(
            "Susan",
            new String[] {
                "Siusan",
                "Sosanna",
                "Susan",
                "Susana",
                "Susann",
                "Susanna",
                "Susannah",
                "Susanne",
                "Suzann",
                "Suzanna",
                "Suzanne",
                "Zuzana" });
    }

    /**
     * Initial WR case.
     * 
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualWright() {
        assertIsMetaphoneEqual("Wright", new String[] { "Rota", "Rudd", "Ryde" });
    }

    /**
     * Match data computed from http://www.lanw.com/java/phonetic/default.htm
     */
    @Test
    public void testIsMetaphoneEqualXalan() {
        assertIsMetaphoneEqual(
            "Xalan",
            new String[] { "Celene", "Celina", "Celine", "Selena", "Selene", "Selina", "Seline", "Suellen", "Xylina" });
    }

    @Test
    public void testMetaphone() {
        assertEquals("HL", getMetaphone("howl"));
        assertEquals("0", getMetaphone("The"));
        assertEquals("KK", getMetaphone("quick"));
        assertEquals("BRN", getMetaphone("brown"));
        assertEquals("FKS", getMetaphone("fox"));
        assertEquals("JMPT", getMetaphone("jumped"));
        assertEquals("OFR", getMetaphone("over"));
        assertEquals("0", getMetaphone("the"));
        assertEquals("LS", getMetaphone("lazy"));
        assertEquals("TKS", getMetaphone("dogs"));
    }
    
    @Test
    public void testWordEndingInMB() {
        assertEquals( "KM", getMetaphone("COMB") );
        assertEquals( "TM", getMetaphone("TOMB") );
        assertEquals( "WM", getMetaphone("WOMB") );
    }

    @Test
    public void testDiscardOfSCEOrSCIOrSCY() {
        assertEquals( "SNS", getMetaphone("SCIENCE") );
        assertEquals( "SN", getMetaphone("SCENE") );
        assertEquals( "S", getMetaphone("SCY") );
    }

    /**
     * Tests (CODEC-57) Metaphone.metaphone(String) returns an empty string when passed the word "why"
     */
    @Test
    public void testWhy() {
        // PHP returns "H". The original metaphone returns an empty string. 
        assertEquals("", getMetaphone("WHY"));
    }

    @Test
    public void testWordsWithCIA() {
        assertEquals( "XP", getMetaphone("CIAPO") );
    }

    @Test
    public void testTranslateOfSCHAndCH() {
        assertEquals( "SKTL", getMetaphone("SCHEDULE") );
        assertEquals( "SKMTK", getMetaphone("SCHEMATIC") );
        assertEquals( "XRKTR", getMetaphone("CHARACTER") );
        assertEquals( "TX", getMetaphone("TEACH") );
    }

    @Test
    public void testTranslateToJOfDGEOrDGIOrDGY() {
        assertEquals( "TJ", getMetaphone("DODGY") );
        assertEquals( "TJ", getMetaphone("DODGE") );
        assertEquals( "AJMT", getMetaphone("ADGIEMTI") );
    }

    @Test
    public void testDiscardOfSilentHAfterG() {
        assertEquals( "KNT", getMetaphone("GHENT") );
        assertEquals( "BK", getMetaphone("BAUGH") );
    }

    @Test
    public void testDiscardOfSilentGN() {
        // NOTE: This does not test for silent GN, but for starting with GN
        assertEquals( "N", getMetaphone("GNU") );

        // NOTE: Trying to test for GNED, but expected code does not appear to execute
        assertEquals( "SKNT", getMetaphone("SIGNED") );
    }

    @Test
    public void testPHTOF() {
        assertEquals( "FX", getMetaphone("PHISH") );
    }

    @Test
    public void testSHAndSIOAndSIAToX() {
        assertEquals( "XT", getMetaphone("SHOT") );
        assertEquals( "OTXN", getMetaphone("ODSIAN") );
        assertEquals( "PLXN", getMetaphone("PULSION") );
    }

    @Test
    public void testTIOAndTIAToX() {
        assertEquals( "OX", getMetaphone("OTIA") );
        assertEquals( "PRXN", getMetaphone("PORTION") );
    }

    @Test
    public void testTCH() {
        assertEquals( "RX", getMetaphone("RETCH") );
        assertEquals( "WX", getMetaphone("WATCH") );
    }

    public void validateFixture(String[][] pairs) {
        if (pairs.length == 0) {
            fail("Test fixture is empty");
        }
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i].length != 2) {
                fail("Error in test fixture in the data array at index " + i);
            }
        }
    }

}
