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
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.net.Association;
import org.dcm4che.net.DimseRSP;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
abstract class CFindRSP implements DimseRSP {

    static class Patient extends CFindRSP {

        public Patient(Main main, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(main, rq, keys, rsp);
        }

        @Override
        protected Attributes nextData() throws IOException {
            return findNextPatientRecord();
        }

    }

    static class Study extends CFindRSP {

        public Study(Main main, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(main, rq, keys, rsp);
        }

        @Override
        protected Attributes nextData() throws IOException {
            return findNextStudyRecord();
        }

    }

    static class Series extends CFindRSP {

        public Series(Main main, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(main, rq, keys, rsp);
        }

        @Override
        protected Attributes nextData() throws IOException {
            return findNextSeriesRecord();
        }

    }

    static class Instance extends CFindRSP {

        public Instance(Main main, Attributes rq, Attributes keys,
                Attributes rsp) {
            super(main, rq, keys, rsp);
        }

        @Override
        protected Attributes nextData() throws IOException {
            return findNextInstanceRecord();
        }

    }

    private final Main main;
    private final DicomDirReader ddr;
    private final Attributes rq;
    private final Attributes keys;
    private final String patID;
    private final String[] studyIUIDs;
    private final String[] seriesIUIDs;
    private final String[] sopIUIDs;
    private Attributes rsp;
    private Attributes data;
    private Attributes patRec;
    private Attributes studyRec;
    private Attributes seriesRec;
    private Attributes instRec;
    private boolean canceled;
    private boolean finished;

    public CFindRSP(Main main, Attributes rq, Attributes keys, Attributes rsp) {
        this.main = main;
        this.ddr = main.getDicomDirReader();
        this.rq = rq;
        this.keys = keys;
        this.rsp = rsp;
        patID = keys.getString(Tag.PatientID, null);
        studyIUIDs = StringUtils.maskNull(keys.getStrings(Tag.StudyInstanceUID));
        seriesIUIDs = StringUtils.maskNull(keys.getStrings(Tag.SeriesInstanceUID));
        sopIUIDs = StringUtils.maskNull(keys.getStrings(Tag.SOPInstanceUID));
        rsp.setInt(Tag.Status, VR.US, Status.Pending);
    }

    @Override
    public void cancel(Association a) throws IOException {
        canceled = true;
    }

    @Override
    public boolean next() throws DicomServiceException {
        if (finished)
            return false;

        if (canceled) {
            data = null;
            finished = true;
            rsp.setInt(Tag.Status, VR.US, Status.Cancel);
        } else {
            try {
                data = nextData();
            } catch (IOException e) {
                throw new DicomServiceException(rq,
                        Status.ProcessingFailure,
                        e.getMessage());
            }
            if (finished = data == null)
                rsp.setInt(Tag.Status, VR.US, Status.Success);
        }
        
        return true;
    }

    protected abstract Attributes nextData() throws IOException;

    @Override
    public Attributes getCommand() {
        return rsp;
    }

    @Override
    public Attributes getDataset() {
        return data;
    }

    protected Attributes findNextPatientRecord() throws IOException {
        if (patRec != null) {
            if (patID != null)
                return null;
            patRec = ddr.findNextDirectoryRecord(patRec, keys, true, true);
            return patRec;
        }
        return patID != null
                ? ddr.findPatientRecord(patID)
                : ddr.findRootDirectoryRecord(keys, true, true);
    }

    protected Attributes findNextStudyRecord() throws IOException {
        if (studyRec != null) {
            if (studyIUIDs.length == 1)
                return null;
            studyRec = studyIUIDs.length == 0
                    ? ddr.findNextDirectoryRecord(studyRec, keys, true, true)
                    : ddr.findNextStudyRecord(studyRec, studyIUIDs);
            if (studyRec != null)
                return studyRec;
        }
        if (findNextPatientRecord() != null)
            studyRec = studyIUIDs.length == 0
                    ? ddr.findLowerDirectoryRecord(patRec, keys, true, true)
                    : ddr.findStudyRecord(patRec, studyIUIDs);
        return studyRec;
    }

    protected Attributes findNextSeriesRecord() throws IOException {
        if (seriesRec != null) {
            if (seriesIUIDs.length == 1)
                return null;
            seriesRec = seriesIUIDs.length == 0
                    ? ddr.findNextDirectoryRecord(seriesRec, keys, true, true)
                    : ddr.findNextSeriesRecord(seriesRec, seriesIUIDs);
            if (seriesRec != null)
                return seriesRec;
        }
        if (findNextStudyRecord() != null)
            seriesRec = seriesIUIDs.length == 0
                    ? ddr.findLowerDirectoryRecord(studyRec, keys, true, true)
                    : ddr.findSeriesRecord(studyRec, seriesIUIDs);
        return seriesRec;
    }

    protected Attributes findNextInstanceRecord() throws IOException {
       if (instRec != null) {
            if (sopIUIDs.length == 1)
                return null;
            instRec = sopIUIDs.length == 0
                    ? ddr.findNextDirectoryRecord(instRec, keys, true, true)
                    : ddr.findNextInstanceRecord(instRec, sopIUIDs);
            if (instRec != null)
                return instRec;
        }
        if (findNextSeriesRecord() != null)
            instRec = sopIUIDs.length == 0
                    ? ddr.findLowerDirectoryRecord(seriesRec, keys, true, true)
                    : ddr.findInstanceRecord(seriesRec, sopIUIDs);
        return instRec;
    }
}
