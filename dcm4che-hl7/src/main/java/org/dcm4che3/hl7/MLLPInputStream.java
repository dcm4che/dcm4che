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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.hl7;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MLLPInputStream extends BufferedInputStream {

    private static final int SOM = 0x0b; // Start of Message
    private static final int EOM1 = 0x1c; // End of Message Byte 1
    private static final int EOM2 = 0x0d; // End of Message Byte 2

    private boolean eom = true;
    private ByteArrayOutputStream readBuffer = new ByteArrayOutputStream();

    public MLLPInputStream(InputStream in) {
        super(in);
    }

    public MLLPInputStream(InputStream in, int size) {
        super(in, size);
    }

    public synchronized boolean hasMoreInput() throws IOException {
        if (!eom)
            throw new IllegalStateException();

        int b = super.read();
        if (b == -1)
            return false;

        if (b != SOM)
            throw new IOException("Missing Start Block character");

        eom = false;
        return true;
    }

    @Override
    public synchronized int read() throws IOException {
        if (eom)
            return -1;

        int b = super.read();
        if (b == -1)
            throw new EOFException();
        
        if (b != EOM1)
            return b;

        eom();
        return -1;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null)
            throw new NullPointerException();

        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();

        if (eom)
            return -1;

        if (len == 0)
            return 0;

        if (read() == -1)
            return -1;

        int rlen = Math.min(count - pos, len-1);
        int remaining = remaining(pos + rlen);
        if (remaining == -1) {
            System.arraycopy(buf, pos-1, b, off, rlen+1);
            pos += rlen;
            return rlen+1;
        }

        System.arraycopy(buf, pos-1, b, off, remaining+1);
        pos += remaining+1;
        eom();
        return remaining + 1;
    }

    public synchronized int copyTo(OutputStream out) throws IOException {
        if (eom)
            throw new IllegalStateException();

        int totlen = 0;
        int remaining;
        int leftover = 0;
        while ((remaining = remaining(count)) == -1) {
            int avail = count - pos;
            out.write(buf, pos - leftover, avail + leftover);
            totlen += avail + leftover;
            pos = count;
            if (read() == -1)
                return totlen;
            leftover = 1;
        }
        out.write(buf, pos - leftover, remaining + leftover);
        totlen += remaining + leftover;
        pos += remaining + 1;
        eom();
        return totlen;
    }

    public synchronized byte[] readMessage() throws IOException {
        if (!hasMoreInput())
            return null;

        readBuffer.reset();
        copyTo(readBuffer);
        return readBuffer.toByteArray();
    }

    private void eom() throws IOException {
        int b = super.read();
        if (b != EOM2)
            throw new IOException("1CH followed by "
                        + Integer.toHexString(b & 0xff) + "H instead by 0DH");
        eom = true;
    }

    private int remaining(int count) {
        for (int i = pos; i < count; i++)
            if (buf[i] == EOM1)
                return i - pos;

        return -1;
    }
}
