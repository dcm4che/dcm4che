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

import org.dcm4che3.data.UID;

/**
 * Transfer syntaxes of DICOM Supplement 253 (waveform compression with ITU-T T.261).
 * <p>
 * The UIDs come from the dictionary ({@link UID#T261Lossless}, {@link UID#T261Lossy}). As long as
 * the supplement is under public comment (v9) the dictionary carries its placeholders
 * {@code UID_TS1_lossless} and {@code UID_TS2_lossy}; the final UIDs replace them in
 * {@code dcm4che-dict} without any change in the callers.
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public final class T261TransferSyntax {

    private T261TransferSyntax() {
    }

    /** @return the UID of the T.261 Transfer Syntax for Lossless Waveform Compression. */
    public static String lossless() {
        return UID.T261Lossless;
    }

    /** @return the UID of the T.261 Transfer Syntax for Lossy Waveform Compression. */
    public static String lossy() {
        return UID.T261Lossy;
    }

    /** @return {@code true} for both T.261 transfer syntaxes. */
    public static boolean isT261(String tsuid) {
        return UID.T261Lossless.equals(tsuid) || UID.T261Lossy.equals(tsuid);
    }

    /** @return {@code true} for the T.261 Transfer Syntax for Lossy Waveform Compression. */
    public static boolean isLossy(String tsuid) {
        return UID.T261Lossy.equals(tsuid);
    }
}
