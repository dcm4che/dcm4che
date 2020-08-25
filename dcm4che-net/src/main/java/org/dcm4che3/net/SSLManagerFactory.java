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

package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public abstract class SSLManagerFactory {

    public static KeyStore createKeyStore(X509Certificate... certs)
            throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.load(null);
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (CertificateException e) {
            throw new AssertionError(e);
        }
        for (X509Certificate cert : certs)
            ks.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
        return ks;
    }

    public static KeyStore loadKeyStore(String type, String url, String password)
            throws IOException, KeyStoreException, NoSuchAlgorithmException,
                CertificateException {
        return loadKeyStore(type, url, password.toCharArray());
    }

    public static KeyStore loadKeyStore(String type, String url, char[] password)
            throws IOException, KeyStoreException, NoSuchAlgorithmException,
                CertificateException {
        KeyStore ks = KeyStore.getInstance(type);
        InputStream in = StreamUtils.openFileOrURL(url);
        try {
            ks.load(in, password);
        } finally {
            SafeClose.close(in);
        }
        return ks;
    }

    public static KeyManager createKeyManager(String type, String url,
            char[] storePassword, char[] keyPassword)
            throws UnrecoverableKeyException, KeyStoreException,
                NoSuchAlgorithmException, CertificateException, IOException {
        return createKeyManager(loadKeyStore(type, url, storePassword), keyPassword);
    }

    public static KeyManager createKeyManager(String type, String url,
            String storePassword, String keyPassword)
            throws UnrecoverableKeyException, KeyStoreException,
                NoSuchAlgorithmException, CertificateException, IOException {
        return createKeyManager(loadKeyStore(type, url, storePassword), keyPassword);
    }

    public static KeyManager createKeyManager(KeyStore ks, String password)
            throws UnrecoverableKeyException, KeyStoreException {
        return createKeyManager(ks, password.toCharArray());
    }

    public static KeyManager createKeyManager(KeyStore ks, char[] password)
            throws UnrecoverableKeyException, KeyStoreException {
        try {
            KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);
            KeyManager[] kms = kmf.getKeyManagers();
            return kms.length > 0 ? kms[0] : null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static TrustManager createTrustManager(KeyStore ks)
            throws KeyStoreException {
        try {
            TrustManagerFactory kmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf.init(ks);
            TrustManager[] tms = kmf.getTrustManagers();
            return tms.length > 0 ? tms[0] : null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static TrustManager createTrustManager(X509Certificate... certs)
            throws KeyStoreException {
        return createTrustManager(createKeyStore(certs));
    }

    public static TrustManager createTrustManager(String type, String url, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
                CertificateException, IOException {
        return createTrustManager(loadKeyStore(type, url, password));
    }

    public static TrustManager createTrustManager(String type, String url, String password)
            throws KeyStoreException, NoSuchAlgorithmException,
                CertificateException, IOException {
        return createTrustManager(loadKeyStore(type, url, password));
    }
}
