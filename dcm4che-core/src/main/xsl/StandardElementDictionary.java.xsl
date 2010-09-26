<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="/elements">
    <xsl:text>package org.dcm4che.data;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.dcm4che.util.TagUtils;

public class StandardElementDictionary extends ElementDictionary {

    public static final ElementDictionary INSTANCE =
            new StandardElementDictionary();

    private final ResourceBundle attributeNames = ResourceBundle.getBundle(
            "org.dcm4che.data.StandardAttributeNames");

    private StandardElementDictionary() {
        super(null, Tag.class);
    }

    @Override
    public String nameOf(int tag) {
        if ((tag &amp; 0x0000FFFF) == 0) {
            if (tag > 0x00020000)
                return attributeNames.getString("GroupLength");
        } else if ((tag &amp; 0x00010000) != 0)
            return  attributeNames.getString(
                    ((tag &amp; 0x0000FF00) == 0) ? "PrivateCreator"
                                              : "PrivateAttribute");
        else if ((tag &amp; 0xFFFFFF00) == Tag.SourceImageIDs)
            tag = Tag.SourceImageIDs;
        else {
            int tmp = tag &amp; 0xFFE00000;
            if (tmp == 0x50000000 || tmp == 0x60000000)
                tag &amp;= 0xFFE0FFFF;
            else if ((tag &amp; 0xFF000000) == 0x7F000000 
                    &amp;&amp; (tag &amp; 0xFFFF0000) != 0x7FE00000)
                tag &amp;= 0xFF00FFFF;
        }
        try {
            return attributeNames.getString(TagUtils.toString(tag));
        } catch (MissingResourceException e) {
            return attributeNames.getString("UnknownAttribute");
        }
    }

    @Override
    public VR vrOf(int tag) {
        if ((tag &amp; 0x0000FFFF) == 0)
            return VR.UL;
        if ((tag &amp; 0x00010000) != 0)
            return ((tag &amp; 0x0000FF00) == 0) ? VR.LO : VR.UN;
        if ((tag &amp; 0xFFFFFF00) == Tag.SourceImageIDs)
            return VR.CS;
        int tmp = tag &amp; 0xFFE00000;
        if (tmp == 0x50000000 || tmp == 0x60000000)
            tag &amp;= 0xFFE0FFFF;
        else if ((tag &amp; 0xFF000000) == 0x7F000000 
                &amp;&amp; (tag &amp; 0xFFFF0000) != 0x7FE00000)
            tag &amp;= 0xFF00FFFF;
        switch (tag) {</xsl:text>
    <xsl:apply-templates select="element[starts-with(@vr,'AE')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'AS')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'AT')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'CS') and @keyword!='SourceImageIDs']"/>
    <xsl:apply-templates select="element[starts-with(@vr,'DA')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'DS')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'DT')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'FL')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'FD')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'IS')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'LO')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'LT')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'OB')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'OF')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'OW')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'PN')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'SH')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'SL')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'SQ')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'SS')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'ST')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'TM')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'UI')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'UL')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'US')]"/>
    <xsl:apply-templates select="element[starts-with(@vr,'UT')]"/>
    <xsl:apply-templates select="element[@keyword!='' and @vr='']"/>
<xsl:text>
        }
        return VR.UN;
    }
}
</xsl:text>
  </xsl:template>

  <xsl:template match="element">
    <xsl:if test="not(starts-with(@tag,'(0028,04x'))">
      <xsl:text>
        case Tag.</xsl:text>
      <xsl:value-of select="@keyword"/>
      <xsl:text>:</xsl:text>
    </xsl:if>
    <xsl:if test="position()=last()">
      <xsl:choose>
        <xsl:when test="@vr=''">
          <xsl:text>
            return null;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>
            return VR.</xsl:text>
          <xsl:value-of select="substring(@vr,string-length(@vr)-1)"/>
          <xsl:text>;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>