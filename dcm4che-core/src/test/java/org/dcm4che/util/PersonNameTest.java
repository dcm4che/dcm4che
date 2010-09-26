package org.dcm4che.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class PersonNameTest {

    @Test
    public void testValueOf() {
        PersonName pn = PersonName.valueOf(
                "Adams^John Robert Quincy^^Rev.^B.A. M.Div.");
        assertEquals("Adams", pn.get(PersonName.FAMILY_NAME));
        assertEquals("John Robert Quincy", pn.get(PersonName.GIVEN_NAME));
        assertEquals("Rev.", pn.get(PersonName.NAME_PREFIX));
        assertEquals("B.A. M.Div.", pn.get(PersonName.NAME_SUFFIX));
    }

    @Test
    public void testValueOf2() {
        PersonName pn = PersonName.valueOf("Hong^Gildong=洪^吉洞=릊^길동");
        assertEquals("Hong", pn.get(PersonName.ALPHABETIC, PersonName.FAMILY_NAME));
        assertEquals("Gildong", pn.get(PersonName.ALPHABETIC, PersonName.GIVEN_NAME));
        assertEquals("洪", pn.get(PersonName.IDEOGRAPHIC, PersonName.FAMILY_NAME));
        assertEquals("吉洞", pn.get(PersonName.IDEOGRAPHIC, PersonName.GIVEN_NAME));
        assertEquals("릊", pn.get(PersonName.PHONETIC, PersonName.FAMILY_NAME));
        assertEquals("길동", pn.get(PersonName.PHONETIC, PersonName.GIVEN_NAME));
    }

    @Test
    public void testToString() {
        PersonName pn = new PersonName();
        pn.set(PersonName.FAMILY_NAME, "Morrison-Jones");
        pn.set(PersonName.GIVEN_NAME, "Susan");
        pn.set(PersonName.NAME_SUFFIX, "Ph.D., Chief Executive Officer");
        assertEquals("Morrison-Jones^Susan^^^Ph.D., Chief Executive Officer",
                pn.toString());
    }

    @Test
    public void testToString2() {
        PersonName pn = new PersonName();
        pn.set(PersonName.ALPHABETIC, PersonName.FAMILY_NAME, "Wang");
        pn.set(PersonName.ALPHABETIC, PersonName.GIVEN_NAME, "XiaoDong");
        pn.set(PersonName.IDEOGRAPHIC, PersonName.FAMILY_NAME, "王");
        pn.set(PersonName.IDEOGRAPHIC, PersonName.GIVEN_NAME, "小東");
        assertEquals("Wang^XiaoDong=王^小東", pn.toString());
    }

}
