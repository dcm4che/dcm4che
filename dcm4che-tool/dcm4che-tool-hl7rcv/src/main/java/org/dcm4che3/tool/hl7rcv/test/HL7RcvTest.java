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

package org.dcm4che3.tool.hl7rcv.test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.tool.hl7rcv.HL7Rcv;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class HL7RcvTest {

    private String host;
    private int port;
    private File xslt;
    
    private HL7Rcv main;
    
    /**
     * @param host
     * @param port
     * @param xslt
     */
    public HL7RcvTest(String host, int port, File xslt) {
        super();
        this.host = host;
        this.port = port;
        this.xslt = xslt;
    }
    
    public void bind() throws Exception{
        
            main = new HL7Rcv();
        
            main.setXSLT(xslt.toURI().toURL());
            main.getConn().setPort(this.port);
            main.getConn().setHostname(this.host);
            
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
                    Executors.newSingleThreadScheduledExecutor();
            main.getDevice().setScheduledExecutor(scheduledExecutorService);
            main.getDevice().setExecutor(executorService);
            main.getDevice().bindConnections();
    }
    
    public void unbind() throws Exception{
     
        if (main!=null && main.getConn()!=null && main.getConn().isListening())
        {
            main.getConn().unbind();
        }
    }
        

}
