package org.dcm4che3.conf.core.integration.tests.olock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.olock.OLockHashCalcFilter;
import org.dcm4che3.conf.core.util.ConfigNodeTraverser;
import org.junit.Test;

public class OLockHashCalcFilterIntegrationTest {
    
    private static final String HASH_PLACEHOLDER = "placeholder";

    private final Map<String, Object> configNode = new HashMap<>();
    
    @Test
    public void usingFilterWithNodeTraverser_CalculatesHashCorreclty_GivenSimpleNodeAndWhenFilterHasNoIgnoredKeys() {
        
        final Map<String, Object> expectedConfigNode = new HashMap<>();
        
        configNode.put("key A", "A");
        configNode.put("key B", "B");
        configNode.put(Configuration.OLOCK_HASH_KEY, HASH_PLACEHOLDER);
        
        // Set the correct expectations.
        expectedConfigNode.putAll(configNode);
        expectedConfigNode.put(Configuration.OLOCK_HASH_KEY, "rq/9EOOe8p7BsGGyktc3i0M/B3A=");
        
        // Act
        ConfigNodeTraverser.traverseMapNode(configNode, new OLockHashCalcFilter());
        
        assertThat("Actual config node", configNode, equalTo(expectedConfigNode));
    }

    @Test
    public void usingFilterWithNodeTraverser_CalculatesHashCorreclty_GivenComplexNodeWithListAndMap() {
        
        final Map<String, Object> expectedConfigNode = new HashMap<>();
        
        Map<String, Object> childNode = new HashMap<>();
        childNode.put("STORAGE", "more values...");
        
        configNode.put("string", "s");
        configNode.put("number", 2);
        configNode.put("boolean", true);
        configNode.put("list", Arrays.asList("A", "B"));
        configNode.put("map", childNode);
        configNode.put(Configuration.OLOCK_HASH_KEY, HASH_PLACEHOLDER);
        
        // Set the correct expectations.
        expectedConfigNode.putAll(configNode);
        expectedConfigNode.put(Configuration.OLOCK_HASH_KEY, "OyOI3mxYTtbiVVo8BIBMNE7Xj1c=");
        
        // Act
        ConfigNodeTraverser.traverseMapNode(configNode, new OLockHashCalcFilter());
        
        assertThat("Actual config node", configNode, equalTo(expectedConfigNode));
    }
    
    @Test
    public void usingFilterWithNodeTraverser_CalculatesHashCorreclty_GivenSimpleNodeAndWhenFilterHasIgnoredKeys() {
        
        final Map<String, Object> expectedConfigNode = new HashMap<>();
        
        configNode.put("config", "value");
        configNode.put(Configuration.VERSION_KEY, 1);
        configNode.put(Configuration.OLOCK_HASH_KEY, HASH_PLACEHOLDER);
        
        // Set the correct expectations.
        expectedConfigNode.putAll(configNode);
        expectedConfigNode.put(Configuration.OLOCK_HASH_KEY, "ZhR7Q+YysTyB3BbiCYjAA9UzR7M=");
        
        // Act
        ConfigNodeTraverser.traverseMapNode(
            configNode,
            new OLockHashCalcFilter("A", Configuration.VERSION_KEY));
        
        assertThat("Actual config node", configNode, equalTo(expectedConfigNode));
        
        // Now change the version and add "A" key, and compare the hashes.
        configNode.put(Configuration.VERSION_KEY, 2);
        configNode.put("A", true);
        
        // Set the correct expectations.
        expectedConfigNode.putAll(configNode);
        expectedConfigNode.put(Configuration.OLOCK_HASH_KEY, "ZhR7Q+YysTyB3BbiCYjAA9UzR7M=");
        
        // Act
        ConfigNodeTraverser.traverseMapNode(
            configNode,
            new OLockHashCalcFilter("A", Configuration.VERSION_KEY));
        
        assertThat("Actual config node", configNode, equalTo(expectedConfigNode));
    }
    
    @Test
    public void usingFilterWithNodeTraverser_CalculatesHashesCorreclty_GivenNodeWithOlockedChildren() {
        
        final Map<String, Object> expectedConfigNode = new HashMap<>();
        
        Map<String, Object> olockedChildNode = new HashMap<>();
        olockedChildNode.put("kiddo", 1);
        olockedChildNode.put(Configuration.OLOCK_HASH_KEY, HASH_PLACEHOLDER);
        
        configNode.put("some", "value");
        configNode.put("child", olockedChildNode);
        configNode.put(Configuration.OLOCK_HASH_KEY, HASH_PLACEHOLDER);
        
        // Set the correct expectations.
        expectedConfigNode.putAll(configNode);
        expectedConfigNode.put(Configuration.OLOCK_HASH_KEY, "pivlNKxux1AA8zMSW/2cb34otmM=");
        olockedChildNode.put(Configuration.OLOCK_HASH_KEY, "kWPKGCMRxmZGs1zB+R7wxFCG/t4=");
        
        // Act
        ConfigNodeTraverser.traverseMapNode(configNode, new OLockHashCalcFilter());
        
        assertThat("Actual config node", configNode, equalTo(expectedConfigNode));
    }
}
