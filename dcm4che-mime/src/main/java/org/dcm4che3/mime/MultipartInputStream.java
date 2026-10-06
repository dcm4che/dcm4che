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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MultipartInputStream extends FilterInputStream {

    private final byte[] boundary;
    private final byte[][] buffers = new byte[2][];
    private byte[][] markBuffers = new byte[2][];
    private int rbuf;
    private int rpos;
    private int markbuf;
    private int markpos;
    private boolean boundarySeen;
    private boolean markBoundarySeen;

    protected MultipartInputStream(InputStream in, byte[] boundary) throws IOException {
        this(in, boundary, new byte[boundary.length]);
        readFully(in, this.buffers[0], 0, boundary.length);
    }

    protected MultipartInputStream(InputStream in, byte[] boundary, byte[] b0) {
        super(in);
        this.boundary = boundary;
        this.buffers[0] = b0;
        this.buffers[1] = new byte[this.boundary.length];
    }

    @Override
    public int read() throws IOException {
        int b;
        if (isBoundary() || (b = in.read()) == -1)
            return -1;

        buffers[1 - rbuf][rpos] = (byte) b;
        b = buffers[rbuf][rpos++] & 0xff;
        switchBufferOnEndOfBuffer();
        return b;
    }

    private void switchBufferOnEndOfBuffer() {
        if (rpos >= boundary.length) {
            rbuf = 1 - rbuf;
            rpos = 0;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isBoundary())
            return -1;

        int l = Math.min(remaining(boundary[0], 1), len);
        System.arraycopy(buffers[rbuf], rpos, b, off, l);
        readFully(in, buffers[1 - rbuf], rpos, l);
        rpos += l;
        switchBufferOnEndOfBuffer();
        return l;
    }

    @Override
    public long skip(long n) throws IOException {
        if (isBoundary())
            return 0L;

        int l = (int) Math.min(remaining(boundary[0], 1), n);
        readFully(in, buffers[1 - rbuf], rpos, l);
        rpos += l;
        switchBufferOnEndOfBuffer();
        return l;
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markBuffers[0] = buffers[0].clone();
        markBuffers[1] = buffers[1].clone();
        markbuf = rbuf;
        markpos = rpos;
        markBoundarySeen = boundarySeen;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        System.arraycopy(markBuffers[0], 0, buffers[0], 0, boundary.length);
        System.arraycopy(markBuffers[1], 0, buffers[1], 0, boundary.length);
        rbuf = markbuf;
        rpos = markpos;
        boundarySeen = markBoundarySeen;
    }

    @Override
    public void close() throws IOException {
        //NOOP
    }

    public void skipAll() throws IOException {
        while (!isBoundary()) {
            int l = remaining(boundary[0], 1);
            readFully(in, buffers[1 - rbuf], rpos, l);
            rpos += l;
            if (rpos >= boundary.length) {
                rpos = 0;
                rbuf = 1 - rbuf;
            }
        }
    }

    public boolean isZIP() {
        return !isBoundary() 
                && 'P' == buffers[rbuf][rpos]
                && 'K' == (rpos + 1 < boundary.length
                    ? buffers[rbuf][rpos+1]
                    : buffers[1-rbuf][0]);
    }

    private boolean isBoundary() {
        if (boundarySeen)
            return true;

        for (int i = 0, j = rpos; j < boundary.length; i++, j++)
            if (buffers[rbuf][j] != boundary[i])
                return false;

        for (int i = boundary.length - rpos, j = 0; j < rpos; i++, j++)
            if (buffers[1 - rbuf][j] != boundary[i])
                return false;

        boundarySeen = true;
        return true;
    }

    static void readFully(InputStream in, byte b[], int off, int len)
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

    private int remaining(byte ch1, int min) {
        for (int i = rpos + min; i < boundary.length; i++)
            if (buffers[rbuf][i] == ch1)
                return i - rpos;

        return boundary.length - rpos;
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
        boolean append = true;
        while (append) {
            int l = remaining((byte) '\n', 0);
            if (rpos + l < boundary.length)
                l++;  // include LF at buffers[rbuf][rpos + l]
            int i = rpos;
            field.growBuffer(l);
            while (l-- > 0 && (append = field.append(buffers[rbuf][i++])));
            readFully(in, buffers[1 - rbuf], rpos, i - rpos);
            rpos = i;
            switchBufferOnEndOfBuffer();
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
