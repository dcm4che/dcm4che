<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="/elements">
    <xsl:text>#
# StandardAttributeNames.properties
#
# This file is generated from Part 6 and Part 7 of the Standard Text.
#
UnknownAttribute=Unknown Attribute
GroupLength=Group Length
PrivateCreator=Private Creator
PrivateAttribute=Private Attribute
</xsl:text>
    <xsl:apply-templates select="element[@keyword!='']" />
  </xsl:template>
  <xsl:template match="element">
    <xsl:value-of select="translate(@tag,'x(,)','0')" />
    <xsl:text>=</xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text>
</xsl:text>
  </xsl:template>
</xsl:stylesheet>