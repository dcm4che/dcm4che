package org.dcm4che.util;

import java.math.BigInteger;
import java.util.UUID;

public class UIDUtils {

    private static final int EXPECT_DOT = 0;
    private static final int EXPECT_FIRST_DIGIT = 1;
    private static final int EXPECT_DOT_OR_DIGIT = 2;
    private static final int ILLEGAL_UID = -1;

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated in
     * accordance with Rec. ITU-T X.667 | ISO/IEC 9834-8. Used by default.
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    public static final String UUID_ROOT = "2.25";
    
    private static String root = UUID_ROOT;

    public static void setRoot(String root) {
        verifyUIDRoot(root);
        UIDUtils.root = root;
    }

    private static void verifyUIDRoot(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");

        verifyUID(root);
    }

    public static String getRoot() {
        return root;
    }

    public static void verifyUID(String uid) {
        verifyUID(uid, true);
    }

    public static void verifyUID(String uid, boolean acceptLeadingZero) {
        if (!isValidUID(uid, acceptLeadingZero))
            throw new IllegalArgumentException(uid);
    }

    public static boolean isValidUID(String uid) {
        return isValidUID(uid, true);
    }

    public static boolean isValidUID(String uid, boolean acceptLeadingZero) {
        int len = uid.length();
        if (len > 64)
            return false;

        int state = EXPECT_FIRST_DIGIT;
        for (int i = 0; i < len; i++) {
            state = nextState(state, uid.charAt(i), acceptLeadingZero);
            if (state == ILLEGAL_UID)
                return false;
        }
        return state != EXPECT_FIRST_DIGIT;
    }

    private static int nextState(int state, int ch, boolean acceptLeadingZero) {
        switch (ch) {
        case '.':
            return state == EXPECT_FIRST_DIGIT
                    ? ILLEGAL_UID 
                    : EXPECT_FIRST_DIGIT;
        case '0':
            if (!acceptLeadingZero)
                return state == EXPECT_DOT
                        ? ILLEGAL_UID
                        : state == EXPECT_FIRST_DIGIT
                                ? EXPECT_DOT 
                                : EXPECT_DOT_OR_DIGIT;
            // fall through
        case '1': case '2': case '3':
        case '4': case '5': case '6':
        case '7': case '8': case '9':
            return state == EXPECT_DOT ? ILLEGAL_UID : EXPECT_DOT_OR_DIGIT;
        }
        return ILLEGAL_UID;
    }

    public static String createUID() {
        return doCreateUID(root);
    }

    public static String createUID(String root) {
        verifyUIDRoot(root);
        return doCreateUID(root);
    }

    private static String doCreateUID(String root) {
        UUID uuid = UUID.randomUUID();
        byte[] b17 = new byte[17];
        fill(b17, 1, uuid.getMostSignificantBits());
        fill(b17, 9, uuid.getLeastSignificantBits());
        return new StringBuilder(64).append(root).append('.')
                .append(new BigInteger(b17)).toString();
    }

    private static void fill(byte[] bb, int off, long val) {
        for (int i = off, shift = 56; shift >= 0; i++, shift -= 8)
            bb[i] = (byte) (val >>> shift);
    }
}
