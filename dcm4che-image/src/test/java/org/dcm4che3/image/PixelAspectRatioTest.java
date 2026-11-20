package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PixelAspectRatioTest {

    private static final float DELTA = 0.000001f;

    @Test
    public void testForImageWithPixelAspectRatio() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 4, 3);
        assertEquals(4.0f / 3.0f, PixelAspectRatio.forImage(attrs), DELTA);
    }

    @Test
    public void testForImageWithPixelSpacing() {
        Attributes attrs = new Attributes();
        // Row spacing / Column spacing
        attrs.setFloat(Tag.PixelSpacing, VR.DS, 0.5f, 0.25f);
        assertEquals(2.0f, PixelAspectRatio.forImage(attrs), DELTA);
    }

    @Test
    public void testForImageWithImagerPixelSpacing() {
        Attributes attrs = new Attributes();
        attrs.setFloat(Tag.ImagerPixelSpacing, VR.DS, 0.8f, 0.4f);
        assertEquals(2.0f, PixelAspectRatio.forImage(attrs), DELTA);
    }

    @Test
    public void testForImageWithNominalScannedPixelSpacing() {
        Attributes attrs = new Attributes();
        attrs.setFloat(Tag.NominalScannedPixelSpacing, VR.DS, 1.2f, 0.6f);
        assertEquals(2.0f, PixelAspectRatio.forImage(attrs), DELTA);
    }

    @Test
    public void testForImageFallbackOrder() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 1, 1);
        attrs.setFloat(Tag.PixelSpacing, VR.DS, 2.0f, 1.0f);
        
        // PixelAspectRatio takes precedence
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), DELTA);

        attrs.remove(Tag.PixelAspectRatio);
        // Now PixelSpacing
        assertEquals(2.0f, PixelAspectRatio.forImage(attrs), DELTA);
    }

    @Test
    public void testForPresentationStateWithAspectRatio() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.PresentationPixelAspectRatio, VR.IS, 16, 9);
        assertEquals(16.0f / 9.0f, PixelAspectRatio.forPresentationState(attrs), DELTA);
    }

    @Test
    public void testForPresentationStateWithSpacing() {
        Attributes attrs = new Attributes();
        attrs.setFloat(Tag.PresentationPixelSpacing, VR.DS, 1.0f, 0.5f);
        assertEquals(2.0f, PixelAspectRatio.forPresentationState(attrs), DELTA);
    }

    @Test
    public void testDefaultFallback() {
        Attributes attrs = new Attributes();
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), DELTA);
        assertEquals(1.0f, PixelAspectRatio.forPresentationState(attrs), DELTA);
    }

    @Test
    public void testInvalidValues() {
        Attributes attrs = new Attributes();
        
        // Length 1 instead of 2
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 1);
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), DELTA);

        // Zero values
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 0, 1);
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), DELTA);

        attrs.setInt(Tag.PixelAspectRatio, VR.IS, 1, 0);
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), DELTA);

        // Negative values
        attrs.setInt(Tag.PixelAspectRatio, VR.IS, -1, 1);
        assertEquals(1.0f, PixelAspectRatio.forImage(attrs), DELTA);

        // Valid spacing fallback when ratio is invalid
        attrs.setFloat(Tag.PixelSpacing, VR.DS, 2.0f, 1.0f);
        assertEquals(2.0f, PixelAspectRatio.forImage(attrs), DELTA);
    }
}
