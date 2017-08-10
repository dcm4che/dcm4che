<!--
  ~ Version: MPL 1.1/GPL 2.0/LGPL 2.1
  ~
  ~  The contents of this file are subject to the Mozilla Public License Version
  ~  1.1 (the "License"); you may not use this file except in compliance with
  ~  the License. You may obtain a copy of the License at
  ~  http://www.mozilla.org/MPL/
  ~
  ~  Software distributed under the License is distributed on an "AS IS" basis,
  ~  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  ~  for the specific language governing rights and limitations under the
  ~  License.
  ~
  ~  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  ~  Java(TM), hosted at https://github.com/dcm4che.
  ~
  ~  The Initial Developer of the Original Code is
  ~  J4Care.
  ~  Portions created by the Initial Developer are Copyright (C) 2015-2017
  ~  the Initial Developer. All Rights Reserved.
  ~
  ~  Contributor(s):
  ~  See @authors listed below
  ~
  ~  Alternatively, the contents of this file may be used under the terms of
  ~  either the GNU General Public License Version 2 or later (the "GPL"), or
  ~  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  ~  in which case the provisions of the GPL or the LGPL are applicable instead
  ~  of those above. If you wish to allow use of your version of this file only
  ~  under the terms of either the GPL or the LGPL, and not to allow others to
  ~  use your version of this file under the terms of the MPL, indicate your
  ~  decision by deleting the provisions above and replace them with the notice
  ~  and other provisions required by the GPL or the LGPL. If you do not delete
  ~  the provisions above, a recipient may use your version of this file under
  ~  the terms of any one of the MPL, the GPL or the LGPL.
  ~
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>

  <xsl:template match="/NativeDicomModel">
    <xsl:text>"</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080005']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080020']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080030']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080050']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080054']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080056']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080061']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00080090']" mode="PN"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00100010']" mode="PN"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00100020']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00100021']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00100030']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00100040']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='0020000D']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00200010']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00201206']"/>
    <xsl:text>","</xsl:text>
    <xsl:apply-templates select="DicomAttribute[@tag='00201208']"/>
    <xsl:text>"
</xsl:text>
  </xsl:template>

  <xsl:template match="DicomAttribute">
    <xsl:apply-templates select="Value"/>
  </xsl:template>

  <xsl:template match="DicomAttribute" mode="PN">
    <xsl:apply-templates select="PersonName"/>
  </xsl:template>

  <xsl:template match="Value">
    <xsl:if test="@number != 1">\</xsl:if>
    <xsl:value-of select="text()"/>
  </xsl:template>

  <xsl:template match="PersonName">
    <xsl:if test="@number != 1">\</xsl:if>
    <xsl:apply-templates select="Alphabetic"/>
    <xsl:if test="Ideographic or Phonetic">
      <xsl:text>=</xsl:text>
      <xsl:apply-templates select="Ideographic"/>
      <xsl:if test="Phonetic">
        <xsl:text>=</xsl:text>
        <xsl:apply-templates select="Phonetic"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Alphabetic|Ideographic|Phonetic">
    <xsl:value-of select="FamilyName"/>
    <xsl:if test="GivenName or MiddleName or NamePrefix or NameSuffix">
      <xsl:text>^</xsl:text>
      <xsl:value-of select="GivenName"/>
      <xsl:if test="MiddleName or NamePrefix or NameSuffix">
        <xsl:text>^</xsl:text>
        <xsl:value-of select="MiddleName"/>
        <xsl:if test="NamePrefix or NameSuffix">
          <xsl:text>^</xsl:text>
          <xsl:value-of select="NamePrefix"/>
          <xsl:if test="NameSuffix">
            <xsl:text>^</xsl:text>
            <xsl:value-of select="NameSuffix"/>
          </xsl:if>
        </xsl:if>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>