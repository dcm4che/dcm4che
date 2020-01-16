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

/**
 * @author apyii
 */
class UniqueKeyCheckFailure {
    public enum FailureType {
        INVALID_ATTRIBUTE,
        MISSING_ATTRIBUTE
    }

    public final FailureType type;
    public final int key;
    public final String value;

    UniqueKeyCheckFailure(FailureType type, int key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}
