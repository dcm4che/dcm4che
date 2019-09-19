<?xml version="1.0" encoding="UTF-8"?>
<!-- ***** BEGIN LICENSE BLOCK *****
   - Version: MPL 1.1/GPL 2.0/LGPL 2.1
   -
   - The contents of this file are subject to the Mozilla Public License Version
   - 1.1 (the "License"); you may not use this file except in compliance with
   - the License. You may obtain a copy of the License at
   - http://www.mozilla.org/MPL/
   -
   - Software distributed under the License is distributed on an "AS IS" basis,
   - WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
   - for the specific language governing rights and limitations under the
   - License.
   -
   - The Original Code is part of dcm4che, an implementation of DICOM(TM) in
   - Java(TM), hosted at https://github.com/dcm4che.
   -
   - The Initial Developer of the Original Code is
   - Agfa Healthcare.
   - Portions created by the Initial Developer are Copyright (C) 2011-2014
   - the Initial Developer. All Rights Reserved.
   -
   - Contributor(s):
   - Gunter Zeilinger <gunterze@gmail.com>
   -
   - Alternatively, the contents of this file may be used under the terms of
   - either the GNU General Public License Version 2 or later (the "GPL"), or
   - the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
   - in which case the provisions of the GPL or the LGPL are applicable instead
   - of those above. If you wish to allow use of your version of this file only
   - under the terms of either the GPL or the LGPL, and not to allow others to
   - use your version of this file under the terms of the MPL, indicate your
   - decision by deleting the provisions above and replace them with the notice
   - and other provisions required by the GPL or the LGPL. If you do not delete
   - the provisions above, a recipient may use your version of this file under
   - the terms of any one of the MPL, the GPL or the LGPL.
   -
   - ***** END LICENSE BLOCK *****  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://docbook.org/ns/docbook"
    exclude-result-prefixes="doc">
  <xsl:output method="xml" indent="yes"/>

  <xsl:include href="uidsEx.xsl"/>

  <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
  
  <xsl:template match="/doc:book">
  <uids>
    <xsl:apply-templates select="doc:chapter[12]/doc:table[1]/doc:tbody/doc:tr"/>
    <xsl:call-template name="uidsEx"/>
  </uids>
  </xsl:template>

  <xsl:template match="doc:tr">
    <xsl:element name="uid">
      <xsl:variable name="uid">
        <xsl:call-template name="para2str">
          <xsl:with-param name="para" select="doc:td[1]/doc:para"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:attribute name="value">
        <xsl:value-of select="$uid"/>
      </xsl:attribute>
      <xsl:variable name="name">
        <xsl:choose>
          <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.40'">MR Image Storage Zero Padded (Retired)</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.12.77'">Zeiss OPT File (Retired)</xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="skipAfterColon">
              <xsl:with-param name="name">
                <xsl:call-template name="para2str">
                  <xsl:with-param name="para" select="doc:td[2]/doc:para"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:attribute name="keyword">
        <xsl:choose>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.50'">JPEGBaseline1</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.51'">JPEGExtended24</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.52'">JPEGExtended35Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.53'">JPEGSpectralSelectionNonHierarchical68Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.54'">JPEGSpectralSelectionNonHierarchical79Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.55'">JPEGFullProgressionNonHierarchical1012Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.56'">JPEGFullProgressionNonHierarchical1113Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.57'">JPEGLosslessNonHierarchical14</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.58'">JPEGLosslessNonHierarchical15Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.59'">JPEGExtendedHierarchical1618Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.60'">JPEGExtendedHierarchical1719Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.61'">JPEGSpectralSelectionHierarchical2022Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.62'">JPEGSpectralSelectionHierarchical2123Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.63'">JPEGFullProgressionHierarchical2426Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.64'">JPEGFullProgressionHierarchical2527Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.65'">JPEGLosslessHierarchical28Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.66'">JPEGLosslessHierarchical29Retired</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.70'">JPEGLossless</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.80'">JPEGLSLossless</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.81'">JPEGLSLossyNearLossless</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.90'">JPEG2000LosslessOnly</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.91'">JPEG2000</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.92'">JPEG2000Part2MultiComponentLosslessOnly</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.93'">JPEG2000Part2MultiComponent</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.100'">MPEG2</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.1.2.4.101'">MPEG2MainProfileHighLevel</xsl:when>
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
      </xsl:attribute>
      <xsl:attribute name="type">
        <xsl:call-template name="removeSpaces">
          <xsl:with-param name="name">
            <xsl:call-template name="replaceNonAlpha">
              <xsl:with-param name="name">
                <xsl:call-template name="para2str">
                  <xsl:with-param name="para" select="doc:td[3]/doc:para"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:value-of select="$name"/>
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
    <xsl:value-of select="translate($str,'&#x200b;&#xad;','')"/>
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
    <xsl:value-of select="normalize-space(translate($name,'-,.@/()&amp;®','        '))"/>
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