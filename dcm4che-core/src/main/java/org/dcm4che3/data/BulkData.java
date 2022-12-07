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
public class BulkData implements Value, Serializable {

    public static final int MAGIC_LEN = 0xfbfb;

    private String uuid;
    private String uri;
    private int uriPathEnd;
    private boolean bigEndian;
    private long offset = 0;
    private long length = -1;

    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        setURI(uri);
        this.bigEndian = bigEndian;
    }

    public BulkData(String uri, long offset, long length, boolean bigEndian) {
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
        this.offset = 0;
        this.length = -1;
        this.uriPathEnd = 0;
        if (uri == null)
            return;

        int pathEnd = uri.indexOf('?');
        if (pathEnd < 0) {
            this.uriPathEnd = uri.length();
            return;
        }
        
        this.uriPathEnd = pathEnd;
        for (String qparam : StringUtils.split(uri.substring(pathEnd + 1), '&')) {
            try {
                if (qparam.startsWith("offset=")) {
                    this.offset = Long.parseLong(qparam.substring(7));
                } else if (qparam.startsWith("length=")) {
                    this.length = Long.parseLong(qparam.substring(7));
                }
            } catch (NumberFormatException ignore) {}
        }
    }

    public boolean bigEndian() {
        return bigEndian;
    }

    public int length() {
        return (int) length;
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
 
        return (int) (length + 1) & ~1;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return (int) ((length == -1) ? -1 : ((length + 1) & ~1));
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        int intLength = (int) length;
        if (intLength < 0)
            throw new UnsupportedOperationException();

        if (intLength == 0)
            return ByteUtils.EMPTY_BYTES;

        InputStream in = openStream();
        try {
            byte[] b = new byte[intLength];
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

    private static final long serialVersionUID = -6563845357491618094L;

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(StringUtils.maskNull(uuid, ""));
        oos.writeUTF(StringUtils.maskNull(uri, ""));
        oos.writeBoolean(bigEndian);
    }

    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        uuid = StringUtils.maskEmpty(ois.readUTF(), null);
        setURI(StringUtils.maskEmpty(ois.readUTF(), null));
        bigEndian = ois.readBoolean();
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
        return offset() + (length & 0xFFFFFFFFl);
    }

    /** Gets the actual length as a long so it can represent the 2 gb to 4 gb range of lengths */
    public long longLength() {
        return length;
    }

    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd)+"?offset="+offset+"&length="+length;
    }

    public void setLength(long length) {
        if( length<-1 || length>0xFFFFFFFEl ) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-2 but was "+length);
        }
        this.length = length;
        this.uri = this.uri.substring(0, this.uriPathEnd)+"?offset="+this.offset+"&length="+length;
    }

    @FunctionalInterface
    public interface Creator {
        BulkData create(String uuid, String uri, boolean bigEndian);
    }
}
