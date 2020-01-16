//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2020                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//
package org.dcm4che3.net.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author apyii
 */
public class KeyCheckFailureCollector {
    private Map<UniqueKeyCheckFailure.FailureType, List<UniqueKeyCheckFailure>> failures;

    public void add(UniqueKeyCheckFailure failure) {
        List<UniqueKeyCheckFailure> previous = failures.getOrDefault(failure.type, new ArrayList<>());
        previous.add(failure);
    }

    public boolean isEmpty() {
        return failures.isEmpty();
    }
}
