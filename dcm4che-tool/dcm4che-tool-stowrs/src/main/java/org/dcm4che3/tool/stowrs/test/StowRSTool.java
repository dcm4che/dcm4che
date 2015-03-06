package org.dcm4che3.tool.stowrs.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.tool.common.test.TestResult;
import org.dcm4che3.tool.common.test.TestTool;
import org.dcm4che3.tool.stowrs.StowRS;
public class StowRSTool implements TestTool{

    public enum StowMetaDataType {
        JSON, XML, NO_METADATA_DICOM;
    }
    private TestResult result;
    private String url;
    private Attributes keys = new Attributes();

    public StowRSTool(String url) {
        this.url = url;
    }

    public void send(String testDescription, StowMetaDataType metadataType, List<File> files){
        long t1, t2;
        StowRS stowrs = new StowRS(keys, metadataType.name(), files, url);
    }

    public void send(String testDescription, StowMetaDataType metadataType, File file){
        long t1, t2;
        
    }

    private void send(StowMetaDataType metadataType, List<File> files) {
        
        switch (metadataType) {
        case NO_METADATA_DICOM:
            
            break;
        case XML:
            
            break;
        case JSON:
            
            break;

        default:
            throw new IllegalArgumentException("Unknown Stow Meta Data Type");
        }        
    }

    public void overrideTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        keys.setString(tag, vr, value);
    }

    @Override
    public void init(TestResult result) {
        this.result = result;
    }

    @Override
    public TestResult getResult() {
     return this.result;
    }

    public String getUrl() {
        return url;
    }
    
}
