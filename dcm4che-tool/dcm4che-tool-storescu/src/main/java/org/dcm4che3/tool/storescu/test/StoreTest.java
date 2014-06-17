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
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.GenericTest;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.dcm4che3.util.TagUtils;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class StoreTest extends GenericTest {

    private String testDescription;
    private String fileName;

    private long totalSize;
    private int filesSent;
    private int warnings;    
    private int failures;

    /**
     * @param testName
     * @param testDescription
     * @param fileName
     */
    public StoreTest(String testDescription, String fileName) {
        super();
        this.testDescription = testDescription;
        this.fileName = fileName;
    }
    
    public StoreResult store() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {

        List<StoreResult> results = new ArrayList<StoreResult>();
        
        long t1, t2;

        Properties config = loadConfig();
        String host = config.getProperty("remoteConn.hostname");
        int port = new Integer(config.getProperty("remoteConn.port"));
        String aeTitle = config.getProperty("store.aetitle");
        String directory = config.getProperty("store.directory");

        File file = new File(directory, fileName);

        assertTrue(
                "file or directory does not exists: " + file.getAbsolutePath(),
                file.exists());

        Device device = new Device("storescu");
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity("STORESCU");
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
                        StoreTest.this.onCStoreRSP(cmd, f);
                    }
                };
            }

        });

        // configure
        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);

        // specify attributes added to the sent object(s). attr can be
        // specified by keyword or tag value (in hex), e.g. PatientName
        // or 00100010. Attributes in nested Datasets can be specified
        // by including the keyword/tag value of the sequence attribute,
        // e.g. 00400275/00400009 for Scheduled Procedure Step ID in
        // the Request Attributes Sequence.

        String[] attributes = new String[0];
        main.setAttributes(new Attributes());
        CLIUtils.addAttributes(main.getAttributes(), attributes);

        // scan
        t1 = System.currentTimeMillis();
        main.scanFiles(Arrays.asList(file.getAbsolutePath()), false); //do not printout
        t2 = System.currentTimeMillis();

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
        
        return new StoreResult(testDescription, directory, totalSize, (t2 - t1), filesSent, warnings, failures);
    }

    private void onCStoreRSP(Attributes cmd, File f) {
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
//            System.err.println(MessageFormat.format("warning",
//                    TagUtils.shortToHexString(status), f));
//            System.err.println(cmd);
            break;
        default:
            ++failures;
            System.err.println(MessageFormat.format("error",
                    TagUtils.shortToHexString(status), f));
            System.err.println(cmd);
        }
    }

}
