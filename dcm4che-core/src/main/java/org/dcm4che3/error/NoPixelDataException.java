//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2019                //
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
package org.dcm4che3.error;

import java.io.IOException;

/**
 * Exception that indicates that the pixel data was missing and could not be returned.
 * @author Andrew Cowan (awcowan@gmail.com)
 */
public class NoPixelDataException extends IOException {
    public NoPixelDataException(String msg) {
        super(msg);
    }

    public NoPixelDataException() {
        super();
    }
}
