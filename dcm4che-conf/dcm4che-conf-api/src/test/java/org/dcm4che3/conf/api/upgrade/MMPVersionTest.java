package org.dcm4che3.conf.api.upgrade;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MMPVersionTest {

    @Test
    public void fromStringVersion() throws Exception {

        MMPVersion mmpVersion;

        mmpVersion = MMPVersion.fromStringVersion("8.1.1");
        Assert.assertEquals(8, mmpVersion.getMajor());
        Assert.assertEquals(1, mmpVersion.getMinor());
        Assert.assertEquals(0, mmpVersion.getPatch());

        mmpVersion = MMPVersion.fromStringVersion("8.1");
        Assert.assertEquals(8, mmpVersion.getMajor());
        Assert.assertEquals(1, mmpVersion.getMinor());
        Assert.assertEquals(0, mmpVersion.getPatch());

        mmpVersion = MMPVersion.fromStringVersion("8.1-5");
        Assert.assertEquals(8, mmpVersion.getMajor());
        Assert.assertEquals(1, mmpVersion.getMinor());
        Assert.assertEquals(5, mmpVersion.getPatch());

        mmpVersion = MMPVersion.fromStringVersion("8.1.6-5");
        Assert.assertEquals(8, mmpVersion.getMajor());
        Assert.assertEquals(1, mmpVersion.getMinor());
        Assert.assertEquals(5, mmpVersion.getPatch());

        try {
            MMPVersion.fromStringVersion("8-2");
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

}