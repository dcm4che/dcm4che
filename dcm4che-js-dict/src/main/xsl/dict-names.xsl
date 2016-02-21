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
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>

  <xsl:template match="/">
    <xsl:text>var DCM4CHE = DCM4CHE || {};
DCM4CHE.elementName = (function (dictionary) {
  var tables = [dictionary],
  forTag = function (tag, privateCreator) {
    var i = tables.length, value;
    if (tag.slice(4) === "0000")
      return "Group Length";
    if ("02468ACE".indexOf(tag.charAt(3)) &lt; 0) {
      if (tag.slice(4,6) === "00")
        return "Private Creator";
      tag = tag.slice(0,4) + "xx" + tag.slice(6);
    } else {
      privateCreator = undefined;
      switch (tag.slice(0,2)) {
      case "10":
        switch (tag.slice(2,4)) {
          case "00":
            if ("012345".indexOf(tag.charAt(7)) >= 0)
              tag = "1000xxx" + tag.slice(7);
            break;
          case "10":
            tag = "1010xxxx";
            break;
        }
        break;
      case "7F":
        if (tag.slice(2,4) === "E0")
          break;
      case "50":
      case "60":
        tag = tag.slice(0,2) + "xx" + tag.slice(4);
      }
    }
    while (i--) {
      if (privateCreator === tables[i].privateCreator) {
        value = tables[i][tag];
        if (value)
          return value;
      }
    }
    return undefined;
  }
  return {
      addDictionary: function (dictionary) {
          tables.push(dictionary);
      },
      forTag:forTag
  }
}({
"privateCreator":undefined</xsl:text>
    <xsl:apply-templates select="//el[@keyword!='']"/>
    <xsl:text>
}));
</xsl:text>
  </xsl:template>  

  <xsl:template match="el">
    <xsl:text>,
"</xsl:text>
    <xsl:value-of select="@tag" />
    <xsl:text>":"</xsl:text>
    <xsl:value-of select="text()"/>
    <xsl:text>"</xsl:text>
  </xsl:template>

</xsl:stylesheet>
