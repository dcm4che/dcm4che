<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" />
  <xsl:param name="MessageControlID">1</xsl:param>
  <xsl:param name="DateTimeOfMessage">20120305101010.101</xsl:param>
  <xsl:param name="AcknowledgementCode">AA</xsl:param>
  <xsl:param name="TextMessage" />
  <xsl:param name="QueryResponseStatus">OK</xsl:param>
  <xsl:param name="pid">PDQ-4711^^^DCM4CHE-TEST&amp;1.2.40.0.13.1.1.999&amp;ISO</xsl:param>
  <xsl:param name="FamilyName">DOE</xsl:param>
  <xsl:param name="GivenName">JOHN</xsl:param>
  <xsl:param name="DOB">19471111</xsl:param>
  <xsl:param name="Sex">M</xsl:param>
  <xsl:param name="Street">STREET</xsl:param>
  <xsl:param name="City">CITY</xsl:param>
  <xsl:param name="Zip">4711</xsl:param>
  <xsl:param name="AccountNumber">ACC-4711^^^DCM4CHE-TEST&amp;1.2.40.0.13.1.1.999&amp;ISO</xsl:param>
  <xsl:param name="pid2"/>
  <xsl:param name="FamilyName2"/>
  <xsl:param name="GivenName2"/>
  <xsl:param name="DOB2"/>
  <xsl:param name="Sex2"/>
  <xsl:param name="Street2"/>
  <xsl:param name="City2"/>
  <xsl:param name="Zip2"/>
  <xsl:param name="AccountNumber2"/>

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
        <field>RSP<component>K22</component><component>RSP_K21</component></field>
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
        <xsl:call-template name="pid">
          <xsl:with-param name="pid" select="$pid" />
          <xsl:with-param name="fname" select="$FamilyName" />
          <xsl:with-param name="gname" select="$GivenName" />
          <xsl:with-param name="dob" select="$DOB" />
          <xsl:with-param name="sex" select="$Sex" />
          <xsl:with-param name="street" select="$Street" />
          <xsl:with-param name="city" select="$City" />
          <xsl:with-param name="zip" select="$Zip" />
          <xsl:with-param name="acc" select="$AccountNumber" />
        </xsl:call-template>
        <xsl:if test="$pid2 and $FamilyName2">
          <xsl:call-template name="pid">
            <xsl:with-param name="pid" select="$pid2"/>
            <xsl:with-param name="fname" select="$FamilyName2" />
            <xsl:with-param name="gname" select="$GivenName2" />
            <xsl:with-param name="dob" select="$DOB2" />
            <xsl:with-param name="sex" select="$Sex2" />
            <xsl:with-param name="street" select="$Street2" />
            <xsl:with-param name="city" select="$City2" />
            <xsl:with-param name="zip" select="$Zip2" />
            <xsl:with-param name="acc" select="$AccountNumber2" />
          </xsl:call-template>
        </xsl:if>
      </xsl:if>
    </hl7>
  </xsl:template>

  <xsl:template name="pid">
    <xsl:param name="pid"/>
    <xsl:param name="fname"/>
    <xsl:param name="gname"/>
    <xsl:param name="dob"/>
    <xsl:param name="sex"/>
    <xsl:param name="street"/>
    <xsl:param name="city"/>
    <xsl:param name="zip"/>
    <xsl:param name="acc"/>
    <PID>
      <field/>
      <field/>
      <field>
        <xsl:call-template name="cx">
          <xsl:with-param name="cx" select="$pid" />
        </xsl:call-template>
      </field>
      <field/>
      <field>
        <xsl:value-of select="$fname" />
        <component>
          <xsl:value-of select="$gname" />
        </component>
      </field>
      <field/>
      <field>
        <xsl:value-of select="$dob" />
      </field>
      <field>
        <xsl:value-of select="$sex" />
      </field>
      <field/>
      <field/>
      <field>
        <xsl:value-of select="$street" />
        <component/>
        <component>
          <xsl:value-of select="$city" />
        </component>
        <component/>
        <component>
          <xsl:value-of select="$zip" />
        </component>
      </field>
      <field/>
      <field/>
      <field/>
      <field/>
      <field/>
      <field/>
      <field>
        <xsl:if test="$acc">
          <xsl:call-template name="cx">
            <xsl:with-param name="cx" select="$acc"/>
          </xsl:call-template>
        </xsl:if>
      </field>
    </PID>
  </xsl:template>

  <xsl:template name="cx">
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
