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
import org.dcm4che.data.VR;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.net.Association;
import org.dcm4che.net.DimseRSP;
import org.dcm4che.net.Status;
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
        String level = validator.getType1String(Tag.QueryRetrieveLevel, 0,
                qrLevels);
        if (validator.hasOffendingElements())
            throw new DicomServiceException(rq,
                    Status.IdentifierDoesNotMatchSOPClass,
                    validator.getErrorComment())
                .setOffendingElements(Tag.QueryRetrieveLevel);
        switch (level.charAt(1)) {
        case 'A':
            return new PatientQuery(rq, keys, rsp);
        case 'T':
            return new StudyQuery(rq, keys, rsp);
        case 'E':
            return new SeriesQuery(rq, keys, rsp);
        case 'M':
            return new InstanceQuery(rq, keys, rsp);
        }
        throw new AssertionError();
    }

    private class PatientQuery implements DimseRSP {

        final Attributes rq;
        final Attributes keys;
        final Attributes rsp;
        Attributes dataset;
        boolean canceled;
        boolean finished;
        final String qrLevel;
        final String patID;
        Attributes patRec;

        public PatientQuery(Attributes rq, Attributes keys, Attributes rsp) {
            this.rq = rq;
            this.keys = keys;
            this.rsp = rsp;
            this.qrLevel = keys.getString(Tag.QueryRetrieveLevel, null);
            this.patID = keys.getString(Tag.PatientID, null);
            // include Specific Character Set in result
            if (!keys.contains(Tag.SpecificCharacterSet))
                keys.setNull(Tag.SpecificCharacterSet, VR.CS);
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
                if (!canceled && nextMatch()) {
                    dataset = result();
                } else {
                    dataset = null;
                    finished = true;
                    rsp.setInt(Tag.Status, VR.US,
                            canceled ? Status.Cancel : Status.Success);
                }
            } catch (IOException e) {
                throw new DicomServiceException(rq,
                        Status.ProcessingFailure,
                        e.getMessage());
            }
            return true;
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
            return result.addSelected(patRec, keys);
        }
    }

    private class StudyQuery extends PatientQuery {

        final String[] studyIUIDs;
        Attributes studyRec;

        public StudyQuery(Attributes rq, Attributes keys, Attributes rsp) {
            super(rq, keys, rsp);
            studyIUIDs = StringUtils.maskNull(
                    keys.getStrings(Tag.StudyInstanceUID));
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

        public SeriesQuery(Attributes rq, Attributes keys, Attributes rsp) {
            super(rq, keys, rsp);
            seriesIUIDs = StringUtils.maskNull(
                    keys.getStrings(Tag.SeriesInstanceUID));
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

        public InstanceQuery(Attributes rq, Attributes keys, Attributes rsp) {
            super(rq, keys, rsp);
            sopIUIDs = StringUtils.maskNull(keys.getStrings(Tag.SOPInstanceUID));
            selectSOPClassUID = keys.contains(Tag.SOPClassUID);
            selectSOPInstanceUID = keys.contains(Tag.SOPInstanceUID);
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
