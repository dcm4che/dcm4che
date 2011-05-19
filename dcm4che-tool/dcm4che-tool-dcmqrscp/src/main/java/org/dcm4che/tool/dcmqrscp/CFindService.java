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
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCFindService;
import org.dcm4che.net.service.BasicQueryTask;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.net.service.QueryTask;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class CFindService extends BasicCFindService {

    private final Main main;
    private final String[] qrLevels;

    public CFindService(Main main, String sopClass, String... qrLevels) {
        super(main.getDevice(), sopClass);
        this.main = main;
        this.qrLevels = qrLevels;
    }

    @Override
    protected QueryTask createQueryTask(Association as, PresentationContext pc,
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
            return new PatientQueryTask(as, pc, rq, keys, rsp);
        case 'T':
            validateStudyQuery(rq, validator, relational, studyRoot);
            return new StudyQueryTask(as, pc, rq, keys, rsp);
        case 'E':
            validateSeriesQuery(rq, validator, relational, studyRoot);
            return new SeriesQueryTask(as, pc, rq, keys, rsp);
        case 'M':
            validateInstanceQuery(rq, validator, relational, studyRoot);
            return new InstanceQueryTask(as, pc, rq, keys, rsp);
        }
        throw new AssertionError();
    }

    private static void validateStudyQuery(Attributes rq,
            AttributesValidator validator, boolean relational,
            boolean studyRoot) throws DicomServiceException {
        validateUniqueKey(validator, Tag.PatientID, relational || studyRoot);
        check(rq, validator);
    }

    private void validateSeriesQuery(Attributes rq,
            AttributesValidator validator, boolean relational,
            boolean studyRoot) throws DicomServiceException {
        validateUniqueKey(validator, Tag.StudyInstanceUID, relational);
        validateUniqueKey(validator, Tag.PatientID, relational || studyRoot);
        check(rq, validator);
    }

    private void validateInstanceQuery(Attributes rq,
            AttributesValidator validator, boolean relational,
            boolean studyRoot) throws DicomServiceException {
        validateUniqueKey(validator, Tag.SeriesInstanceUID, relational);
        validateUniqueKey(validator, Tag.StudyInstanceUID, relational);
        validateUniqueKey(validator, Tag.PatientID, relational || studyRoot);
        check(rq, validator);
    }

    private static void check(Attributes rq, AttributesValidator validator)
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

    private static void validateUniqueKey(AttributesValidator validator,
            int tag, boolean optional) {
        if (optional)
            validator.getType3String(tag, 0, 1, null);
        else
            validator.getType1String(tag, 0, 1);
        
    }

    private class PatientQueryTask extends BasicQueryTask {

        final String qrLevel;
        final String retrieveAET;
        final String patID;
        Attributes patRec;

        public PatientQueryTask(Association as, PresentationContext pc,
                Attributes rq, Attributes keys, Attributes rsp)
                throws DicomServiceException {
            super(as, pc, rq, keys, rsp);
            this.qrLevel = keys.getString(Tag.QueryRetrieveLevel, null);
            this.retrieveAET = as.getCalledAET();
            this.patID = keys.getString(Tag.PatientID, null);
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

        private void wrappedFindNextPatient() throws DicomServiceException {
            try {
                findNextPatient();
            } catch (IOException e) {
                throw new DicomServiceException(rq, Status.ProcessingFailure);
            }
        }

        protected boolean findNextPatient() throws IOException {
            DicomDirReader ddr = main.getDicomDirReader();
            patRec = patRec == null
                ? patID == null
                    ? ddr.findRootDirectoryRecord(keys, true, true)
                    : ddr.findPatientRecord(patID)
                : patID == null
                    ? ddr.findNextDirectoryRecord(patRec, keys, true, true)
                    : null;
            return patRec != null;
        }

        @Override
        protected Attributes adjust(Attributes match) {
            Attributes adjust = super.adjust(match);
            adjust.setString(Tag.QueryRetrieveLevel, VR.CS, qrLevel);
            adjust.setString(Tag.RetrieveAETitle, VR.AE, retrieveAET);
            String availability = main.getInstanceAvailability();
            if (availability != null)
                adjust.setString(Tag.InstanceAvailability, VR.CS, availability);
            DicomDirReader ddr = main.getDicomDirReader();
            adjust.setString(Tag.StorageMediaFileSetID, VR.SH,
                    ddr.getFileSetID());
            adjust.setString(Tag.StorageMediaFileSetUID, VR.UI,
                    ddr.getFileSetUID());
            return adjust;
        }
    }

    private class StudyQueryTask extends PatientQueryTask {

        final String[] studyIUIDs;
        Attributes studyRec;

        public StudyQueryTask(Association as, PresentationContext pc,
                Attributes rq, Attributes keys, Attributes rsp)
                throws DicomServiceException {
            super(as, pc, rq, keys, rsp);
            studyIUIDs = StringUtils.maskNull(
                    keys.getStrings(Tag.StudyInstanceUID));
            wrappedFindNextStudy();
        }

        @Override
        public boolean hasMoreMatches() throws DicomServiceException {
            return studyRec != null;
        }

        @Override
        public Attributes nextMatch() throws DicomServiceException {
            Attributes tmp = studyRec;
            wrappedFindNextStudy();
            return tmp;
        }

        private void wrappedFindNextStudy() throws DicomServiceException {
            try {
                findNextStudy();
            } catch (IOException e) {
                throw new DicomServiceException(rq, Status.ProcessingFailure);
            }
        }

        protected boolean findNextStudy() throws IOException {
            if (patRec == null)
                return false;

            DicomDirReader ddr = main.getDicomDirReader();
            if (studyRec != null) {
                studyRec = studyIUIDs.length == 1
                    ? null
                    : studyIUIDs.length == 0
                        ? ddr.findNextDirectoryRecord(studyRec, keys, true, true)
                        : ddr.findNextStudyRecord(studyRec, studyIUIDs);
            }
            while (studyRec == null && super.findNextPatient())
                studyRec = studyIUIDs.length == 0
                        ? ddr.findLowerDirectoryRecord(patRec, keys, true, true)
                        : ddr.findStudyRecord(patRec, studyIUIDs);
            return studyRec != null;
        }
    }

    private class SeriesQueryTask extends StudyQueryTask {

        final String[] seriesIUIDs;
        Attributes seriesRec;

        public SeriesQueryTask(Association as, PresentationContext pc,
                Attributes rq, Attributes keys, Attributes rsp)
                throws DicomServiceException {
            super(as, pc, rq, keys, rsp);
            seriesIUIDs = StringUtils.maskNull(
                    keys.getStrings(Tag.SeriesInstanceUID));
            wrappedFindNextSeries();
       }

        @Override
        public boolean hasMoreMatches() throws DicomServiceException {
            return seriesRec != null;
        }

        @Override
        public Attributes nextMatch() throws DicomServiceException {
            Attributes tmp = seriesRec;
            wrappedFindNextSeries();
            return tmp;
        }

        private void wrappedFindNextSeries() throws DicomServiceException {
            try {
                findNextSeries();
            } catch (IOException e) {
                throw new DicomServiceException(rq, Status.ProcessingFailure);
            }
        }

        protected boolean findNextSeries() throws IOException {
            if (studyRec == null)
                return false;

            DicomDirReader ddr = main.getDicomDirReader();
            if (seriesRec != null) {
                seriesRec = seriesIUIDs.length == 1
                    ? null
                    : seriesIUIDs.length == 0
                        ? ddr.findNextDirectoryRecord(seriesRec, keys, true, true)
                        : ddr.findNextSeriesRecord(seriesRec, seriesIUIDs);
            }
            while (seriesRec == null && super.findNextStudy())
                seriesRec = seriesIUIDs.length == 0
                    ? ddr.findLowerDirectoryRecord(studyRec, keys, true, true)
                    : ddr.findSeriesRecord(studyRec, seriesIUIDs);
            return seriesRec != null;
        }
    }

    private class InstanceQueryTask extends SeriesQueryTask {

        final String[] sopIUIDs;
        Attributes instRec;

        public InstanceQueryTask(Association as, PresentationContext pc,
                Attributes rq, Attributes keys, Attributes rsp)
                throws DicomServiceException {
            super(as, pc, rq, keys, rsp);
            sopIUIDs = StringUtils.maskNull(keys.getStrings(Tag.SOPInstanceUID));
            wrappedFindNextInstance();
        }

        @Override
        public boolean hasMoreMatches() throws DicomServiceException {
            return instRec != null;
        }

        @Override
        public Attributes nextMatch() throws DicomServiceException {
            Attributes tmp = instRec;
            wrappedFindNextInstance();
            return tmp;
        }

        private void wrappedFindNextInstance() throws DicomServiceException {
            try {
                findNextInstance();
            } catch (IOException e) {
                throw new DicomServiceException(rq, Status.ProcessingFailure);
            }
        }

        protected boolean findNextInstance() throws IOException {
            if (seriesRec == null)
                return false;

            DicomDirReader ddr = main.getDicomDirReader();
            if (instRec != null) {
                instRec = sopIUIDs.length == 1
                    ? null
                    : sopIUIDs.length == 0
                        ? ddr.findNextDirectoryRecord(instRec, keys, true, true)
                        : ddr.findNextInstanceRecord(instRec, sopIUIDs);
            }
            while (instRec == null && super.findNextSeries())
                instRec = sopIUIDs.length == 0
                        ? ddr.findLowerDirectoryRecord(seriesRec, keys, true, true)
                        : ddr.findInstanceRecord(seriesRec, sopIUIDs);
            return instRec != null;
        }

        @Override
        protected Attributes adjust(Attributes match) {
            match.setString(Tag.SOPClassUID, VR.UI,
                    match.getString(Tag.ReferencedSOPClassUIDInFile, null));
            match.setString(Tag.SOPInstanceUID, VR.UI,
                    match.getString(Tag.ReferencedSOPInstanceUIDInFile, null));
            return super.adjust(match);
        }
    }

}
