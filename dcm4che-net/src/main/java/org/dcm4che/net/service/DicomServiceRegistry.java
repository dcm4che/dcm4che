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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationStateException;
import org.dcm4che.net.Commands;
import org.dcm4che.net.DimseRQHandler;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.CommonExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomServiceRegistry implements DimseRQHandler {

    private final ArrayList<DicomService> services =
            new ArrayList<DicomService>();
    private final HashSet<String> sopCUIDs = new HashSet<String>();
    private final HashMap<String, CEchoSCP> cechoSCPs =
            new HashMap<String, CEchoSCP>(1);
    private final HashMap<String, CStoreSCP> cstoreSCPs =
            new HashMap<String, CStoreSCP>();
    private HashMap<String, CGetSCP> cgetSCPs;
    private HashMap<String, CFindSCP> cfindSCPs;
    private HashMap<String, CMoveSCP> cmoveSCPs;
    private HashMap<String, NGetSCP> ngetSCPs;
    private HashMap<String, NSetSCP> nsetSCPs;
    private HashMap<String, NActionSCP> nactionSCPs;
    private HashMap<String, NCreateSCP> ncreateSCPs;
    private HashMap<String, NDeleteSCP> ndeleteSCPs;
    private HashMap<String, NEventReportSCU> neventReportSCUs;

    public synchronized void addDicomService(DicomService service) {
        services.add(service);
        String[] sopClasses = service.getSOPClasses();
        for (String uid : sopClasses)
            sopCUIDs.add(uid);
        if (service instanceof CEchoSCP) {
            for (String uid : sopClasses)
                cechoSCPs.put(uid, (CEchoSCP) service);
        }
        if (service instanceof CStoreSCP) {
            for (String uid : sopClasses)
                cstoreSCPs.put(uid, (CStoreSCP) service);
        }
        if (service instanceof CGetSCP) {
            if (cgetSCPs == null)
                cgetSCPs = new HashMap<String, CGetSCP>();
            for (String uid : sopClasses)
                cgetSCPs.put(uid, (CGetSCP) service);
        }
        if (service instanceof CFindSCP) {
            if (cfindSCPs == null)
                cfindSCPs = new HashMap<String, CFindSCP>();
            for (String uid : sopClasses)
                cfindSCPs.put(uid, (CFindSCP) service);
        }
        if (service instanceof CMoveSCP) {
            if (cmoveSCPs == null)
                cmoveSCPs = new HashMap<String, CMoveSCP>();
            for (String uid : sopClasses)
                cmoveSCPs.put(uid, (CMoveSCP) service);
        }
        if (service instanceof NGetSCP) {
            if (ngetSCPs == null)
                ngetSCPs = new HashMap<String, NGetSCP>();
            for (String uid : sopClasses)
                ngetSCPs.put(uid, (NGetSCP) service);
        }
        if (service instanceof NSetSCP) {
            if (nsetSCPs == null)
                nsetSCPs = new HashMap<String, NSetSCP>();
            for (String uid : sopClasses)
                nsetSCPs.put(uid, (NSetSCP) service);
        }
        if (service instanceof NCreateSCP) {
            if (ncreateSCPs == null)
                ncreateSCPs = new HashMap<String, NCreateSCP>();
            for (String uid : sopClasses)
                ncreateSCPs.put(uid, (NCreateSCP) service);
        }
        if (service instanceof NActionSCP) {
            if (nactionSCPs == null)
                nactionSCPs = new HashMap<String, NActionSCP>();
            for (String uid : sopClasses)
                nactionSCPs.put(uid, (NActionSCP) service);
        }
        if (service instanceof NEventReportSCU) {
            if (neventReportSCUs == null)
                neventReportSCUs = new HashMap<String, NEventReportSCU>();
            for (String uid : sopClasses)
                neventReportSCUs.put(uid, (NEventReportSCU) service);
        }
        if (service instanceof NDeleteSCP) {
            if (ndeleteSCPs == null)
                ndeleteSCPs = new HashMap<String, NDeleteSCP>();
            for (String uid : sopClasses)
                ndeleteSCPs.put(uid, (NDeleteSCP) service);
        }
    }

    public synchronized boolean removeDicomService(DicomService service) {
        if (!services.remove(service))
            return false;

        String[] sopClasses = service.getSOPClasses();
        for (String uid : sopClasses)
            sopCUIDs.remove(uid);
        if (service instanceof CEchoSCP) {
            for (String uid : sopClasses)
                cechoSCPs.remove(uid);
        }
        if (service instanceof CStoreSCP) {
            for (String uid : sopClasses)
                cstoreSCPs.remove(uid);
        }
        if (service instanceof CGetSCP) {
            if (cgetSCPs != null) {
                for (String uid : sopClasses)
                    cgetSCPs.remove(uid);
            }
        }
        if (service instanceof CFindSCP) {
            if (cfindSCPs != null) {
                for (String uid : sopClasses)
                    cfindSCPs.remove(uid);
            }
        }
        if (service instanceof CMoveSCP) {
            if (cmoveSCPs != null) {
                for (String uid : sopClasses)
                    cmoveSCPs.remove(uid);
            }
        }
        if (service instanceof NGetSCP) {
            if (ngetSCPs != null) {
                for (String uid : sopClasses)
                    ngetSCPs.remove(uid);
            }
        }
        if (service instanceof NSetSCP) {
            if (nsetSCPs != null) {
                for (String uid : sopClasses)
                    nsetSCPs.remove(uid);
            }
        }
        if (service instanceof NCreateSCP) {
            if (ncreateSCPs != null) {
                for (String uid : sopClasses)
                    ncreateSCPs.remove(uid);
            }
        }
        if (service instanceof NActionSCP) {
            if (nactionSCPs != null) {
                for (String uid : sopClasses)
                    nactionSCPs.remove(uid);
            }
        }
        if (service instanceof NEventReportSCU) {
            if (neventReportSCUs != null) {
                for (String uid : sopClasses)
                    neventReportSCUs.remove(uid);
            }
        }
        if (service instanceof NDeleteSCP) {
            if (ndeleteSCPs != null) {
                for (String uid : sopClasses)
                    ndeleteSCPs.remove(uid);
            }
        }
        return true;
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc,
            Attributes cmd, PDVInputStream data) throws IOException {
        try {
            final int cmdfield = cmd.getInt(Tag.CommandField, -1);
            if (cmdfield == Commands.C_STORE_RQ) {
                cstore(as, pc, cmd, data);
            } else {
                Attributes dataset =  null;
                if (data != null) {
                    dataset = data.readDataset(pc.getTransferSyntax());
                    Association.LOG_DIMSE.debug("Dataset:\n{}", dataset);
                }
                switch (cmdfield) {
                case Commands.C_GET_RQ:
                    cget(as, pc, cmd, dataset);
                    break;
                case Commands.C_FIND_RQ:
                    cfind(as, pc, cmd, dataset);
                    break;
                case Commands.C_MOVE_RQ:
                    cmove(as, pc, cmd, dataset);
                    break;
                case Commands.C_ECHO_RQ:
                    cecho(as, pc, cmd, dataset);
                    break;
                case Commands.N_EVENT_REPORT_RQ:
                    neventReport(as, pc, cmd, dataset);
                    break;
                case Commands.N_GET_RQ:
                    nget(as, pc, cmd, dataset);
                    break;
                case Commands.N_SET_RQ:
                    nset(as, pc, cmd, dataset);
                    break;
                case Commands.N_ACTION_RQ:
                    naction(as, pc, cmd, dataset);
                    break;
                case Commands.N_CREATE_RQ:
                    ncreate(as, pc, cmd, dataset);
                    break;
                case Commands.N_DELETE_RQ:
                    ndelete(as, pc, cmd, dataset);
                    break;
                default:
                    throw new DicomServiceException(Status.UnrecognizedOperation);
                }
            }
        } catch (DicomServiceException e) {
            Attributes rsp = e.mkRSP(cmd);
            try {
                as.writeDimseRSP(pc, rsp, e.getDataset());
            } catch (AssociationStateException ase) {
                Association.LOG_ACSE.warn("{} << DIMSE-RSP failed: {}",
                        as, ase.getMessage());
            }
        }
    }

    private <T> T service(HashMap<String, T> map, String cuid)
            throws DicomServiceException {
        T ret = map != null ? map.get(cuid) : null;
        if (ret == null)
            throw new DicomServiceException(sopCUIDs.contains(cuid)
                        ? Status.UnrecognizedOperation
                        : Status.NoSuchSOPclass);
        return ret;
    }

    private CStoreSCP cstoreSCP(String cuid, Association as) throws DicomServiceException {
        CStoreSCP ret = cstoreSCPs.get(cuid);
        if (ret != null)
            return ret;

        if (sopCUIDs.contains(cuid))
            throw new DicomServiceException(Status.UnrecognizedOperation);

        CommonExtendedNegotiation commonExtNeg = as
                .getCommonExtendedNegotiationFor(cuid);
        if (commonExtNeg != null) {
            for (String uid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                ret = cstoreSCPs.get(uid);
                if (ret != null)
                    return ret;
            }
            ret = cstoreSCPs.get(commonExtNeg.getServiceClassUID());
            if (ret != null)
                return ret;
        }
        ret = cstoreSCPs.get("*");
        if (ret != null)
            return ret;

        throw new DicomServiceException(Status.NoSuchSOPclass);
    }

    private void cstore(Association as, PresentationContext pc,
            Attributes cmd, PDVInputStream data) throws IOException {
        cstoreSCP(cmd.getString(Tag.AffectedSOPClassUID), as)
                .onCStoreRQ(as, pc, cmd, data);
    }

    private void cget(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(cgetSCPs, cmd.getString(Tag.AffectedSOPClassUID))
                .onCGetRQ(as, pc, cmd, dataset);
    }

    private void cfind(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(cfindSCPs, cmd.getString(Tag.AffectedSOPClassUID))
                .onCFindRQ(as, pc, cmd, dataset);
    }

    private void cmove(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(cmoveSCPs, cmd.getString(Tag.AffectedSOPClassUID))
                .onCMoveRQ(as, pc, cmd, dataset);
    }

    private void cecho(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(cechoSCPs, cmd.getString(Tag.AffectedSOPClassUID))
                .onCEchoRQ(as, pc, cmd);
    }

    private void nget(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(ngetSCPs, cmd.getString(Tag.RequestedSOPClassUID))
                .onNGetRQ(as, pc, cmd, dataset);
    }

    private void nset(Association as, PresentationContext pc, Attributes cmd,
            Attributes dataset) throws IOException {
        service(nsetSCPs, cmd.getString(Tag.RequestedSOPClassUID))
                .onNSetRQ(as, pc, cmd, dataset);
    }

    private void ncreate(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(ncreateSCPs, cmd.getString(Tag.AffectedSOPClassUID))
                .onNCreateRQ(as, pc, cmd, dataset);
    }

    private void naction(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(nactionSCPs, cmd.getString(Tag.RequestedSOPClassUID))
                .onNActionRQ(as, pc, cmd, dataset);
    }

    private void ndelete(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(ndeleteSCPs, cmd.getString(Tag.RequestedSOPClassUID))
                .onNDeleteRQ(as, pc, cmd);
    }

    private void neventReport(Association as, PresentationContext pc,
            Attributes cmd, Attributes dataset) throws IOException {
        service(neventReportSCUs, cmd.getString(Tag.AffectedSOPClassUID))
                .onNEventReportRQ(as, pc, cmd, dataset);
    }

    @Override
    public void onClose(Association as) {
        for (DicomService service : this.services)
            service.onClose(as);
    }

}
