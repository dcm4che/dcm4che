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

package org.dcm4che3.util;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class StreamUtils {
    
    private static final int COPY_BUFFER_SIZE = 2048;

    public static int readAvailable(InputStream in, byte b[], int off, int len)
            throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        int wpos = off;
        while (len > 0) {
            int count = in.read(b, wpos, len);
            if (count < 0)
                break;
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }

    public static void readFully(InputStream in, byte b[], int off, int len)
            throws IOException {
        if (readAvailable(in, b, off, len) < len)
            throw new EOFException();
    }
    
    public static  void skipFully(InputStream in, long n) throws IOException {
        while (n > 0) {
            long count = in.skip(n);
            if (count <= 0)
                throw new EOFException();
            n -= count;
        }
    }

    public static void copy(InputStream in, OutputStream out, byte buf[])
            throws IOException {
        int count;
        while ((count = in.read(buf, 0, buf.length)) > 0)
            if (out != null)
                out.write(buf, 0, count);
    }

    public static  void copy(InputStream in, OutputStream out)
            throws IOException {
        copy(in, out, new byte[COPY_BUFFER_SIZE]);
    }

    public static  void copy(InputStream in, OutputStream out, int len,
            byte buf[]) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            int count = in.read(buf, 0, Math.min(len, buf.length));
            if (count < 0)
                throw new EOFException();
            out.write(buf, 0, count);
            len -= count;
        }
    }

    public static  void copy(InputStream in, OutputStream out, int len)
            throws IOException {
        copy(in, out, len, new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static void copy(InputStream in, OutputStream out, int len,
            int swapBytes, byte buf[]) throws IOException {
        if (swapBytes == 1) {
            copy(in, out, len, buf);
            return;
        }
        if (!(swapBytes == 2 || swapBytes == 4))
            throw new IllegalArgumentException("swapBytes: " + swapBytes);
        if (len < 0 || (len % swapBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
        int off = 0;
        while (len > 0) {
            int count = in.read(buf, off, Math.min(len, buf.length - off));
            if (count < 0)
                throw new EOFException();
            len -= count;
            count += off;
            off = count % swapBytes;
            count -= off;
            switch (swapBytes) {
            case 2:
                ByteUtils.swapShorts(buf, 0, count);
                break;
            case 4:
                ByteUtils.swapInts(buf, 0, count);
                break;
            case 8:
                ByteUtils.swapLongs(buf, 0, count);
                break;
            }
            out.write(buf, 0, count);
            if (off > 0)
                System.arraycopy(buf, count, buf, 0, off);
        }
    }

    public static void copy(InputStream in, OutputStream out, int len,
            int swapBytes) throws IOException {
        copy(in, out, len, swapBytes,
                new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static InputStream openFileOrURL(String name) throws IOException {
        if (name.startsWith("resource:")) {
            URL url = ResourceLocator.getResourceURL(name.substring(9), StreamUtils.class);
            if (url == null)
                throw new FileNotFoundException(name);
            return url.openStream();
        }
        if (name.indexOf(':') < 2)
            return new FileInputStream(name);
        return new URL(name).openStream();
    }
}
