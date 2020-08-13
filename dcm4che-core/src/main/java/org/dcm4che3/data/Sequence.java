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
import java.util.Collection;
import java.util.ListIterator;

import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Sequence extends ArrayList<Attributes> implements Value {

    private static final long serialVersionUID = 7062970085409148066L;

    private final Attributes parent;
    private final String privateCreator;
    private final int tag;
    private volatile int length = -1;
    private volatile boolean readOnly;

    Sequence(Attributes parent, String privateCreator, int tag, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;
        this.privateCreator = privateCreator;
        this.tag = tag;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
        for (Attributes attrs : this) {
            attrs.setReadOnly();
        }
    }

    private void ensureModifiable() {
        if (readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    public final Attributes getParent() {
        return parent;
    }

    public void trimToSize(boolean recursive) {
        ensureModifiable();
        super.trimToSize();
        if (recursive)
            for (Attributes attrs: this)
                attrs.trimToSize(recursive);
    }

    @Override
    public int indexOf(Object o) {
        ListIterator<Attributes> it = listIterator();
            while (it.hasNext())
                if (it.next() == o)
                    return it.previousIndex();
        return -1;
    }

    @Override
    public boolean add(Attributes attrs) {
        ensureModifiable();
        return super.add(attrs.setParent(parent, privateCreator, tag));
    }

    @Override
    public void add(int index, Attributes attrs) {
        ensureModifiable();
        super.add(index, attrs.setParent(parent, privateCreator, tag));
    }

    @Override
    public boolean addAll(Collection<? extends Attributes> c) {
        ensureModifiable();
        setParent(c);
        return super.addAll(c);
    }

    private void setParent(Collection<? extends Attributes> c) {
        boolean bigEndian = parent.bigEndian();
        for (Attributes attrs : c) {
            if (attrs.bigEndian() != bigEndian)
                throw new IllegalArgumentException(
                    "Endian of Item must match Endian of parent Data Set");
            if (!attrs.isRoot())
                throw new IllegalArgumentException(
                    "Item already contained by Sequence");
        }
        for (Attributes attrs : c)
            attrs.setParent(parent, privateCreator, tag);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Attributes> c) {
        ensureModifiable();
        setParent(c);
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        ensureModifiable();
        for (Attributes attrs: this)
            attrs.setParent(null, null, 0);
        super.clear();
    }

    @Override
    public Attributes remove(int index) {
        ensureModifiable();
        return super.remove(index).setParent(null, null, 0);
    }

    @Override
    public boolean remove(Object o) {
        ensureModifiable();
        if (o instanceof Attributes && super.remove(o)) {
            ((Attributes) o).setParent(null, null, 0);
            return true;
        }
        return false;
    }

    @Override
    public Attributes set(int index, Attributes attrs) {
        ensureModifiable();
        return super.set(index, attrs.setParent(parent, privateCreator, tag));
    }

    @Override
    public String toString() {
        return "" + size() + " Items";
    }

    @Override
    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        int len = 0;
        for (Attributes item : this) {
            len += 8 + item.calcLength(encOpts, explicitVR);
            if (item.isEmpty() ? encOpts.undefEmptyItemLength
                               : encOpts.undefItemLength)
                len += 8;
        }
        if (isEmpty() ? encOpts.undefEmptySequenceLength
                      : encOpts.undefSequenceLength)
            len += 8;
        length = len;
        return len;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        if (isEmpty())
            return encOpts.undefEmptySequenceLength ? -1 : 0;

        if (encOpts.undefSequenceLength)
            return -1;

        if (length == -1)
            calcLength(encOpts, explicitVR, vr);

        return length;
    }

    @Override
    public void writeTo(DicomOutputStream out, VR vr) throws IOException {
        for (Attributes item : this)
            item.writeItemTo(out);
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }
}
