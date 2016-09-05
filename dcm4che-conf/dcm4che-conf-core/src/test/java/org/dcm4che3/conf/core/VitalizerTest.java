/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core;

import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.misc.DeepEqualsDiffer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

/**
 * Created by aprvf on 14/10/2014.
 */
@RunWith(JUnit4.class)
public class VitalizerTest {

    BeanVitalizer getVitalizer() {
        return new DefaultBeanVitalizer();

    }

    @ConfigurableClass
    public static class TestConfigSubClass {
        @ConfigurableProperty(name = "prop1")
        int prop1;

        @ConfigurableProperty(name = "prop2")
        boolean prop2;

        @ConfigurableProperty(name = "str")
        String s;

        public int getProp1() {
            return prop1;
        }

        public void setProp1(int prop1) {
            this.prop1 = prop1;
        }

        public boolean isProp2() {
            return prop2;
        }

        public void setProp2(boolean prop2) {
            this.prop2 = prop2;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }
    }

    @ConfigurableClass
    public static class TestConfigClassWithIssues {

        @ConfigurableProperty(name = "primitivePropThatDoesNotExistInConfig")
        int unsetProp;

        @ConfigurableProperty(name = "noBool")
        boolean unsetPropBool;

        @ConfigurableProperty(name = "nullBool")
        Boolean unsetPropBigBool;

        public int getUnsetProp() {
            return unsetProp;
        }

        public void setUnsetProp(int unsetProp) {
            this.unsetProp = unsetProp;
        }

        public boolean isUnsetPropBool() {
            return unsetPropBool;
        }

        public void setUnsetPropBool(boolean unsetPropBool) {
            this.unsetPropBool = unsetPropBool;
        }

        public Boolean getUnsetPropBigBool() {
            return unsetPropBigBool;
        }

        public void setUnsetPropBigBool(Boolean unsetPropBigBool) {
            this.unsetPropBigBool = unsetPropBigBool;
        }
    }

    public enum MyEnum {
        OPTION1,OPTION2
    }

    @ConfigurableClass
    public static class TestConfigClass {

        @ConfigurableProperty(defaultValue = "OPTION1")
        MyEnum myEnum;

        @ConfigurableProperty(name = "prop1")
        int prop1;

        @ConfigurableProperty(name = "intProp1")
        int prop2;

        @ConfigurableProperty(name = "boolProp1")
        boolean boolProp;

        @ConfigurableProperty(name = "boolProp2")
        Boolean boolProp2;

        @ConfigurableProperty(name = "strProp")
        String str;

        @ConfigurableProperty(name = "nullStrProp")
        String nullstr;

        @ConfigurableProperty(name = "subProp1")
        TestConfigSubClass subProp1;

        @ConfigurableProperty(name = "subProp2")
        TestConfigSubClass subP;

        @ConfigurableProperty(name = "subMap")
        Map<String, String> mapProp;

        @ConfigurableProperty(name = "objSubMap")
        Map<String, TestConfigSubClass> objMapProp;

        @ConfigurableProperty(name = "setProp")
        Set<String> aSet;

        @ConfigurableProperty(name = "strArrayProp")
        String[] strArrayProp;

        @ConfigurableProperty
        boolean[] boolArrayProp;

        @ConfigurableProperty
        byte[] byteArrayProp;


        @ConfigurableProperty(name = "intArrayProp")
        int[] intArrayProp;

        public MyEnum getMyEnum() {
            return myEnum;
        }

        public void setMyEnum(MyEnum myEnum) {
            this.myEnum = myEnum;
        }

        public byte[] getByteArrayProp() {
            return byteArrayProp;
        }

        public void setByteArrayProp(byte[] byteArrayProp) {
            this.byteArrayProp = byteArrayProp;
        }

        public Set<String> getaSet() {
            return aSet;
        }

        public void setaSet(Set<String> aSet) {
            this.aSet = aSet;
        }

        public String getNullstr() {
            return nullstr;
        }

        public void setNullstr(String nullstr) {
            this.nullstr = nullstr;
        }

        public String[] getStrArrayProp() {
            return strArrayProp;
        }

        public void setStrArrayProp(String[] strArrayProp) {
            this.strArrayProp = strArrayProp;
        }

        public boolean[] getBoolArrayProp() {
            return boolArrayProp;
        }

        public void setBoolArrayProp(boolean[] boolArrayProp) {
            this.boolArrayProp = boolArrayProp;
        }

        public int[] getIntArrayProp() {
            return intArrayProp;
        }

        public void setIntArrayProp(int[] intArrayProp) {
            this.intArrayProp = intArrayProp;
        }

        public int getProp1() {
            return prop1;
        }

        public void setProp1(int prop1) {
            this.prop1 = prop1;
        }

        public int getProp2() {
            return prop2;
        }

        public void setProp2(int prop2) {
            this.prop2 = prop2;
        }

        public boolean isBoolProp() {
            return boolProp;
        }

        public void setBoolProp(boolean boolProp) {
            this.boolProp = boolProp;
        }

        public Boolean getBoolProp2() {
            return boolProp2;
        }

        public void setBoolProp2(Boolean boolProp2) {
            this.boolProp2 = boolProp2;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public TestConfigSubClass getSubProp1() {
            return subProp1;
        }

        public void setSubProp1(TestConfigSubClass subProp1) {
            this.subProp1 = subProp1;
        }

        public TestConfigSubClass getSubP() {
            return subP;
        }

        public void setSubP(TestConfigSubClass subP) {
            this.subP = subP;
        }

        public Map<String, String> getMapProp() {
            return mapProp;
        }

        public void setMapProp(Map<String, String> mapProp) {
            this.mapProp = mapProp;
        }

        public Map<String, TestConfigSubClass> getObjMapProp() {
            return objMapProp;
        }

        public void setObjMapProp(Map<String, TestConfigSubClass> objMapProp) {
            this.objMapProp = objMapProp;
        }
    }

    public static class ConfClassWithSetters {

    }


    HashMap<String, Object> getTestConfigClassMap() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("prop1", 21);
        map.put("intProp1", 111);
        map.put("boolProp1", false);
        map.put("boolProp2", true);
        map.put("strProp", "a cool str");
        map.put("byteArrayProp", "aGVsbG8=");

        HashMap<String, Object> subClassInst1 = new HashMap<String, Object>();
        subClassInst1.put("prop1", 123);
        subClassInst1.put("prop2", false);
        subClassInst1.put("str", "hallo!!");

        HashMap<String, Object> subClassInst2 = new HashMap<String, Object>();
        subClassInst2.put("prop1", 3323);
        subClassInst2.put("prop2", true);
        subClassInst2.put("str", "hallo noch mal!!");

        map.put("subProp1", subClassInst1);
        map.put("subProp2", subClassInst2);

        Map<String, String> m = new HashMap<String, String>();
        m.put("what1", "that1");
        m.put("waht2", "that2");

        map.put("subMap", m);

        HashMap<String, Object> subClassElem1 = new HashMap<String, Object>();
        subClassElem1.put("prop1", 50);
        subClassElem1.put("prop2", false);
        subClassElem1.put("str", "heey    !!");

        HashMap<String, Object> subClassElem2 = new HashMap<String, Object>();
        subClassElem2.put("prop1", 1);
        subClassElem2.put("prop2", true);
        subClassElem2.put("str", "wow much number such true wow");

        HashMap<String, Map> subClassHashMap = new HashMap<String, Map>();
        subClassHashMap.put("elem1", subClassElem1);
        subClassHashMap.put("elem2", subClassElem2);

        map.put("objSubMap", subClassHashMap);

        map.put("setProp", new HashSet<String>(Arrays.asList("abc", "cde", "efg")));

        map.put("strArrayProp", Arrays.asList("abc", "cde", "efg"));
        map.put("intArrayProp", Arrays.asList(1, 2, 3));
        map.put("boolArrayProp", Arrays.asList(true, false, true));

        return map;
    }


    @Test
    public void testBackAndForthTestConfigClass() throws ConfigurationException {
        HashMap<String, Object> testConfigClassNode = getTestConfigClassMap();
        DefaultBeanVitalizer beanVitalizer = new DefaultBeanVitalizer();

        TestConfigClass configuredInstance = beanVitalizer.newConfiguredInstance(testConfigClassNode, TestConfigClass.class);
        Object generatedNode = beanVitalizer.createConfigNodeFromInstance(configuredInstance);

        //boolean b = DeepEquals.deepEquals(testConfigClassNode, generatedNode);
        //Assert.assertTrue("Objects should be equal.Last pair of unmatched properties:"+DeepEquals.getLastPair(), b);


        DeepEqualsDiffer.assertDeepEquals("Config node before deserialization must be the same as after serializing back", testConfigClassNode, generatedNode);
        //boolean b = DeepEquals.deepEquals(testConfigClassNode, generatedNode);
        //Assert.assertTrue("Config node before deserialization must be the same as after serializing back. Last keys"+DeepEquals.lastDualKey,b);

    }

    @Test
    public void testNulls() throws ConfigurationException {

        HashMap<String, Object> testConfigClassNode = getTestConfigClassMap();
        BeanVitalizer beanVitalizer = new DefaultBeanVitalizer();

        TestConfigClass configuredInstance = beanVitalizer.newConfiguredInstance(testConfigClassNode, TestConfigClass.class);

        // unspecified string attribute results into null
        Assert.assertEquals(null, configuredInstance.getNullstr());

        Map<String, Object> generatedNode = beanVitalizer.createConfigNodeFromInstance(configuredInstance);

        // null value produces no attribute-value record in serialized repr
        Assert.assertEquals(false, generatedNode.containsKey("nullStrProp"));


        // primitive nulls

        try {
            TestConfigClassWithIssues testConfigClassWithIssues = beanVitalizer.newConfiguredInstance(new HashMap<String, Object>(), TestConfigClassWithIssues.class);
            throw new RuntimeException("Should brake!");
        } catch (ConfigurationException e) {
            // ok
        }

        try {
            HashMap<String, Object> configNode = new HashMap<String, Object>();
            configNode.put("primitivePropThatDoesNotExistInConfig", 0);
            TestConfigClassWithIssues testConfigClassWithIssues = beanVitalizer.newConfiguredInstance(configNode, TestConfigClassWithIssues.class);
            throw new RuntimeException("Should brake!");
        } catch (ConfigurationException e) {
            // ok
        }

        HashMap<String, Object> configNode = new HashMap<String, Object>();
        configNode.put("primitivePropThatDoesNotExistInConfig", 0);
        configNode.put("noBool", false);
        TestConfigClassWithIssues testConfigClassWithIssues = beanVitalizer.newConfiguredInstance(configNode, TestConfigClassWithIssues.class);


        // enum nulls
        Map<String, Object> configNodeFromInstance = new DefaultBeanVitalizer().createConfigNodeFromInstance(new TestConfigClass());
        Assert.assertFalse("null-valued enum is not persisted", configNodeFromInstance.containsKey("myEnum"));


    }

    @Test
    public void testPerformance() throws ConfigurationException {
        HashMap<String, Object> testConfigClassNode = getTestConfigClassMap();
        BeanVitalizer beanVitalizer = new DefaultBeanVitalizer();

        for (int i = 0; i < 1000; i++) {
            TestConfigClass configuredInstance = beanVitalizer.newConfiguredInstance(testConfigClassNode, TestConfigClass.class);
            Object generatedNode = beanVitalizer.createConfigNodeFromInstance(configuredInstance);
        }

    }

}
