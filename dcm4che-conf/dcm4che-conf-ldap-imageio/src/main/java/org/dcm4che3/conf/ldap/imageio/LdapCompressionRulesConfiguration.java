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
package org.dcm4che3.conf.ldap.imageio;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.CompressionRules;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapCompressionRulesConfiguration {

    private LdapDicomConfiguration config;

    public LdapCompressionRulesConfiguration(LdapDicomConfiguration config) {
         this.config = config;
    }

    public void store(CompressionRules rules, String parentDN)
            throws NamingException {
        for (CompressionRule rule : rules)
            config.createSubcontext(
                    LdapUtils.dnOf("cn", rule.getCommonName(), parentDN),
                    storeTo(rule, new BasicAttributes(true)));
    }

    private static Attributes storeTo(CompressionRule rule,
            BasicAttributes attrs) {
        attrs.put("objectclass", "dcmCompressionRule");
        attrs.put("cn", rule.getCommonName());
        LdapUtils.storeNotEmpty(attrs, "dcmPhotometricInterpretation", 
                rule.getPhotometricInterpretations());
        LdapUtils.storeNotEmpty(attrs, "dcmBitsStored",
                rule.getBitsStored());
        LdapUtils.storeNotDef(attrs, "dcmPixelRepresentation",
                rule.getPixelRepresentation(), -1);
        LdapUtils.storeNotEmpty(attrs, "dcmAETitle", rule.getAETitles());
        LdapUtils.storeNotEmpty(attrs, "dcmSOPClass", rule.getSOPClasses());
        LdapUtils.storeNotEmpty(attrs, "dcmBodyPartExamined",
                rule.getBodyPartExamined());
        attrs.put("dicomTransferSyntax", rule.getTransferSyntax());
        LdapUtils.storeNotEmpty(attrs, "dcmImageWriteParam",
                rule.getImageWriteParams());
        return attrs;
    }

    public void load(CompressionRules rules, String dn) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                config.search(dn, "(objectclass=dcmCompressionRule)");
        try {
            while(ne.hasMore())
                rules.add(compressionRule(ne.next().getAttributes()));
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private CompressionRule compressionRule(Attributes attrs)
            throws NamingException {
        return new CompressionRule(
            LdapUtils.stringValue(attrs.get("cn"), null),
            LdapUtils.stringArray(attrs.get("dcmPhotometricInterpretation")),
            LdapUtils.intArray(attrs.get("dcmBitsStored")),
            LdapUtils.intValue(attrs.get("dcmPixelRepresentation"), -1),
            LdapUtils.stringArray(attrs.get("dcmAETitle")),
            LdapUtils.stringArray(attrs.get("dcmSOPClass")),
            LdapUtils.stringArray(attrs.get("dcmBodyPartExamined")),
            LdapUtils.stringValue(attrs.get("dicomTransferSyntax"), null),
            LdapUtils.stringArray(attrs.get("dcmImageWriteParam")));
    }

    public void merge(CompressionRules prevRules, CompressionRules rules,
            String parentDN) throws NamingException {
        for (CompressionRule prevRule : prevRules) {
            String cn = prevRule.getCommonName();
            if (rules == null || rules.findByCommonName(cn) == null)
                config.destroySubcontext(LdapUtils.dnOf("cn", cn, parentDN));
        }
        for (CompressionRule rule : rules) {
            String cn = rule.getCommonName();
            String dn = LdapUtils.dnOf("cn", cn, parentDN);
            CompressionRule prevRule = prevRules != null
                    ? prevRules.findByCommonName(cn)
                    : null;
            if (prevRule == null)
                config.createSubcontext(dn,
                        storeTo(prevRule, new BasicAttributes(true)));
            else
                config.modifyAttributes(dn, storeDiffs(prevRule, rule, 
                        new ArrayList<ModificationItem>()));
        }
    }

    private List<ModificationItem> storeDiffs(CompressionRule prev,
            CompressionRule rule, List<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dcmPhotometricInterpretation",
                prev.getPhotometricInterpretations(),
                rule.getPhotometricInterpretations());
        LdapUtils.storeDiff(mods, "dcmBitsStored",
                prev.getBitsStored(),
                rule.getBitsStored());
        LdapUtils.storeDiff(mods, "dcmPixelRepresentation",
                prev.getPixelRepresentation(),
                rule.getPixelRepresentation(), -1);
        LdapUtils.storeDiff(mods, "dcmAETitle",
                prev.getAETitles(),
                rule.getAETitles());
        LdapUtils.storeDiff(mods, "dcmSOPClass",
                prev.getSOPClasses(),
                rule.getSOPClasses());
        LdapUtils.storeDiff(mods, "dcmBodyPartExamined",
                prev.getBodyPartExamined(),
                rule.getBodyPartExamined());
        LdapUtils.storeDiff(mods, "dicomTransferSyntax",
                prev.getTransferSyntax(),
                rule.getTransferSyntax());
        LdapUtils.storeDiff(mods, "dcmImageWriteParam",
                prev.getImageWriteParams(),
                rule.getImageWriteParams());
       return mods;
    }

}
