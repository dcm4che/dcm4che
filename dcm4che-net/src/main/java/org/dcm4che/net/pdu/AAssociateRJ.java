package org.dcm4che.net.pdu;

import java.io.IOException;

public class AAssociateRJ extends IOException {

    public static final int RESULT_REJECTED_PERMANENT = 1;
    public static final int RESULT_REJECTED_TRANSIENT = 2;

    public static final int SOURCE_SERVICE_USER = 1;
    public static final int SOURCE_SERVICE_PROVIDER_ACSE = 2;
    public static final int SOURCE_SERVICE_PROVIDER_PRES = 3;

    public static final int REASON_NO_REASON_GIVEN = 1;
    public static final int REASON_APP_CTX_NAME_NOT_SUPPORTED = 2;
    public static final int REASON_CALLING_AET_NOT_RECOGNIZED = 3;
    public static final int REASON_CALLED_AET_NOT_RECOGNIZED = 7;

    public static final int REASON_PROTOCOL_VERSION_NOT_SUPPORTED = 2;

    public static final int REASON_TEMPORARY_CONGESTION = 1;
    public static final int REASON_LOCAL_LIMIT_EXCEEDED = 2;

    private static final String[] SOURCE_SERVICE_USER_REASONS = {
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 0]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 1 - no-reason-given]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 2 - application-context-name-not-supported]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 3 - calling-AE-title-not-recognized]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 4]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 5]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 6]",
            "A-ASSOCIATE-RJ[result: 1, source: 1, reason: 7 - called-AE-title-not-recognized]",
    };

    private static final String[] SOURCE_SERVICE_PROVIDER_PRES_REASONS = {
            "A-ASSOCIATE-RJ[result: 1, source: 3, reason: 0]",
            "A-ASSOCIATE-RJ[result: 1, source: 3, reason: 1 - no-reason-given]",
            "A-ASSOCIATE-RJ[result: 1, source: 3, reason: 2 - protocol-version-not-supported]",
    };

    private static final String[] SOURCE_SERVICE_PROVIDER_ACSE_REASONS = {
            "A-ASSOCIATE-RJ[result: 2, source: 2, reason: 0]",
            "A-ASSOCIATE-RJ[result: 2, source: 2, reason: 1 - temporary-congestion]",
            "A-ASSOCIATE-RJ[result: 2, source: 2, reason: 2 - local-limit-exceeded]",
    };

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
        try {
            switch (source) {
            case SOURCE_SERVICE_USER:
                if (result == RESULT_REJECTED_PERMANENT)
                    return SOURCE_SERVICE_USER_REASONS[reason];
                break;
            case SOURCE_SERVICE_PROVIDER_ACSE:
                if (result == RESULT_REJECTED_PERMANENT)
                    return SOURCE_SERVICE_PROVIDER_ACSE_REASONS[reason];
                break;
            case SOURCE_SERVICE_PROVIDER_PRES:
                if (result == RESULT_REJECTED_TRANSIENT)
                    return SOURCE_SERVICE_PROVIDER_PRES_REASONS[reason];
                break;
            }
        } catch (IndexOutOfBoundsException ignore) {}
        return "A-ASSOCIATE-RJ[result: " + result
                          + ", source: " + source
                          + ", reason: " + reason + ']';
    }

    public final int getResult() {
        return result;
    }

    public final int getSource() {
        return source;
    }

    public final int getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
