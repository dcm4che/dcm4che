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
package org.dcm4che3.tool.dcmgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class DcmGen{

    private static final Logger LOG = LoggerFactory.getLogger(DcmGen.class);
    
    private static Options options;

    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.dcmgen.messages");

    private int instanceCount = 1;

    private int seriesCount = 1;

    private File outputDir;

    private File seedFile;

    public DcmGen() {}

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();
        options.addOption(null, "override", true, rb.getString("override"));
        CLIUtils.addCommonOptions(options);
        return CLIUtils.parseComandLine(args,options, rb, DcmGen.class);
    }


    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            DcmGen main = new DcmGen();
            cl = parseComandLine(args);
            Attributes overrideAttrs = new Attributes();
            
            if(cl.hasOption("override")) {
                CLIUtils.addAttributes(overrideAttrs, cl.getOptionValues("override"));    
            }
            
            if(cl.getArgList().size()<2) {
                throw new ParseException("Missing required arguments");
            }
            else {
                if(cl.getArgs()[0].contains(":"))
                    setCounts(cl, main,true);
                    else
                        setCounts(cl, main, false);
                if(cl.getArgList().size()==3) {
                    main.seedFile = new File(cl.getArgs()[1]);
                    main.outputDir = new File(cl.getArgs()[2]);
                }
                else if(cl.getArgList().size()==2) {
                    main.seedFile = new File(cl.getArgs()[1]);
                    main.outputDir = new File("./");
                }
                else {
                    throw new ParseException("Too many arguments specified");
                }
                if(main.seriesCount > main.instanceCount) {
                    throw new ParseException("Series count can not exceeed instance count");
                }
                main.generateDICOM(overrideAttrs);
            }

        } catch (ParseException e) {
            LOG.error("DcmGen\t" + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    public List<String> generateDICOM(Attributes overrideAttrs) {
        ArrayList<String> generatedFiles = new ArrayList<String>();
        String studyIUID = UIDUtils.createUID();
        Attributes seedAttrs = null;
        Attributes fmiOld = null;
        Attributes fmi = null;
        if (this.seedFile.getName().endsWith(".xml")) {
            try {
                seedAttrs = SAXReader.parse(new FileInputStream(this.seedFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            DicomInputStream din = null;
            
            try {
                din = new DicomInputStream(this.seedFile);
                din.setIncludeBulkData(IncludeBulkData.URI);
                seedAttrs = din.readDataset(-1, -1);
                fmiOld = din.readFileMetaInformation();
                
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally {
                SafeClose.close(din);
            }
        }
        DicomOutputStream dout = null;
        Attributes modified = new Attributes();
        if(!overrideAttrs.isEmpty())
            seedAttrs.update(overrideAttrs, modified);
        
        if(!modified.isEmpty()) {
            if(modified.contains(Tag.StudyInstanceUID)) {
                studyIUID = modified.getString(Tag.StudyInstanceUID);
            }
        }
        int splitCnt = this.instanceCount / this.seriesCount;
        
        int i=0;
        
        if(this.instanceCount % this.seriesCount !=0) {
            this.seriesCount++;
        }
        while ( i<this.instanceCount) {
            String seriesuid = UIDUtils.createUID();
            seedAttrs.setString(Tag.SeriesInstanceUID, VR.UI, seriesuid);
            for(int j=0;j<splitCnt;j++) {
                if(i==this.instanceCount)
                    break;
                String iuid = UIDUtils.createUID();
                seedAttrs.setString(Tag.SOPInstanceUID, VR.UI, iuid);
                try {
                    File parent = new File(this.outputDir+"/"+studyIUID+"/"+seriesuid);
                    if(!parent.exists())
                        parent.mkdirs();
                    File outFile = new File(parent,iuid+".dcm");
                    dout = new DicomOutputStream(outFile);
                    fmi = seedAttrs.createFileMetaInformation(fmiOld.getString(Tag.TransferSyntaxUID));
                    dout.writeDataset(fmi, seedAttrs);
                    generatedFiles.add(outFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    SafeClose.close(dout);
                }
                i++;
            }
            
        }
        return generatedFiles;
    }

    private static void setCounts(CommandLine cl, DcmGen main, boolean both)
            throws ParseException {
        try{
            main.instanceCount = Integer.parseInt(cl.getArgs()[0].split(":")[0]);
            if(both)
            main.seriesCount = Integer.parseInt(cl.getArgs()[0].split(":")[1]);
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new ParseException("number of instances/number of series incorrectly specified");
        }
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public void setSeriesCount(int seriesCount) {
        this.seriesCount = seriesCount;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setSeedFile(File seedFile) {
        this.seedFile = seedFile;
    }

}