<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" />
  <xsl:param name="MessageControlID">1</xsl:param>
  <xsl:param name="DateTimeOfMessage">20120305101010.101</xsl:param>
  <xsl:param name="AcknowledgementCode">AA</xsl:param>
  <xsl:param name="TextMessage" />
  <xsl:param name="QueryResponseStatus">OK</xsl:param>
  <xsl:param name="pid">PIX-4711^^^DCM4CHE-TEST&amp;1.2.40.0.13.1.1.999&amp;ISO</xsl:param>
  <xsl:param name="pid2"/>

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
        <field>RSP<component>K23</component><component>RSP_K23</component></field>
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
      <QAK>
        <field><xsl:value-of select="QPD/field[2]" /></field>
        <field><xsl:value-of select="$QueryResponseStatus" /></field>
      </QAK>
      <xsl:copy-of select="QPD" />
      <xsl:if test="$QueryResponseStatus = 'OK'">
        <PID>
          <field/>
          <field/>
          <field>
            <xsl:call-template name="pid">
              <xsl:with-param name="cx" select="$pid" />
            </xsl:call-template>
            <xsl:if test="$pid2">
              <repeat>
                <xsl:call-template name="pid">
                  <xsl:with-param name="cx" select="$pid2" />
                </xsl:call-template>
              </repeat>
            </xsl:if>
          </field>
        </PID>
      </xsl:if>
    </hl7>
  </xsl:template>

  <xsl:template name="pid">
    <xsl:param name="cx"/>
    <xsl:value-of select="substring-before($cx,'^^^')" />
    <component/>
    <component/>
    <component>
      <xsl:value-of select="substring-before(substring-after($cx,'^^^'),'&amp;')" />
      <subcomponent>
        <xsl:value-of select="substring-before(substring-after($cx,'&amp;'),'&amp;')" />
      </subcomponent>
      <subcomponent>
        <xsl:value-of select="substring-after(substring-after($cx,'&amp;'),'&amp;')" />
      </subcomponent>
    </component>
  </xsl:template>
</xsl:stylesheet>
