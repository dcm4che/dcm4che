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
   - Java(TM), hosted at https://github.com/gunterze/dcm4che.
   -
   - The Initial Developer of the Original Code is
   - Agfa Healthcare.
   - Portions created by the Initial Developer are Copyright (C) 2011
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

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" />
  <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
  <xsl:template match="/article">
    <xsl:apply-templates select="//row[(count(entry)=4) and starts-with(entry/para,'1.')]" />
    <xsl:apply-templates select="//row[(count(entry)=3) and starts-with(entry/para,'1.2.840.10008.6.1.') and entry[2]/para!='']" >
      <xsl:with-param name="namepos">3</xsl:with-param>
      <xsl:with-param name="type">Context Group Name</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="row">
    <xsl:param name="namepos">2</xsl:param>
    <xsl:param name="type" select="entry[3]/para" />
    <xsl:variable name="uid" select="entry[1]/para" />
    <xsl:variable name="name">
      <xsl:call-template name="skipAfterColon">
         <xsl:with-param name="name" select="entry[$namepos]/para" />
      </xsl:call-template>
      <xsl:if test="$namepos = 3">
         <xsl:text> (</xsl:text>
         <xsl:value-of select="entry[2]/para" />
         <xsl:text>)</xsl:text>
      </xsl:if>
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