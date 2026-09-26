package org.dcm4che3.image;

import static org.junit.Assert.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;


public class PixelAspectRatioTest {

    @Test
    public void testForImageWithPixelAspectRatio() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 4, 3);
        assertEquals(4.0f / 3.0f, PixelAspectRatio.forImage(attrs), 0.0001f);
    }

    @Test
    public void testForImageWithPixelSpacing() {
        Attributes attrs = new Attributes();
        attrs.setFloat(Tag.PixelSpacing, VR.DS, 0.5f, 0.25f);
        assertEquals(2.0f, PixelAspectRatio.forImage(attrs), 0.0001f);
    }

    @Test
    public void testForImageDefault() {
        Attributes attrs = new Attributes();
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), 0.0001f);
    }

    @Test
    public void testForPresentationState() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.PresentationPixelAspectRatio, VR.IS, 16, 9);
        assertEquals(16.0f / 9.0f, PixelAspectRatio.forPresentationState(attrs), 0.0001f);
    }

    @Test
    public void testForPresentationStateWithSpacing() {
        Attributes attrs = new Attributes();
        attrs.setFloat(Tag.PresentationPixelSpacing, VR.DS, 1.0f, 1.0f);
        assertEquals(1.0f, PixelAspectRatio.forPresentationState(attrs), 0.0001f);
    }

    @Test
    public void testInvalidValues() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 0, 1); // Invalid 0
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), 0.0001f);
        
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 1, -1); // Invalid negative
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), 0.0001f);
    }
}
