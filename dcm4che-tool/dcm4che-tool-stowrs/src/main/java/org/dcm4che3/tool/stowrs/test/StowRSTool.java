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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.tool.stowrs.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.stowrs.StowRS;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class StowRSTool implements TestTool{

    public enum StowMetaDataType {
        JSON, XML, NO_METADATA_DICOM;
    }
    private TestResult result;
    private final String url;
    private final Attributes keys = new Attributes();

    public StowRSTool(String url) {
        this.url = url;
    }

    public void send(String testDescription, StowMetaDataType metadataType, List<File> files, String transferSyntax) throws IOException, InterruptedException{
        long t1, t2;
        StowRS stowrs = new StowRS(keys, metadataType, files, url, transferSyntax);
        t1 = System.currentTimeMillis();
        stowrs.stow();
        t2 = System.currentTimeMillis();
        init(new StowRSResult(testDescription, t2-t1, stowrs.getResponses()));
    }

    public void send(String testDescription, StowMetaDataType metadataType, File file, String transferSyntax) throws IOException, InterruptedException{
        send(testDescription, metadataType, Collections.singletonList(file), transferSyntax);
    }

    public void overrideTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        keys.setString(tag, vr, value);
    }

    @Override
    public void init(TestResult resultIn) {
        this.result = resultIn;
    }

    @Override
    public TestResult getResult() {
     return this.result;
    }

    public String getUrl() {
        return url;
    }
    
}
