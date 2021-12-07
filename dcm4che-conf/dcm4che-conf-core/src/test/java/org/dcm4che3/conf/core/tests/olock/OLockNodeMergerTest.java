package org.dcm4che3.conf.core.tests.olock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.OptimisticLockException;
import org.dcm4che3.conf.core.olock.OLockNodeMerger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OLockNodeMergerTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private final Map<String, Object> nodeBeingPersisted = new HashMap<>();
    private final Map<String, Object> nodeInStorage = new HashMap<>();
    
    @Test
    public void merge_DoesNotChangeNodeBeingPersisted_GivenNodeBeingPersistedMatchingNodeInStorage() {
        
        final Map<String, Object> expectedNodeBeingPersisted = new HashMap<>();
        expectedNodeBeingPersisted.put("setting", "one and only");
        
        nodeBeingPersisted.putAll(expectedNodeBeingPersisted);
        nodeBeingPersisted.put(Configuration.OLOCK_HASH_KEY, "wf8jEDyPvsPKvDHQVrjCCNblq9E=");
        
        nodeInStorage.putAll(expectedNodeBeingPersisted);
        
        OLockNodeMerger.merge(nodeBeingPersisted, nodeInStorage);
        
        assertThat("Node being persisted", nodeBeingPersisted, equalTo(expectedNodeBeingPersisted));
    }
    
    @Test
    public void merge_MergesNodeBeingPersistedIntoStorageNode_GivenNodeBeingPersistedWithOneNewSettingAndCorrectOldHash() {
        
        final Map<String, Object> expectedNodeBeingPersisted = new HashMap<>();
        expectedNodeBeingPersisted.put("setting A", 1);
        expectedNodeBeingPersisted.put("setting B", 2);
        
        nodeBeingPersisted.putAll(expectedNodeBeingPersisted);
        nodeBeingPersisted.put(Configuration.OLOCK_HASH_KEY, "UfsuOBRrANEFW1XebTWk8Y8EFlQ=");
        
        nodeInStorage.put("setting A", 1);
        nodeInStorage.put(Configuration.OLOCK_HASH_KEY, "UfsuOBRrANEFW1XebTWk8Y8EFlQ=");
        
        OLockNodeMerger.merge(nodeBeingPersisted, nodeInStorage);
        
        assertThat("Node being persisted", nodeBeingPersisted, equalTo(expectedNodeBeingPersisted));
    }
    
    @Test
    public void merge_ThrowsOptimisticLockException_GivenNodeBeingPersistedWithDifferentHashThanNodeInStorage() {
        
        expectedException.expect(OptimisticLockException.class);
        expectedException.expectMessage("Concurrent modification at path ''");

        nodeBeingPersisted.put("setting", "one and only");
        nodeBeingPersisted.put(Configuration.OLOCK_HASH_KEY, "bad hash");
        
        nodeInStorage.putAll(nodeBeingPersisted);
        nodeInStorage.put(Configuration.OLOCK_HASH_KEY, "I have changed since you asked");
        
        OLockNodeMerger.merge(nodeBeingPersisted, nodeInStorage);
    }
    
    @Test
    public void merge_ThrowsOptimisticLockException_GivenNodeBeingPersistedWithDifferentHashThanNewGeneratedOne() {
        
        expectedException.expect(OptimisticLockException.class);
        expectedException.expectMessage("Concurrent modification at path ''");

        nodeBeingPersisted.put("setting", "one and only");
        nodeBeingPersisted.put(Configuration.OLOCK_HASH_KEY, "bad hash");
        
        nodeInStorage.putAll(nodeBeingPersisted);
        nodeInStorage.put(Configuration.OLOCK_HASH_KEY, "wf8jEDyPvsPKvDHQVrjCCNblq9E=");
        
        OLockNodeMerger.merge(nodeBeingPersisted, nodeInStorage);
    }
}
