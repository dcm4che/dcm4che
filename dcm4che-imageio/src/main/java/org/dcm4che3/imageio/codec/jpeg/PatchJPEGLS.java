package org.dcm4che3.imageio.codec.jpeg;

public enum PatchJPEGLS {
    JAI2ISO,
    ISO2JAI,
    ISO2JAI_IF_APP_OR_COM;

    public JPEGLSCodingParam createJPEGLSCodingParam(byte[] jpeg) {
        JPEGHeader jpegHeader = new JPEGHeader(jpeg, JPEG.SOS);
        int soiOff = jpegHeader.offsetOf(JPEG.SOI);
        int sof55Off = jpegHeader.offsetOf(JPEG.SOF55);
        int lseOff = jpegHeader.offsetOf(JPEG.LSE);
        int sosOff = jpegHeader.offsetOf(JPEG.SOS);

        if (soiOff == -1)
            return null; // no JPEG

        if (sof55Off == -1)
            return null; // no JPEG-LS

        if (lseOff != -1)
            return null; // already patched

        if (sosOff == -1)
            return null;

        if (this == ISO2JAI_IF_APP_OR_COM
                && jpegHeader.numberOfMarkers() == 3)
            return null;

        int p = jpeg[sof55Off+3] & 255;
        if (p <= 12)
            return null;

        JPEGLSCodingParam param = this == JAI2ISO
                ? JPEGLSCodingParam.getJAIJPEGLSCodingParam(p)
                : JPEGLSCodingParam.getDefaultJPEGLSCodingParam(p,
                        jpeg[sosOff+6] & 255);
        param.setOffset(sosOff-1);
        return param;
    }
}
