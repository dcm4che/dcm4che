/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.net;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since April 2019
 */
public class KeycloakClient {
    private Device device;
    private String keycloakClientID;
    private String keycloakServerURL;
    private String keycloakRealm;
    private String keycloakClientSecret;
    private String userID;
    private String password;
    private GrantType keycloakGrantType = GrantType.client_credentials;
    private boolean tlsAllowAnyHostname;
    private boolean tlsDisableTrustManager;

    public enum GrantType {
        client_credentials, password
    }

    public KeycloakClient() {
    }

    public KeycloakClient(String keycloakClientID) {
        setKeycloakClientID(keycloakClientID);
    }

    public Device getDevice() {
        return device;
    }

    void setDevice(Device device) {
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " +
                        this.device.getDeviceName());
        }
        this.device = device;
    }

    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public String getKeycloakServerURL() {
        return keycloakServerURL;
    }

    public void setKeycloakServerURL(String keycloakServerURL) {
        this.keycloakServerURL = keycloakServerURL;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }

    public GrantType getKeycloakGrantType() {
        return keycloakGrantType;
    }

    public void setKeycloakGrantType(GrantType keycloakGrantType) {
        this.keycloakGrantType = keycloakGrantType;
    }

    public String getKeycloakClientSecret() {
        return keycloakClientSecret;
    }

    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTLSAllowAnyHostname() {
        return tlsAllowAnyHostname;
    }

    public void setTLSAllowAnyHostname(boolean tlsAllowAnyHostname) {
        this.tlsAllowAnyHostname = tlsAllowAnyHostname;
    }

    public boolean isTLSDisableTrustManager() {
        return tlsDisableTrustManager;
    }

    public void setTLSDisableTrustManager(boolean tlsDisableTrustManager) {
        this.tlsDisableTrustManager = tlsDisableTrustManager;
    }

    public KeycloakClient clone() {
        KeycloakClient clone = new KeycloakClient();
        clone.device = device;
        clone.keycloakClientID = keycloakClientID;
        clone.keycloakServerURL = keycloakServerURL;
        clone.keycloakRealm = keycloakRealm;
        clone.keycloakClientSecret = keycloakClientSecret;
        clone.userID = userID;
        clone.password = password;
        clone.keycloakGrantType = keycloakGrantType;
        clone.tlsAllowAnyHostname = tlsAllowAnyHostname;
        clone.tlsDisableTrustManager = tlsDisableTrustManager;
        return clone;
    }

    public void reconfigure(KeycloakClient src) {
        keycloakServerURL = src.keycloakServerURL;
        keycloakRealm = src.keycloakRealm;
        keycloakGrantType = src.keycloakGrantType;
        keycloakClientSecret = src.keycloakClientSecret;
        userID = src.userID;
        password = src.password;
        tlsAllowAnyHostname = src.tlsAllowAnyHostname;
        tlsDisableTrustManager = src.tlsDisableTrustManager;
    }

    @Override
    public String toString() {
        return "KeycloakClient[keycloakClientID=" + keycloakClientID
                + ",keycloakServerURL=" + keycloakServerURL
                + ",keycloakRealm=" + keycloakRealm
                + ",keycloakGrantType=" + keycloakGrantType
                + ",keycloakClientSecret=" + keycloakClientSecret
                + ",userID=" + userID
                + ",password=" + password
                + ",tlsAllowAnyHostname=" + tlsAllowAnyHostname
                + ",tlsDisableTrustManager=" + tlsDisableTrustManager
                + ']';
    }

}
