package org.dcm4che3.conf.core.refindexing;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;

/**
 * Created by aprvf on 25.02.2016.
 */
@ConfigurableClass
public class SampleReferableConfClass {


    @ConfigurableProperty
    String myName;

    @ConfigurableProperty
    Integer myNumber;

    @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.UUID)
    String uuid;


    public SampleReferableConfClass(String uuid) {
        this.uuid = uuid;
    }


    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public Integer getMyNumber() {
        return myNumber;
    }

    public void setMyNumber(Integer myNumber) {
        this.myNumber = myNumber;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
