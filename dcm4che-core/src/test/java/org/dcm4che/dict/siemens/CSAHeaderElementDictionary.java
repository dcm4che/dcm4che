package org.dcm4che.dict.siemens;

import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.VR;

public class CSAHeaderElementDictionary extends ElementDictionary {

    /** (0029,xx08) VR=CS VM=1 CSA Image Header Type */
    public static final int CSAImageHeaderType = 0x00290008;

    /** (0029,xx09) VR=LO VM=1 CSA Image Header Version */
    public static final int CSAImageHeaderVersion = 0x00290009;

    /** (0029,xx10) VR=OB VM=1 CSA Image Header Info */
    public static final int CSAImageHeaderInfo = 0x00290010;

    /** (0029,xx18) VR=CS VM=1 CSA Series Header Info */
    public static final int CSASeriesHeaderType = 0x00290018;

    /** (0029,xx19) VR=LO VM=1 CSA Series Header Version */
    public static final int CSASeriesHeaderVersion = 0x00290019;

    /** (0029,xx20) VR=OB VM=1 CSA Series Header Info */
    public static final int CSASeriesHeaderInfo = 0x00290020;

    public CSAHeaderElementDictionary() {
        super("SIEMENS CSA HEADER", CSAHeaderElementDictionary.class);
    }

    @Override
    public String nameOf(int tag) {
        switch (tag & 0xFFFF00FF) {
        case CSAImageHeaderType:
            return "CSA Image Header Type";
        case CSAImageHeaderVersion:
            return "CSA Image Header Version";
        case CSAImageHeaderInfo:
            return "CSA Image Header Info";
        case CSASeriesHeaderVersion:
            return "CSA Series Header Version";
        case CSASeriesHeaderType:
            return "CSA Series Header Type";
        case CSASeriesHeaderInfo:
            return "CSA Series Header Info";
        }
        return "Unknown Attribute";
    }

    @Override
    public VR vrOf(int tag) {
        switch (tag & 0xFFFF00FF) {
        case CSAImageHeaderType:
        case CSASeriesHeaderType:
            return VR.CS;
        case CSAImageHeaderVersion:
        case CSASeriesHeaderVersion:
            return VR.LO;
        case CSAImageHeaderInfo:
        case CSASeriesHeaderInfo:
            return VR.OB;
        }
        return VR.UN;
    }

}
