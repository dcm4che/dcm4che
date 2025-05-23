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
 * Portions created by the Initial Developer are Copyright (C) 2020
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

package org.dcm4che3.conf.core.api.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.util.Sets;
import org.dcm4che3.conf.core.api.ChangeEventSource;
import org.dcm4che3.conf.core.api.ConfigChanges;
import org.dcm4che3.conf.core.api.ConfigChanges.Changed;
import org.dcm4che3.conf.core.api.InternalConfigChangeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class InternalConfigChangeEventTest {

    /**
     * System Under Test (SUT).
     */
    private InternalConfigChangeEvent event;
    
    @Test
    void contructor_SetsMembersCorrectly_GivenValidSourceAndChangesByPath() {
        
        Map<String, ConfigChanges> changesByPath = new HashMap<>();
        changesByPath.put("path1", new ConfigChanges());
        changesByPath.put("path2", new ConfigChanges());
        changesByPath.put("path3", new ConfigChanges());
        
        event = new InternalConfigChangeEvent(ChangeEventSource.INTERNAL, changesByPath);
        
        assertThat(event.getSource()).as("Source").isEqualTo(ChangeEventSource.INTERNAL);
        assertThat(event.getChangesByPath()).as("Changes by path").isSameAs(changesByPath);
        
        assertThat(event.getChangedPaths()).as("Changed paths")
                .containsExactlyInAnyOrder("path1", "path2", "path3")
                .isNotSameAs(changesByPath.keySet());
        
        assertThat(event.getTransactionId()).as("Transaction ID").isEmpty();
    }
    
    @Test
    void contructor_ThrowsIllegalArgumentException_GivenNullSource() {
        
        Map<String, ConfigChanges> changesByPath = new HashMap<>();
        
        assertThatThrownBy(() -> new InternalConfigChangeEvent(null, changesByPath))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given source is null.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 666 })
    void setTransactionId_SetsTransactionId_GivenValidTransactionId(Integer transactionId) {

        event = new InternalConfigChangeEvent(ChangeEventSource.INTERNAL, new HashMap<>());
        event.setTransactionId(transactionId);

        assertThat(event.getTransactionId()).as("Transaction ID").isEqualTo(Optional.ofNullable(transactionId));
    }   
    
    @Test
    void toString_ReturnsCorrectString_WhenCalled() {
        
        final String expectedToString = "InternalConfigChangeEvent [source=EXTERNAL, transactionId=12345, "
                + "changesByPath={a={AE_TITLES=[AE1, AE2]}, b={AE_TITLES=[AE3]}}]";
        
        Map<String, ConfigChanges> changesByPath = new HashMap<>();
        changesByPath.put("a", createConfigChangesWithValue(Sets.set("AE1", "AE2")));
        changesByPath.put("b", createConfigChangesWithValue(Sets.set("AE3")));

        event = new InternalConfigChangeEvent(ChangeEventSource.EXTERNAL, changesByPath);
        event.setTransactionId(12345);
        
        assertThat(event).hasToString(expectedToString);
    }
    
    private ConfigChanges createConfigChangesWithValue(Set<Object> aeTitles) {
        
        ConfigChanges configChanges = new ConfigChanges();
        configChanges.put(Changed.AE_TITLES, aeTitles);
        
        return configChanges;
    }
}
