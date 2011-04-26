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

package org.dcm4che.tool.dcmrcv;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.service.VerificationService;
import org.dcm4che.tool.common.CLIUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DcmRcv {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che.tool.dcmrcv.dcmrcv");

    private final Device device = new Device("dcmrcv");
    private final ApplicationEntity ae = new ApplicationEntity("*");
    private final Connection conn = new Connection();

    private final StorageSCP storageSCP = new StorageSCP(this);

    public DcmRcv() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        ae.addTransferCapability(
                new TransferCapability(null, 
                        "*",
                        TransferCapability.Role.SCP,
                        "*"));
        ae.addDicomService(new VerificationService());
        ae.addDicomService(storageSCP);
    }

    public void setHostname(String hostname) {
        conn.setHostname(hostname);
    }

    public void setPort(int port) {
        conn.setPort(port);
    }

    public void setAETitle(String aet) {
        ae.setAETitle(aet);
    }

    public void setMaxOpsInvoked(int maxOpsInvoked) {
        ae.setMaxOpsInvoked(maxOpsInvoked);
    }

    public void setMaxOpsPerformed(int maxOpsPerformed) {
        ae.setMaxOpsPerformed(maxOpsPerformed);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, DcmRcv.class);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmRcv dcmrcv = new DcmRcv();
            String port = port(cl.getArgList());
            String[] aetAndPort = split(port, ':', 1);
            dcmrcv.setPort(Integer.parseInt(aetAndPort[1]));
            if (aetAndPort[0] != null) {
                String[] aetAndIP = split(aetAndPort[0], '@', 0);
                dcmrcv.setAETitle(aetAndIP[0]);
                if (aetAndIP[1] != null)
                    dcmrcv.setHostname(aetAndIP[1]);
            }
            CLIUtils.configure(dcmrcv.ae, cl);
            ExecutorService executorService = Executors.newCachedThreadPool();
            try {
                dcmrcv.start(executorService);
            } catch (Exception e) {
                executorService.shutdownNow();
            }
        } catch (ParseException e) {
            System.err.println("dcmrcv: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcmrcv: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void start(Executor executor) throws IOException {
        device.setExecutor(executor);
        conn.bind();
    }

    private static String port(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 1)
            throw new ParseException(rb.getString("toomany"));
        return argList.get(0);
    }

    private static String[] split(String s, char delim, int defPos) {
        String[] s2 = new String[2];
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        } else {
            s2[defPos] = s;
        }
        return s2;
    }

}
