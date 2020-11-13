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


import static org.junit.Assert.*;

import org.dcm4che3.data.PersonName;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class PersonNameTest {

    @Test
    public void testValueOf() {
        PersonName pn = new PersonName(
                "Adams^John Robert Quincy^^Rev.^B.A. M.Div.");
        assertEquals("Adams", pn.get(PersonName.Component.FamilyName));
        assertEquals("John Robert Quincy", pn.get(PersonName.Component.GivenName));
        assertEquals("Rev.", pn.get(PersonName.Component.NamePrefix));
        assertEquals("B.A. M.Div.", pn.get(PersonName.Component.NameSuffix));
    }

    @Test
    public void testSubsumeSurplusComponentInSuffix() {
        PersonName pn = new PersonName("Adams^John Robert Quincy^^Rev.^B.A.^M.Div.", true);
        assertEquals("Adams", pn.get(PersonName.Component.FamilyName));
        assertEquals("John Robert Quincy", pn.get(PersonName.Component.GivenName));
        assertEquals("Rev.", pn.get(PersonName.Component.NamePrefix));
        assertEquals("B.A. M.Div.", pn.get(PersonName.Component.NameSuffix));
    }

    @Test
    public void testValueOf2() {
        PersonName pn = new PersonName("Hong^Gildong=洪^吉洞=홍^길동");
        assertEquals("Hong", pn.get(PersonName.Group.Alphabetic, PersonName.Component.FamilyName));
        assertEquals("Gildong", pn.get(PersonName.Group.Alphabetic, PersonName.Component.GivenName));
        assertEquals("洪", pn.get(PersonName.Group.Ideographic, PersonName.Component.FamilyName));
        assertEquals("吉洞", pn.get(PersonName.Group.Ideographic, PersonName.Component.GivenName));
        assertEquals("홍", pn.get(PersonName.Group.Phonetic, PersonName.Component.FamilyName));
        assertEquals("길동", pn.get(PersonName.Group.Phonetic, PersonName.Component.GivenName));
    }

    @Test
    public void testSetGroup() {
        PersonName pn = new PersonName();
        pn.set(PersonName.Group.Alphabetic, "Hong^Gildong");
        pn.set(PersonName.Group.Ideographic, "洪^吉洞");
        pn.set(PersonName.Group.Phonetic, "홍^길동");
        assertEquals("Hong", pn.get(PersonName.Group.Alphabetic, PersonName.Component.FamilyName));
        assertEquals("Gildong", pn.get(PersonName.Group.Alphabetic, PersonName.Component.GivenName));
        assertEquals("洪", pn.get(PersonName.Group.Ideographic, PersonName.Component.FamilyName));
        assertEquals("吉洞", pn.get(PersonName.Group.Ideographic, PersonName.Component.GivenName));
        assertEquals("홍", pn.get(PersonName.Group.Phonetic, PersonName.Component.FamilyName));
        assertEquals("길동", pn.get(PersonName.Group.Phonetic, PersonName.Component.GivenName));
    }

    @Test
    public void testToString() {
        PersonName pn = new PersonName();
        pn.set(PersonName.Component.FamilyName, "Morrison-Jones");
        pn.set(PersonName.Component.GivenName, "Susan");
        pn.set(PersonName.Component.NameSuffix, "Ph.D., Chief Executive Officer");
        assertEquals("Morrison-Jones^Susan^^^Ph.D., Chief Executive Officer",
                pn.toString());
    }

    @Test
    public void testToString2() {
        PersonName pn = new PersonName();
        pn.set(PersonName.Group.Alphabetic, PersonName.Component.FamilyName, "Wang");
        pn.set(PersonName.Group.Alphabetic, PersonName.Component.GivenName, "XiaoDong");
        pn.set(PersonName.Group.Ideographic, PersonName.Component.FamilyName, "王");
        pn.set(PersonName.Group.Ideographic, PersonName.Component.GivenName, "小東");
        assertEquals("Wang^XiaoDong=王^小東", pn.toString());
        assertEquals("Wang^XiaoDong", pn.toString(PersonName.Group.Alphabetic, true));
        assertEquals("王^小東", pn.toString(PersonName.Group.Ideographic, true));
        assertEquals("", pn.toString(PersonName.Group.Phonetic, true));
        assertEquals("Wang^XiaoDong^^^", pn.toString(PersonName.Group.Alphabetic, false));
        assertEquals("王^小東^^^", pn.toString(PersonName.Group.Ideographic, false));
        assertEquals("^^^^", pn.toString(PersonName.Group.Phonetic, false));
    }

}
