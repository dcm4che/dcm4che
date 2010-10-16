package org.dcm4che.dict.siemens;

import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.VR;

public class SiemensCSAHeader extends ElementDictionary {

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

    public SiemensCSAHeader() {
        super("SIEMENS CSA HEADER", SiemensCSAHeader.class);
    }

    @Override
    public String keywordOf(int tag) {
        switch (tag & 0xFFFF00FF) {
        case CSAImageHeaderType:
            return "CSAImageHeaderType";
        case CSAImageHeaderVersion:
            return "CSAImageHeaderVersion";
        case CSAImageHeaderInfo:
            return "CSAImageHeaderInfo";
        case CSASeriesHeaderVersion:
            return "CSASeriesHeaderVersion";
        case CSASeriesHeaderType:
            return "CSASeriesHeaderType";
        case CSASeriesHeaderInfo:
            return "CSASeriesHeaderInfo";
        }
        return null;
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
