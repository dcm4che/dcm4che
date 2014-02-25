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

package org.dcm4che3.tool.dcmqrscp;

import java.io.IOException;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicQueryTask;
import org.dcm4che3.net.service.DicomServiceException;

class PatientQueryTask extends BasicQueryTask {

    protected final DicomDirReader ddr;
    protected final String availability;
    protected final String[] patIDs;
    protected Attributes patRec;

    public PatientQueryTask(Association as, PresentationContext pc, Attributes rq, Attributes keys,
            DicomDirReader ddr, String availability) throws DicomServiceException {
        super(as, pc, rq, keys);
        this.ddr = ddr;
        this.availability = availability;
        this.patIDs = keys.getStrings(Tag.PatientID);
        wrappedFindNextPatient();
    }

    @Override
    public boolean hasMoreMatches() throws DicomServiceException {
        return patRec != null;
    }

    @Override
    public Attributes nextMatch() throws DicomServiceException {
        Attributes tmp = patRec;
        wrappedFindNextPatient();
        return tmp;
    }

    @Override
    protected Attributes adjust(Attributes match) {
        Attributes adjust = super.adjust(match);
        adjust.remove(Tag.DirectoryRecordType);
        if (keys.contains(Tag.SOPClassUID))
             adjust.setString(Tag.SOPClassUID, VR.UI,
                     match.getString(Tag.ReferencedSOPClassUIDInFile));
        if (keys.contains(Tag.SOPInstanceUID))
             adjust.setString(Tag.SOPInstanceUID, VR.UI,
                     match.getString(Tag.ReferencedSOPInstanceUIDInFile));
        adjust.setString(Tag.QueryRetrieveLevel, VR.CS,
                keys.getString(Tag.QueryRetrieveLevel));
        adjust.setString(Tag.RetrieveAETitle, VR.AE, as.getCalledAET());
        if (availability != null)
            adjust.setString(Tag.InstanceAvailability, VR.CS, availability);
        adjust.setString(Tag.StorageMediaFileSetID, VR.SH, ddr.getFileSetID());
        adjust.setString(Tag.StorageMediaFileSetUID, VR.UI, ddr.getFileSetUID());
        match.setString(Tag.SOPClassUID, VR.UI,
                match.getString(Tag.ReferencedSOPClassUIDInFile));
        match.setString(Tag.SOPInstanceUID, VR.UI,
                match.getString(Tag.ReferencedSOPInstanceUIDInFile));

        return adjust;
    }

    private void wrappedFindNextPatient() throws DicomServiceException {
        try {
            findNextPatient();
        } catch (IOException e) {
            throw wrapException(Status.UnableToProcess, e);
        }
    }

    protected boolean findNextPatient() throws IOException {
        if (patRec == null)
            patRec = ddr.findPatientRecord(patIDs);
        else if (patIDs != null && patIDs.length == 1)
            patRec = null;
        else
            patRec = ddr.findNextPatientRecord(patRec, patIDs);

        return patRec != null;
    }
}