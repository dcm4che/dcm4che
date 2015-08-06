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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Fragments extends ArrayList<Object> implements Value {

    private static final long serialVersionUID = -6667210062541083610L;

    private final String privateCreator;
    private final int tag;
    private final VR vr;
    private final boolean bigEndian;

    public Fragments(String privateCreator, int tag, VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.privateCreator = privateCreator;
        this.tag = tag;
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    public String privateCreator() {
        return privateCreator;
    }

    public final int tag() {
        return tag;
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
        if (getClass() != obj.getClass())
            return false;

        Fragments other = (Fragments) obj;
        if (bigEndian != other.bigEndian)
            return false;
        if (privateCreator == null) {
            if (other.privateCreator != null)
                return false;
        } else if (!privateCreator.equals(other.privateCreator))
            return false;
        if (tag != other.tag)
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
            hashCode = 31 * hashCode + itemHashCode(e);
        
        hashCode = prime * hashCode + (bigEndian ? 1231 : 1237);
        hashCode = prime * hashCode + ((privateCreator == null) ? 0 : privateCreator.hashCode());
        hashCode = prime * hashCode + tag;
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
