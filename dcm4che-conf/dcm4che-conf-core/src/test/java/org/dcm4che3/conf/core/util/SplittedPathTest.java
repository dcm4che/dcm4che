package org.dcm4che3.conf.core.util;

import org.dcm4che3.conf.core.api.Path;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by aprvf on 22.08.2016.
 */
public class SplittedPathTest {

    @Test
    public void getInnerPathitems() throws Exception {

        SplittedPath splittedPath = new SplittedPath(new Path("one", "two", "three", "four").getPathItems(), 3);
        Assert.assertEquals(4, splittedPath.getTotalDepth());
        Assert.assertEquals(new Path("one", "two", "three").getPathItems(), splittedPath.getOuterPathItems());
        Assert.assertEquals(new Path("four").getPathItems(), splittedPath.getInnerPathitems());

    }

    @Test
    public void getTotalDepth() throws Exception {

        SplittedPath splittedPath = new SplittedPath(new Path("one", "two").getPathItems(), 3);
        Assert.assertEquals(2, splittedPath.getTotalDepth());


        splittedPath = new SplittedPath(new Path("one", "two", "three").getPathItems(), 3);
        Assert.assertEquals(3, splittedPath.getTotalDepth());

    }

}