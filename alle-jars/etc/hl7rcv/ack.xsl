<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" />
  <xsl:param name="MessageControlID">1</xsl:param>
  <xsl:param name="DateTimeOfMessage">20120305101010.101</xsl:param>
  <xsl:param name="AcknowledgementCode">AA</xsl:param>
  <xsl:param name="TextMessage" />

  <xsl:template match="/hl7">
    <hl7>
      <MSH fieldDelimiter="{MSH/@fieldDelimiter}"
           componentDelimiter="{MSH/@componentDelimiter}"
           repeatDelimiter="{MSH/@repeatDelimiter}"
           escapeDelimiter="{MSH/@escapeDelimiter}"
           subcomponentDelimiter="{MSH/@subcomponentDelimiter}">
        <field><xsl:value-of select="MSH/field[3]" /></field>
        <field><xsl:value-of select="MSH/field[4]" /></field>
        <field><xsl:value-of select="MSH/field[1]" /></field>
        <field><xsl:value-of select="MSH/field[2]" /></field>
        <field><xsl:value-of select="$DateTimeOfMessage" /></field>
        <field><xsl:value-of select="MSH/field[6]" /></field>
        <field>ACK</field>
        <field><xsl:value-of select="$MessageControlID" /></field>
        <field><xsl:value-of select="MSH/field[9]" /></field>
        <field><xsl:value-of select="MSH/field[10]" /></field>
        <field><xsl:value-of select="MSH/field[11]" /></field>
        <field><xsl:value-of select="MSH/field[12]" /></field>
        <field><xsl:value-of select="MSH/field[13]" /></field>
        <field><xsl:value-of select="MSH/field[14]" /></field>
        <field><xsl:value-of select="MSH/field[15]" /></field>
        <field><xsl:value-of select="MSH/field[16]" /></field>
      </MSH>
      <MSA>
        <field><xsl:value-of select="$AcknowledgementCode" /></field>
        <field><xsl:value-of select="MSH/field[8]" /></field>
        <field><xsl:value-of select="$TextMessage" /></field>
      </MSA>
    </hl7>
  </xsl:template>
</xsl:stylesheet>
