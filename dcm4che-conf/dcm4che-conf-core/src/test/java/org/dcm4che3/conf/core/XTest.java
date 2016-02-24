package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.util.XNodeUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by aprvf on 22.02.2016.
 */
public class XTest {

    @Test
    @Ignore
    public void testXpath() {

        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee_arc");
        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee-arc");
    }
}
