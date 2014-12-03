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

package org.dcm4che3.imageio.codec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.PhotometricInterpretation;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@ConfigurableClass
public class CompressionRules
        implements Iterable<CompressionRule>, Serializable {
    
    private static final long serialVersionUID = 5027417735779753342L;

    @LDAP(noContainerNode = true)
    @ConfigurableProperty
    private List<CompressionRule> list =
            new ArrayList<CompressionRule>();

    public void add(CompressionRule rule) {
        if (findByCommonName(rule.getCommonName()) != null)
            throw new IllegalStateException("CompressionRule with cn: '"
                    + rule.getCommonName() + "' already exists");
        int index = Collections.binarySearch(list, rule);
        if (index < 0)
            index = -(index+1);
        list.add(index, rule);
    }

    public List<CompressionRule> getList() {
        return list;
    }

    public void setList(List<CompressionRule> list) {
        this.list.clear();
        for (CompressionRule rule : list) add(rule);
    }

    public void add(CompressionRules rules) {
         for (CompressionRule rule : rules)
             add(rule);
    }

    public boolean remove(CompressionRule ac) {
        return list.remove(ac);
    }

    public void clear() {
        list.clear();
    }

    public CompressionRule findByCommonName(String commonName) {
        for (CompressionRule rule : list)
            if (commonName.equals(rule.getCommonName()))
                return rule;
        return null;
    }

    public CompressionRule findCompressionRule(String aeTitle, Attributes attrs) {
        try {
            return findCompressionRule(aeTitle,
                    PhotometricInterpretation.fromString(
                            attrs.getString(Tag.PhotometricInterpretation)),
                    attrs.getInt(Tag.BitsStored, 0),
                    attrs.getInt(Tag.PixelRepresentation, 0),
                    attrs.getString(Tag.SOPClassUID),
                    attrs.getStrings(Tag.ImageType),
                    attrs.getString(Tag.BodyPartExamined));
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    public CompressionRule findCompressionRule(String aeTitle,
            PhotometricInterpretation pmi,
            int bitsStored, int pixelRepresentation, 
            String sopClass, String[] imgTypes, String bodyPart) {
        for (CompressionRule ac : list)
            if (ac.matchesCondition(pmi, bitsStored, pixelRepresentation,
                    aeTitle, sopClass, imgTypes, bodyPart))
                return ac;
        return null;
    }

    @Override
    public Iterator<CompressionRule> iterator() {
        return list.iterator();
    }
}
