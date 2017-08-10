package org.dcm4che3.conf.api.upgrade;

import org.junit.Assert;
import org.junit.Test;

public class NumericVersionTest
{

    @Test
    public void fromStringVersion() throws Exception
    {

        try
        {
            NumericVersion.fromStringVersion( "8.1.1" );
            Assert.fail();
        }
        catch ( IllegalArgumentException ignored )
        {
        }
        try
        {
            NumericVersion.fromStringVersion( "8.-1.6-5" );
            Assert.fail();
        }
        catch ( IllegalArgumentException ignored )
        {
        }
        try
        {
            NumericVersion.fromStringVersion( "8-2" );
            Assert.fail();
        }
        catch ( IllegalArgumentException ignored )
        {
        }
        try
        {
            NumericVersion.fromStringVersion( "8.1.0.6-5" );
            Assert.fail();
        }
        catch ( IllegalArgumentException ignored )
        {
        }
        try
        {
            NumericVersion.fromStringVersion( "8.1-5" );
            Assert.fail();
        }
        catch ( IllegalArgumentException ignored )
        {
        }

        NumericVersion numericVersion;
        numericVersion = NumericVersion.fromStringVersion( "8.1.6-5" );
        Assert.assertEquals( 8, numericVersion.getMajor() );
        Assert.assertEquals( 1, numericVersion.getMinor() );
        Assert.assertEquals( 6, numericVersion.getRevision() );
        Assert.assertEquals( 5, numericVersion.getPatch() );

        numericVersion = NumericVersion.fromStringVersion( "008.0001.0006-0000005" );
        Assert.assertEquals( 8, numericVersion.getMajor() );
        Assert.assertEquals( 1, numericVersion.getMinor() );
        Assert.assertEquals( 6, numericVersion.getRevision() );
        Assert.assertEquals( 5, numericVersion.getPatch() );

        numericVersion = NumericVersion.fromStringVersion( "80.10.600-5000" );
        Assert.assertEquals( 80, numericVersion.getMajor() );
        Assert.assertEquals( 10, numericVersion.getMinor() );
        Assert.assertEquals( 600, numericVersion.getRevision() );
        Assert.assertEquals( 5000, numericVersion.getPatch() );
    }

}