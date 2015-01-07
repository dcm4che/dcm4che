/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.filecache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class FileCacheTest {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String b_c = Paths.get("b", "c").toString();
    private static final String[] FILES1 = { "a", b_c};
    private static final String[] FILES2 = { "d", "e" };
    private static final String[] FILES3 = { "f", "a" };
    private static final String[] DELETED = { "a", b_c, "b" };
    private static final String[] DELETED_LRU = { b_c, "b", "d", "e" };
    private static final String[] NOT_DELETED = { "d", "e", "f" };
    private static final String[] NOT_DELETED_LRU = FILES3;
    private static final Path CACHE_ROOT_DIR = Paths.get("target", "filecache");
    private static final Path JOURNAL_ROOT_DIR = Paths.get("target", "journaldir");
    private static final String JOURNAL_FILE_NAME_PATTERN = "20150107/HHmmss.SSS";
    private static final int FILE_SIZE = 1000;
    private static final long FREED = FILE_SIZE * 2;
    private static final long FREED_LRU = FILE_SIZE * 3;
    private static final long DELAY = 1L;
    private static final long DELAY_LRU = 1000L;

    private FileCache fileCache = new FileCache();

    @Before
    public void setUp() throws Exception {
        fileCache.setFileCacheRootDirectory(CACHE_ROOT_DIR);
        fileCache.setJournalRootDirectory(JOURNAL_ROOT_DIR);
        fileCache.setJournalFileNamePattern(JOURNAL_FILE_NAME_PATTERN);
        fileCache.setJournalFileSize(2);
        fileCache.clear();
    }

    @Test
    public void testRegister() throws Exception {
        registerFiles(DELAY);
        assertEquals(Arrays.asList(FILES3), 
                Files.readAllLines(fileCache.getJournalFile(), UTF_8));
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(
                fileCache.getJournalDirectory().resolve("20150107"))) {
            Iterator<Path> iter = dir.iterator();
            assertTrue(iter.hasNext());
            Path path1 = iter.next();
            assertTrue(iter.hasNext());
            Path path2 = iter.next();
            assertFalse(iter.hasNext());
            if (path1.compareTo(path2) > 0) {
                Path tmp = path1;
                path1 = path2;
                path2 = tmp;
            }
            assertEquals(Arrays.asList(FILES1), Files.readAllLines(path1, UTF_8));
            assertEquals(Arrays.asList(FILES2), Files.readAllLines(path2, UTF_8));
        }
    }

    private void registerFiles(long delay) throws Exception {
        for (String file : FILES1)
            fileCache.register(createFile(file));
        Thread.sleep(delay);
        for (String file : FILES2)
            fileCache.register(createFile(file));
        Thread.sleep(delay);
        for (String file : FILES3)
            fileCache.register(createFile(file));
    }

    private Path createFile(String file) throws IOException {
        Path path = toPath(file);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[FILE_SIZE]);
        return path;
    }

    private Path toPath(String file) {
        return CACHE_ROOT_DIR.resolve(file);
    }

    @Test
    public void testFreeFIFO() throws Exception {
        registerFiles(DELAY);
        assertEquals(FREED, fileCache.free(FREED));
        assertNotExists(DELETED);
        assertExists(NOT_DELETED);
    }

    @Test
    public void testFreeLRU() throws Exception {
        fileCache.setCacheAlgorithm(FileCache.Algorithm.LRU);
        registerFiles(DELAY_LRU);
        assertEquals(FREED_LRU, fileCache.free(FREED));
        assertNotExists(DELETED_LRU);
        assertExists(NOT_DELETED_LRU);
    }

    private void assertNotExists(String[] files) {
        for (String file : files)
            assertTrue(Files.notExists(toPath(file)));
    }

    private void assertExists(String[] files) {
        for (String file : files)
            assertTrue(Files.exists(toPath(file)));
    }

}
