<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" />
  <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
  <xsl:template match="/article">
    <uids>
      <xsl:apply-templates select="//row[(count(entry)=4) and starts-with(entry/para,'1.')]" />
    </uids>
  </xsl:template>
  <xsl:template match="row">
    <xsl:variable name="uid" select="entry[1]/para" />
    <xsl:variable name="name">
      <xsl:call-template name="skipAfterColon">
         <xsl:with-param name="name" select="entry[2]/para" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="keyword">
      <xsl:choose>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.70'">JPEGLosslessNonHierarchicalProcess14SelectionValue1</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.9.1.1'">TwelveLeadECGWaveformStorage</xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="removeSpaces">
            <xsl:with-param name="name">
              <xsl:call-template name="replaceNonAlpha">
                 <xsl:with-param name="name" select="$name" />
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="type" select="entry[3]/para" />
    <uid uid="{$uid}" name="{$name}" keyword="{$keyword}" type="{$type}" />
  </xsl:template>
  <xsl:template name="skipAfterColon">
    <xsl:param name="name"/>
    <xsl:variable name="before" select="substring-before($name,':')" />
    <xsl:choose>
      <xsl:when test="$before">
        <xsl:value-of select="normalize-space($before)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space($name)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="replaceNonAlpha">
    <xsl:param name="name"/>
    <xsl:value-of select="normalize-space(translate($name,'-,@/()&amp;','       '))"/>
  </xsl:template>
  <xsl:template name="removeSpaces">
    <xsl:param name="name"/>
    <xsl:variable name="after" select="substring-after($name, ' ')"/>
    <xsl:choose>
      <xsl:when test="$after">
        <xsl:value-of select="substring-before($name, ' ')"/>
        <xsl:value-of select="translate(substring($after,1,1),$LOWER,$UPPER)"/>
        <xsl:call-template name="removeSpaces">
          <xsl:with-param name="name" select="substring($after,2)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>