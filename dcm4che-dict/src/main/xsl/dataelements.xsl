<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://docbook.org/ns/docbook"
    exclude-result-prefixes="doc">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/doc:book">
  <dataelements>
    <xsl:apply-templates select="doc:chapter[9]/doc:table/doc:tbody/doc:tr"/>
    <xsl:apply-templates select="doc:chapter[10]/doc:table/doc:tbody/doc:tr"/>
    <xsl:apply-templates select="doc:chapter[8]/doc:table/doc:tbody/doc:tr"/>
  </dataelements>
  </xsl:template>
  <xsl:template match="doc:tr">
    <xsl:element name="el">
      <xsl:variable name="tag">
        <xsl:call-template name="para2str">
          <xsl:with-param name="para" select="doc:td[1]/doc:para"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:attribute name="tag">
        <xsl:value-of select="translate($tag,'(,)','')"/>
      </xsl:attribute>
      <xsl:attribute name="keyword">
        <xsl:call-template name="para2str">
          <xsl:with-param name="para" select="doc:td[3]/doc:para"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:variable name="vr">
        <xsl:call-template name="para2str">
          <xsl:with-param name="para" select="doc:td[4]/doc:para"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="$vr != 'See Note '">
        <xsl:attribute name="vr">
          <xsl:value-of select="$vr"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:attribute name="vm">
        <xsl:call-template name="para2str">
          <xsl:with-param name="para" select="doc:td[5]/doc:para"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:if test="doc:td[6]/doc:para/doc:emphasis">
        <xsl:attribute name="retired">true</xsl:attribute>
      </xsl:if>
      <xsl:call-template name="para2str">
        <xsl:with-param name="para" select="doc:td[2]/doc:para"/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>
  <xsl:template name="para2str">
    <xsl:param name="para"/>
    <xsl:variable name="str">
      <xsl:variable name="emphasis" select="$para/doc:emphasis"/>
      <xsl:choose>
        <xsl:when test="$emphasis">
          <xsl:value-of select="$emphasis/text()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$para/text()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="translate($str,'&#8203;','')"/>
  </xsl:template>
</xsl:stylesheet>