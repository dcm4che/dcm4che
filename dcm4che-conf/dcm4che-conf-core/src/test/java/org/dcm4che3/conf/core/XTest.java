package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.util.SplittedPath;
import org.dcm4che3.conf.core.util.XNodeUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Created by aprvf on 22.02.2016.
 */
public class XTest {

    @Test
    public void testXpath() {

        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee_arc");
        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee-arc");
        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee-arc");
        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee-arc/dicomNetworkAE/8.1_8.0.1");

        Assert.assertEquals(null, Nodes.simpleOrPersistablePathToPathItemsOrNull("//*[_.uuid='fd2c0253-ed6b-42a3-8d6a-3cef5c76cf34']"));
    }

    @Test
    public void testSplittedPath() {

        SplittedPath splittedPath = getSplittedPath("/dicomConfigurationRoot/dicomDevicesRoot/EXSYS1624/dicomNetworkAE/SCALLINONE ES");

        Assert.assertNotNull(splittedPath);

    }

    private SplittedPath getSplittedPath(String path) {

        List<Object> pathItems = Nodes.simpleOrPersistablePathToPathItemsOrNull(path);
        if (pathItems == null) return null;
        return new SplittedPath(pathItems, 3);
    }
}
