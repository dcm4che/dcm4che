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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;

/**
 * Fragments are used for encapsulation of an encoded (=compressed) pixel data
 * stream into the Pixel Data (7FE0,0010) portion of the DICOM Data Set. They
 * are encoded as a sequence of items with Value Representation OB.
 * 
 * <p>
 * Each item is either a byte[], {@link BulkData} or {@link Value#NULL}.
 * 
 * <p>
 * The first Item in the sequence of items before the encoded Pixel Data Stream
 * is a Basic Offset Table item. The value of the Basic Offset Table, however,
 * is not required to be present. The first item is then {@link Value#NULL}.
 * 
 * <p>
 * Depending on the transfer syntax, a frame may be entirely contained within a
 * single fragment, or may span multiple fragments to support buffering during
 * compression or to avoid exceeding the maximum size of a fixed length
 * fragment. A recipient can detect fragmentation of frames by comparing the
 * number of fragments (the number of Items minus one for the Basic Offset
 * Table) with the number of frames.
 * 
 * <p>
 * See also <a href=
 * "http://medical.nema.org/medical/dicom/current/output/chtml/part05/sect_A.4.html">
 * DICOM Part 5: A.4 TRANSFER SYNTAXES FOR ENCAPSULATION OF ENCODED PIXEL
 * DATA</a> and <a href=
 * "http://medical.nema.org/medical/dicom/current/output/chtml/part05/sect_8.2.html">
 * DICOM Part 5: 8.2 Native or Encapsulated Format Encoding</a>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Fragments extends ArrayList<Object> implements Value {

    private static final long serialVersionUID = -6667210062541083610L;

    private final VR vr;
    private final boolean bigEndian;

    public Fragments(VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    public final VR vr() {
        return vr;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    @Override
    public String toString() {
        return "" + size() + " Fragments";
    }

    @Override
    public boolean add(Object frag) {
        add(size(), frag);
        return true;
    }

    @Override
    public void add(int index, Object frag) {
        super.add(index, 
                frag == null || (frag instanceof byte[]) && ((byte[]) frag).length == 0
                    ? Value.NULL
                    : frag);
    }

    @Override
    public boolean addAll(Collection<? extends Object> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        for (Object o : c)
            add(index++, o);
        return !c.isEmpty();
    }

    @Override
    public void writeTo(DicomOutputStream out, VR vr)
            throws IOException {
        for (Object frag : this)
            out.writeAttribute(Tag.Item, vr, frag, null);
    }

    @Override
    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        int len = 0;
        for (Object frag : this) {
            len += 8;
            if (frag instanceof Value)
                len += ((Value) frag).calcLength(encOpts, explicitVR, vr);
            else
                len += (((byte[]) frag).length + 1) & ~1;
        }
        return len;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return -1;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Fragments other = (Fragments) obj;
        if (bigEndian != other.bigEndian)
            return false;
        if (vr != other.vr)
            return false;

        ListIterator<Object> e1 = listIterator();
        ListIterator<Object> e2 = other.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (!itemsEqual(o1, o2))
                return false;
        }
        if (e1.hasNext() || e2.hasNext())
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        
        int hashCode = 1;
        for (Object e : this)
            hashCode = prime * hashCode + itemHashCode(e);
        
        hashCode = prime * hashCode + (bigEndian ? 1231 : 1237);
        hashCode = prime * hashCode + ((vr == null) ? 0 : vr.hashCode());
        return hashCode;
    }

    private boolean itemsEqual(Object o1, Object o2) {

        if (o1 == null) {
            return o2 == null;
        } else {
            if (o1 instanceof byte[]) {
                if (o2 instanceof byte[] && ((byte[]) o1).length == ((byte[]) o2).length) {
                    return Arrays.equals((byte[]) o1, (byte[]) o2);
                } else {
                    return false;
                }
            } else {
                return o1.equals(o2);
            }
        }
    }

    private int itemHashCode(Object e) {
        if (e == null) {
            return 0;
        } else {
            if (e instanceof byte[])
                return Arrays.hashCode((byte[]) e);
            else
                return e.hashCode();
        }
    }
}
