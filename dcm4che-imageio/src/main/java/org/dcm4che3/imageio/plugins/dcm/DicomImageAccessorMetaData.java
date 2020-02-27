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
package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.imageio.metadata.DicomImageAccessor;
import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadata;

/**
 * Abstract class which all DicomImageAccessor implementations should implement to
 * @author Andrew Cowan (awcowan@gmail.com)
 */
public abstract class DicomImageAccessorMetaData extends IIOMetadata implements DicomImageAccessor {

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
}
