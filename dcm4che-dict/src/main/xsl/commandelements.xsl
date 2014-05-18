<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://docbook.org/ns/docbook"
    exclude-result-prefixes="doc">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/doc:book">
  <commandelements>
    <xsl:apply-templates select="doc:chapter[17]/doc:section/doc:table/doc:tbody/doc:tr">
      <xsl:sort select="doc:td[1]/doc:para"/>
    </xsl:apply-templates>
  </commandelements>
  </xsl:template>
  <xsl:template match="doc:tr">
    <xsl:element name="el">
      <xsl:attribute name="tag">
        <xsl:value-of select="translate(doc:td[1]/doc:para,'(,)','')"/>
      </xsl:attribute>
      <xsl:attribute name="keyword">
        <xsl:value-of select="doc:td[3]/doc:para"/>
      </xsl:attribute>
      <xsl:attribute name="vr">
        <xsl:value-of select="doc:td[4]/doc:para"/>
      </xsl:attribute>
      <xsl:attribute name="vm">
        <xsl:value-of select="doc:td[5]/doc:para"/>
      </xsl:attribute>
      <xsl:value-of select="doc:td[2]/doc:para"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>