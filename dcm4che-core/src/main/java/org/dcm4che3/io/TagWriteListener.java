/*
 * *** BEGIN LICENSE BLOCK *****
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.io;

import org.dcm4che3.data.VR;

/**
 * Callback notified by {@link DicomOutputStream} immediately before a DICOM tag header is written,
 * allowing callers to track the byte offset of specific tags (e.g. {@code PixelData}, {@code Item})
 * without a second pass over the written stream.
 *
 * @see DicomOutputStream#setTagWriteListener(TagWriteListener)
 */
public interface TagWriteListener {

    /**
     * Called immediately before a DICOM tag header is written to the output stream.
     *
     * @param tag the DICOM tag being written
     * @param vr the Value Representation of the tag
     * @param length the value length being written ({@code -1} for undefined length)
     * @param byteOffset the byte offset in the output stream where this tag header will be written
     */
    void onTagWrite(int tag, VR vr, int length, long byteOffset);
}
