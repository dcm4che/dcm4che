/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2013-2021
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.qstar;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.dcm4che3.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.ws.BindingProvider;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jul 2023
 */
public class QStarUtils {
    private static final Logger LOG = LoggerFactory.getLogger(QStarUtils.class);
    private static final WSWebService service = new WSWebService();
    private static final ObjectFactory factory = new ObjectFactory();
    private static final String[] ACCESS_STATES = {
            "0 - ACCESS_STATE_NONE",
            "1 - ACCESS_STATE_EMPTY",
            "2 - ACCESS_STATE_UNSTABLE",
            "3 - ACCESS_STATE_STABLE",
            "4 - ACCESS_STATE_OUT_OF_CACHE",
            "5 - ACCESS_STATE_OFFLINE"
    };
    private static final String[] DETAILED_ACCESS_STATES = {
            "0 - DETAILED_ACCESS_STATE_NONE",
            "1 - DETAILED_ACCESS_STATE_EMPTY",
            "2 - DETAILED_ACCESS_STATE_CACHED_PRIMARY",
            "3 - DETAILED_ACCESS_STATE_CACHED_MIG",
            "4 - DETAILED_ACCESS_STATE_CACHED_MIG_OUT",
            "5 - DETAILED_ACCESS_STATE_CACHED_MIG_REP",
            "6 - DETAILED_ACCESS_STATE_CACHED_REP_OUT",
            "7 - DETAILED_ACCESS_STATE_MIGRATED",
            "8 - DETAILED_ACCESS_STATE_MIGRATED_OUT",
            "9 - DETAILED_ACCESS_STATE_ARCHIVED_REP",
            "10 - DETAILED_ACCESS_STATE_ARCHIVED_OUT",
            "11 - DETAILED_ACCESS_STATE_REPLICATED_OUT",
            "12 - DETAILED_ACCESS_STATE_REPLICATED",
            "13 - DETAILED_ACCESS_STATE_OUT_OF_CACHE",
            "14 - DETAILED_ACCESS_STATE_OFFLINE"
    };
    private static final String[] JOB_STATUS_NAMES = {
            "0",
            "1 - INQUEUE",
            "2 - PROCESSING",
            "3 - COMPLETED",
            "4 - FAILED",
            "5 - PARTIALLY_COMPLETED"
    };

    private QStarUtils() {
    }

    public static WSWebServiceSoapPort getWSWebServiceSoapPort(String url) {
        WSWebServiceSoapPort port = service.getWSWebServiceSoapPort();
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> reqCtx = bindingProvider.getRequestContext();
        reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        return port;
    }

    public static void setTlsClientParameters(WSWebServiceSoapPort port, Device device,
                                              String tlsProtocol, String[] cipherSuites, boolean disableCNCheck)
            throws GeneralSecurityException, IOException {
        Client client = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        conduit.setTlsClientParameters(createTLSClientParameters(device, tlsProtocol, cipherSuites, disableCNCheck));
    }

    private static TLSClientParameters createTLSClientParameters(
            Device device,
            String tlsProtocol,
            String[] cipherSuites,
            boolean disableCNCheck)
            throws GeneralSecurityException, IOException {
        TLSClientParameters params = new TLSClientParameters();
        params.setKeyManagers(device.keyManagers());
        params.setTrustManagers(device.trustManagers());
        params.setSecureSocketProtocol(tlsProtocol);
        for (String cipherSuite : cipherSuites)
            params.getCipherSuites().add(cipherSuite.trim());
        params.setDisableCNCheck(disableCNCheck);
        return params;
    }

    public static WSUserLoginResponse login(WSWebServiceSoapPort port, String userName, String userPassword) {
        WSUserLoginRequest rq = factory.createWSUserLoginRequest();
        rq.setUserName(userName);
        rq.setUserPassword(userPassword);
        LOG.debug("<< WSUserLoginRequest{userName='{}', userPassword='{}'}", userName, userPassword);
        WSUserLoginResponse userLogin = port.wsUserLogin(rq);
        LOG.debug(">> WSUserLoginResponse{result={}, resultString='{}', wsdlVersion='{}', userToken='{}'}",
                userLogin.getResult(),
                userLogin.getResultString(),
                userLogin.getWsdlVersion(),
                userLogin.getUserToken());
        return userLogin;
    }

    public static WSUserLogoutResponse logout(WSWebServiceSoapPort port, WSUserLoginResponse userLogin) {
        WSUserLogoutRequest rq = factory.createWSUserLogoutRequest();
        rq.setUserToken(userLogin.getUserToken());
        LOG.debug("<< WSUserLogoutRequest{userToken='{}'}", userLogin.getUserToken());
        WSUserLogoutResponse userLogout = port.wsUserLogout(rq);
        LOG.debug(">> WSUserLogoutResponse{result={}}", userLogout.getResult());
        return userLogout;
    }

    public static WSGetFileInfoResponse getFileInfo(
            WSWebServiceSoapPort port,
            WSUserLoginResponse userLogin,
            String filePath) {
        WSGetFileInfoRequest rq = factory.createWSGetFileInfoRequest();
        rq.setSFileFullPath(filePath);
        rq.setUserToken(userLogin.getUserToken());
        LOG.debug("<< WSGetFileInfoRequest{sFileFullPath='{}', userToken='{}'}", filePath, userLogin.getUserToken());
        WSGetFileInfoResponse fileInfo = port.wsGetFileInfo(rq);
        if (LOG.isDebugEnabled())
            LOG.debug(">> WSGetFileInfoResponse{status={}, info={}}", fileInfo.getStatus(), toString(fileInfo.getInfo()));
        return fileInfo;
    }

    public static WSBatchFileRetrieveResponse batchFileRetrieve(
            WSWebServiceSoapPort port,
            WSUserLoginResponse userLogin,
            long jobPriority, List<String> fileList,
            String targetDir) {
        WSBatchFileRetrieveRequest rq = factory.createWSBatchFileRetrieveRequest();
        rq.setJobPriority(jobPriority);
        rq.setFileCount(BigInteger.valueOf(fileList.size()));
        rq.setFileList(createWSFileList(fileList));
        rq.setTargetDir(targetDir);
        rq.setUserToken(userLogin.getUserToken());
        LOG.debug("<< WSBatchFileRetrieveRequest{jobPriority={}, fileCount='{}', targetDir='{}', userToken='{}'}",
                jobPriority,
                fileList.size(),
                targetDir,
                userLogin.getUserToken());
        WSBatchFileRetrieveResponse fileRetrieve = port.wsBatchFileRetrieve(rq);
        LOG.debug(">> WSBatchFileRetrieveResponse{jobId={}}", fileRetrieve.getJobId());
        return fileRetrieve;
    }

    private static WSFileList createWSFileList(List<String> fileList) {
        WSFileList wsFileList = factory.createWSFileList();
        wsFileList.getFileName().addAll(fileList);
        return wsFileList;
    }

    public static WSBatchJobStatusResponse batchJobStatus(
            WSWebServiceSoapPort port,
            WSUserLoginResponse userLogin,
            BigInteger jobId) {
        WSBatchJobStatusRequest rq = factory.createWSBatchJobStatusRequest();
        rq.setJobId(jobId);
        rq.setUserToken(userLogin.getUserToken());
        LOG.debug("<< WSBatchJobStatusRequest{jobId={}, userToken='{}'}", jobId, userLogin.getUserToken());
        WSBatchJobStatusResponse jobStatus = port.wsBatchJobStatus(rq);
        LOG.debug(">> WSBatchJobStatusResponse{jobStatus={}}", jobStatusAsString(jobStatus.getJobStatus()));
        return jobStatus;
    }

    public static String jobStatusAsString(long jobStatus) {
        return toString(jobStatus, JOB_STATUS_NAMES);
    }

    public static WSBatchJobObjectStatusResponse batchJobObjectStatus(
            WSWebServiceSoapPort port,
            WSUserLoginResponse userLogin,
            BigInteger jobId,
            String file) {
        WSBatchJobObjectStatusRequest rq = factory.createWSBatchJobObjectStatusRequest();
        rq.setJobId(jobId);
        rq.setJobObjectName(file);
        rq.setUserToken(userLogin.getUserToken());
        LOG.debug("<< WSBatchJobObjectStatusRequest{jobId={}, jobObjectName='{}', userToken='{}'}",
                jobId, file, userLogin.getUserToken());
        WSBatchJobObjectStatusResponse jobObjectStatus = port.wsBatchJobObjectStatus(rq);
        LOG.debug(">> WSBatchJobObjectStatusResponse{jobObjectStatus={}}",
                jobObjectStatusAsString(jobObjectStatus.getJobObjectStatus()));
        return jobObjectStatus;
    }

    public static String jobObjectStatusAsString(long jobObjectStatus) {
        return toString(jobObjectStatus, JOB_STATUS_NAMES);
    }

    public static WSMMPurgeFileResponse purgeFile(WSWebServiceSoapPort port, WSUserLoginResponse userLogin, String filePath) {
        WSMMPurgeFileRequest rq = factory.createWSMMPurgeFileRequest();
        rq.setFileName(filePath);
        rq.setUserToken(userLogin.getUserToken());
        LOG.debug("<< WSMMPurgeFileRequest{fileName='{}', userToken='{}'}", filePath, userLogin.getUserToken());
        WSMMPurgeFileResponse purgeFile = port.wsmmPurgeFile(rq);
        LOG.debug(">> WSMMPurgeFileResponse{error={}, description={}}", purgeFile.getError(), purgeFile.getDescription());
        return purgeFile;
    }

    private static String toString(long i, String[] values) {
        try {
            return values[(int) i];
        } catch (IndexOutOfBoundsException e) {
            return Long.toString(i);
        }
    }

    private static String toString(WSFileInfo info) {
        if (info == null) return "";
        StringBuilder sb = new StringBuilder(1024);
        sb.append("\n  WSFileInfo{\n    size=").append(info.getSize());
        sb.append(",\n    mode=").append(info.getMode());
        sb.append(",\n    uid=").append(info.getUid());
        sb.append(",\n    gid=").append(info.getGid());
        sb.append(",\n    nlink=").append(info.getNlink());
        sb.append(",\n    flags=").append(info.getFlags());
        sb.append(",\n    presentCount=").append(info.getPresentCount());
        sb.append(",\n    primaryCount=").append(info.getPrimaryCount());
        sb.append(",\n    replicatedCount=").append(info.getReplicatedCount());
        sb.append(",\n    archivedCount=").append(info.getArchivedCount());
        sb.append(",\n    pageSize=").append(info.getPageSize());
        sb.append(",\n    aTime=").append(toInstant(info.getATime()));
        sb.append(",\n    mTime=").append(toInstant(info.getMTime()));
        sb.append(",\n    cTime=").append(toInstant(info.getCTime()));
        sb.append(",\n    crTime=").append(toInstant(info.getCrTime()));
        sb.append(",\n    cmTime=").append(toInstant(info.getCmTime()));
        sb.append(",\n    onOff=").append(info.getOnOff());
        sb.append(",\n    retentionEndTime=").append(toInstant(info.getRetentionEndTime()));
        sb.append(",\n    digest=").append(quote(info.getDigest()));
        sb.append(",\n    objectId=").append(quote(info.getObjectId()));
        sb.append(",\n    objectIdLength=").append(info.getObjectIdLength());
        sb.append(",\n    digestType=").append(info.getDigestType());
        sb.append(",\n    encryptionType=").append(info.getEncryptionType());
        sb.append(",\n    cryptoFlags=").append(info.getCryptoFlags());
        sb.append(",\n    flocExtents=[");
        for (WSFileExtentInfo extent : info.getFlocExtents().getExtent()) {
            sb.append("\n      WSFileExtentInfo{\n        offset=").append(extent.getOffset());
            sb.append(",\n        size=").append(extent.getSize());
            sb.append(",\n        vol=").append(extent.getVol());
            sb.append(",\n        pos=").append(extent.getPos());
            sb.append(",\n        isOffline=").append(extent.getIsOffline());
            sb.append(",\n        mediaBarcode=").append(quote(extent.getMediaBarcode()));
            sb.append(",\n        libraryName=").append(quote(extent.getLibraryName()));
            sb.append(",\n        offlineLocation=").append(quote(extent.getOfflineLocation()));
            sb.append(",\n        extentCopies=[");
            for (WSFileExtentCopyInfo copy : extent.getExtentCopies().getCopies()) {
                sb.append("\n          WSFileExtentCopyInfo{\n            isOffline=").append(copy.getIsOffline());
                sb.append(",\n            mediaBarcode=").append(quote(copy.getMediaBarcode()));
                sb.append(",\n            offlineLocation=").append(quote(copy.getOfflineLocation()));
                sb.append(",\n            copyNumber=").append(copy.getCopyNumber());
                sb.append(",\n            deviceName=").append(quote(copy.getDeviceName()));
                sb.append(",\n            slotNumber=").append(copy.getSlotNumber());
                sb.append(",\n            side=").append(quote(copy.getSide()));
                sb.append(",\n            copyInSet=").append(copy.getCopyInSet());
                sb.append(",\n            copyInDB=").append(copy.getCopyInDB());
                sb.append(",\n            copyInDevice=").append(copy.getCopyInDevice());
                sb.append('}');
            }
            sb.append("],\n        copiesCount=").append(extent.getCopiesCount());
            sb.append('}');
        }
        sb.append("],\n    extCount=").append(info.getExtCount());
        sb.append(",\n    flocError=").append(quote(info.getFlocError()));
        sb.append(",\n    stateAccess=").append(stateAccessAsString(info.getStateAccess()));
        sb.append(",\n    stateAccessDetailed=").append(stateAccessDetailedAsString(info.getStateAccessDetailed()));
        sb.append('}');
        return sb.toString();
    }

    public static String stateAccessAsString(long stateAccess) {
        return toString(stateAccess, ACCESS_STATES);
    }

    public static String stateAccessDetailedAsString(long stateAccessdDetailed) {
        return toString(stateAccessdDetailed, DETAILED_ACCESS_STATES);
    }

    private static Object toInstant(BigInteger value) {
        return value.signum() == 0 ? "" : Instant.ofEpochSecond(value.longValue());
    }

    private static String quote(String value) {
        return value == null ? "" : '\'' + value + '\'';
    }
}
