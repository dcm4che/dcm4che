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

package org.dcm4che3.data;

import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Bill Wallace <wayfarer3130@gmail.com>
 */
public class BulkData implements Value {

    public static final int MAGIC_LEN = 0xfbfb;

    private final String uuid;
    private String uri;
    private int uriPathEnd;
    private final boolean bigEndian;
    private long offset = 0;
    private int length = -1;

    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        setURI(uri);
        this.bigEndian = bigEndian;
    }

    public BulkData(String uri, long offset, int length, boolean bigEndian) {
        this.uuid = null;
        this.uriPathEnd = uri.length();
        this.uri = uri + "?offset=" + offset + "&length=" + length;
        this.offset = offset;
        this.length = length;
        this.bigEndian = bigEndian;
    }
    
    public String getUUID() {
        return uuid;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
        this.uriPathEnd = uri.length();
        this.offset = 0;
        this.length = -1;
        int pathEnd = uri.indexOf('?');
        if (pathEnd < 0)
            return;
        
        this.uriPathEnd = pathEnd;
        if (!uri.startsWith("?offset=", pathEnd))
            return;
        
        int offsetEnd = uri.indexOf("&length=", pathEnd + 8);
        if (offsetEnd < 0)
            return;

        try {
            this.offset = Integer.parseInt(uri.substring(pathEnd + 8, offsetEnd));
            this.length = Integer.parseInt(uri.substring(offsetEnd + 8));
        } catch (NumberFormatException ignore) {}
    }

    public boolean bigEndian() {
        return bigEndian;
    }

    public int length() {
        return length;
    }

    public long offset() {
        return offset;
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public String toString() {
        return "BulkData[uuid=" + uuid
                + ", uri=" +  uri 
                + ", bigEndian=" + bigEndian
                + "]";
    }

    public File getFile() {
        try {
            return new File(new URI(uriWithoutOffsetAndLength()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("uri: " + uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("uri: " + uri);
        }
    }

    public String uriWithoutOffsetAndLength() {
        if (uri == null)
            throw new IllegalStateException("uri: null");

        return uri.substring(0, uriPathEnd);
    }

    public InputStream openStream() throws IOException {
        if (uri == null)
            throw new IllegalStateException("uri: null");
 
        if (!uri.startsWith("file:"))
            return new URL(uri).openStream();

        InputStream in = new FileInputStream(getFile());
        StreamUtils.skipFully(in, offset);
        return in;

    }

    @Override
    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        if (length == -1)
            throw new UnsupportedOperationException();
 
        return (length + 1) & ~1;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return (length == -1) ? -1 : ((length + 1) & ~1);
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        if (length == -1)
            throw new UnsupportedOperationException();

        if (length == 0)
            return ByteUtils.EMPTY_BYTES;

        InputStream in = openStream();
        try {
            byte[] b = new byte[length];
            StreamUtils.readFully(in, b, 0, b.length);
            if (this.bigEndian != bigEndian) {
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
            if (this.bigEndian != out.isBigEndian())
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
        oos.writeUTF(StringUtils.maskNull(uuid, ""));
        oos.writeUTF(StringUtils.maskNull(uri, ""));
        oos.writeBoolean(bigEndian);
    }

    public static Value deserializeFrom(ObjectInputStream ois)
            throws IOException {
        return new BulkData(
            StringUtils.maskEmpty(ois.readUTF(), null),
            StringUtils.maskEmpty(ois.readUTF(), null),
            ois.readBoolean());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BulkData other = (BulkData) obj;
        if (bigEndian != other.bigEndian)
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (bigEndian ? 1231 : 1237);
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    /** Returns the index after the segment ends */
    public long getSegmentEnd() {
        if( length==-1 ) return -1;
        return offset() + longLength();
    }

    /** Gets the actual length as a long so it can represent the 2 gb to 4 gb range of lengths */
    public long longLength() {
        if( length==-1 ) return -1;
        return length & 0xFFFFFFFFl;
    }

    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd)+"?offset="+offset+"&length="+this.length;
    }

    public void setLength(long longLength) {
        if( longLength<-1 || longLength>0xFFFFFFF0l ) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-16 but was "+longLength);
        }
        this.length = (int) longLength;
        this.uri = this.uri.substring(0, this.uriPathEnd)+"?offset="+this.offset+"&length="+this.length;
    }
}
