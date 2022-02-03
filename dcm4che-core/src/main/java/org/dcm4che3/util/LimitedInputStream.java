/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013-2021
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Oct 2021
 */
public final class LimitedInputStream extends FilterInputStream {

    private long remaining;
    private long mark = -1;
    private final boolean closeSource;

    public LimitedInputStream(InputStream in, long limit, boolean closeSource) {
        super(Objects.requireNonNull(in));
        if (limit <= 0) throw new IllegalArgumentException("limit must be > 0");
        this.remaining = limit;
        this.closeSource = closeSource;
    }

    @Override
    public int read() throws IOException {
        int result;
        if (remaining == 0 || (result = in.read()) < 0) {
            return -1;
        }

        --remaining;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result;
        if (remaining == 0 || (result = in.read(b, off, (int) Math.min(len, remaining))) < 0) {
            return -1;
        }

        remaining -= result;
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = in.skip(Math.min(n, remaining));
        remaining -= result;
        return result;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), remaining);
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = remaining;
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        remaining = mark;
    }

    @Override
    public void close() throws IOException {
        if (closeSource) in.close();
    }

    public long getRemaining() {
        return remaining;
    }
}
