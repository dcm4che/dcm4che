package org.dcm4che3.data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link BulkData}, specifically getFile() with special characters in paths.
 */
public class BulkDataTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void getFile_simplePath() throws IOException {
        File f = tempFolder.newFile("test.dcm");
        String uri = f.toURI().toString();
        BulkData bd = new BulkData(null, uri, false);
        assertEquals(f, bd.getFile());
    }

    @Test
    public void getFile_pathWithSpaces() throws IOException {
        File dir = tempFolder.newFolder("path with spaces");
        File f = new File(dir, "test.dcm");
        f.createNewFile();
        String uri = f.toURI().toString();
        // URI will contain %20 for spaces
        assert uri.contains("%20") : "URI should encode spaces as %20: " + uri;
        BulkData bd = new BulkData(null, uri, false);
        assertEquals(f, bd.getFile());
    }

    @Test
    public void getFile_pathWithSpecialChars() throws IOException {
        File dir = tempFolder.newFolder("données été");
        File f = new File(dir, "test.dcm");
        f.createNewFile();
        String uri = f.toURI().toString();
        BulkData bd = new BulkData(null, uri, false);
        assertEquals(f, bd.getFile());
    }

    @Test
    public void getFile_withOffsetAndLength() throws IOException {
        File f = tempFolder.newFile("test.dcm");
        String uri = f.toURI().toString();
        BulkData bd = new BulkData(uri, 128, 1024, false);
        assertEquals(f, bd.getFile());
    }

    @Test
    public void getFile_pathWithSpacesAndOffsetLength() throws IOException {
        File dir = tempFolder.newFolder("my folder");
        File f = new File(dir, "test.dcm");
        f.createNewFile();
        String uri = f.toURI().toString();
        BulkData bd = new BulkData(uri, 128, 1024, false);
        assertEquals(f, bd.getFile());
    }

    @Test
    public void getFile_uncPathWithAuthority() {
        // Reproduces https://github.com/dcm4che/dcm4che/issues/1562
        // file: URI with authority component (e.g. WSL or network share)
        String uri = "file://wsl.localhost/Ubuntu-24.04/home/spe/test/AIRAmed%20Demo%20MS/test.pdf";
        BulkData bd = new BulkData(null, uri, false);
        File result = bd.getFile();
        assertEquals(new File("\\\\wsl.localhost/Ubuntu-24.04/home/spe/test/AIRAmed Demo MS/test.pdf"), result);
    }

    @Test
    public void getFile_uncPathWithAuthorityAndOffsetLength() {
        String uri = "file://wsl.localhost/Ubuntu-24.04/home/spe/test/AIRAmed%20Demo%20MS/test.pdf";
        BulkData bd = new BulkData(uri, 1030, 2820688, false);
        File result = bd.getFile();
        assertEquals(new File("\\\\wsl.localhost/Ubuntu-24.04/home/spe/test/AIRAmed Demo MS/test.pdf"), result);
    }
}

