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
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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

package org.dcm4che3.opencv;

/**
 * @author Nicolas Roduit
 * @since Mar 2018
 */
class SOFSegment {
    private final boolean jfif;
    private final int marker;
    private final int samplePrecision;
    private final int lines; // height
    private final int samplesPerLine; // width
    private final int components;

    SOFSegment(boolean jfif, int marker, int samplePrecision, int lines, int samplesPerLine, int components) {
        this.jfif = jfif;
        this.marker = marker;
        this.samplePrecision = samplePrecision;
        this.lines = lines;
        this.samplesPerLine = samplesPerLine;
        this.components = components;
    }

    public boolean isJFIF() {
        return jfif;
    }

    public int getMarker() {
        return marker;
    }

    public int getSamplePrecision() {
        return samplePrecision;
    }

    public int getLines() {
        return lines;
    }

    public int getSamplesPerLine() {
        return samplesPerLine;
    }

    public int getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return String.format("SOF%d[%04x, precision: %d, lines: %d, samples/line: %d]", marker & 0xff - 0xc0, marker,
            samplePrecision, lines, samplesPerLine);
    }
}
