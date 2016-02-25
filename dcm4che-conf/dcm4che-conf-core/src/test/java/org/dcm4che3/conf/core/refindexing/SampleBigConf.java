package org.dcm4che3.conf.core.refindexing;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurableClass
public class SampleBigConf {

    @ConfigurableProperty
    SampleReferableConfClass child1;

    @ConfigurableProperty
    Map<String, SampleReferableConfClass> childMap = new HashMap<String, SampleReferableConfClass>();

    @ConfigurableProperty
    List<SampleReferableConfClass> childList = new ArrayList<SampleReferableConfClass>();

    public SampleReferableConfClass getChild1() {
        return child1;
    }

    public void setChild1(SampleReferableConfClass child1) {
        this.child1 = child1;
    }

    public Map<String, SampleReferableConfClass> getChildMap() {
        return childMap;
    }

    public void setChildMap(Map<String, SampleReferableConfClass> childMap) {
        this.childMap = childMap;
    }

    public List<SampleReferableConfClass> getChildList() {
        return childList;
    }

    public void setChildList(List<SampleReferableConfClass> childList) {
        this.childList = childList;
    }
}
