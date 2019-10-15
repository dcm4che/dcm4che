/*
 * **** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.util;

import java.nio.Buffer;

/**
 * Utility to avoid {@link NoSuchMethodError} on builds with Java 9 running on Java 7 or Java 8
 * caused by overloaded methods for derived classes of {@link Buffer} with covariant return types
 * for {@link Buffer#clear()}, {@link Buffer#flip()}, {@link Buffer#limit(int)}, {@link Buffer#mark()},
 * {@link Buffer#position(int)}, {@link Buffer#reset()}, {@link Buffer#rewind()} added in Java 9.
 *
 * <p> Usage: replace {@code buffer.clear()} by {@code SafeBuffer.clear(buffer)}, ...
 *
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Oct 2019
 */
public class SafeBuffer {
    public static Buffer clear(Buffer buf) {
        return buf.clear();
    }

    public static Buffer flip(Buffer buf) {
        return buf.flip();
    }

    public static Buffer limit(Buffer buf, int newLimit) {
        return buf.limit(newLimit);
    }

    public static Buffer mark(Buffer buf) {
        return buf.mark();
    }

    public static Buffer position(Buffer buf, int newPosition) {
        return buf.position(newPosition);
    }

    public static Buffer reset(Buffer buf) {
        return buf.reset();
    }

    public static Buffer rewind(Buffer buf) {
        return buf.rewind();
    }
}
