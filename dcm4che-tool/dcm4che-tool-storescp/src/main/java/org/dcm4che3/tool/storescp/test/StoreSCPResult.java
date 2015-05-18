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

package org.dcm4che3.tool.storescp.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.test.TestResult;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 */

public class StoreSCPResult implements TestResult {


    private String testDescription;
    private String fileName;
    private long time;
    private int filesReceived;
    private List<Attributes> cStoreRQAttributes;
    private List<String> instanceLocations;
    private List<String> sopUIDs;
    /**
     * @param testDescription
     * @param fileName
     * @param size
     * @param time
     * @param filesReceived
     * @param sopIUIDs 
     * @param warnings
     * @param failures
     * @param cmdRSP 
     */
    public StoreSCPResult(String testDescription, long time, int filesReceived
            , List<Attributes> cmdRQ, List<String> sopIUIDs
            , List<String> instanceLocations) {
        this.testDescription = testDescription;
        this.instanceLocations = instanceLocations;
        this.time = time;
        this.filesReceived = filesReceived;
        this.cStoreRQAttributes = cmdRQ;
        this.sopUIDs = sopIUIDs;
    }
    public String getTestDescription() {
        return testDescription;
    }
    public String getFileName() {
        return fileName;
    }
    public long getTime() {
        return time;
    }
    public int getFilesReceived() {
        return filesReceived;
    }
    public List<Attributes> getcStoreRQAttributes() {
        return cStoreRQAttributes;
    }
    public List<String> getSopUIDs() {
        return sopUIDs;
    }
    public List<Attributes> getInstanceAttributes() {
        ArrayList<Attributes> attrs = new ArrayList<Attributes>();
        DicomInputStream din = null;
        for(String instancePath : instanceLocations) {
            File file = new File(instancePath);
            try{
                din = new DicomInputStream(file);
                
                attrs.add(din.readDataset(-1, Tag.PixelData));
                
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    din.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return attrs;
    }
}
