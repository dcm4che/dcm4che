package org.dcm4che3.conf.core.refindexing;

import org.apache.commons.jxpath.JXPathContext;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.util.PathPattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefIndexingTest {

    private HashMap<String, Path> uuidToSimplePathCache;
    private Configuration configuration;
    private DefaultBeanVitalizer vitalizer;

    @Before
    public void init() {
        configuration = new InMemoryConfiguration();
        uuidToSimplePathCache = new HashMap<String, Path>();
        configuration = new ReferenceIndexingDecorator(configuration, uuidToSimplePathCache);
        vitalizer = new DefaultBeanVitalizer();
    }

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
        Assert.assertEquals(5, uuidToSimplePathCache.size());

        PathPattern pathPattern = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

        Assert.assertEquals("Romeo", ((Map) configuration.getConfigurationNode(configuration.getPathByUUID("UUID1"), null)).get("myName"));
        Assert.assertEquals("Juliet", ((Map) configuration.getConfigurationNode(configuration.getPathByUUID("UUID2"), null)).get("myName"));

        configuration.removeNode(new Path("confRoot","childList"));

        Assert.assertFalse(uuidToSimplePathCache.containsKey("UUID2"));
        Assert.assertFalse(uuidToSimplePathCache.containsKey("UUID3"));

    }



}
