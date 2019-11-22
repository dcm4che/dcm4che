package org.dcm4che3.imageio.codec;

import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class ImageReaderFactoryTest {


    @Test
    public void getImageReaderParam_DefaultConfiguration() {
        ImageReaderFactory.ImageReaderParam imageReaderParam = ImageReaderFactory.getImageReaderParam("1.2.840.10008.1.2.5");
        assertThat(imageReaderParam.getClassName(), CoreMatchers.equalTo("org.dcm4che3.imageio.plugins.rle.RLEImageReader"));
        assertThat(imageReaderParam.getFormatName(), CoreMatchers.equalTo("rle"));
        assertThat(imageReaderParam.isImageTypeSpecifierRequired(), CoreMatchers.equalTo(true));
        assertThat(imageReaderParam.getPatchJPEGLS(), CoreMatchers.nullValue());

        imageReaderParam = ImageReaderFactory.getImageReaderParam("1.2.840.10008.1.2.4.80");
        assertThat(imageReaderParam.getClassName(), CoreMatchers.equalTo("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader"));
        assertThat(imageReaderParam.getFormatName(), CoreMatchers.equalTo("jpeg"));
        assertThat(imageReaderParam.isImageTypeSpecifierRequired(), CoreMatchers.equalTo(false));
        assertThat(imageReaderParam.getPatchJPEGLS(), CoreMatchers.equalTo(PatchJPEGLS.ISO2JAI_IF_APP_OR_COM));

        imageReaderParam = ImageReaderFactory.getImageReaderParam("1.2.840.10008.1.2.4.70");
        assertThat(imageReaderParam.getClassName(), CoreMatchers.equalTo("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader"));
        assertThat(imageReaderParam.getFormatName(), CoreMatchers.equalTo("jpeg"));
        assertThat(imageReaderParam.isImageTypeSpecifierRequired(), CoreMatchers.equalTo(false));
        assertThat(imageReaderParam.getPatchJPEGLS(), CoreMatchers.nullValue());
    }

    @Test
    public void getImageReaderParam_QuirkyConfigurationFile() throws IOException {
        URL fileURL = this.getClass().getResource("/QuirkyImageReader.properties");
        ImageReaderFactory factory = new ImageReaderFactory();
        factory.load(fileURL.openStream());


        ImageReaderFactory.ImageReaderParam imageReaderParam = factory.getMapTransferSyntaxUIDs().get("1.2.840.10008.1.2.4.51");
        assertThat(imageReaderParam.getClassName(), CoreMatchers.equalTo("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader"));
        assertThat(imageReaderParam.getFormatName(), CoreMatchers.equalTo("jpeg"));
        assertThat(imageReaderParam.isImageTypeSpecifierRequired(), CoreMatchers.equalTo(false));
        assertThat(imageReaderParam.getPatchJPEGLS(), CoreMatchers.nullValue());

        imageReaderParam = factory.getMapTransferSyntaxUIDs().get("1.2.840.10008.1.2.4.80");
        assertThat(imageReaderParam.getClassName(), CoreMatchers.equalTo("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader"));
        assertThat(imageReaderParam.getFormatName(), CoreMatchers.equalTo("jpeg"));
        assertThat(imageReaderParam.isImageTypeSpecifierRequired(), CoreMatchers.equalTo(true));
        assertThat(imageReaderParam.getPatchJPEGLS(), CoreMatchers.equalTo(PatchJPEGLS.ISO2JAI_IF_APP_OR_COM));

        imageReaderParam = factory.getMapTransferSyntaxUIDs().get("1.2.840.10008.1.2.4.81");
        assertThat(imageReaderParam.getClassName(), CoreMatchers.equalTo("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader"));
        assertThat(imageReaderParam.getFormatName(), CoreMatchers.equalTo("jpeg"));
        assertThat(imageReaderParam.isImageTypeSpecifierRequired(), CoreMatchers.equalTo(true));
        assertThat(imageReaderParam.getPatchJPEGLS(), CoreMatchers.equalTo(PatchJPEGLS.JAI2ISO));

    }


}