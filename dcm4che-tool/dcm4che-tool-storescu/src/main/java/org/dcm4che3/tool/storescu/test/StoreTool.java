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

package org.dcm4che3.tool.storescu.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.util.TagUtils;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class StoreTool implements TestTool {

    private String host;
    private int port;
    private String aeTitle;
    private File baseDirectory;
    private Device device;
    private Connection conn;
    private String sourceAETitle;
    
    private long totalSize;
    private int filesSent;
    private int warnings;
    private int failures;
    private ArrayList<Attributes> cmdRSP = new ArrayList<Attributes>();
    private StoreResult result;
    
    /**
     * @param host
     * @param port
     * @param aeTitle
     * @param baseDirectory
     * @param device 
     * @param conn 
     */
    public StoreTool(String host, int port, String aeTitle, File baseDirectory, Device device, String sourceAETitle, Connection conn) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.baseDirectory = baseDirectory;
        this.device = device;
        this.sourceAETitle = sourceAETitle;
        this.conn = conn;
    }

    public void store(String testDescription, String fileName)
            throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {

        long t1, t2;
        Path p = Paths.get(fileName);
        if(!p.isAbsolute() && baseDirectory == null)
            throw new IllegalArgumentException("No base Directory and file"
                    + " to send is provided as a relative path");
        
        File file = p.isAbsolute()? new File(fileName):new File(baseDirectory, fileName);

        assertTrue(
                "file or directory does not exists: " + file.getAbsolutePath(),
                file.exists());
        
        device.setInstalled(true);
        ApplicationEntity ae = new ApplicationEntity(sourceAETitle);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        StoreSCU main = new StoreSCU(ae);

        main.setRspHandlerFactory(new StoreSCU.RSPHandlerFactory() {

            @Override
            public DimseRSPHandler createDimseRSPHandler(final File f) {

                return new DimseRSPHandler(0) {

                    @Override
                    public void onDimseRSP(Association as, Attributes cmd,
                            Attributes data) {
                        super.onDimseRSP(as, cmd, data);
                        StoreTool.this.onCStoreRSP(cmd, f);
                    }
                };
            }

        });

        // configure connection params
        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);
        //ensure secure connection
        main.getRemoteConnection().setTlsCipherSuites(conn.getTlsCipherSuites());
        main.getRemoteConnection().setTlsProtocols(conn.getTlsProtocols());
        main.setAttributes(new Attributes());
        // scan
        main.scanFiles(Arrays.asList(file.getAbsolutePath()), false);
        // create executor
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        device.setExecutor(executorService);
        device.setScheduledExecutor(scheduledExecutorService);

        // open and send
        try {
            main.open();

            t1 = System.currentTimeMillis();
            main.sendFiles();
            t2 = System.currentTimeMillis();
        } finally {
            main.close();
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }
        init(new StoreResult(testDescription, fileName, totalSize, (t2 - t1),
                filesSent, warnings, failures, cmdRSP));
    }

    private void onCStoreRSP(Attributes cmd, File f) {
        cmdRSP.add(cmd);

        int status = cmd.getInt(Tag.Status, -1);
        switch (status) {
        case Status.Success:
            totalSize += f.length();
            ++filesSent;
            break;
        case Status.CoercionOfDataElements:
        case Status.ElementsDiscarded:
        case Status.DataSetDoesNotMatchSOPClassWarning:
            totalSize += f.length();
            ++filesSent;
            ++warnings;
            // System.err.println(MessageFormat.format("warning",
            // TagUtils.shortToHexString(status), f));
            // System.err.println(cmd);
            break;
        default:
            ++failures;
            System.err.println(MessageFormat.format("error",
                    TagUtils.shortToHexString(status), f));
            System.err.println(cmd);
        }
    }

    @Override
    public void init(TestResult result) {
        this.result = (StoreResult) result;
    }

    @Override
    public StoreResult getResult() {
        return this.result;
    }

    public void setbaseDir(String dir) {
        this.baseDirectory = new File (dir);
    }

}
