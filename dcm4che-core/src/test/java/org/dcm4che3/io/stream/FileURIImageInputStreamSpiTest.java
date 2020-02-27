package org.dcm4che3.io.stream;

import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileURIImageInputStreamSpiTest {
    private FileURIImageInputStreamSpi spi = new FileURIImageInputStreamSpi();

    @Test
    public void toFile_CheckUNCPath() throws URISyntaxException {
        Assume.assumeTrue(System.getProperty("os.name").contains("Windows"));

        String pathStr = "\\\\localhost\\cache2\\file.dcm";
        Path p = Paths.get(pathStr);
        URI fileURI = p.toUri();
        File file = spi.toFile(fileURI);
        assertEquals(pathStr, file.toString());
    }

    @Test
    public void toFile_WindowsPath() throws URISyntaxException {
        Assume.assumeTrue(System.getProperty("os.name").contains("Windows"));

        String pathStr = "C:\\MyCache\\1";
        Path p = Paths.get(pathStr);
        URI fileURI = p.toUri();
        File file = spi.toFile(fileURI);
        assertEquals(pathStr, file.toString());
    }

    @Test
    public void toFile_UnixPath() {
        Assume.assumeTrue(System.getProperty("os.name").contains("Linux"));

        String pathStr = "/opt/cache/3/file.dcm";
        Path p = Paths.get(pathStr);
        URI fileURI = p.toUri();
        File file = spi.toFile(fileURI);
        assertEquals(pathStr, file.toString());
    }
}