package org.dcm4che3.tool.stowrs.test;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.tool.common.SimpleHTTPResponse;

public class StowRSResponse extends SimpleHTTPResponse{

    Attributes responseAttributes = new Attributes();
    public StowRSResponse(int status, String message) {
        super(status, message);
    }
    
    public StowRSResponse(int status, String message, Attributes responseAttrs) {
        super(status, message);
        this.responseAttributes = responseAttrs;
    }

    public Attributes getResponseAttributes() {
        return responseAttributes;
    }
}
