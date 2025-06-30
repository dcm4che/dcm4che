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

package org.dcm4che3.conf.core.integration.tests.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.DuplicateUUIDException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.junit.jupiter.api.Test;

class ReferenceIndexingDecoratorIntegrationTest {

    private static final String TEST_UUID = UUID.randomUUID().toString();
    private static final String ROOT_PATH = "confRoot";
    
    private final HashMap<String, Path> uuidToSimplePathCache = new HashMap<>();
    private final Configuration lowerLevelConfiguration = new InMemoryConfiguration();
    private final DefaultBeanVitalizer vitalizer = new DefaultBeanVitalizer();
    
    private final SampleBigConf sampleBigConf = new SampleBigConf();
    
    /**
     * System Under Test (SUT).
     */
    private final TestableReferenceIndexingDecorator decorator = new TestableReferenceIndexingDecorator(
            new SampleCachingConfigDecorator(lowerLevelConfiguration),
            uuidToSimplePathCache);

    @Test
    void testSimpleChanges() {

        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Romeo");

        SampleReferableConfClass uuid2obj = new SampleReferableConfClass("UUID2");
        uuid2obj.setMyName("Juliet");

        sampleBigConf.childList.add(uuid2obj);
        sampleBigConf.childList.add(new SampleReferableConfClass("UUID3"));

        sampleBigConf.childMap.put("first", new SampleReferableConfClass("UUID4"));
        sampleBigConf.childMap.put("second", new SampleReferableConfClass("UUID5"));

        decorator.persistNode(new Path(ROOT_PATH), createConfigNodeFromInstance(sampleBigConf), null);

        // check if uuids are indexed
        assertThat("UUIDs should have been indexed", uuidToSimplePathCache.keySet(),
                hasItems("UUID1", "UUID2", "UUID3", "UUID4", "UUID5"));

        matchUUIDSearch("/confRoot/child1", sampleBigConf.child1);
        matchUUIDSearch("/confRoot/childList/#0", uuid2obj);

        decorator.removeNode(new Path(ROOT_PATH, "childList"));

        Assertions.assertThat(uuidToSimplePathCache.keySet())
                .withFailMessage(() -> "UUIDs of removed nodes should not be in cache")
                .doesNotContainAnyElementsOf(Stream.of("UUID2", "UUID3").collect(Collectors.toSet()));
    }

    @Test
    void persistNode_DetectsDuplicatedUuidButDoesNotActOnExceptionAndCachesTheLastPathWithSameUuid_WhenThereAreDuplicatedUuidForDifferentPaths() {
        
        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Good");
        
        SampleReferableConfClass dupeConfig = new SampleReferableConfClass(TEST_UUID);
        dupeConfig.myName = "dupe";
        
        sampleBigConf.childList.add(dupeConfig);
        sampleBigConf.childList.add(new SampleReferableConfClass(TEST_UUID));
        
        decorator.persistNode(new Path(ROOT_PATH), createConfigNodeFromInstance(sampleBigConf), null);
        
        assertThat(uuidToSimplePathCache).as("Cached entries")
                .containsOnly(
                        entry("UUID1", Path.fromSimpleEscapedPath("/confRoot/child1")),
                        entry(TEST_UUID, Path.fromSimpleEscapedPath("/confRoot/childList/#1")));
        
        assertThat(decorator.duplicateUuidExceptions).as("Duplicate UUID exceptions")
                .hasSize(1)
                .element(0)
                .usingRecursiveComparison()
                .isEqualTo(new DuplicateUUIDException(TEST_UUID,
                    new Path(ROOT_PATH, "childList", 0),
                    new Path(ROOT_PATH, "childList", 1)));
    }
    
    @Test
    void persistNode_DoesNotAddDuplicateUUIDException_WhenThereAreDuplicatedUuidForSamePathSimulatingAnotherThreadAddingRemovedUuidBack() {
        
        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Good");
        
        SampleReferableConfClass dupeConfig = new SampleReferableConfClass(TEST_UUID);
        dupeConfig.myName = "dupe";
        
        sampleBigConf.childList.add(dupeConfig);
        
        // Persist same node first so the index is populated.
        decorator.persistNode(new Path(ROOT_PATH), createConfigNodeFromInstance(sampleBigConf), null);
        
        assertThat(uuidToSimplePathCache).as("Initial cached entries").containsOnlyKeys("UUID1", TEST_UUID);
        
        // This will put the referable UUID back to the index just before "addReferablesToIndex" code runs.
        decorator.anotherThreadSimulatorBeforeAddReferablesToIndexRunnable = () -> {
            uuidToSimplePathCache.put(TEST_UUID, new Path(ROOT_PATH, "childList", 0));
        };
        
        decorator.persistNode(new Path(ROOT_PATH), createConfigNodeFromInstance(sampleBigConf), null);
        
        assertThat(uuidToSimplePathCache).as("Cached entries")
                .containsOnly(
                        entry("UUID1", Path.fromSimpleEscapedPath("/confRoot/child1")),
                        entry(TEST_UUID, Path.fromSimpleEscapedPath("/confRoot/childList/#0")));
        
        assertThat(decorator.duplicateUuidExceptions).as("Duplicate UUID exceptions").isEmpty();
    }
    
    @Test
    void refreshNode_RemovesNodeFromIndex_WhenNodeIsNotAvailableInConfigurationAnymore() {
        
        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Romeo");

        SampleReferableConfClass uuid2obj = new SampleReferableConfClass("UUID2");
        uuid2obj.setMyName("Juliet");

        sampleBigConf.childList.add(uuid2obj);
        sampleBigConf.childList.add(new SampleReferableConfClass("UUID3"));

        sampleBigConf.childMap.put("first", new SampleReferableConfClass("UUID4"));
        sampleBigConf.childMap.put("second", new SampleReferableConfClass("UUID5"));

        decorator.persistNode(new Path(ROOT_PATH), createConfigNodeFromInstance(sampleBigConf), null);

        // check if uuids are indexed
        assertThat(uuidToSimplePathCache).as("Initial cached entries")
                .containsOnlyKeys("UUID1", "UUID2", "UUID3", "UUID4", "UUID5");
        
        Path pathToRemove = new Path(ROOT_PATH, "childMap");
        // cache the node first
        decorator.getConfigurationNode(pathToRemove, null);
        lowerLevelConfiguration.removeNode(pathToRemove);
        
        decorator.refreshNode(pathToRemove);

        assertThat(uuidToSimplePathCache).as("Cached entries after refresh")
                .containsOnly(
                        entry("UUID1", Path.fromSimpleEscapedPath("/confRoot/child1")),
                        entry("UUID2", Path.fromSimpleEscapedPath("/confRoot/childList/#0")),
                        entry("UUID3", Path.fromSimpleEscapedPath("/confRoot/childList/#1")));
    }
    
    private Map<String, Object> createConfigNodeFromInstance(Object config) {

        return vitalizer.createConfigNodeFromInstance(config);
    }

    /**
     * @apiNote JDK 9 has this available, but not 8. 
     */
    private Map.Entry<String, Path> entry(String key, Path value) {
        
        return new Map.Entry<String, Path>() {

            String entryKey = key;
            Path entryValue = value;
            
            @Override
            public String getKey() {

                return entryKey;
            }

            @Override
            public Path getValue() {
                
                return entryValue;
            }

            @Override
            public Path setValue(Path value) {

                return null;
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    private void matchUUIDSearch(String expectedPath, SampleReferableConfClass referenceNode) {
        
        Path mappedPath = decorator.getPathByUUID(referenceNode.uuid);
        assertThat("UUID should map to the correct node PATH when searching by UUID",
                mappedPath.toSimpleEscapedPath(), equalTo(expectedPath));
        Map<String, Object> mappedNode = (Map<String,Object>) decorator.getConfigurationNode(mappedPath, null);
        assertThat("Stored node should match the expected path", mappedNode.get("myName"), equalTo(referenceNode.myName));
    }

    private static final class TestableReferenceIndexingDecorator extends ReferenceIndexingDecorator {
     
        private Runnable anotherThreadSimulatorBeforeAddReferablesToIndexRunnable;
        private List<DuplicateUUIDException> duplicateUuidExceptions = new ArrayList<>();
        
        public TestableReferenceIndexingDecorator(Configuration delegate, Map<String, Path> uuidToSimplePathCache) {
            
            super(delegate, uuidToSimplePathCache);
        }

        @Override
        protected List<DuplicateUUIDException> addReferablesToIndex(List<Object> pathItems, Object configNode) {
            
            if (anotherThreadSimulatorBeforeAddReferablesToIndexRunnable != null) {
                anotherThreadSimulatorBeforeAddReferablesToIndexRunnable.run();
            }
            
            List<DuplicateUUIDException> exceptions = super.addReferablesToIndex(pathItems, configNode);
            
            duplicateUuidExceptions.addAll(exceptions);
            
            return exceptions;
        }
    }
}
