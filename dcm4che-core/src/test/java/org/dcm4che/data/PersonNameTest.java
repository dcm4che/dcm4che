package org.dcm4che.data;

import static org.junit.Assert.*;

import org.junit.Test;

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
    }

}
