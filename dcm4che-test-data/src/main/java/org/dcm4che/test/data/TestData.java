package org.dcm4che.test.data;


import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Class which provides access to test files.
 */
public class TestData {
    private final String fileName;
    private File file;

    public TestData(String fileName) {
        this.fileName = fileName;
    }


    public URL toURL() {
        URL url = getClass().getResource(fileName.startsWith("/") ? fileName : "/" + fileName);
        assert url != null : "Unable to find URL for " + fileName;
        return url;
    }

    private URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError("Failed to bind test data: "+fileName, e);
        }
    }

    public URI toURI() {
        return toURI(toURL());
    }



    public File toFile() {
        if (this.file == null) {
            this.file = createFile();
        }
        return this.file;
    }

    private File createFile() {
        URL dataURL = toURL();

        try {
            if ("file".equals(dataURL.getProtocol())) {
                return Paths.get(this.toURI()).toFile();
            } else {
                Path temp = Files.createTempFile("TestData", this.fileName);
                Files.copy(dataURL.openStream(), temp, StandardCopyOption.REPLACE_EXISTING);
                return temp.toFile();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to copy to temp file: "+fileName, e);
        }
    }

    public ImageInputStream toImageInputStream() throws IOException {
        return new FileImageInputStream(this.toFile());
    }
}
