<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="no" method="xml"/>
  <xsl:param name="MessageControlID">1</xsl:param>

  <xsl:template match="/hl7">
    <hl7>
      <MSH
        fieldDelimiter="{MSH/@fieldDelimiter}" 
        componentDelimiter="{MSH/@componentDelimiter}"
        repeatDelimiter="{MSH/@repeatDelimiter}"
        escapeDelimiter="{MSH/@escapeDelimiter}" 
        subcomponentDelimiter="{MSH/@subcomponentDelimiter}">
        <field><xsl:value-of select="MSH/field[3]"/></field>
        <field><xsl:value-of select="MSH/field[4]"/></field>
        <field><xsl:value-of select="MSH/field[1]"/></field>
        <field><xsl:value-of select="MSH/field[2]"/></field>
        <field/>
        <field/>
        <field>ACK</field>
        <field><xsl:value-of select="$MessageControlID"/></field>
        <field><xsl:value-of select="MSH/field[9]"/></field>
        <field><xsl:value-of select="MSH/field[10]"/></field>
        <field/>
        <field/>
        <field/>
        <field/>
        <field/>
        <field><xsl:value-of select="MSH/field[16]"/></field>
      </MSH>
      <MSA>
        <field><xsl:value-of select="AA"/></field>
        <field><xsl:value-of select="MSH/field[8]"/></field>
      </MSA>
    </hl7>
  </xsl:template>
</xsl:stylesheet>
