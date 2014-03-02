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

import org.dcm4che3.soundex.KPhonetik;
import org.junit.Test;

public class KPhoneticTest {

    private String getKPhoneticString(String arg){
        KPhonetik inst = new KPhonetik();
        return inst.toFuzzy(arg);
    }
    
    protected void checkEncodings(String[][] data) throws Exception {
        for (int i = 0; i < data.length; i++) {
            checkEncoding(data[i][1], data[i][0]);
        }
    }
    
    public void checkEncoding(String expected, String source) throws Exception {
        assertEquals("Source: " + source, expected, getKPhoneticString(source));
    }

    @Test
    public void testAabjoe() throws Exception {
        checkEncoding("01", "Aabjoe");
    }

    @Test
    public void testAaclan() throws Exception {
        checkEncoding("0856", "Aaclan");
    }

    @Test
    public void testEdgeCases() throws Exception {
        String[][] data = {
            {"a", "0"},
            {"e", "0"},
            {"i", "0"},
            {"o", "0"},
            {"u", "0"},
            {"\u00E4", "0"},
            {"\u00F6", "0"},
            {"\u00FC", "0"},
            {"aa", "0"},
            {"ha", "0"},
            {"h", ""},
            {"aha", "0"},
            {"b", "1"},
            {"p", "1"},
            {"ph", "3"},
            {"f", "3"},
            {"v", "3"},
            {"w", "3"},
            {"g", "4"},
            {"k", "4"},
            {"q", "4"},
            {"x", "48"},
            {"ax", "048"},
            {"cx", "48"},
            {"l", "5"},
            {"cl", "45"},
            {"acl", "085"},
            {"mn", "6"},
            {"r", "7"}};
        checkEncodings(data);
    }

    @Test
    public void testExamples() throws Exception {
        String[][] data = {
            {"m\u00DCller", "657"},
            {"schmidt", "862"},
            {"schneider", "8627"},
            {"fischer", "387"},
            {"weber", "317"},
            {"wagner", "3467"},
            {"becker", "147"},
            {"hoffmann", "0366"},
            {"sch\u00C4fer", "837"},
            {"Breschnew", "17863"},
            {"Wikipedia", "3412"},
            {"peter", "127"},
            {"pharma", "376"},
            {"mönchengladbach", "664645214"},
            {"deutsch", "28"},
            {"deutz", "28"},
            {"hamburg", "06174"},
            {"hannover", "0637"},
            {"christstollen", "478256"},
            {"Xanthippe", "48621"},
            {"Zacharias", "8478"},
            {"Holzbau", "0581"},
            {"matsch", "68"},
            {"matz", "68"},
            {"Arbeitsamt", "071862"},
            {"Eberhard", "01772"},
            {"Eberhardt", "01772"},
            {"heithabu", "021"}};
        checkEncodings(data);
    }

    @Test
    public void testHyphen() throws Exception {
        String[][] data = {{"bergisch-gladbach", "174845214"}, {"Müller-Lüdenscheidt", "65752682"},
            // From the Javadoc example:
            {"M�ller-L�denscheidt", "65752682"}};
        checkEncodings(data);
    }

    @Test
    public void testIsEncodeEquals() {
        String[][] data = {
            {"Meyer", "Mayr"},
            {"house", "house"},
            {"House", "house"},
            {"Haus", "house"},
            {"ganz", "Gans"},
            {"ganz", "Gänse"},
            {"Miyagi", "Miyako"}};
        for (int i = 0; i < data.length; i++) {
            assertEquals(getKPhoneticString(data[i][1]), getKPhoneticString(data[i][0]));
        }
    }

    @Test
    public void testVariationsMella() throws Exception {
        String data[] = {"mella", "milah", "moulla", "mellah", "muehle", "mule"};
        checkEncodingVariations("65", data);
    }

    @Test
    public void testVariationsMeyer() throws Exception {
        String data[] = {"Meier", "Maier", "Mair", "Meyer", "Meyr", "Mejer", "Major"};
        checkEncodingVariations("67", data);
    }
    
    protected void checkEncodingVariations(String expected, String data[]) throws Exception {
        for (int i = 0; i < data.length; i++) {
            checkEncoding(expected, data[i]);
        }
    }
}
