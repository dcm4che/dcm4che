<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="/elements">
    <xsl:text>package org.dcm4che.data;

public class Tag {
</xsl:text>
    <xsl:apply-templates select="element[@keyword!='']" />
    <xsl:text>
}
</xsl:text>
  </xsl:template>
  <xsl:template match="element">
    <xsl:text>
    /** </xsl:text>
    <xsl:value-of select="@tag" />
    <xsl:text> VR=</xsl:text>
    <xsl:value-of select="@vr" />
    <xsl:text> VM=</xsl:text>
    <xsl:value-of select="@vm" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:if test="@retired='true'">
      <xsl:text> (retired)</xsl:text>
    </xsl:if>
    <xsl:text> */
    public static final int </xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text> = 0x</xsl:text>
    <xsl:value-of select="translate(@tag,'x(,)','0')" />
    <xsl:text>;
</xsl:text>
  </xsl:template>
</xsl:stylesheet>