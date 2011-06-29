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

package org.dcm4che.net.service;

import java.io.File;
import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.net.Association;
import org.dcm4che.net.Commands;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicCStoreSCP extends DicomService implements CStoreSCP {

    public BasicCStoreSCP(String... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onCStoreRQ(Association as, PresentationContext pc,
            Attributes rq, PDVInputStream data) throws IOException {
        Attributes rsp = Commands.mkRSP(rq, Status.Success);
        store(as, rq, data, pc.getTransferSyntax(), rsp);
        as.writeDimseRSP(pc, rsp);
    }

    protected void store(Association as, Attributes rq, PDVInputStream data,
            String tsuid, Attributes rsp) throws IOException {
        DicomInputStream in = new DicomInputStream(data, tsuid);
        configure(in);
        try {
            Attributes ds = in.readDataset(-1, -1);
            store(as, rq, ds, tsuid, rsp);
        } finally {
            dispose(in);
        }
    }

    protected void configure(DicomInputStream in) {
        
    }

    protected void dispose(DicomInputStream in) {
        for (File f : in.getBulkDataFiles()) {
            f.delete();
        }
    }

    protected void store(Association as, Attributes rq, Attributes ds,
            String tsuid, Attributes rsp) throws DicomServiceException {
        Attributes fmi = createFileMetaInformation(as, rq, ds, tsuid);
        File dir = selectDirectory(as, rq, ds);
        File file = createFile(dir, as, rq, ds);
        try {
            store(as, rq, ds, fmi, dir, file, rsp);
            file = null;
        } finally {
            if (file != null)
                file.delete();
        }
    }

    protected void store(Association as, Attributes rq, Attributes ds,
            Attributes fmi, File dir, File file, Attributes rsp)
            throws DicomServiceException {
        DicomOutputStream out = null;
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(fmi, ds);
        } catch (IOException e) {
            throw new DicomServiceException(rq, Status.OutOfResources, e);
        } finally {
            SafeClose.close(out);
        }
    }

    protected Attributes createFileMetaInformation(Association as, Attributes rq,
            Attributes ds, String tsuid) {
        return Attributes.createFileMetaInformation(
                rq.getString(Tag.AffectedSOPInstanceUID, null),
                rq.getString(Tag.AffectedSOPClassUID, null),
                tsuid);
    }

    protected File selectDirectory(Association as, Attributes rq, Attributes ds) {
        return new File(".");
    }

    protected File createFile(File dir, Association as, Attributes rq,
            Attributes ds) {
        return new File(dir, rq.getString(Tag.AffectedSOPInstanceUID, null));
    }


}
