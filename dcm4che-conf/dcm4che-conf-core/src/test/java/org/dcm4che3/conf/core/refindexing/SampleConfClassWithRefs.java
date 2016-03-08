package org.dcm4che3.conf.core.refindexing;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurableClass
public class SampleConfClassWithRefs {

    @ConfigurableProperty
    String someIrrelevantProp;

    @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.Reference)
    SampleReferableConfClass ref1;

    @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.Reference)
    SampleReferableConfClass ref2;

    @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.CollectionOfReferences)
    List<SampleReferableConfClass> refsList = new ArrayList<SampleReferableConfClass>();

    @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.CollectionOfReferences)
    Map<String, SampleReferableConfClass> refsMap = new HashMap<String, SampleReferableConfClass>();

    public String getSomeIrrelevantProp() {
        return someIrrelevantProp;
    }

    public void setSomeIrrelevantProp(String someIrrelevantProp) {
        this.someIrrelevantProp = someIrrelevantProp;
    }

    public SampleReferableConfClass getRef1() {
        return ref1;
    }

    public void setRef1(SampleReferableConfClass ref1) {
        this.ref1 = ref1;
    }

    public SampleReferableConfClass getRef2() {
        return ref2;
    }

    public void setRef2(SampleReferableConfClass ref2) {
        this.ref2 = ref2;
    }

    public List<SampleReferableConfClass> getRefsList() {
        return refsList;
    }

    public void setRefsList(List<SampleReferableConfClass> refsList) {
        this.refsList = refsList;
    }

    public Map<String, SampleReferableConfClass> getRefsMap() {
        return refsMap;
    }

    public void setRefsMap(Map<String, SampleReferableConfClass> refsMap) {
        this.refsMap = refsMap;
    }
}
