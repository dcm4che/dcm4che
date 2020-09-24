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
   - Hesham Elbadawi <bsdreko@gmail.com>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <xsl:output  indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="/">
    <xsl:apply-templates select="uids"/>
  </xsl:template>
  <xsl:template match="uids">
<xsl:for-each select="uid">
<xsl:value-of select="@keyword" />=<xsl:value-of select="@value" />
<xsl:choose>
<!-- Image Storage Transfer Syntax-->
<xsl:when test="@type='SOPClass' and contains(@keyword,'ImageStorage')">
<xsl:text>&#58;</xsl:text>
<xsl:call-template name="TS" >
<xsl:with-param name="ids" select="/uids/uid[@type='TransferSyntax' and (contains(@keyword, 'JPEG') or contains(@keyword, 'JPIPReferenced') or contains(@keyword, 'JPIPReferencedDeflate')  or contains(@keyword, 'RLELossless')  or contains(@keyword, 'ImplicitVRLittleEndian')  or contains(@keyword, 'ExplicitVRLittleEndian')  or contains(@keyword, 'ExplicitVRBigEndian')  or contains(@keyword, 'DeflatedExplicitVRLittleEndian')   ) and (not(contains(@keyword, 'Retired')) or @keyword='ExplicitVRBigEndian')]">
</xsl:with-param> 
</xsl:call-template>
</xsl:when>
<!-- Video Transfer Syntax-->
<xsl:when test="@type='SOPClass' and contains(@keyword,'Video')">
<xsl:text>&#58;</xsl:text>
<xsl:call-template name="TS" >
<xsl:with-param name="ids" select="/uids/uid[@type='TransferSyntax' and (contains(@keyword, 'MPEG')  or contains(@keyword, 'ImplicitVRLittleEndian')  or contains(@keyword, 'ExplicitVRLittleEndian')  or contains(@keyword, 'ExplicitVRBigEndian')  or contains(@keyword, 'DeflatedExplicitVRLittleEndian')   ) and (not(contains(@keyword, 'Retired')) or @keyword='ExplicitVRBigEndian')]">
</xsl:with-param> 
</xsl:call-template>
</xsl:when>
<!-- Dimse and other Transfer Syntax-->
<xsl:otherwise>
<xsl:text>&#58;</xsl:text>
<xsl:call-template name="TS" >
<xsl:with-param name="ids" select="/uids/uid[@type='TransferSyntax' and ( contains(@keyword, 'ImplicitVRLittleEndian')  or contains(@keyword, 'ExplicitVRLittleEndian')  or contains(@keyword, 'ExplicitVRBigEndian')  or contains(@keyword, 'DeflatedExplicitVRLittleEndian') or @keyword='ExplicitVRBigEndian')]">
</xsl:with-param> 
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:for-each>
  </xsl:template>
<xsl:template name="TS">
<xsl:param name="ids" />
<xsl:for-each select="$ids">
<xsl:value-of select="@value"/>
<xsl:if test="position() != last()">,</xsl:if>
</xsl:for-each>
<xsl:text>&#xA;</xsl:text>
</xsl:template>
</xsl:stylesheet>