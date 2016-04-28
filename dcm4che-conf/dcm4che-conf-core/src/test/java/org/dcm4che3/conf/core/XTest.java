package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.util.XNodeUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by aprvf on 22.02.2016.
 */
public class XTest {

    @Test
    public void testXpath() {

        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee_arc");
        XNodeUtil.parseReference("/dicomConfigurationRoot/dicomDevicesRoot/dcm4chee-arc");
        XNodeUtil.parseReference("//*[_.uuid='fd2c0253-ed6b-42a3-8d6a-3cef5c76cf34']");
    }
}
