package org.dcm4che.net.pdu;

import java.io.IOException;

public class AAssociateRJ extends IOException {

    private final int result;
    private final int source;
    private final int reason;

    public AAssociateRJ(int result, int source, int reason) {
        super(createMessage(result, source, reason));
        this.result = result;
        this.source = source;
        this.reason = reason;
    }

    private static String createMessage(int result, int source, int reason) {
        // TODO Auto-generated method stub
        return null;
    }
}
