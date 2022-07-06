package org.dcm4che3.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.StandardElementDictionary;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TagUtilsTest  {
    private final String name;
    private final int tagAsInt;
    private final String tagAsHex;

    public TagUtilsTest(String name, int tagAsInt, String tagAsHex){
        this.name = name;
        this.tagAsInt = tagAsInt;
        this.tagAsHex = tagAsHex;
    }
    @Parameterized.Parameters
    public static Collection<Object[]> getTags() {
        return Arrays.stream(Tag.class.getFields()).map(field -> {
                    try {
                        return field.getInt(null);
                    } catch (Exception e) {
                        return -1;
                    }
                }).filter(integer -> integer != -1)
                .map(tag -> new Object[]{StandardElementDictionary.keywordOf(tag, null), tag, TagUtils.toHexString(tag)})
                .collect(Collectors.toList());
    }
    @Test
    public void forName_shouldBeKnown() {
        int tag = TagUtils.forName(tagAsHex);
        assertEquals(tagAsInt, tag);
    }
}
