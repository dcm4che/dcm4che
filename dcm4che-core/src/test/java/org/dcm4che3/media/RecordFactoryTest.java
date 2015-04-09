package org.dcm4che3.media;

import static org.junit.Assert.assertEquals;

import org.dcm4che3.data.UID;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class RecordFactoryTest {

    @Test
    public void testGetRecordType() {
        RecordFactory f = new RecordFactory();
        assertEquals(RecordType.IMAGE,
                f.getRecordType(UID.SecondaryCaptureImageStorage));
    }

}
