//
// ///////////////////////////////////////////////////////////////
//                 C O P Y R I G H T  (c) 2020                 //
//         Agfa HealthCare N.V. and/or its affiliates          //
//                    All Rights Reserved                      //
/////////////////////////////////////////////////////////////////
//                                                             //
//        THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF       //
//         Agfa HealthCare N.V. and/or its affiliates.         //
//       The copyright notice above does not evidence any      //
//      actual or intended publication of such source code.    //
//                                                             //
/////////////////////////////////////////////////////////////////
//

package org.dcm4che3.net;

import org.dcm4che3.conf.test.ConfigurablePropertiesTestBase;
import org.dcm4che3.net.web.WebServiceAEExtension;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ConfigurablePropertiesTest extends ConfigurablePropertiesTestBase {

    @Override
    protected Class[] getClassesToTest() {
        return new Class[]{ExternalArchiveAEExtension.class, Connection.class, TCGroupConfigAEExtension.class,
                TCGroupConfigAEExtension.TCGroupDetails.class, ApplicationEntity.class, Device.class,
                TransferCapability.class, WebServiceAEExtension.class, StorageOptions.class};
    }

    @Override
    protected Set<Field> getPropertiesToSkip() throws NoSuchFieldException {
        Set<Field> set = new HashSet<>();
        set.add(Connection.class.getDeclaredField("connectionInstalled"));
        set.add(ApplicationEntity.class.getDeclaredField("aeInstalled"));
        return set;
    }
}
