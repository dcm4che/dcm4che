<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="/elements">
    <xsl:text>package org.dcm4che.data;

public class Keyword {

    public static String valueOf(int tag) {
        if ((tag &amp; 0xFFFFFF00) == Tag.SourceImageIDs)
            return "SourceImageIDs";
        int tmp = tag &amp; 0xFFE00000;
        if (tmp == 0x50000000 || tmp == 0x60000000)
            tag &amp;= 0xFFE0FFFF;
        else if ((tag &amp; 0xFF000000) == 0x7F000000 
                &amp;&amp; (tag &amp; 0xFFFF0000) != 0x7FE00000)
            tag &amp;= 0xFF00FFFF;
        switch (tag) {
</xsl:text>
    <xsl:apply-templates 
      select="element[@keyword!='' and not(starts-with(@tag,'(0028,04x'))]" />
    <xsl:text>        }
        return null;
    }

}
</xsl:text>
  </xsl:template>
  <xsl:template match="element">
    <xsl:text>        case Tag.</xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text>:
            return "</xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text>";
</xsl:text>
  </xsl:template>
</xsl:stylesheet>