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

package org.dcm4che.tool.dcmqrscp;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.AttributesValidator;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.net.Association;
import org.dcm4che.net.DimseRSP;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.AbstractCFindService;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class CFindService extends AbstractCFindService {

    private final Main main;
    private final String[] qrLevels;

    public CFindService(Main main, String sopClass, String... qrLevels) {
        super(main.getDevice(), sopClass);
        this.main = main;
        this.qrLevels = qrLevels;
    }

    @Override
    protected DimseRSP doCFind(Association as, PresentationContext pc,
            Attributes rq, Attributes keys, Attributes rsp)
            throws DicomServiceException {
        AttributesValidator validator = new AttributesValidator(keys);
        String level = validator.getType1String(
                Tag.QueryRetrieveLevel, 0, 1, qrLevels);
        check(rq, validator);
        String cuid = rq.getString(Tag.AffectedSOPClassUID, null);
        boolean relational = relational(as, cuid);
        boolean studyRoot =
                cuid.equals(UID.StudyRootQueryRetrieveInformationModelFIND);
        switch (level.charAt(1)) {
        case 'A':
            return new PatientQuery(as, rq, keys, rsp)
                    .validate(validator, relational, studyRoot);
        case 'T':
            return new StudyQuery(as, rq, keys, rsp)
                    .validate(validator, relational, studyRoot);
        case 'E':
            return new SeriesQuery(as, rq, keys, rsp)
                    .validate(validator, relational, studyRoot);
        case 'M':
            return new InstanceQuery(as, rq, keys, rsp)
                    .validate(validator, relational, studyRoot);
        }
        throw new AssertionError();
    }

    private void check(Attributes rq, AttributesValidator validator)
            throws DicomServiceException {
        if (validator.hasOffendingElements())
        throw new DicomServiceException(rq,
                Status.IdentifierDoesNotMatchSOPClass,
                validator.getErrorComment())
            .setOffendingElements(validator.getOffendingElements());
    }

    private boolean relational(Association as, String cuid) {
        ExtendedNegotiation extNeg = as.getAAssociateAC()
                .getExtNegotiationFor(cuid);
        byte[] info = extNeg != null ? extNeg.getInformation() : null;
        return info != null && info.length > 0 && info[0] == 1;
    }

    private void validateUniqueKey(AttributesValidator validator, int tag,
            boolean optional) {
        if (optional)
            validator.getType3String(tag, 0, 1, null);
        else
            validator.getType1String(tag, 0, 1);
        
    }

    private class PatientQuery implements DimseRSP {

        final Attributes rq;
        final Attributes keys;
        final Attributes rsp;
        Attributes dataset;
        boolean canceled;
        boolean finished;
        final String qrLevel;
        final String retrieveAET;
        final String patID;
        Attributes patRec;

        public PatientQuery(Association as, Attributes rq, Attributes keys,
                Attributes rsp) {
            this.rq = rq;
            this.keys = keys;
            this.rsp = rsp;
            this.qrLevel = keys.getString(Tag.QueryRetrieveLevel, null);
            this.retrieveAET = as.getCalledAET();
            this.patID = keys.getString(Tag.PatientID, null);
            // include Specific Character Set in result
            if (!keys.contains(Tag.SpecificCharacterSet))
                keys.setNull(Tag.SpecificCharacterSet, VR.CS);
        }

        public DimseRSP validate(AttributesValidator validator,
                boolean relational, boolean studyRoot)
                throws DicomServiceException {
            check(rq, validator);
            return this;
        }

        @Override
        public void cancel(Association a) throws IOException {
            canceled = true;
        }

        @Override
        public Attributes getCommand() {
            return rsp;
        }

        @Override
        public Attributes getDataset() {
            return dataset;
        }

        @Override
        public boolean next() throws DicomServiceException {
            if (finished)
                return false;

            try {
                int status;
                if (!canceled && nextMatch()) {
                    dataset = result();
                    status = pendingStatus();
                } else {
                    dataset = null;
                    finished = true;
                    status = canceled ? Status.Cancel : Status.Success;
                }
                rsp.setInt(Tag.Status, VR.US, status);
            } catch (IOException e) {
                throw new DicomServiceException(rq,
                        Status.ProcessingFailure,
                        e.getMessage());
            }
            return true;
        }

        private int pendingStatus() {
            Attributes unsupported = new Attributes();
            unsupported.addNotSelected(keys, dataset);
            unsupported.remove(Tag.SpecificCharacterSet);
            return unsupported.isEmpty() ? Status.Pending
                    : Status.PendingWarning;
        }

        protected boolean nextMatch() throws IOException {
            DicomDirReader ddr = main.getDicomDirReader();
            if (patRec != null) {
                if (patID != null)
                    return false;
                patRec = ddr.findNextDirectoryRecord(patRec, keys, true, true);
            } else {
                patRec = patID != null
                    ? ddr.findPatientRecord(patID)
                    : ddr.findRootDirectoryRecord(keys, true, true);
            }
            return patRec != null;
        }

        protected Attributes result() {
            Attributes result = new Attributes(keys.size());
            result.setString(Tag.QueryRetrieveLevel, VR.CS, qrLevel);
            result.setString(Tag.RetrieveAETitle, VR.AE, retrieveAET);
            String availability = main.getInstanceAvailability();
            if (availability != null)
                result.setString(Tag.InstanceAvailability, VR.CS, availability);
            DicomDirReader ddr = main.getDicomDirReader();
            result.setString(Tag.StorageMediaFileSetID, VR.SH,
                    ddr.getFileSetID());
            result.setString(Tag.StorageMediaFileSetUID, VR.UI,
                    ddr.getFileSetUID());
            return result.addSelected(patRec, keys);
        }
    }

    private class StudyQuery extends PatientQuery {

        final String[] studyIUIDs;
        Attributes studyRec;

        public StudyQuery(Association as, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(as, rq, keys, rsp);
            studyIUIDs = StringUtils.maskNull(
                    keys.getStrings(Tag.StudyInstanceUID));
        }

        @Override
        public DimseRSP validate(AttributesValidator validator,
                boolean relational, boolean studyRoot)
                throws DicomServiceException {
            validateUniqueKey(validator, Tag.PatientID, relational || studyRoot);
            return super.validate(validator, relational, studyRoot);
        }

        @Override
        protected boolean nextMatch() throws IOException {
            DicomDirReader ddr = main.getDicomDirReader();
            if (studyRec != null) {
                if (studyIUIDs.length == 1)
                    return false;
                studyRec = studyIUIDs.length == 0
                        ? ddr.findNextDirectoryRecord(studyRec, keys, true, true)
                        : ddr.findNextStudyRecord(studyRec, studyIUIDs);
                if (studyRec != null)
                    return true;
            }
            while (studyRec == null && super.nextMatch())
                studyRec = studyIUIDs.length == 0
                        ? ddr.findLowerDirectoryRecord(patRec, keys, true, true)
                        : ddr.findStudyRecord(patRec, studyIUIDs);
            return studyRec != null;
        }

        @Override
        protected Attributes result() {
            return super.result().addSelected(studyRec, keys);
        }
    }

    private class SeriesQuery extends StudyQuery {

        final String[] seriesIUIDs;
        Attributes seriesRec;

        public SeriesQuery(Association as, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(as, rq, keys, rsp);
            seriesIUIDs = StringUtils.maskNull(
                    keys.getStrings(Tag.SeriesInstanceUID));
        }

        @Override
        public DimseRSP validate(AttributesValidator validator,
                boolean relational, boolean studyRoot)
                throws DicomServiceException {
            validateUniqueKey(validator, Tag.StudyInstanceUID, relational);
            return super.validate(validator, relational, studyRoot);
        }

        @Override
        protected boolean nextMatch() throws IOException {
            DicomDirReader ddr = main.getDicomDirReader();
            if (seriesRec != null) {
                if (seriesIUIDs.length == 1)
                    return false;
                seriesRec = seriesIUIDs.length == 0
                        ? ddr.findNextDirectoryRecord(seriesRec, keys, true, true)
                        : ddr.findNextSeriesRecord(seriesRec, seriesIUIDs);
                if (seriesRec != null)
                    return true;
            }
            while (seriesRec == null && super.nextMatch())
                seriesRec = seriesIUIDs.length == 0
                        ? ddr.findLowerDirectoryRecord(studyRec, keys, true, true)
                        : ddr.findSeriesRecord(studyRec, seriesIUIDs);
            return seriesRec != null;
        }

        @Override
        protected Attributes result() {
             return super.result().addSelected(seriesRec, keys);
        }
    }

    private class InstanceQuery extends SeriesQuery {

        final String[] sopIUIDs;
        final boolean selectSOPClassUID;
        final boolean selectSOPInstanceUID;
        Attributes instRec;

        public InstanceQuery(Association as, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(as, rq, keys, rsp);
            sopIUIDs = StringUtils.maskNull(keys.getStrings(Tag.SOPInstanceUID));
            selectSOPClassUID = keys.contains(Tag.SOPClassUID);
            selectSOPInstanceUID = keys.contains(Tag.SOPInstanceUID);
        }

        @Override
        public DimseRSP validate(AttributesValidator validator,
                boolean relational, boolean studyRoot)
                throws DicomServiceException {
            validateUniqueKey(validator, Tag.SeriesInstanceUID, relational);
            return super.validate(validator, relational, studyRoot);
        }

        @Override
        protected boolean nextMatch() throws IOException {
            DicomDirReader ddr = main.getDicomDirReader();
            if (instRec != null) {
                if (sopIUIDs.length == 1)
                    return false;
                instRec = sopIUIDs.length == 0
                        ? ddr.findNextDirectoryRecord(instRec, keys, true, true)
                        : ddr.findNextInstanceRecord(instRec, sopIUIDs);
                if (instRec != null)
                    return true;
            }
            while (instRec == null && super.nextMatch())
                instRec = sopIUIDs.length == 0
                        ? ddr.findLowerDirectoryRecord(seriesRec, keys, true, true)
                        : ddr.findInstanceRecord(seriesRec, sopIUIDs);
            return instRec != null;
        }

        @Override
        protected Attributes result() {
            Attributes result = super.result().addSelected(instRec, keys);
            if (selectSOPClassUID)
                result.setString(Tag.SOPClassUID, VR.UI,
                        instRec.getString(Tag.ReferencedSOPClassUIDInFile, null));
            if (selectSOPInstanceUID)
                result.setString(Tag.SOPInstanceUID, VR.UI,
                        instRec.getString(Tag.ReferencedSOPInstanceUIDInFile, null));
            return result;
        }
    }

}
