package org.dcm4che3.net;

import java.io.IOException;

public class AssociationStateException extends IOException {

    private static final long serialVersionUID = -4990622421265093014L;

    public AssociationStateException(State state) {
        super(state.toString());
     }

}
