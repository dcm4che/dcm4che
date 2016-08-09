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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.net;

import java.io.Serializable;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Aug 2016
 */
public class ApplicationEntityInfo implements Serializable {

    private String deviceName;
    private String description;
    private String aeTitle;
    private String[] applicationCluster;
    private Boolean associationInitiator;
    private Boolean associationAcceptor;
    private String[] networkConnectionReference;
    private String[] preferredCalledAETitle;
    private String[] preferredCallingAETitle;
    private String[] supportedCharacterSet;
    private boolean installed;


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String[] getApplicationCluster() {
        return applicationCluster;
    }

    public void setApplicationCluster(String[] applicationCluster) {
        this.applicationCluster = applicationCluster;
    }

    public Boolean getAssociationInitiator() {
        return associationInitiator;
    }

    public void setAssociationInitiator(Boolean associationInitiator) {
        this.associationInitiator = associationInitiator;
    }

    public Boolean getAssociationAcceptor() {
        return associationAcceptor;
    }

    public void setAssociationAcceptor(Boolean associationAcceptor) {
        this.associationAcceptor = associationAcceptor;
    }

    public String[] getNetworkConnectionReference() {
        return networkConnectionReference;
    }

    public void setNetworkConnectionReference(String[] networkConnectionReference) {
        this.networkConnectionReference = networkConnectionReference;
    }

    public String[] getPreferredCalledAETitle() {
        return preferredCalledAETitle;
    }

    public void setPreferredCalledAETitle(String[] preferredCalledAETitle) {
        this.preferredCalledAETitle = preferredCalledAETitle;
    }

    public String[] getPreferredCallingAETitle() {
        return preferredCallingAETitle;
    }

    public void setPreferredCallingAETitle(String[] preferredCallingAETitle) {
        this.preferredCallingAETitle = preferredCallingAETitle;
    }

    public String[] getSupportedCharacterSet() {
        return supportedCharacterSet;
    }

    public void setSupportedCharacterSet(String[] supportedCharacterSet) {
        this.supportedCharacterSet = supportedCharacterSet;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public String toString() {
        return "ApplicationEntityInfo[dicomAETitle=" + aeTitle
                + "]";
    }

}
