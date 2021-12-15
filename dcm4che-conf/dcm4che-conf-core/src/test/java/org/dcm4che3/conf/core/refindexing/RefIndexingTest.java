package org.dcm4che3.conf.core.refindexing;

import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class RefIndexingTest {

    private final HashMap<String, Path> uuidToSimplePathCache = new HashMap<>();
    private final Configuration lowerLevelConfiguration = new InMemoryConfiguration();
    private final DefaultBeanVitalizer vitalizer = new DefaultBeanVitalizer();
    private final Configuration configuration = new ReferenceIndexingDecorator(
            new SampleCachingConfigDecorator(lowerLevelConfiguration),
            uuidToSimplePathCache
    );

    @Test
    public void testSimpleChanges() {


        SampleBigConf sampleBigConf = new SampleBigConf();

        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Romeo");

        SampleReferableConfClass uuid2obj = new SampleReferableConfClass("UUID2");
        uuid2obj.setMyName("Juliet");

        sampleBigConf.childList.add(uuid2obj);
        sampleBigConf.childList.add(new SampleReferableConfClass("UUID3"));

        sampleBigConf.childMap.put("first", new SampleReferableConfClass("UUID4"));
        sampleBigConf.childMap.put("second", new SampleReferableConfClass("UUID5"));

        configuration.persistNode(new Path("confRoot"), vitalizer.createConfigNodeFromInstance(sampleBigConf), null);

        // check if uuids are indexed
        assertThat("UUIDs should have been indexed", uuidToSimplePathCache.keySet(),
                hasItems("UUID1", "UUID2", "UUID3", "UUID4", "UUID5"));

        matchUUIDSearch("/confRoot/child1", sampleBigConf.child1);
        matchUUIDSearch("/confRoot/childList/#0", uuid2obj);

        configuration.removeNode(new Path("confRoot","childList"));

        assertThat("UUIDs of removed nodes should not be in cache", uuidToSimplePathCache.keySet(),
                not(anyOf(hasItem("UUID2"), hasItem("UUID3")))
        );

    }

    @Test
    public void refreshNode_whenNodeIsNotAvailableInConfigurationAnymore_removesNodeFromIndex() {
        SampleBigConf sampleBigConf = new SampleBigConf();

        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Romeo");

        SampleReferableConfClass uuid2obj = new SampleReferableConfClass("UUID2");
        uuid2obj.setMyName("Juliet");

        sampleBigConf.childList.add(uuid2obj);
        sampleBigConf.childList.add(new SampleReferableConfClass("UUID3"));

        sampleBigConf.childMap.put("first", new SampleReferableConfClass("UUID4"));
        sampleBigConf.childMap.put("second", new SampleReferableConfClass("UUID5"));

        configuration.persistNode(new Path("confRoot"), vitalizer.createConfigNodeFromInstance(sampleBigConf), null);

        // check if uuids are indexed
        assertThat("UUIDs should have been indexed", uuidToSimplePathCache.keySet(),
                hasItems("UUID1", "UUID2", "UUID3", "UUID4", "UUID5")
        );

        Path pathToRemove = new Path("confRoot", "childMap");
        // cache the node first
        configuration.getConfigurationNode(pathToRemove, null);
        lowerLevelConfiguration.removeNode(pathToRemove);
        configuration.refreshNode(pathToRemove);

        assertThat("UUIDs of removed nodes should not be in cache", uuidToSimplePathCache.keySet(),
                not(anyOf(hasItem("UUID4"), hasItem("UUID5")))
        );
        assertThat("UUIDs of untouched nodes should still be in cache", uuidToSimplePathCache.keySet(),
                hasItems("UUID1", "UUID2", "UUID3")
        );

    }

    @SuppressWarnings("unchecked")
    private void matchUUIDSearch(String expectedPath, SampleReferableConfClass referenceNode) {
        Path mappedPath = configuration.getPathByUUID(referenceNode.uuid);
        assertThat("UUID should map to the correct node PATH when searching by UUID",
                mappedPath.toSimpleEscapedPath(), equalTo(expectedPath));
        Map<String, Object> mappedNode = (Map<String,Object>) configuration.getConfigurationNode(mappedPath, null);
        assertThat("Stored node should match the expected path", mappedNode.get("myName"), equalTo(referenceNode.myName));
    }


}
