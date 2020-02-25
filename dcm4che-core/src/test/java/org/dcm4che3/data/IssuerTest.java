/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class IssuerTest {
    // Correct Layout: localNamespaceEntityID & universalEntityID & universalEntityIDType
    public static final String NAMESPACE = "MyNamespace";
    public static final String ENTITY_ID = "MyEntityID";
    public static final String ENTITY_ID_TYPE = "MyEntityIDType";
    public static final String ALL3_WITHAMPERSANDS_UNESCAPED = NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE;
    public static final String ALL3_WITHAMPERSANDS_ESCAPED = NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE;

    @Test
    public void validatePositiveScenario_NamespaceProvidedOnly_ShouldParseNamespaceValueOnly() {
        Issuer issuer = new Issuer(NAMESPACE);
        validate(issuer, NAMESPACE, null, null);
    }

    @Test
    public void validatePositiveScenario_NamespaceProvidedWithOneTrailingAmpersand_ShouldParseNamespaceValueOnly() {
        Issuer issuer = new Issuer(NAMESPACE + "&");
        validate(issuer, NAMESPACE, null, null);
    }

    @Test
    public void validatePositiveScenario_NamespaceProvidedWithTwoTrailingAmpersands_ShouldParseNamespaceValueOnly() {
        Issuer issuer = new Issuer(NAMESPACE + "&&");
        validate(issuer, NAMESPACE, null, null);
    }

    @Test
    public void validatePositiveScenario_NoNamespaceProvidedButEntityIDAndEntityTypeIDProvided_ShouldAcceptInputAsValid() {
        Issuer issuer = new Issuer("&" + ENTITY_ID + "&" + ENTITY_ID_TYPE);
        validate(issuer, null, ENTITY_ID, ENTITY_ID_TYPE);

    }

    @Test
    public void validatePositiveScenario_NoNamespaceProvidedButEntityIDAndEntityTypeIDProvidedWithEscapedAmpersands_ShouldParseInputAsNamespaceValue() {
        // THIS IS A CHANGE IN BEHAVIOUR WITH THIS COMMIT !
        // The Issuer (as expected) does not interpret the escaped ampersands "\T\" as actual
        // ampersands and therefore simply sets the entire String into the Namespace property.
        Issuer issuer = new Issuer("\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE);
        validate(issuer, "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, null, null);
    }

    @Test
    public void validatePositiveScenario_TwoEscapedValuesProvidedInEntityIDFieldAndEntityTypeIDProvided_ShouldParseEntityIDAndEntityIDTypeValues() {
        // THIS IS A CHANGE IN BEHAVIOUR WITH THIS COMMIT !
        // No Namespace but includes a multi-part ampersand-escaped EntityID and EntityIDType
        Issuer issuer = new Issuer("&" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE + "&" + ENTITY_ID_TYPE);
        validate(issuer, null, ENTITY_ID + "&" + ENTITY_ID_TYPE, ENTITY_ID_TYPE);
    }

    @Test
    public void validatePositiveScenario_TwoEscapedValuesProvidedInNamespaceAlongWithEntityIDAndEntityTypeID_ShouldParseAllThreeValues() {
        // THIS IS A CHANGE IN BEHAVIOUR WITH THIS COMMIT !
        // A multi-part ampersand-escaped Namespace with EntityID and EntityIDType
        Issuer issuer = new Issuer("\\T\\" + NAMESPACE + "\\T\\" + ENTITY_ID + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE);
        validate(issuer, "&" + NAMESPACE + "&" + ENTITY_ID, ENTITY_ID, ENTITY_ID_TYPE);
    }

    @Test
    public void validatePositiveScenario_ThreeEscapedValuesProvidedInNamespaceField_ShouldParseNamespaceValueOnly() {
        // THIS IS A CHANGE IN BEHAVIOUR WITH THIS COMMIT !
        Issuer issuer = new Issuer(ALL3_WITHAMPERSANDS_ESCAPED);
        validate(issuer, NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, null, null);
    }

    @Test
    public void validatePositiveScenario_ThreeUnEscapedValuesProvided_ShouldParseAllThreeValues() {
        Issuer issuer = new Issuer(ALL3_WITHAMPERSANDS_UNESCAPED);
        validate(issuer, NAMESPACE, ENTITY_ID, ENTITY_ID_TYPE);
    }

    @Test
    public void validatePositiveScenario_ThreeEscapedValuesProvidedToMultiParmCtor_ShouldLeaveEscapedNamespaceValueAlone() {
        // THIS IS A CHANGE IN BEHAVIOUR WITH THIS COMMIT !
        Issuer issuer = new Issuer(ALL3_WITHAMPERSANDS_ESCAPED, null, null);
        validate(issuer, ALL3_WITHAMPERSANDS_ESCAPED, null, null);
    }

    @Test
    public void validatePositiveScenario_ThreeUnescapedValuesProvidedToMultiParmCtor_ShouldParseNamespaceValueOnly() {
        Issuer issuer = new Issuer(ALL3_WITHAMPERSANDS_UNESCAPED, null, null);
        validate(issuer, ALL3_WITHAMPERSANDS_UNESCAPED, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNegativeScenario_WhereNoDataProvided_ShouldThrowIllegalArgumentException() {
        // This one has no data at all inside it
        new Issuer("&&&");
        fail("Expected an IllegalArgumentException to be thrown!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNegativeScenario_WhereAllDataProvidedPlusExtraTrailingAmpersands_ShouldThrowIllegalArgumentException() {
        new Issuer(ALL3_WITHAMPERSANDS_UNESCAPED + "&&");
        fail("Expected an IllegalArgumentException to be thrown!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNegativeScenario_WhereNamespaceAndEntityIDProvided_ShouldThrowIllegalArgumentException() {
        new Issuer(NAMESPACE + "&" + ENTITY_ID);
        fail("Expected an IllegalArgumentException to be thrown!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNegativeScenario_WhereNamespaceAndEntityIDProvidedWithTrailingAmpersand_ShouldThrowIllegalArgumentException() {
        new Issuer(NAMESPACE + "&" + ENTITY_ID + "&");
        fail("Expected an IllegalArgumentException to be thrown!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateNegativeScenario_WhereOnlyEntityIDProvided_ShouldThrowIllegalArgumentException() {
        new Issuer("&" + ENTITY_ID + "&");
        fail("Expected an IllegalArgumentException to be thrown!");
    }

    private void validate(Issuer issuer, String expectedNamespace, String expectedEntityID,
            String expectedEntityIDType) {
        assertEquals("Issuer.localNamespaceEntityID is wrong!", expectedNamespace, issuer.getLocalNamespaceEntityID());
        assertEquals("Issuer.universalEntityID is wrong!", expectedEntityID, issuer.getUniversalEntityID());
        assertEquals("Issuer.universalEntityIDType is wrong!", expectedEntityIDType, issuer.getUniversalEntityIDType());
    }
}
