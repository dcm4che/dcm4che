
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

package org.dcm4che3.net.web;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.AEExtension;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@LDAP(objectClasses = "dcmArchiveAEWebServices", noContainerNode = true)
@ConfigurableClass
public class WebServiceAEExtension extends AEExtension {

    private static final long serialVersionUID = -2390448404282661045L;

    @ConfigurableProperty(name = "dcmWadoRSBaseURL")
    private String wadoRSBaseURL;

    @ConfigurableProperty(name = "dcmWadoURIBaseURL")
    private String wadoURIBaseURL;

    @ConfigurableProperty(name = "dcmStowRSBaseURL")
    private String stowRSBaseURL;

    @ConfigurableProperty(name = "dcmQidoRSBaseURL")
    private String qidoRSBaseURL;

    @ConfigurableProperty(name = "dcmRsCapabilitiesBaseURL")
    private String rsCapabilitiesBaseURL;
    
    @ConfigurableProperty(name = "dcmStowQidoVerificationDelaySec", defaultValue = "60")
    private int stowQidoVerificationDelaySec;

    public String getWadoRSBaseURL() {
        return wadoRSBaseURL;
    }

    public void setWadoRSBaseURL(String wadoRSBaseURL) {
        this.wadoRSBaseURL = wadoRSBaseURL;
    }

    public String getWadoURIBaseURL() {
        return wadoURIBaseURL;
    }

    public void setWadoURIBaseURL(String wadoURIBaseURL) {
        this.wadoURIBaseURL = wadoURIBaseURL;
    }

    public String getStowRSBaseURL() {
        return stowRSBaseURL;
    }

    public void setStowRSBaseURL(String stowRSBaseURL) {
        this.stowRSBaseURL = stowRSBaseURL;
    }

    public String getQidoRSBaseURL() {
        return qidoRSBaseURL;
    }

    public void setQidoRSBaseURL(String qidoRSBaseURL) {
        this.qidoRSBaseURL = qidoRSBaseURL;
    }

    public String getRsCapabilitiesBaseURL() {
        return rsCapabilitiesBaseURL;
    }

    public void setRsCapabilitiesBaseURL(String rsCapabilitiesBaseURL) {
        this.rsCapabilitiesBaseURL = rsCapabilitiesBaseURL;
    }
    
    public int getStowQidoVerificationDelaySec() {
        return stowQidoVerificationDelaySec;
    }

    public void setStowQidoVerificationDelaySec(int stowQidoVerificationDelaySec) {
        this.stowQidoVerificationDelaySec = stowQidoVerificationDelaySec;
    }

    @Override
    public void reconfigure(AEExtension from) {
        WebServiceAEExtension webServiceAEExtension = (WebServiceAEExtension) from;
        setQidoRSBaseURL(webServiceAEExtension.getQidoRSBaseURL());
        setRsCapabilitiesBaseURL(webServiceAEExtension.getRsCapabilitiesBaseURL());
        setStowRSBaseURL(webServiceAEExtension.getStowRSBaseURL());
        setWadoRSBaseURL(webServiceAEExtension.getWadoRSBaseURL());
        setWadoURIBaseURL(webServiceAEExtension.getWadoURIBaseURL());
        setStowQidoVerificationDelaySec(webServiceAEExtension.getStowQidoVerificationDelaySec());
    }
}
