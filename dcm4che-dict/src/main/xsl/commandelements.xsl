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

  <xsl:template match="/doc:book">
  <commandelements>
    <xsl:apply-templates select="doc:chapter[17]/doc:section/doc:table/doc:tbody/doc:tr">
      <xsl:sort select="doc:td[1]/doc:para"/>
    </xsl:apply-templates>
  </commandelements>
  </xsl:template>

  <xsl:template match="doc:tr">
    <xsl:element name="el">
      <xsl:attribute name="tag">
        <xsl:value-of select="translate(doc:td[1]/doc:para,'(,)','')"/>
      </xsl:attribute>
      <xsl:attribute name="keyword">
        <xsl:value-of select="doc:td[3]/doc:para"/>
      </xsl:attribute>
      <xsl:attribute name="vr">
        <xsl:value-of select="doc:td[4]/doc:para"/>
      </xsl:attribute>
      <xsl:attribute name="vm">
        <xsl:value-of select="doc:td[5]/doc:para"/>
      </xsl:attribute>
      <xsl:if test="not(doc:td[6])">
        <xsl:attribute name="retired">true</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="doc:td[2]/doc:para"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>