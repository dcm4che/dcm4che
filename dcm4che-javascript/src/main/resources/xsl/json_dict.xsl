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

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="elements">
      <xsl:text>/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK *****
 * This file is generated from Part 6 and 7 of the Standard Text Edition 2011.
 */
 
 /**
 * @author Gunter Zeilinger &lt;gunterze@gmail.com&gt;
 * @author Hesham Elbadawi  &lt;bsdreko@gmail.com&gt;
 */
 
 </xsl:text>
 
var dictionary = [{"CREATOR": "standard", "DICT":{
  <xsl:apply-templates select="element"/>
  <xsl-text disable-output-escaping="yes">
//add Private Dictionary
function addDict(dictJSON, privateCreator)
{
dictionary.push({"CREATOR" : privateCreator, "DICT":JSON.parse(dictJSON)});
}

function keywordOf(tag, privateCreator)
{
    if(privateCreator==undefined || privateCreator == null)
    return dictionary["standard"].DICT[tag].KEYWORD;

    for(var i=0 ; i&lt;dictionary.length;i++)
    {
        if(dictionary[i].CREATOR == privateCreator)
        if(dictionary[i].DICT[tag]!=undefined)
        return dictionary[i].DICT[tag].KEYWORD;
    }
    return dictionary[0].DICT[tag].KEYWORD;
}

function tagOf(keyword, privateCreator)
{
    if(privateCreator==undefined || privateCreator == null)
        privateCreator = "standard";

    for(var i=0 ; i&lt;dictionary.length;i++)
    {
        if(dictionary[i].CREATOR == privateCreator)
            for (name in dictionary[i].DICT) {
                if (dictionary[i].DICT[name].KEYWORD == keyword)
                    return name;
            }
            }

    return tagOf(keyword);
}

//default
function tagOf(keyword) {
    for (name in dictionary[0].DICT) {
        if (dictionary[0].DICT[name].KEYWORD == keyword)
            return name;
    }
}

function getDictSize(index)
{
    var cnt=0;
    for(name in dictionary[index].DICT)
    {
        cnt++;
    }
    return cnt;
}
</xsl-text>
  </xsl:template>  
  <xsl:template match="element">
    <xsl:text>"</xsl:text>
    <xsl:value-of select="@tag" />
    <xsl:text>": { </xsl:text>
    <xsl:text> "VR": "</xsl:text>
    <xsl:value-of select="@vr" />
    <xsl:text>" , </xsl:text>
    <xsl:text> "VM": "</xsl:text>
    <xsl:value-of select="@vm" />
    <xsl:text>" , </xsl:text>
    <xsl:text> "KEYWORD": "</xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text>" }</xsl:text>
    <xsl:choose>
    <xsl:when test="position()=last()">
    <xsl:text>}}];</xsl:text>
    </xsl:when>
    <xsl:otherwise>
    <xsl:text> , </xsl:text>
    </xsl:otherwise>
</xsl:choose>
<xsl:text>
</xsl:text>
  </xsl:template>
</xsl:stylesheet>
