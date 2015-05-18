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

package org.dcm4che3.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class BulkData implements Value {

    public static final int MAGIC_LEN = 0xfbfb;

    public final String uri;
    public final String uuid;
    public final boolean bigEndian;
    private int uriPathEnd;
    private long offset;
    private int length = -1;
    private long[] offsets;
    private int[] lengths;

    public BulkData(String uuid, String uri, boolean bigEndian) {
        if (uri != null) {
            if (uuid != null)
                throw new IllegalArgumentException("uuid and uri are mutually exclusive");
            parseURI(uri);
        } else if (uuid == null) {
            throw new IllegalArgumentException("uuid or uri must be not null");
        }
        this.uuid = uuid;
        this.uri = uri;
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

    public BulkData(String uri, long[] offsets, int[] lengths, boolean bigEndian) {
        if (offsets.length == 0)
            throw new IllegalArgumentException("offsets.length == 0");

        if (offsets.length != lengths.length)
            throw new IllegalArgumentException(
                    "offsets.length[" + offsets.length
                    + "] != lengths.length[" + lengths.length + "]");

        this.uuid = null;
        this.uriPathEnd = uri.length();
        this.uri = appendQuery(uri, offsets, lengths);
        this.offsets = offsets.clone();
        this.lengths = lengths.clone();
        this.bigEndian = bigEndian;
    }

    /**
     * Returns a {@code BulkData} instance combining all {@code BulkData} instances in {@code bulkDataFragments}.
     *
     * @param  bulkDataFragments {@code Fragments} instance with {@code BulkData} instances
     *         referencing individual fragments
     * @return a {@code BulkData} instance combining all {@code BulkData} instances in {@code bulkDataFragments}.
     * @throws ClassCastException if {@code bulkDataFragments} contains {@code byte[]}
     * @throws IllegalArgumentException if {@code bulkDataFragments} contains URIs referencing different Resources
     *         or without Query Parameter {@code length}.
     */
    public static BulkData fromFragments(Fragments bulkDataFragments) {
        int size = bulkDataFragments.size();
        String uri = null;
        long[] offsets = new long[size];
        int[] lengths = new int[size];
        for (int i = 0; i < size; i++) {
            Object value = bulkDataFragments.get(i);
            if (value == Value.NULL)
                continue;

            BulkData bulkdata = (BulkData) value;
            String uriWithoutQuery = bulkdata.uriWithoutQuery();
            if (uri == null)
                uri = uriWithoutQuery;
            else if (!uri.equals(uriWithoutQuery))
                throw new IllegalArgumentException("BulkData URIs references different Resources");
            if (bulkdata.length() == -1)
                throw new IllegalArgumentException("BulkData Reference with unspecified length");
            offsets[i] = bulkdata.offset();
            lengths[i] = bulkdata.length();
        }
        return new BulkData(uri, offsets, lengths, false);
    }

    /**
     * Returns {@code true}, if the URI of this {@code BulkData} instance specifies offset and length of individual
     * data fragments by Query Parameters {@code offsets} and {@code lengths} and therefore can be converted
     * by {@link #toFragments} to a {@code Fragments} instance containing {@code BulkData} instances
     * referencing individual fragments.
     *
     * @return {@code true} if this {@code BulkData} instance can be converted to a {@code Fragments} instance
     * by {@link #toFragments}
     */
    public boolean hasFragments() {
        return  offsets != null && lengths != null;
    }

    /**
     * Returns a {@code Fragments} instance with containing {@code BulkData} instances referencing
     * individual fragments, referenced by this {@code BulkData} instances.
     *
     * @param  privateCreator
     * @param  tag
     * @param  vr
     * @return {@code Fragments} instance with containing {@code BulkData} instances referencing
     * individual fragments, referenced by this {@code BulkData} instances
     * @throws UnsupportedOperationException, if the URI {@code BulkData} instance does not specify
     * offset and length of individual data fragments by Query Parameters {@code offsets} and {@code lengths}
     */
    public Fragments toFragments (String privateCreator, int tag, VR vr) {
        if (offsets == null || lengths == null)
            throw new UnsupportedOperationException();

        if (offsets.length != lengths.length)
            throw new IllegalStateException("offsets.length[" + offsets.length
                    + "] != lengths.length[" + lengths.length + "]");

        Fragments fragments = new Fragments(privateCreator, tag, vr, bigEndian, lengths.length);
        String uriWithoutQuery = uriWithoutQuery();
        for (int i = 0; i < lengths.length; i++)
            fragments.add(lengths[i] == 0
                    ? Value.NULL
                    : new BulkData(uriWithoutQuery, offsets[i], lengths[i], bigEndian));
        return fragments;
    }

    private void parseURI(String uri) {
        int index = uri.indexOf('?');
        if (index == -1) {
            uriPathEnd = uri.length();
            return;
        }
        uriPathEnd = index;
        if (uri.startsWith("offset=", index+1))
            parseURIWithOffset(uri, index+8);
        else if (uri.startsWith("offsets=", index+1))
            parseURIWithOffsets(uri, index+9);
    }

    private void parseURIWithOffset(String uri, int from) {
        int index = uri.indexOf("&length=");
        if (index == -1)
            return;

        try {
            offset = Long.parseLong(uri.substring(from, index));
            length = Integer.parseInt(uri.substring(index + 8));
        } catch (NumberFormatException e) {}
    }

    private void parseURIWithOffsets(String uri, int from) {
        int index = uri.indexOf("&lengths=");
        if (index == -1)
            return;
        try {
            offsets = parseLongs(uri.substring(from, index));
            lengths = parseInts(uri.substring(index + 9));
        } catch (NumberFormatException e) {}
    }

    private static long[] parseLongs(String s) {
        String[] ss = StringUtils.split(s, ',');
        long[] longs = new long[ss.length];
        for (int i = 0; i < ss.length; i++) {
            longs[i] = Long.parseLong(ss[i]);
        }
        return longs;
    }

    private static int[] parseInts(String s) {
        String[] ss = StringUtils.split(s, ',');
        int[] ints = new int[ss.length];
        for (int i = 0; i < ss.length; i++) {
            ints[i] = Integer.parseInt(ss[i]);
        }
        return ints;
    }

    private String appendQuery(String uri, long[] offsets, int[] lengths) {
        StringBuilder sb = new StringBuilder(uri);
        sb.append( "?offsets=");
        for (long offset : offsets)
            sb.append(offset).append(',');
        sb.setLength(sb.length()-1);
        sb.append("&lengths=");
        for (int length : lengths)
            sb.append(length).append(',');
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public long offset() {
        return offset;
    }

    public int length() {
        return length;
    }

    public long[] offsets() {
        return offsets;
    }

    public int[] lengths() {
        return lengths;
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public String toString() {
        return "BulkData[uuid=" + uuid + ", uri=" +  uri + ", bigEndian=" + bigEndian  + "]";
    }

    public File getFile() {
        try {
            return new File(new URI(uriWithoutQuery()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("uri: " + uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("uri: " + uri);
        }
    }

    public String uriWithoutQuery() {
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


}
