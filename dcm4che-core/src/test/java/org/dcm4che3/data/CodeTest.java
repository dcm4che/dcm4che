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


import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
public class CodeTest {

	private static final String DESGNATOR = "dcm4chee";
	private static final String CODE_VALUE_SHORT = "short";
	private static final String CODE_VALUE_LONG = "longCodeValueExample";
	private static final String CODE_VALUE_URN = "urn:test:12345aefde";
	private static final String CODE_VALUE_URL = "http://codeserver/code/test";
	private static final String MEANING_SHORT = "Standard short codevalue";
	private static final String MEANING_LONG = "long codevalue";
	private static final String MEANING_URN = "URN codevalue";
	private static final String MEANING_URL = "URL codevalue";
	private static final String VERSION_LONG = "1.1";
	private static final String VERSION_URN = "1.2";

	private static final Attributes CODE_ITEM_STANDARD = getCodeItem(CODE_VALUE_SHORT, DESGNATOR, MEANING_SHORT, null, Code.CodeValueType.SHORT);
	private static final Attributes CODE_ITEM_LONG = getCodeItem(CODE_VALUE_LONG, DESGNATOR, MEANING_LONG, VERSION_LONG, Code.CodeValueType.LONG);
	private static final Attributes CODE_ITEM_URN = getCodeItem(CODE_VALUE_URN, DESGNATOR, MEANING_URN, VERSION_URN, Code.CodeValueType.URN);
	private static final Attributes CODE_ITEM_URL = getCodeItem(CODE_VALUE_URL, DESGNATOR, MEANING_URL, null, Code.CodeValueType.URN);

	@Test
    public void testStandardCodeFromItem() {
		Code code = new Code(CODE_ITEM_STANDARD);
        assertEquals("CodeValue", CODE_VALUE_SHORT, code.getCodeValue());
        assertEquals("Designator", DESGNATOR, code.getCodingSchemeDesignator());
        assertEquals("Meaning", MEANING_SHORT, code.getCodeMeaning());
        assertEquals("Version",null, code.getCodingSchemeVersion());
        assertEquals("CodeValueType", Code.CodeValueType.SHORT, code.getCodeValueType());
    }

	@Test
    public void testLongCodeFromItem() {
		Code code = new Code(CODE_ITEM_LONG);
        assertEquals("CodeValue", CODE_VALUE_LONG, code.getCodeValue());
        assertEquals("Designator", DESGNATOR, code.getCodingSchemeDesignator());
        assertEquals("Meaning", MEANING_LONG, code.getCodeMeaning());
        assertEquals("Version",VERSION_LONG, code.getCodingSchemeVersion());
        assertEquals("CodeValueType", Code.CodeValueType.LONG, code.getCodeValueType());
    }
    
	@Test
    public void testUrnCodeFromItem() {
		Code code = new Code(CODE_ITEM_URN);
        assertEquals("CodeValue", CODE_VALUE_URN, code.getCodeValue());
        assertEquals("Designator", DESGNATOR, code.getCodingSchemeDesignator());
        assertEquals("Meaning", MEANING_URN, code.getCodeMeaning());
        assertEquals("Version", VERSION_URN, code.getCodingSchemeVersion());
        assertEquals("CodeValueType", Code.CodeValueType.URN, code.getCodeValueType());
    }
    
	@Test
    public void testUrlCodeFromItem() {
		Code code = new Code(CODE_ITEM_URL);
        assertEquals("CodeValue",CODE_VALUE_URL, code.getCodeValue());
        assertEquals("Designator",DESGNATOR, code.getCodingSchemeDesignator());
        assertEquals("Meaning",MEANING_URL, code.getCodeMeaning());
        assertEquals("Version",null, code.getCodingSchemeVersion());
        assertEquals("CodeValueType", Code.CodeValueType.URN, code.getCodeValueType());
    }

	@Test(expected = NullPointerException.class)
    public void testMissingCodeValueFromItem() {
		new Code(new Attributes(CODE_ITEM_URL, Tag.CodingSchemeDesignator, Tag.CodeMeaning));
    }
	
	@Test(expected = NullPointerException.class)
    public void testMissingDesignatorFromItem() {
		new Code(new Attributes(CODE_ITEM_URL, Tag.CodeValue, Tag.CodeMeaning));
    }
	
	@Test(expected = NullPointerException.class)
    public void testMissingCodeMeaningFromItem() {
		new Code(new Attributes(CODE_ITEM_URL, Tag.CodeValue, Tag.CodingSchemeDesignator));
    }

	@Test
    public void testStandardCodeToItem() {
		Code code = new Code(CODE_VALUE_SHORT, DESGNATOR, null, MEANING_SHORT, Code.CodeValueType.SHORT);
		assertEquals("CodeItem short", CODE_ITEM_STANDARD, code.toItem());
    }

	@Test
    public void testLongCodeToItem() {
		Code code = new Code(CODE_VALUE_LONG, DESGNATOR, VERSION_LONG, MEANING_LONG, Code.CodeValueType.LONG);
		assertEquals("CodeItem long", CODE_ITEM_LONG, code.toItem());
    }

	@Test
    public void testUrnCodeToItem() {
		Code code = new Code(CODE_VALUE_URN, DESGNATOR, VERSION_URN, MEANING_URN, Code.CodeValueType.URN);
		assertEquals("CodeItem urn", CODE_ITEM_URN, code.toItem());
    }

	@Test
    public void testUrlCodeToItem() {
		Code code = new Code(CODE_VALUE_URL, DESGNATOR, null, MEANING_URL, Code.CodeValueType.URN);
		assertEquals("CodeItem url", CODE_ITEM_URL, code.toItem());
    }

	@Test(expected = NullPointerException.class)
    public void testMissingCodeValueType() {
		new Code(CODE_VALUE_URN, DESGNATOR, VERSION_URN, MEANING_URN, null);
    }

	@Test
    public void testGuessCodeValueType() {
		Code code = new Code(CODE_VALUE_SHORT, DESGNATOR, null, MEANING_URL);
		assertEquals("CodeValueType for "+CODE_VALUE_SHORT, Code.CodeValueType.SHORT, code.getCodeValueType());
		code = new Code(CODE_VALUE_LONG, DESGNATOR, null, MEANING_LONG);
		assertEquals("CodeValueType for "+CODE_VALUE_LONG, Code.CodeValueType.LONG, code.getCodeValueType());
		code = new Code(CODE_VALUE_URN, DESGNATOR, null, MEANING_URN);
		assertEquals("CodeValueType for "+CODE_VALUE_URN, Code.CodeValueType.URN, code.getCodeValueType());
		code = new Code(CODE_VALUE_URL, DESGNATOR, null, MEANING_URL);
		assertEquals("CodeValueType for "+CODE_VALUE_URL, Code.CodeValueType.URN, code.getCodeValueType());
		code = new Code("urn:short:abc", DESGNATOR, null, "Short URL");
		assertEquals("CodeValueType for urn:short:abc", Code.CodeValueType.SHORT, code.getCodeValueType());
    }

	@Test
    public void testCodeFromToStringWithVersion() {
		String codeString = new Code(CODE_ITEM_LONG).toString();
		Code code = new Code(codeString);
		assertEquals("CodeItem of Code(String)", CODE_ITEM_LONG, code.toItem());
	}
	
	@Test
    public void testCodeFromToStringWithoutVersion() {
		String codeString = new Code(CODE_ITEM_STANDARD).toString();
		Code code = new Code(codeString);
		assertEquals("CodeItem of Code(String)", CODE_ITEM_STANDARD, code.toItem());
	}

	private static Attributes getCodeItem(String value, String designator, String meaning, String version, Code.CodeValueType type) {
    	Attributes codeItem = new Attributes(4);
    	switch (type) {
    	case SHORT:
    		codeItem.setString(Tag.CodeValue, VR.SH, value); break;
    	case LONG:
    		codeItem.setString(Tag.LongCodeValue, VR.UC, value); break;
    	case URN:
    		codeItem.setString(Tag.URNCodeValue, VR.UR, value); break;
    	}
    	codeItem.setString(Tag.CodingSchemeDesignator, VR.SH, designator);
    	codeItem.setString(Tag.CodeMeaning, VR.LO, meaning);
    	if (version != null) {
    		codeItem.setString(Tag.CodingSchemeVersion, VR.SH, version);
    	}
    	return codeItem;
    }

}
