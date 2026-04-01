<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:apply-templates select="NativeDicomModel"/>
    </xsl:template>
    <xsl:template match="NativeDicomModel|Item">
        <xsl:param name="level"></xsl:param>
        <xsl:value-of select="$level"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="DicomAttribute[@tag='0040A010']/Value"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="DicomAttribute[@tag='0040A040']/Value"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="DicomAttribute[@tag='0040A043']/Item/DicomAttribute[@tag='00080104']/Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A050']" mode="ContinuityOfContent"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A504']" mode="ContentTemplateSequence"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A168']" mode="CodeValue"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A120']" mode="Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A121']" mode="Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A122']" mode="Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A123']/PersonName" />
        <xsl:apply-templates select="DicomAttribute[@tag='0040A124']" mode="Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A160']" mode="Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A300']" mode="MeasuredValue"/>
        <xsl:apply-templates select="DicomAttribute[@tag='00081199']" mode="ReferencedSOPSequence"/>
        <xsl:apply-templates select="DicomAttribute[@tag='00700023']" mode="Value"/>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A130']" mode="Value"/>
        <xsl:text>
</xsl:text>
        <xsl:apply-templates select="DicomAttribute[@tag='0040A730']/Item">
            <xsl:with-param name="level" select="concat($level,'>')"/>
         </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="DicomAttribute" mode="ContinuityOfContent">
        <xsl:text> [</xsl:text>
        <xsl:value-of select="Value"/>
        <xsl:text>]</xsl:text>
    </xsl:template>
    <xsl:template match="DicomAttribute" mode="ContentTemplateSequence">
        <xsl:text> (</xsl:text>
        <xsl:value-of select="Item/DicomAttribute[@tag='00080105']/Value"/>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="Item/DicomAttribute[@tag='0040DB00']/Value"/>
        <xsl:text>)</xsl:text>
    </xsl:template>
    <xsl:template match="DicomAttribute" mode="CodeValue">
        <xsl:text> = </xsl:text>
        <xsl:value-of select="Item/DicomAttribute[@tag='00080104']/Value"/>
    </xsl:template>
    <xsl:template match="DicomAttribute" mode="ReferencedSOPSequence">
        <xsl:param name="prefix"> = </xsl:param>
        <xsl:value-of select="$prefix"/>
        <xsl:value-of select="Item/DicomAttribute[@tag='00081155']/Value"/>
        <xsl:text> [</xsl:text>
        <xsl:value-of select="Item/DicomAttribute[@tag='00081150']/Value"/>
        <xsl:text>]</xsl:text>
        <xsl:apply-templates select="Item/DicomAttribute[@tag='00081199']" mode="ReferencedSOPSequence">
            <xsl:with-param name="prefix">, PR: </xsl:with-param>
        </xsl:apply-templates>       
        <xsl:apply-templates select="Item/DicomAttribute[@tag='0008114B']" mode="ReferencedSOPSequence">
            <xsl:with-param name="prefix">, RWV: </xsl:with-param>
        </xsl:apply-templates>       
    </xsl:template>
    <xsl:template match="DicomAttribute" mode="MeasuredValue">
        <xsl:text> = </xsl:text>
        <xsl:value-of select="Item/DicomAttribute[@tag='0040A30A']/Value"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="Item/DicomAttribute[@tag='004008EA']/Item/DicomAttribute[@tag='00080104']/Value"/>
    </xsl:template>
    <xsl:template match="PersonName">
       <xsl:apply-templates select="Alphabetic"/>
       <xsl:apply-templates select="Ideographic"/>
       <xsl:apply-templates select="Phonetic"/>
    </xsl:template>
    <xsl:template match="Alphabetic | Ideographic | Phonetic">
        <xsl:text> = </xsl:text>
        <xsl:value-of select="normalize-space(concat(NamePrefix,' ',GivenName,' ',MiddleName,' ',FamilyName,' ',NameSuffix))"/>
    </xsl:template>
    <xsl:template match="DicomAttribute" mode="Value">
        <xsl:text> = </xsl:text>
        <xsl:value-of select="Value"/>
    </xsl:template>
</xsl:stylesheet>
