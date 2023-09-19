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
 * Java(TM), hosted at https://github.com/dcm4che.
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

package org.dcm4che3.tool.dcmassoc;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StringUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Leandros Athanasiadis <leandre84@gmail.com>
 */
public class DcmAssoc {

    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.dcmassoc.messages");

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private Association as;

    public DcmAssoc(ApplicationEntity ae) throws IOException {
        this.remote = new Connection();
        this.ae = ae;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public Connection getRemoteConnection() {
        return remote;
    }


    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addConnectOption(opts);
        CLIUtils.addBindClientOption(opts, "DCMASSOC");
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCommonOptions(opts);
        addPCOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, DcmAssoc.class);
    }

    private static void addPCOptions(Options opts) {
        opts.addOption(Option.builder()
                .hasArg()
                .argName("number:cuid:tsuid[(,|;)...]")
                .desc(rb.getString("pc"))
                .longOpt("pc")
                .build());
    }

    public static void main(String[] args) {
        long t1, t2;
        try {
            CommandLine cl = parseComandLine(args);
            Device device = new Device("dcmassoc");
            Connection conn = new Connection();
            device.addConnection(conn);
            ApplicationEntity ae = new ApplicationEntity("DCMASSOC");
            device.addApplicationEntity(ae);
            ae.addConnection(conn);
            DcmAssoc main = new DcmAssoc(ae);
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(conn, ae, cl);
            CLIUtils.configure(conn, cl);
            main.remote.setTlsProtocols(conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(conn.getTlsCipherSuites());
            configureStorageSOPClasses(main, cl);

            ExecutorService executorService = Executors
                    .newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService = Executors
                    .newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);
            try {
                t1 = System.currentTimeMillis();
                main.open();
                t2 = System.currentTimeMillis();
                System.out.println(MessageFormat.format(
                        rb.getString("connected"), main.as.getRemoteAET(), t2
                                - t1));

            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println("dcmassoc: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcmassoc: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void configureStorageSOPClasses(DcmAssoc main, CommandLine cl)
            throws Exception {
        String[] pcs = cl.getOptionValues("pc");
        if (pcs != null) {
            for (String pc : pcs) {
                String[] ss = StringUtils.split(pc, ':');
                configureStorageSOPClass(main, Integer.parseInt(ss[0]), ss[1], ss[2]);
            }
        }
        else {
            configureStorageSOPClass(main, 1, UID.Verification, UID.ImplicitVRLittleEndian);
        }    
    }

    private static void configureStorageSOPClass(DcmAssoc main, int pos, String cuid, String tsuids0) {
        for (String tsuids2 : StringUtils.split(tsuids0, ';')) {
            main.addOfferedStorageSOPClass(pos, CLIUtils.toUID(cuid), CLIUtils.toUIDs(tsuids2));
        }
    }

    public void addOfferedStorageSOPClass(int pos, String cuid, String... tsuids) {
        rq.addPresentationContext(new PresentationContext(pos, cuid, tsuids));
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            if (as.isReadyForDataTransfer())
                as.release();
            as.waitForSocketClose();
        }
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

}
