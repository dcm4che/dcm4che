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
package org.dcm4che3.imageio.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.dcm4che3.data.UID;
import org.junit.Test;

/**
 * The T.261 waveform transfer syntaxes of DICOM Supplement 253 in the dictionary and in
 * {@link TransferSyntaxType}.
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public class TransferSyntaxTypeTest {

    @Test
    public void testT261InDictionary() {
        assertEquals(UID.T261Lossless, UID.forName("T261Lossless"));
        assertEquals(UID.T261Lossy, UID.forName("T261Lossy"));
        assertEquals("T.261 Transfer Syntax for Lossless Waveform Compression", UID.nameOf(UID.T261Lossless));
        assertEquals("T.261 Transfer Syntax for Lossy Waveform Compression", UID.nameOf(UID.T261Lossy));
        assertFalse(UID.T261Lossless.equals(UID.T261Lossy));
    }

    @Test
    public void testT261TransferSyntaxType() {
        assertEquals(TransferSyntaxType.T261, TransferSyntaxType.forUID(UID.T261Lossless));
        assertEquals(TransferSyntaxType.T261, TransferSyntaxType.forUID(UID.T261Lossy));
        assertTrue(TransferSyntaxType.T261.isWaveformDataEncapsulated());
        assertFalse(TransferSyntaxType.T261.isPixeldataEncapsulated());
        assertTrue(TransferSyntaxType.T261.canEncodeSigned());
        assertEquals(24, TransferSyntaxType.T261.getMaxBitsStored());
        for (TransferSyntaxType type : TransferSyntaxType.values())
            if (type != TransferSyntaxType.T261)
                assertFalse(type.name(), type.isWaveformDataEncapsulated());
    }

    @Test
    public void testT261LossyCompression() {
        assertFalse(TransferSyntaxType.isLossyCompression(UID.T261Lossless));
        assertTrue(TransferSyntaxType.isLossyCompression(UID.T261Lossy));
        assertFalse(TransferSyntaxType.isYBRCompression(UID.T261Lossless));
        assertFalse(TransferSyntaxType.isYBRCompression(UID.T261Lossy));
    }
}
