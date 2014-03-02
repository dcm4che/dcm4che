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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.imageio.codec;

import java.io.Serializable;
import java.util.EnumSet;

import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.util.Property;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CompressionRule
        implements Comparable<CompressionRule>, Serializable {

    private static final long serialVersionUID = 2010254518169306864L;

    private final String commonName;
    private final Condition condition;
    private final String tsuid;
    private final Property[] imageWriteParams;

    public CompressionRule(String commonName, String[] pmis, int[] bitsStored,
            int pixelRepresentation, String[] aeTitles, String[] sopClasses,
            String[] bodyPartExamined, String tsuid, String... params) {
        this.commonName = commonName;
        this.condition = new Condition(pmis, bitsStored, pixelRepresentation,
                StringUtils.maskNull(aeTitles),
                StringUtils.maskNull(sopClasses),
                StringUtils.maskNull(bodyPartExamined));
        this.tsuid = tsuid;
        this.imageWriteParams = Property.valueOf(params);
    }
 
    public final String getCommonName() {
        return commonName;
    }

    public PhotometricInterpretation[] getPhotometricInterpretations() {
        return condition.getPhotometricInterpretations();
    }

    public int[] getBitsStored() {
        return condition.getBitsStored();
    }

    public final int getPixelRepresentation() {
        return condition.pixelRepresentation;
    }

    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    public final String[] getBodyPartExamined() {
        return condition.bodyPartExamined;
    }

    public final String getTransferSyntax() {
        return tsuid;
    }

    public Property[] getImageWriteParams() {
        return imageWriteParams;
    }

    public boolean matchesCondition(PhotometricInterpretation pmi, 
            int bitsStored, int pixelRepresentation, String aeTitle, 
            String sopClass, String bodyPart) {
        return condition.matches(pmi, bitsStored, pixelRepresentation, aeTitle,
                sopClass, bodyPart);
    }

    @Override
    public int compareTo(CompressionRule o) {
        return condition.compareTo(o.condition);
    }

    private static class Condition
            implements Comparable<Condition>, Serializable {

        private static final long serialVersionUID = -4069284624944470710L;

        final EnumSet<PhotometricInterpretation> pmis;
        final int bitsStoredMask;
        final int pixelRepresentation = -1;
        final String[] aeTitles;
        final String[] sopClasses;
        final String[] bodyPartExamined;
        final int weight;

        Condition(String[] pmis, int[] bitsStored, int pixelRepresentation,
                String[] aeTitles, String[] sopClasses, String[] bodyPartExamined) {
            this.pmis = EnumSet.noneOf(PhotometricInterpretation.class);
            for (String pmi : pmis)
                this.pmis.add(PhotometricInterpretation.fromString(pmi));

            this.bitsStoredMask = toBitsStoredMask(bitsStored);
            this.aeTitles = aeTitles;
            this.sopClasses = sopClasses;
            this.bodyPartExamined = bodyPartExamined;
            this.weight = (aeTitles.length != 0 ? 4 : 0)
                      + (sopClasses.length != 0 ? 2 : 0)
                       + (bodyPartExamined.length != 0 ? 1 : 0);
        }

        private int toBitsStoredMask(int[] bitsStored) {
            int mask = 0;
            for (int i : bitsStored)
                mask |= 1 << i;

            return mask;
        }

        PhotometricInterpretation[] getPhotometricInterpretations() {
            return pmis.toArray(new PhotometricInterpretation[pmis.size()]);
        }

        int[] getBitsStored() {
            int n = 0;
            for (int i = 8; i <= 16; i++)
                if (matchBitStored(i))
                    n++;

            int[] bitsStored = new int[n];
            for (int i = 8, j = 0; i <= 16; i++)
                if (matchBitStored(i))
                    bitsStored[j++] = i;

            return bitsStored;
        }

        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        public boolean matches(PhotometricInterpretation pmi, 
                int bitsStored, int pixelRepresentation, 
                String aeTitle, String sopClass, String bodyPart) {
            return pmis.contains(pmi)
                    && matchBitStored(bitsStored)
                    && matchPixelRepresentation(pixelRepresentation)
                    && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, sopClass)
                    && isEmptyOrContains(this.bodyPartExamined, bodyPart);
        }

        private boolean matchPixelRepresentation(int pixelRepresentation) {
            return this.pixelRepresentation == -1 
                    || this.pixelRepresentation == pixelRepresentation;
        }

        private boolean matchBitStored(int bitsStored) {
            return ((1<<bitsStored) & bitsStoredMask) != 0;
        }

        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (o == null || a.length == 0)
                return true;

            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return true;

            return false;
        }
    }

}
