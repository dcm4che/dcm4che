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

package org.dcm4che.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.util.ByteUtils;
import org.dcm4che.util.StreamUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class BulkDataLocator implements Value {

    public static final int MAGIC_LEN = 0xfbfb;

    public final String uri;
    public final String transferSyntax;
    public final long offset;
    public final int length;

    public BulkDataLocator(String uri, String transferSyntax, long offset,
            int length) {
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("uri: " + uri);
        }
        if (transferSyntax == null)
            throw new NullPointerException("transferSyntax");
        this.uri = uri;
        this.transferSyntax = transferSyntax;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public String toString() {
        return "BulkDataLocator[uri=" +  uri 
                + ", tsuid=" + transferSyntax
                + ", pos=" + offset
                + ", len=" + length + "]";
    }

    public InputStream openStream() throws IOException {
        try {
            return new URI(uri).toURL().openStream();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int calcLength(DicomOutputStream out, VR vr) {
        return getEncodedLength(out, vr);
    }

    @Override
    public int getEncodedLength(DicomOutputStream out, VR vr) {
        return (length + 1) & ~1;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        if (length == 0)
            return ByteUtils.EMPTY_BYTES;

        InputStream in = openStream();
        try {
            StreamUtils.skipFully(in, offset);
            byte[] b = new byte[length];
            StreamUtils.readFully(in, b, 0, b.length);
            if (transferSyntax.equals(UID.ExplicitVRBigEndian) 
                    ? !bigEndian
                    : bigEndian) {
                vr.toggleEndian(b, false);
            }
            return b;
        } finally {
            in.close();
        }

    }

    @Override
    public void writeTo(DicomOutputStream out, VR vr) throws IOException {
        InputStream in = openStream();
        try {
            StreamUtils.skipFully(in, offset);
            if (transferSyntax.equals(UID.ExplicitVRBigEndian)
                    ? !out.isBigEndian()
                    : out.isBigEndian())
                StreamUtils.copy(in, out, length, vr.numEndianBytes());
            else
                StreamUtils.copy(in, out, length);
            if ((length & 1) != 0)
                out.write(vr.paddingByte());
        } finally {
            in.close();
        }
    }

    public void serializeTo(ObjectOutputStream oos) throws IOException {
        oos.writeInt(length);
        oos.writeLong(offset);
        oos.writeUTF(uri);
        oos.writeUTF(transferSyntax);
    }

    public static Value deserializeFrom(ObjectInputStream ois)
            throws IOException {
        int len = ois.readInt();
        long off = ois.readLong();
        String uri = ois.readUTF();
        String tsuid = ois.readUTF();
        return new BulkDataLocator(uri, tsuid, off, len);
    }
}
