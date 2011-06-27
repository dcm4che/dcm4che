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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che.tool.common;

import java.io.File;
import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.net.Association;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCStoreSCP;
import org.dcm4che.util.FilePathFormat;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CStoreService extends BasicCStoreSCP {

    private static final String PART = ".part";

    private File directory;
    private FilePathFormat filePathFormat;

    public CStoreService(String... sopClasses) {
        super(sopClasses);
    }

    public final void setDirectory(File directory) {
        this.directory = directory;
    }

    public final void setFilePathFormat(FilePathFormat filePathFormat) {
        this.filePathFormat = filePathFormat;
    }

    @Override
    protected void doCStore(Association as, PresentationContext pc,
            Attributes rq, PDVInputStream data, Attributes rsp)
            throws IOException {
        if (directory == null)
            data.skipAll();
        else
            storeToFile(pc, rq, data);
    }

    private void storeToFile(PresentationContext pc, Attributes rq,
            PDVInputStream data) throws IOException {
        String fileID = rq.getString(Tag.AffectedSOPClassUID, null);
        File tmpFile = new File(directory, fileID + PART);
        try {
            System.out.println("M-WRITE " + tmpFile);
            storeToFile(pc, rq, data, tmpFile);
            if (filePathFormat != null) {
                Attributes attrs = readAttrs(tmpFile);
                fileID = accept(attrs) ? filePathFormat.format(attrs) : null;
            }
            if (fileID != null) {
                File newFile = new File(directory, fileID);
                if (filePathFormat != null)
                    mkdirs(newFile.getParentFile());
                if (tmpFile.renameTo(newFile))
                    System.out.println("M-RENAME " + tmpFile + " to " + newFile);
                else
                    System.out.println("Failed to rename " + tmpFile 
                            + " to " + newFile);
            }
        } finally {
            if (tmpFile.delete())
                System.out.println("M-DELETE " + tmpFile);
        }
    }

    private void mkdirs(File dir) {
        if (dir.mkdirs())
            System.out.println("M-WRITE " + dir);
    }

    protected boolean accept(Attributes attrs) {
        return true;
    }

    private static Attributes readAttrs(File f) throws IOException {
        DicomInputStream in = new DicomInputStream(f);
        try {
            in.setIncludeBulkData(false);
            return in.readDataset(-1, Tag.PixelData);
        } finally {
            in.close();
        }
    }

    private void storeToFile(PresentationContext pc, Attributes rq,
            PDVInputStream data, File f) throws IOException {
        DicomOutputStream out = new DicomOutputStream(f);
        try {
            out.writeFileMetaInformation(
                    Attributes.createFileMetaInformation(
                            rq.getString(Tag.AffectedSOPInstanceUID, null),
                            rq.getString(Tag.AffectedSOPClassUID, null),
                            pc.getTransferSyntax()));
            data.copyTo(out);
        } finally {
            out.close();
        }
    }

}
