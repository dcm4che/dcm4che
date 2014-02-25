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

package org.dcm4che3.conf.prefs.imageio;

import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.CompressionRules;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesCompressionRulesConfiguration {

    public static void store(CompressionRules rules, Preferences parentDN) {
        Preferences parent = parentDN.node("dcmCompressionRule");
        int index = 1;
        for (CompressionRule rule : rules)
            storeTo(rule, parent.node("" + index++));
    }

    private static void storeTo(CompressionRule rule, Preferences prefs) {
        PreferencesUtils.storeNotNull(prefs, "cn", rule.getCommonName());
        PreferencesUtils.storeNotEmpty(prefs, "dcmPhotometricInterpretation",
                rule.getPhotometricInterpretations());
        PreferencesUtils.storeNotEmpty(prefs, "dcmBitsStored",
                rule.getBitsStored());
        PreferencesUtils.storeNotDef(prefs, "dcmPixelRepresentation",
                rule.getPixelRepresentation(), -1);
        PreferencesUtils.storeNotEmpty(prefs, "dcmAETitle",
                rule.getAETitles());
        PreferencesUtils.storeNotEmpty(prefs, "dcmSOPClass",
                rule.getSOPClasses());
        PreferencesUtils.storeNotEmpty(prefs, "dcmBodyPartExamined",
                rule.getBodyPartExamined());
        PreferencesUtils.storeNotNull(prefs, "dicomTransferSyntax",
                rule.getTransferSyntax());
        PreferencesUtils.storeNotEmpty(prefs, "dcmImageWriteParams",
                rule.getImageWriteParams());
    }

     public static void load(CompressionRules rules, Preferences parentNode)
            throws BackingStoreException {
        Preferences rulesNode = parentNode.node("dcmCompressionRule");
        for (String ruleIndex : rulesNode.childrenNames()) {
            rules.add(compressionRule(rulesNode.node(ruleIndex)));
        }
    }

    private static CompressionRule compressionRule(Preferences prefs) {
        return new CompressionRule(
            prefs.get("cn", null),
            PreferencesUtils.stringArray(prefs, "dcmPhotometricInterpretation"),
            PreferencesUtils.intArray(prefs, "dcmBitsStored"),
            prefs.getInt("dcmPixelRepresentation", -1),
            PreferencesUtils.stringArray(prefs, "dcmAETitle"),
            PreferencesUtils.stringArray(prefs, "dcmSOPClass"),
            PreferencesUtils.stringArray(prefs, "dcmBodyPartExamined"),
            prefs.get("dicomTransferSyntax", null),
            PreferencesUtils.stringArray(prefs, "dcmImageWriteParams"));
    }

    public static void merge(CompressionRules prevRules, CompressionRules rules,
            Preferences parentNode) throws BackingStoreException {

        Preferences rulesNodes = parentNode.node("dcmCompressionRule");
        int acIndex = 1;
        Iterator<CompressionRule> prevIter = prevRules.iterator();
        for (CompressionRule rule : rules) {
            Preferences ruleNode = rulesNodes.node("" + acIndex++);
            if (prevIter.hasNext())
                storeDiffs(ruleNode, prevIter.next(), rule);
            else
                storeTo(rule, ruleNode);
        }
        while (prevIter.hasNext()) {
            prevIter.next();
            parentNode.node("" + acIndex++).removeNode();
        }
      }

    private static void storeDiffs(Preferences prefs, CompressionRule a, CompressionRule b) {
        PreferencesUtils.storeDiff(prefs, "cn", 
                a.getCommonName(),
                b.getCommonName());
        PreferencesUtils.storeDiff(prefs, "dcmPhotometricInterpretation",
                a.getPhotometricInterpretations(),
                b.getPhotometricInterpretations());
        PreferencesUtils.storeDiff(prefs, "dcmBitsStored",
                a.getBitsStored(),
                b.getBitsStored());
        PreferencesUtils.storeDiff(prefs, "dcmPixelRepresentation",
                a.getPixelRepresentation(),
                b.getPixelRepresentation(),
                -1);
        PreferencesUtils.storeDiff(prefs, "dcmAETitle",
                a.getAETitles(),
                b.getAETitles());
        PreferencesUtils.storeDiff(prefs, "dcmSOPClass",
                a.getSOPClasses(),
                b.getSOPClasses());
        PreferencesUtils.storeDiff(prefs, "dcmBodyPartExamined",
                a.getBodyPartExamined(),
                b.getBodyPartExamined());
        PreferencesUtils.storeDiff(prefs, "dicomTransferSyntax",
                a.getTransferSyntax(),
                b.getTransferSyntax());
        PreferencesUtils.storeDiff(prefs, "dcmImageWriteParams",
                a.getImageWriteParams(),
                b.getImageWriteParams());
    }

}
