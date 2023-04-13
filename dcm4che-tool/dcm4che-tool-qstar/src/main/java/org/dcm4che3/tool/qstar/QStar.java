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

package org.dcm4che3.tool.qstar;

import org.apache.commons.cli.*;
import org.dcm4che3.qstar.*;
import org.dcm4che3.tool.common.CLIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.BindingProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Apr 2023
 */
public class QStar {

    private static final Logger LOG = LoggerFactory.getLogger(QStar.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.qstar.messages");
    private static final WSWebService service = new WSWebService();
    private static final ObjectFactory factory = new ObjectFactory();

    private final WSWebServiceSoapPort port;
    private WSUserLoginResponse userLogin;

    private QStar(String url) {
        port = service.getWSWebServiceSoapPort();
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> reqCtx = bindingProvider.getRequestContext();
        reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            String[] user = cl.getOptionValues("u");
            if (user == null)
                throw new MissingOptionException(
                        rb.getString("missing-user-opt"));
            Iterator<String> argsIter = cl.getArgList().iterator();
            if (!argsIter.hasNext())
                throw new ParseException(rb.getString("missing-url"));

            QStar qstar = new QStar(argsIter.next());
            if (qstar.login(user[0], user[1])) {
                while (argsIter.hasNext()) {
                    qstar.getFileInfo(argsIter.next());
                }
                qstar.logout();
            }
        } catch (ParseException e) {
            System.err.println("qstar: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("qstar: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private boolean login(String userName, String userPassword) {
        try {
            WSUserLoginRequest rq = factory.createWSUserLoginRequest();
            rq.setUserName(userName);
            rq.setUserPassword(userPassword);
            LOG.info("<< WSUserLoginRequest{userName='{}', userPassword='{}'}", userName, userPassword);
            userLogin = port.wsUserLogin(rq);
            LOG.info(">> WSUserLoginResponse{result={}, resultString='{}', wsdlVersion='{}', userToken='{}'}",
                    userLogin.getResult(),
                    userLogin.getResultString(),
                    userLogin.getWsdlVersion(),
                    userLogin.getUserToken());
            return true;
        } catch (Exception e) {
            LOG.info("Login Failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private void getFileInfo(String filePath) {
        try {
            WSGetFileInfoRequest rq = factory.createWSGetFileInfoRequest();
            rq.setSFileFullPath(filePath);
            rq.setUserToken(userLogin.getUserToken());
            LOG.info("<< WSGetFileInfoRequest{sFileFullPath='{}', userToken='{}'}", filePath, userLogin.getUserToken());
            WSGetFileInfoResponse fileInfo = port.wsGetFileInfo(rq);
            LOG.info(">> WSGetFileInfoResponse{status={}, info={}}", fileInfo.getStatus(), toString(fileInfo.getInfo()));
        } catch (Exception e) {
            LOG.info("GetFileInfo Failed:\n", e);
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
        sb.append(",\n    aTime=").append(info.getATime());
        sb.append(",\n    mTime=").append(info.getMTime());
        sb.append(",\n    cTime=").append(info.getCTime());
        sb.append(",\n    crTime=").append(info.getCrTime());
        sb.append(",\n    cmTime=").append(info.getCmTime());
        sb.append(",\n    onOff=").append(info.getOnOff());
        sb.append(",\n    retentionEndTime=").append(info.getRetentionEndTime());
        appendNotNull(sb, ",\n    digest=", info.getDigest());
        appendNotNull(sb, ",\n    objectId=", info.getObjectId());
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
            appendNotNull(sb, ",\n        mediaBarcode=", extent.getMediaBarcode());
            appendNotNull(sb, ",\n        libraryName=", extent.getLibraryName());
            appendNotNull(sb, ",\n        offlineLocation=", extent.getOfflineLocation());
            sb.append(",\n        extentCopies=[");
            for (WSFileExtentCopyInfo copy : extent.getExtentCopies().getCopies()) {
                sb.append("\n          WSFileExtentCopyInfo{\n            isOffline=").append(copy.getIsOffline());
                appendNotNull(sb, ",\n            mediaBarcode=", copy.getMediaBarcode());
                appendNotNull(sb, ",\n            offlineLocation=", copy.getOfflineLocation());
                sb.append(",\n            copyNumber=").append(copy.getCopyNumber());
                appendNotNull(sb, ",\n            deviceName=", copy.getDeviceName());
                sb.append(",\n            slotNumber=").append(copy.getSlotNumber());
                appendNotNull(sb, ",\n            side=", copy.getSide());
                sb.append(",\n            copyInSet=").append(copy.getCopyInSet());
                sb.append(",\n            copyInDB=").append(copy.getCopyInDB());
                sb.append(",\n            copyInDevice=").append(copy.getCopyInDevice());
                sb.append('}');
            }
            sb.append("],\n        copiesCount=").append(extent.getCopiesCount());
            sb.append('}');
        }
        sb.append("],\n    extCount=").append(info.getExtCount());
        appendNotNull(sb, ",\n    flocError=", info.getFlocError());
        sb.append(",\n    stateAccess=").append(info.getStateAccess());
        sb.append(",\n    stateAccessDetailed=").append(info.getStateAccessDetailed());
        sb.append('}');
        return sb.toString();
    }

    private static void appendNotNull(StringBuilder sb, String prompt, String value) {
        sb.append(prompt);
        if (value != null) sb.append('\'').append(value).append('\'');
    }

    private void logout() {
        try {
            WSUserLogoutRequest rq = factory.createWSUserLogoutRequest();
            rq.setUserToken(userLogin.getUserToken());
            LOG.info("<< WSUserLogoutRequest{userToken='{}'}", userLogin.getUserToken());
            WSUserLogoutResponse userLogout = port.wsUserLogout(rq);
            LOG.info(">> WSUserLogoutResponse{result={}}", userLogout.getResult());
        } catch (Exception e) {
            LOG.info("Logout Failed:\n", e);
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("u")
                .longOpt("user")
                .numberOfArgs(2)
                .valueSeparator(':')
                .argName("user:password")
                .desc(rb.getString("user"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, QStar.class);
    }
}
