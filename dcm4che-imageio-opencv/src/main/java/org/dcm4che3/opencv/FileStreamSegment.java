/*
 * **** BEGIN LICENSE BLOCK *****
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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.opencv;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.io.PathProvider;
import org.dcm4che3.io.RandomAccessFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nicolas Roduit
 * @since Mar 2018
 */
class FileStreamSegment extends StreamSegment {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStreamSegment.class);

    private final String filePath;

    FileStreamSegment(File file, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = file.getAbsolutePath();
    }

    FileStreamSegment(RandomAccessFile fdes, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = getFilePath(fdes);
    }

    FileStreamSegment(ExtendSegmentedInputImageStream stream) {
        super(stream.getSegmentPositions(), stream.getSegmentLengths(), stream.getImageDescriptor());
        this.filePath = stream.getFile().getAbsolutePath();
    }

    public String getFilePath() {
        return filePath;
    }

    public static String getFilePath(RandomAccessFile file) {
        if( file instanceof PathProvider) {
            return ((PathProvider) file).getPath();
        }
        try {
            Field fpath = RandomAccessFile.class.getDeclaredField("path");
            if (fpath != null) {
                fpath.setAccessible(true);
                return (String) fpath.get(file);
            }
        } catch (Exception e) {
            LOGGER.error("get path from RandomAccessFile", e); //$NON-NLS-1$
        }
        return null;
    }

    public static RandomAccessFile getRandomAccessFile(FileImageInputStream fstream) {
        if( fstream instanceof RandomAccessFileProvider ) {
            return ((RandomAccessFileProvider) fstream).getRandomAccessFile();
        }
        try {
            Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
            if (fRaf != null) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            LOGGER.error("getFileDescriptor from FileImageInputStream", e); //$NON-NLS-1$
        }
        return null;
    }

    public static RandomAccessFile getRandomAccessFile(FileImageOutputStream fstream) {
        try {
            Field fRaf = FileImageOutputStream.class.getDeclaredField("raf");
            if (fRaf != null) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            LOGGER.error("getFileDescriptor from FileImageOutputStream", e); //$NON-NLS-1$
        }
        return null;
    }
}
