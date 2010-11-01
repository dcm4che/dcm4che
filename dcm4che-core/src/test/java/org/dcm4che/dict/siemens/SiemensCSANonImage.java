package org.dcm4che.dict.siemens;

import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.VR;

public class SiemensCSANonImage extends ElementDictionary {

    public static final String PrivateCreator = "SIEMENS CSA NON-IMAGE";

    /** (0029,xx08) VR=CS VM=1 CSA Data Type */
    public static final int CSADataType = 0x00290008;

    /** (0029,xx09) VR=LO VM=1 CSA Data Version */
    public static final int CSADataVersion = 0x00290009;

    /** (0029,xx10) VR=OB VM=1 CSA Data Info */
    public static final int CSADataInfo = 0x00290010;

    public SiemensCSANonImage() {
        super(PrivateCreator, SiemensCSANonImage.class);
    }

    @Override
    public String keywordOf(int tag) {
        switch (tag & 0xFFFF00FF) {
        case CSADataType:
            return "CSADataType";
        case CSADataVersion:
            return "CSADataVersion";
        case CSADataInfo:
            return "CSADataInfo";
        }
        return null;
    }

    @Override
    public VR vrOf(int tag) {
        switch (tag & 0xFFFF00FF) {
        case CSADataType:
            return VR.CS;
        case CSADataVersion:
            return VR.LO;
        case CSADataInfo:
            return VR.OB;
        }
        return VR.UN;
    }

}
