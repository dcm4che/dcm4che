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
 * Portions created by the Initial Developer are Copyright (C) 2022
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
package org.dcm4che3.net;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Maciek Siemczyk
 */
public class ApplicationEntityTest {

    private static final String AE_TITLE = "SomeAET";
    
    /**
     * System Under Test (SUT).
     */
    private ApplicationEntity applicationEntity = new ApplicationEntity(AE_TITLE);

    @Test
    public void getAETitleAliases_ReturnsEmptyList_WhenThereIsNoAliases() {

        assertThat("Returned not empty list", applicationEntity.getAETitleAliases(), is(empty()));
    }
    
    @Test
    public void getAETitleAliases_ReturnsCorrectlyPopulatedList_WhenThereAreAliases() {
        
        final String firstAlias = "firstAlias";
        final String secondAlias = "secondAlias";
        
        // Set some random alias first to make sure the list is cleared out first.
        applicationEntity.setAETitleAliases("alias");

        applicationEntity.setAETitleAliases(firstAlias, secondAlias);
        
        assertThat(
                "Returned wrong aliases",
                applicationEntity.getAETitleAliases(),
                is(Arrays.asList(firstAlias, secondAlias)));
    }
}
