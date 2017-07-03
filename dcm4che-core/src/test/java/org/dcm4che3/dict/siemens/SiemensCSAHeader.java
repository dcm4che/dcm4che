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

package org.dcm4che3.dict.siemens;

import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SiemensCSAHeader extends ElementDictionary {

    public static final String PrivateCreator = "SIEMENS CSA HEADER";

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
        super(PrivateCreator, SiemensCSAHeader.class);
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
