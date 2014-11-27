package org.dcm4che3.test;

import org.dcm4che3.test.DicomParameters;
import org.dcm4che3.test.DicomTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SampleTest extends DicomTest {

    @Test
    @DicomParameters(
            files = "myhl7.hl7",
            targetDeviceName = "hl7-dest",
            sourceDeviceName = "source",
            targetConnectionCn = "primary",
            sourceConnectionCn = "hl7"
    )
    public void testSomeHL7() {
        sendHL7();
    }
}
