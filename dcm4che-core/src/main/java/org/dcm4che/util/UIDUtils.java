package org.dcm4che.util;

import java.math.BigInteger;
import java.util.UUID;
import java.util.regex.Pattern;

public class UIDUtils {

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated in
     * accordance with Rec. ITU-T X.667 | ISO/IEC 9834-8.
     * @see &lt;a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}$lt;/a>
     */
    private static final String UUID_ROOT = "2.25";

    private static final Pattern PATTERN =
            Pattern.compile("[12]((\\.0)|(\\.[1-9]\\d*))+");

    public static boolean isValid(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    public static String createUID() {
        return doCreateUID(UUID_ROOT);
    }

    public static String createUID(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");
        if (!isValid(root))
            throw new IllegalArgumentException(root);
        return doCreateUID(root);
    }

    private static String doCreateUID(String root) {
        byte[] b17 = new byte[17];
        UUID uuid = UUID.randomUUID();
        ByteUtils.longToBytesBE(uuid.getMostSignificantBits(), b17, 1);
        ByteUtils.longToBytesBE(uuid.getLeastSignificantBits(), b17, 9);
        String uuidStr = new BigInteger(b17).toString();
        int rootlen = root.length();
        int uuidlen = uuidStr.length();
        char[] cs = new char[rootlen + uuidlen + 1];
        root.getChars(0, rootlen, cs, 0);
        cs[rootlen] = '.';
        uuidStr.getChars(0, uuidlen, cs, rootlen + 1);
        return new String(cs);
    }
}
