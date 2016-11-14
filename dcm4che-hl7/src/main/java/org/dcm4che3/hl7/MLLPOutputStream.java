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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MLLPOutputStream extends FilterOutputStream {

    private static final int SOM = 0x0b; // Start of Message 
    private static final byte[] EOM = { 0x1c, 0x0d }; // End of Message

    private boolean somWritten;

    public MLLPOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public synchronized void write(int b) throws IOException {
        writeStartBlock();
        out.write(b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len)
            throws IOException {
        writeStartBlock();
        out.write(b, off, len);
    }

    public void writeMessage(byte[] b) throws IOException {
        writeMessage(b, 0, b.length);
    }

    public synchronized void writeMessage(byte b[], int off, int len)
            throws IOException {
        if (somWritten)
            throw new IllegalStateException();

        byte[] msg = new byte[len+3];
        msg[0] = SOM;
        System.arraycopy(b, off, msg, 1, len);
        System.arraycopy(EOM, 0, msg, len + 1, 2);
        out.write(msg);
        out.flush();
    }

    private void writeStartBlock() throws IOException {
        if (!somWritten) {
            out.write(SOM);
            somWritten = true;
        }
    }

    public synchronized void finish() throws IOException {
        if (!somWritten)
            throw new IllegalStateException();
        out.write(EOM);
        out.flush();
        somWritten = false;
    }

}
