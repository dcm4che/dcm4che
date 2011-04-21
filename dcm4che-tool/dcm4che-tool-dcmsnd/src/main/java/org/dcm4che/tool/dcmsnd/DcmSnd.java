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

package org.dcm4che.tool.dcmsnd;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dcm4che.data.UID;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DcmSnd {

    private static final String USAGE = 
        "dcmsnd [Options] <aet>[@<host>[:<port>]] [<file>..][<directory>..]";

    private static final String DESCRIPTION = 
        "\nLoad DICOM Composite Object(s) from specified DICOM file(s) and " +
        "send them to the specified remote Application Entity using " +
        "appropriate SOP Classes of the Storage Service Class. If a " +
        "directory is specified, DICOM Composite Objects in files under " +
        "that directory and further sub-directories are sent." +
        "\nIf no DICOM file(s) are specified, the DICOM connection to the " +
        "specified remote Application Entity will be verfied using the " +
        "Verification SOP Class." +
        "\nIf <port> is not specified, DICOM default port 104 is assumed. If " +
        "also no <host> is specified, localhost is assumed." +
        "\n-\nOptions:";

    private static final String EXAMPLE = 
        "--\nExample: dcmsnd STORESCP@localhost:11112 image.dcm" +
        "\n=> Send DICOM Composite Object image.dcm to Application Entity " +
        "STORESCP, listening on local port 11112.";

    private final Device device = new Device("dcmsnd");
    private final ApplicationEntity ae = new ApplicationEntity("DCMSND");
    private final Connection conn = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private String host = "localhost";
    private int port = 104;

    private Association as;

    public DcmSnd() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException{
        Options opts = new Options();
        opts.addOption("h", "help", false, "display this help and exit");
        opts.addOption("V", "version", false,
                "output version information and exit");
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(opts, args);
        if (cl.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        if (cl.hasOption("V")) {
            System.out.println("dcmsnd " + 
                    DcmSnd.class.getPackage().getImplementationVersion());
            System.exit(0);
        }
        return cl;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            DcmSnd dcmsnd = new DcmSnd();
            List<String> argList = cl.getArgList();
            if (argList.isEmpty())
                throw new ParseException("Missing remote AE argument");
            String remoteAE = argList.get(0);
            String[] calledAETAddress = split(remoteAE, '@');
            dcmsnd.setCalledAET(calledAETAddress[0]);
            if (calledAETAddress[1] == null) {
                dcmsnd.setRemoteHost("127.0.0.1");
                dcmsnd.setRemotePort(104);
            } else {
                String[] hostPort = split(calledAETAddress[1], ':');
                dcmsnd.setRemoteHost(hostPort[0]);
                dcmsnd.setRemotePort((hostPort[1] != null 
                                        ? Integer.parseInt(hostPort[1])
                                        : 104));
            }
            if (cl.hasOption("L")) {
                String localAE = cl.getOptionValue("L");
                String[] callingAETHost = split(localAE, '@');
                dcmsnd.setCallingAET(callingAETHost[0]);
                if (callingAETHost[1] != null) {
                    dcmsnd.setLocalHost(callingAETHost[1]);
                }
            }
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            try {
                dcmsnd.open(executorService);
                dcmsnd.echo();
            } finally {
                dcmsnd.close();
                executorService.shutdown();
            }
        } catch (ParseException e) {
            System.err.println("dcmsnd: " + e.getMessage());
            System.err.println("Try `dcmsnd --help' for more information.");
            System.exit(2);
        } catch (Exception e) {
            System.err.println("dcmsnd: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void open(Executor executor)
            throws IOException, InterruptedException {
        device.setExecutor(executor);
        rq.setCallingAET(ae.getAETitle());
        if (rq.getNumberOfPresentationContexts() == 0)
            rq.addPresentationContext(
                    new PresentationContext(1, UID.VerificationSOPClass,
                            UID.ImplicitVRLittleEndian));
        as = ae.connect(conn, host, port, rq);
    }

    private static String[] split(String s, char delim) {
        String[] s2 = { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    public void setCallingAET(String aet) {
        ae.setAETitle(aet);
    }

    public void setCalledAET(String aet) {
        rq.setCalledAET(aet);
    }

    public void setRemoteHost(String host) {
        this.host = host;
    }

    public void setRemotePort(int port) {
        this.port = port;
    }

    public void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

}
