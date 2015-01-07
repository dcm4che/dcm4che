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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class FileCache {

    private static final Logger LOG = LoggerFactory.getLogger(FileCache.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private Path fileCacheRootDirectory;
    private Path journalRootDirectory;
    private String journalFileName = "journal";
    private String orphanedFileName = "orphaned";
    private String journalDirectoryName = "journal.d";
    private SimpleDateFormat journalFileNamePattern =
            new SimpleDateFormat("yyyyMMdd/HHmmss.SSS");
    private int journalFileSize = 100;
    private boolean leastRecentlyUsed;
    private int currentJournalFileSize = -1;
    private final AtomicBoolean freeIsRunning = new AtomicBoolean();

    public Path getFileCacheRootDirectory() {
        return fileCacheRootDirectory;
    }

    public void setFileCacheRootDirectory(Path fileCacheRootDirectory) {
        this.fileCacheRootDirectory = fileCacheRootDirectory;
    }

    public Path getJournalRootDirectory() {
        return journalRootDirectory;
    }

    public void setJournalRootDirectory(Path journalRootDirectory) {
        this.journalRootDirectory = journalRootDirectory;
    }

    public String getJournalFileName() {
        return journalFileName;
    }

    public void setJournalFileName(String journalFileName) {
        this.journalFileName = journalFileName;
    }

    public Path getJournalFile() {
        return journalRootDirectory.resolve(journalFileName);
    }

    public String getJournalDirectoryName() {
        return journalDirectoryName;
    }

    public void setJournalDirectoryName(String journalDirectoryName) {
        this.journalDirectoryName = journalDirectoryName;
    }

    public Path getJournalDirectory() {
        return journalRootDirectory.resolve(journalDirectoryName);
    }

    public String getJournalFileNamePattern() {
        return journalFileNamePattern.toPattern();
    }

    public void setJournalFileNamePattern(String pattern) {
        this.journalFileNamePattern = new SimpleDateFormat(pattern);
    }

    public int getJournalFileSize() {
        return journalFileSize;
    }

    public void setJournalFileSize(int journalFileSize) {
        this.journalFileSize = journalFileSize;
    }

    public String getOrphanedFileName() {
        return orphanedFileName;
    }

    public void setOrphanedFileName(String orphanedFileName) {
        this.orphanedFileName = orphanedFileName;
    }

    public Path getOrphanedFile() {
        return journalRootDirectory.resolve(orphanedFileName);
    }

    public Collection<Path> getOrphanedFiles() throws IOException {
        Path orphanFile = getOrphanedFile();
        if (Files.notExists(orphanFile))
            return Collections.emptyList();
        
        ArrayList<Path> files = new ArrayList<Path>();
        try (BufferedReader r = Files.newBufferedReader(
                orphanFile, UTF_8)) {
            String fileName;
            while ((fileName = r.readLine()) != null) {
                files.add(fileCacheRootDirectory.resolve(fileName));
            }
        }
        return files;
    }

    public boolean isLeastRecentlyUsed() {
        return leastRecentlyUsed;
    }

    public void setLeastRecentlyUsed(boolean leastRecentlyUsed) {
        this.leastRecentlyUsed = leastRecentlyUsed;
    }

    @Override 
    public String toString() {
        return "FileCache[cacheDir=" + fileCacheRootDirectory
                + ", journalDir=" + journalRootDirectory + "]";
    }

    public synchronized void register(Path path) throws IOException {
        LOG.debug("{}: registering - {}", this, path);
        Files.createDirectories(journalRootDirectory);
        Path journalFile = getJournalFile();
        String entry = fileCacheRootDirectory.relativize(path).toString();
        int size = currentJournalFileSize;
        if (size < 0)
            size = countLines(journalFile);
        if (size >= journalFileSize) {
            Path dir = getJournalDirectory();
            Path target = dir.resolve(
                    journalFileNamePattern.format(new Date()));
            LOG.debug("{}: journalFileSize[{}] exeeded, move {} to {}", 
                    this, journalFileSize, journalFile, target);
            Files.createDirectories(target.getParent());
            Files.move(journalFile, target);
            size = 0;
        }
        Files.write(journalFile, Collections.singleton(entry), UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        currentJournalFileSize = size + 1;
        if (leastRecentlyUsed) {
            try {
                LOG.debug("{}: update modification time of - {}", this, path);
                Files.setLastModifiedTime(path, Files.getLastModifiedTime(journalFile));
            } catch (IOException e) {
                LOG.info("{}: failed to update modification time of - {}", this, path, e);
            }
        }
        LOG.debug("{}: registered - {}", this, path);
    }

    public long free(long size) throws IOException {
        LOG.info("{}: try to free {} bytes", this, size);
        if (!freeIsRunning.compareAndSet(false, true)) {
            LOG.info("{}: free already running", this);
            return -1;
        }

        try {
            long freed = free(getJournalDirectory(), size);
            LOG.info("{}: freed {} bytes", this, freed);
            return freed;
        } finally {
            freeIsRunning.set(false);
        }
    }

    private long free(Path dir, long size) throws IOException {
        long remaining = size;
        for (Path file : listFiles(dir)) {
            if (Files.isDirectory(file)) {
                remaining -= free(file, remaining);
            } else {
                remaining -= free(file);
            }
            if (remaining <= 0)
                break;
        }
        return size - remaining;
    }

    private Collection<Path> listFiles(Path dir) throws IOException {
        TreeSet<Path> files = new TreeSet<Path>();
        try (DirectoryStream<Path> dirPath = Files.newDirectoryStream(dir)) {
            for (Path path : dirPath)
                files.add(path);
        }
        return files;
    }

    public void clear() throws IOException {
        LOG.info("{}: clearing", this);
        deleteDirContent(fileCacheRootDirectory);
        deleteDirContent(journalRootDirectory);
        LOG.info("{}: cleared", this);
    }

    private int countLines(Path journalFile) throws IOException {
        int lines = 0;
        if (Files.exists(journalFile))
            try (BufferedReader r = Files.newBufferedReader(journalFile, UTF_8)) {
                while (r.readLine() == null)
                    lines ++;
            }
        return lines;
    }

    private static void deleteDirContent(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try ( DirectoryStream<Path> in = Files.newDirectoryStream(dir) ) {
                for (Path path : in) {
                    if (Files.isDirectory(path))
                        deleteDirContent(path);
                    Files.delete(path);
                }
            }
        }
    }

    private long free(Path journalFile) throws IOException {
        LOG.debug("{}: deleting files referenced by journal - {}",
                this, journalFile);
        long freed = 0L;
        FileTime lastModifiedTime = Files.getLastModifiedTime(journalFile);
        try (BufferedReader r = Files.newBufferedReader(
                journalFile, UTF_8)) {
            String fileName;
            while ((fileName = r.readLine()) != null) {
                Path path = fileCacheRootDirectory.resolve(fileName);
                if (Files.notExists(path)) {
                    LOG.debug("{}: {} already deleted");
                    continue;
                }
                if (leastRecentlyUsed) {
                    try {
                        if (Files.getLastModifiedTime(path)
                                .compareTo(lastModifiedTime) > 0)  {
                            LOG.debug("{}: {} recently accessed - do not delete",
                                    this, path);
                            continue;
                        }
                    } catch (IOException e) {
                        LOG.info("{}: failed to get modification time of - {}",
                                this, path, e);
                    }
                }
                try {
                    LOG.debug("{}: delete - {}", this, path);
                    long fileSize = Files.size(path);
                    Files.delete(path);
                    freed += fileSize;
                    purgeEmptyDirectories(path);
                } catch (IOException e) {
                    LOG.warn("{}: failed to delete - {}", this, path, e);
                    try {
                        Path orphanedFile = getOrphanedFile();
                        Files.write(orphanedFile, Collections.singleton(fileName), UTF_8,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException e2) {
                        LOG.warn("{}: failed to record orphaned file - {}",
                                this, path, e2);
                    }
                }
            }
        }
        try {
            LOG.debug("{}: delete journal - {}", this, journalFile);
            Files.delete(journalFile);
        } catch (IOException e) {
            LOG.warn("{}: failed to delete journal - {}", this, journalFile, e);
        }
        LOG.debug("{}: deleted files referenced by journal - {} - freed {} bytes",
                this, journalFile, freed);
        return freed;
    }

    private void purgeEmptyDirectories(Path path) {
        while (!(path = path.getParent()).equals(fileCacheRootDirectory))
            try {
                Files.delete(path);
                LOG.debug("{}: purged empty directory - {}", this, path);
            } catch (DirectoryNotEmptyException e) {
                return;
            } catch (IOException e) {
                LOG.warn("{}: failed to purge empty directory {}", this, path, e);
            }
    }

}
