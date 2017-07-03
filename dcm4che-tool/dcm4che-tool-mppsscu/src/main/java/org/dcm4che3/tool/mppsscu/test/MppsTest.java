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

package org.dcm4che3.tool.mppsscu.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.tool.mppsscu.MppsSCU;
import org.dcm4che3.util.TagUtils;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class MppsTest {

    private String host;
    private int port;
    String aeTitle;
    String baseDirectory;

    private int nCreateSent;  
    private int nCreateWarnings;    
    private int nCreateFailures;
    private int nSetSent;  
    private int nSetFailures;
    
    /**
     * @param host
     * @param port
     * @param aeTitle
     * @param baseDirectory
     */
    public MppsTest(String host, int port, String aeTitle, String baseDirectory) {
        super();
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.baseDirectory = baseDirectory;
    }

    public MppsResult mppsscu(String testDescription, String fileName) throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {

        long t1, t2, t3;

        File file = new File(baseDirectory, fileName);

        assertTrue(
                "file or directory does not exists: " + file.getAbsolutePath(),
                file.exists());

        Device device = new Device("mppsscu");
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity("MPPSSCU");
        device.addApplicationEntity(ae);
        ae.addConnection(conn);

        final MppsSCU main = new MppsSCU(ae);
        
        main.setRspHandlerFactory(new MppsSCU.RSPHandlerFactory() {

            @Override
            public DimseRSPHandler createDimseRSPHandlerForNCreate(final MppsSCU.MppsWithIUID mppsWithUID) {

                return new DimseRSPHandler(0) {

                    @Override
                    public void onDimseRSP(Association as, Attributes cmd,
                            Attributes data) {
                        
                        switch(cmd.getInt(Tag.Status, -1)) {
                        case Status.Success:
                        case Status.AttributeListError:
                        case Status.AttributeValueOutOfRange:
                            mppsWithUID.iuid = cmd.getString(
                                    Tag.AffectedSOPInstanceUID, mppsWithUID.iuid);
                            main.addCreatedMpps(mppsWithUID);
                        }
                        
                        super.onDimseRSP(as, cmd, data);
                        MppsTest.this.onNCreateRSP(cmd);
                    }
                };
            }
            
            @Override
            public DimseRSPHandler createDimseRSPHandlerForNSet() {
                
                return new DimseRSPHandler(0) {
                    
                    @Override
                    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                        
                        super.onDimseRSP(as, cmd, data);
                        MppsTest.this.onNSetRSP(cmd);
                    }
                };
            }

        });

        // configure
        main.getAAssociateRQ().setCalledAET(aeTitle);
        main.getRemoteConnection().setHostname(host);
        main.getRemoteConnection().setPort(port);
        main.setTransferSyntaxes(new String[]{UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired});
        main.setAttributes(new Attributes());

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
            main.createMpps();
            t2 = System.currentTimeMillis();
            main.updateMpps();
            t3 = System.currentTimeMillis();
        } finally {
            main.close();
            executorService.shutdown();
            scheduledExecutorService.shutdown();
        }

        return new MppsResult(testDescription, fileName, 
                nCreateSent, nCreateWarnings, nCreateFailures, 
                nSetSent, nSetFailures, (t2-t1), (t3-t2));
    }

    private void onNCreateRSP(Attributes cmd) {
        int status = cmd.getInt(Tag.Status, -1);
        switch (status) {
        case Status.Success:
            ++nCreateSent;
            break;
        case Status.CoercionOfDataElements:
        case Status.AttributeListError:
        case Status.AttributeValueOutOfRange:
            ++nCreateSent;
            ++nCreateWarnings;
            break;
        default:
            ++nCreateFailures;
            System.err.println(MessageFormat.format("error",
                    TagUtils.shortToHexString(status)));
            System.err.println(cmd);
        }
    }

    private void onNSetRSP(Attributes cmd) {
        int status = cmd.getInt(Tag.Status, -1);
        switch (status) {
        case Status.Success:
            ++nSetSent;
            break;
        default:
            ++nSetFailures;
            System.err.println(MessageFormat.format("error",
                    TagUtils.shortToHexString(status)));
            System.err.println(cmd);
        }
    }
}
