package org.dcm4che3.imageio.codec;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Test;

public class CompressorTest {

    @Test
    public void testCompress_GivenMultiframeImage_WritesMostDataUsingBuffers() throws Exception {

        try (CountOutputStream cos = new CountOutputStream()) {
            test("cplx_p02.dcm", cos, UID.JPEGBaseline8Bit);

            assertTrue("Individual bytes written", cos.byteCount < 60000 && cos.byteCount > 0);
            assertTrue("Bytes written using buffers", cos.bufferCount > 60000);
        }
    }

    private void test(String inputFileName, OutputStream outputStream, String outTransferSyntax) throws IOException {
        File inputFile = new File("target/test-data/" + inputFileName);

        try (DicomInputStream dis = new DicomInputStream(inputFile)) {
        	dis.setIncludeBulkData(IncludeBulkData.URI);
        	dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
        	Attributes attributes = dis.readDataset();
            try (Compressor compressor = new Compressor(attributes,
            		dis.getFileMetaInformation().getString(Tag.TransferSyntaxUID))) {
            	compressor.compress(outTransferSyntax);

            	try (DicomOutputStream dos = new DicomOutputStream(outputStream, outTransferSyntax)) {
            		Attributes newFmi = attributes.createFileMetaInformation(outTransferSyntax);
            		dos.writeDataset(newFmi, attributes);
            		dos.flush();
            	}
            }
        }
    }

    private static class CountOutputStream extends OutputStream {
    	public int byteCount;
    	public int bufferCount;

    	@Override
    	public void write(byte[] b, int off, int len) {
    		bufferCount += len;
    	}

		@Override
		public void write(int b) throws IOException {
			byteCount++;
		}
    }
}
