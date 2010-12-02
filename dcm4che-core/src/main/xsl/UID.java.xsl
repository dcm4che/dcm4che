<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="/uids">
    <xsl:text>package org.dcm4che.data;

import java.math.BigInteger;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

import org.dcm4che.util.ByteUtils;

public class UID {

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated in
     * accordance with Rec. ITU-T X.667 | ISO/IEC 9834-8.
     * @see &lt;a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}$lt;/a>
     */
    private static final String UUID_ROOT = "2.25";

    private static final Pattern PATTERN =
            Pattern.compile("[12]((\\.0)|(\\.[1-9]\\d*))+");

    private static final ResourceBundle rb = 
            ResourceBundle.getBundle("org.dcm4che.data.UIDNames");

    public static String nameOf(String uid) {
        try {
            return rb.getString(uid);
        } catch (Exception e) {
            return uid;
        }
    }

    public static String forName(String keyword) {
        try {
            return (String) UID.class.getField(keyword).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException(keyword);
        }
    }

    public static boolean isValid(String uid) {
        return uid.length() &lt;= 64 &amp;&amp; PATTERN.matcher(uid).matches();
    }

    public static String createUID() {
        return doCreateUID(UUID_ROOT);
    }

    public static String createUID(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException(root + " exeeds 24 characters");
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
</xsl:text>
    <xsl:apply-templates select="uid" />
    <xsl:text>
}
</xsl:text>
  </xsl:template>
  <xsl:template match="uid">
    <xsl:text>
    /** </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text>, </xsl:text>
    <xsl:value-of select="@type" />
    <xsl:text> */
    public static final String </xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text> = "</xsl:text>
    <xsl:value-of select="@uid" />
    <xsl:text>";
</xsl:text>
  </xsl:template>
</xsl:stylesheet>