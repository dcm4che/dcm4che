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

/**
 * Value Multiplicity (VM) of a DICOM Data Element as defined by
 * DICOM PS3.5 &#167;6.4 and specified per Data Element in PS3.6.
 *
 * <p>A VM is a range {@code min-max}, where an unbounded maximum is
 * denoted by {@code n} (e.g. {@code 1-n}). VMs of the form {@code k-kn}
 * (e.g. {@code 2-2n}) additionally constrain the number of values to a
 * multiple of {@code k}.
 */
public enum VM {
    VM_0_N("0-n", 0, -1, 1),
    VM_1("1", 1, 1, 1),
    VM_1_2("1-2", 1, 2, 1),
    VM_1_3("1-3", 1, 3, 1),
    VM_1_8("1-8", 1, 8, 1),
    VM_1_16("1-16", 1, 16, 1),
    VM_1_32("1-32", 1, 32, 1),
    VM_1_99("1-99", 1, 99, 1),
    VM_1_N("1-n", 1, -1, 1),
    VM_2("2", 2, 2, 1),
    VM_2_4("2-4", 2, 4, 1),
    VM_2_N("2-n", 2, -1, 1),
    VM_2_2N("2-2n", 2, -1, 2),
    VM_3("3", 3, 3, 1),
    VM_3_4("3-4", 3, 4, 1),
    VM_3_N("3-n", 3, -1, 1),
    VM_3_3N("3-3n", 3, -1, 3),
    VM_4("4", 4, 4, 1),
    VM_4_5("4-5", 4, 5, 1),
    VM_4_N("4-n", 4, -1, 1),
    VM_4_4N("4-4n", 4, -1, 4),
    VM_5("5", 5, 5, 1),
    VM_6("6", 6, 6, 1),
    VM_6_N("6-n", 6, -1, 1),
    VM_7("7", 7, 7, 1),
    VM_8("8", 8, 8, 1),
    VM_9("9", 9, 9, 1),
    VM_12("12", 12, 12, 1),
    VM_12_N("12-n", 12, -1, 1),
    VM_16("16", 16, 16, 1),
    VM_24("24", 24, 24, 1),
    VM_32("32", 32, 32, 1),
    VM_256("256", 256, 256, 1);

    private final String str;

    /** Minimum number of values. */
    public final int min;

    /** Maximum number of values; {@code -1} = unbounded ({@code n}). */
    public final int max;

    /** Number of values must be a multiple of this ({@code k} in {@code k-kn}). */
    public final int multiple;

    VM(String str, int min, int max, int multiple) {
        this.str = str;
        this.min = min;
        this.max = max;
        this.multiple = multiple;
    }

    /**
     * Tests if the given number of values conforms to this VM.
     */
    public boolean contains(int numValues) {
        return numValues >= min
                && (max < 0 || numValues <= max)
                && numValues % multiple == 0;
    }

    /**
     * Returns the VM for its notation in PS3.6 (e.g. {@code "1-n"}).
     *
     * @throws IllegalArgumentException if there is no such VM
     */
    public static VM fromString(String s) {
        for (VM vm : values())
            if (vm.str.equals(s))
                return vm;
        throw new IllegalArgumentException(s);
    }

    @Override
    public String toString() {
        return str;
    }
}
