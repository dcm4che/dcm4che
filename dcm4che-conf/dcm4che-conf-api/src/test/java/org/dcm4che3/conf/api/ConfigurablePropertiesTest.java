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

package org.dcm4che3.conf.api;

import org.dcm4che3.conf.api.upgrade.ConfigurationMetadata;
import org.dcm4che3.conf.api.upgrade.UpgradeScript;
import org.dcm4che3.conf.dicom.DicomConfigurationRoot;
import org.dcm4che3.conf.test.ConfigurablePropertiesTestBase;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ConfigurablePropertiesTest extends ConfigurablePropertiesTestBase {

    @Override
    protected Class[] getClassesToTest() {
        return new Class[]{AttributeCoercion.class, AttributeCoercion.Condition.class, AttributeCoercions.class,
                TCConfiguration.class, TCConfiguration.TCGroup.class, ConfigurationMetadata.class, UpgradeScript.class,
                DicomConfigurationRoot.class, DicomConfigurationRoot.DicomConfigurationNode.class,
                DicomConfigurationRoot.MetadataRoot.class, DicomConfigurationRoot.GlobalConfiguration.class};
    }

    @Override
    protected Set<Field> getPropertiesToSkip() {
        return new HashSet<>();
    }
}
