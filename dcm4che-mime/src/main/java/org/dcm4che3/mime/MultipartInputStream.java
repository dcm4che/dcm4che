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
package org.dcm4che3.mime;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MultipartInputStream extends FilterInputStream {

    private final byte[] boundary;
    private final byte[] buffer;
    private byte[] markBuffer;
    private int rpos;
    private int markpos;
    private boolean boundarySeen;
    private boolean markBoundarySeen;

    protected MultipartInputStream(InputStream in, String boundary) {
        super(in);
        this.boundary = boundary.getBytes();
        this.buffer = new byte[this.boundary.length];
        this.rpos = buffer.length;
    }

    @Override
    public int read() throws IOException {
        return isBoundary() ? -1 : buffer[rpos++];
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isBoundary())
            return -1;

        int l = Math.min(remaining(), len);
        System.arraycopy(buffer, rpos, b, off, l);
        rpos += l;
        return l;
    }

    @Override
    public long skip(long n) throws IOException {
        if (isBoundary())
            return 0L;

        long l = Math.min(remaining(), n);
        rpos += l;
        return l;
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markBuffer = buffer.clone();
        markpos = rpos;
        markBoundarySeen = boundarySeen;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        System.arraycopy(markBuffer, 0, buffer, 0, buffer.length);
        rpos = markpos;
        boundarySeen = markBoundarySeen;
    }

    @Override
    public void close() throws IOException {
        //NOOP
    }

    public void skipAll() throws IOException {
        while (!isBoundary())
            rpos += remaining();
    }

    public boolean isZIP() throws IOException {
        return !isBoundary() 
                && buffer[rpos] == 'P'
                && buffer[rpos+1] == 'K';
    }

    private boolean isBoundary() throws IOException {
        if (boundarySeen)
            return true;

        if (rpos < buffer.length) {
            if (buffer[rpos] != boundary[0])
                return false;

            System.arraycopy(buffer, rpos, buffer, 0, buffer.length - rpos);
        }
        readFully(in, buffer, buffer.length - rpos, rpos);
        rpos = 0;

        for (int i = 0; i < buffer.length; i++)
            if (buffer[i] != boundary[i])
                return false;

        boundarySeen = true;
        return true;
    }

    private static void readFully(InputStream in, byte b[], int off, int len)
            throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            int count = in.read(b, off, len);
            if (count < 0)
                throw new EOFException();
            off += count;
            len -= count;
        }
    }

    private int remaining() {
        for (int i = rpos+1; i < buffer.length; i++)
            if (buffer[i] == boundary[0])
                return i - rpos;

        return buffer.length - rpos;
    }

    public Map<String, List<String>> readHeaderParams() throws IOException {
        Map<String, List<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Field field = new Field();
        while (readHeaderParam(field)) {
            String name = field.toString();
            String value = "";
            int endName = name.indexOf(':');
            if (endName != -1) {
                value =  unquote(name.substring(endName+1).trim());
                name = name.substring(0, endName);
            }
            List<String> list = map.get(name);
            if (list == null) {
                map.put(name.toLowerCase(), list = new ArrayList<String>(1));
            }
            list.add(value);
        }
        return map;
    }

    private static String unquote(String s) {
        int srcEnd = s.length() - 1;
        if (srcEnd < 0 || s.charAt(0) != '\"') {
            return s;
        }
        if (srcEnd == 0 || s.charAt(srcEnd) != '\"') { // missing closing quote
            srcEnd++;
        }
        char[] cs = new char[srcEnd - 1];
        s.getChars(1, srcEnd, cs, 0);
        boolean backslash = false;
        int count = 0;
        for (char c : cs) {
            if (!(backslash = !backslash && c == '\\')) {
                cs[count++] = c;
            }
        }
        return new String(cs, 0, count);
    }

    private boolean readHeaderParam(Field field) throws IOException {
        field.reset();
        OUTER:
        while (!isBoundary()) {
            field.growBuffer(buffer.length);
            while (rpos < buffer.length)
                if (!field.append(buffer[rpos++]))
                    break OUTER;
        }
        return !field.isEmpty();
    }

    private static final class Field {
        byte[] buffer = new byte[256];
        int length;

        void reset() {
            length = 0;
        }

        boolean isEmpty() {
            return length == 0;
        }

        void growBuffer(int grow) {
            if (length + grow > buffer.length) {
                byte[] copy = new byte[length + grow];
                System.arraycopy(buffer, 0, copy, 0, length);
                buffer = copy;
            }
        }

        boolean append(byte b) {
            if (b == '\n' && length > 0 && buffer[length-1] == '\r') {
                length--;
                return false;
            }

            buffer[length++] = b;
            return true;
        }

        public String toString() {
            return new String(buffer, 0, length);
        }

    }

}
