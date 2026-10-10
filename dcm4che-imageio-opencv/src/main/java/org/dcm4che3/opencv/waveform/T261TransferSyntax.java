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
 * Portions created by the Initial Developer are Copyright (C) 2026
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
package org.dcm4che3.opencv.waveform;

/**
 * Transfer syntaxes of DICOM Supplement 253 (waveform compression with ITU-T T.261).
 * <p>
 * The supplement under public comment (v9) does not assign the UIDs yet: it uses the placeholders
 * {@code UID_TS1_lossless} and {@code UID_TS2_lossy}. The values used here default to those
 * placeholders and can be overridden with the system properties {@code dcm4che.t261.lossless.uid} /
 * {@code dcm4che.t261.lossy.uid} or with {@link #setUIDs(String, String)} once the final UIDs are
 * published and added to the dictionary, without any other change in the callers.
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public final class T261TransferSyntax {

    /** Placeholder of the lossless transfer syntax in Sup 253 v9. */
    public static final String PLACEHOLDER_LOSSLESS = "UID_TS1_lossless";

    /** Placeholder of the lossy transfer syntax in Sup 253 v9. */
    public static final String PLACEHOLDER_LOSSY = "UID_TS2_lossy";

    private static volatile String lossless =
            System.getProperty("dcm4che.t261.lossless.uid", PLACEHOLDER_LOSSLESS);
    private static volatile String lossy =
            System.getProperty("dcm4che.t261.lossy.uid", PLACEHOLDER_LOSSY);

    private T261TransferSyntax() {
    }

    public static String lossless() {
        return lossless;
    }

    public static String lossy() {
        return lossy;
    }

    /** Replaces the placeholders with the UIDs assigned by the standard. */
    public static void setUIDs(String losslessUID, String lossyUID) {
        if (losslessUID == null || lossyUID == null)
            throw new NullPointerException();
        lossless = losslessUID;
        lossy = lossyUID;
    }

    public static boolean isT261(String tsuid) {
        return tsuid != null && (tsuid.equals(lossless) || tsuid.equals(lossy));
    }

    public static boolean isLossy(String tsuid) {
        return tsuid != null && tsuid.equals(lossy);
    }
}
