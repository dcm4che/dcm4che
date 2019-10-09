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
package org.dcm4che3.conf.dicom.adapters;

import static org.junit.Assert.assertEquals;

import org.dcm4che3.data.Issuer;
import org.junit.Test;

public class IssuerTypeAdapterTest {
    // Correct Layout: localNamespaceEntityID & universalEntityID & universalEntityIDType
    public static final String NAMESPACE = "MyNamespace";
    public static final String ENTITY_ID = "MyEntityID";
    public static final String ENTITY_ID_TYPE = "MyEntityIDType";
    public static final String ALL3_WITHAMPERSANDS_UNESCAPED = NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE;
    public static final String ALL3_WITHAMPERSANDS_ESCAPED = NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE;
    static final IssuerTypeAdapter issuerTypeAdapter = new IssuerTypeAdapter();

    // FromConfigNode Test Cases - This is the process of taking the serialized JSON from the DB and re-constituting
    // the Java object
    @Test
    public void testFromConfigNode_NamespaceValueProvidedOnlyWithTwoTrailingAmpersands_ShouldParseNamespaceValue() {
        validateFromConfigNode(NAMESPACE + "&&", NAMESPACE, null, null);
    }

    @Test
    public void testFromConfigNode_AllThreeValuesProvidedWithAmpersandsUnescaped_ShouldParseAllThreeValues() {
        validateFromConfigNode(ALL3_WITHAMPERSANDS_UNESCAPED, NAMESPACE, ENTITY_ID, ENTITY_ID_TYPE);
    }

    @Test
    public void testFromConfigNode_NamespaceValueProvidedWithOneTrailingAmpersand_ShouldBeLenientAndParseNamespaceValue() {
        // This one has only 1 trailing ampersand but we should be lenient and try to parse it anyway
        validateFromConfigNode(NAMESPACE + "&", NAMESPACE, null, null);
    }

    @Test
    public void testFromConfigNode_NoNamespaceValueProvidedWithEntityIDAndEntityIDTypeProvided_ShouldParseEntityIDAndEntityIDTypeValues() {
        validateFromConfigNode("&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, null, ENTITY_ID, ENTITY_ID_TYPE);
    }

    // ToConfigNode Test Cases - This is the process of taking the Java object and serializing it into JSON format to be
    // persisted into the DB
    @Test
    public void testToConfigNode_ProvideAllThreeValuesWithProperAmpersandDelimiters_ShouldParseAllValuesProperly() {
        Issuer issuer = new Issuer(NAMESPACE, ENTITY_ID, ENTITY_ID_TYPE);
        validateToConfigNode(issuer, NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE);
    }

    @Test
    public void testToConfigNode_ProvideAllThreeValuesInTheNamespaceFieldUnescaped_ShouldParseAndEscapeTheNamespaceValueOnly() {
        Issuer issuer = new Issuer(NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, null, null);
        validateToConfigNode(issuer, NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE);
    }

    @Test
    public void testToConfigNode_ProvideAllThreeValuesInTheNamespaceFieldEscaped_ShouldParseAndEscapeTheNamespaceValueOnly() {
        // Verify that already-escaped delimited values can also be serialized without losing the escaping
        Issuer issuer = new Issuer(NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE, null, null);
        validateToConfigNode(issuer, NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE);
    }

    @Test
    public void testToConfigNode_ProvideAllThreeValuesInTheNamespaceFieldEscaped_ShouldParseAndEscapeTheNamespaceValueAsWellAsTheOtherTwo() {
        // A case where the Namespace is delimited and also includes non-null values in the last 2 parameters
        Issuer issuer = new Issuer(NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, ENTITY_ID, ENTITY_ID_TYPE);
        validateToConfigNode(issuer,
                NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE);
    }

    @Test
    public void testToConfigNode_ProvideMultipleDelimitedValuesInEntityIDFieldUnescaped_ShouldParseAndEscapeTheValues() {
        Issuer issuer = new Issuer(NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, NAMESPACE + "&" + ENTITY_ID,
                ENTITY_ID_TYPE);
        validateToConfigNode(issuer, NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE + "&" + NAMESPACE
                + "\\T\\" + ENTITY_ID + "&" + ENTITY_ID_TYPE);
    }

    @Test
    public void testToConfigNode_ProvideMultipleDelimitedValuesInEntityIDTypeFieldUnescaped_ShouldParseAndEscapeTheValues() {
        Issuer issuer = new Issuer(NAMESPACE + "&" + ENTITY_ID + "&" + ENTITY_ID_TYPE, ENTITY_ID,
                ENTITY_ID + "&" + ENTITY_ID_TYPE);
        validateToConfigNode(issuer, NAMESPACE + "\\T\\" + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE + "&" + ENTITY_ID + "&"
                + ENTITY_ID + "\\T\\" + ENTITY_ID_TYPE);
    }

    private void validateFromConfigNode(String configNode, String expectedLocalNamespaceEntityID,
            String expectedUniversalEntityID, String expectedUniversalEntityIDType) {
        Issuer issuer = issuerTypeAdapter.fromConfigNode(configNode, null, null, null);
        assertEquals("Resulting LocalNamespaceEntityID value is wrong!", expectedLocalNamespaceEntityID,
                issuer.getLocalNamespaceEntityID());
        assertEquals("Resulting UniversalEntityID value is wrong!", expectedUniversalEntityID,
                issuer.getUniversalEntityID());
        assertEquals("Resulting UniversalEntityIDType value is wrong!", expectedUniversalEntityIDType,
                issuer.getUniversalEntityIDType());
    }

    private void validateToConfigNode(Issuer issuer, String expectedResult) {
        String result = issuerTypeAdapter.toConfigNode(issuer, null, null);
        assertEquals("Resulting serialized string value is wrong!", expectedResult, result);
    }
}
