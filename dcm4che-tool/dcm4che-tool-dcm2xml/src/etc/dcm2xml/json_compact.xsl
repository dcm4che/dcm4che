<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/NativeDicomModel">
        <xsl:text>{</xsl:text>
        <xsl:apply-templates select="DicomAttribute"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="DicomAttribute">
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="@keyword"><xsl:value-of select="@keyword"/></xsl:when>
            <xsl:otherwise>private-<xsl:value-of select="position()"/></xsl:otherwise>
        </xsl:choose>
        <xsl:text>":{</xsl:text>
        <xsl:apply-templates select="@tag"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="@privateCreator"/>
        <xsl:if test="@privateCreator">,</xsl:if>
        <xsl:apply-templates select="@vr"/>
        <xsl:apply-templates select="*"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="@tag|@privateCreator|@vr|@uri|@uuid">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>":"</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="Value|PersonName|Item">
        <xsl:variable name="array" select="last()>1"/>
        <xsl:text>,</xsl:text>
        <xsl:if test="position()=1">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="name()"/>
            <xsl:text>":</xsl:text>
            <xsl:if test="$array">[</xsl:if>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="../@vr='SQ' or ../@vr='PN'">
                <xsl:text>{</xsl:text>
                <xsl:apply-templates select="*"/>
                <xsl:text>}</xsl:text>
            </xsl:when>
            <xsl:when test="../@vr='DS' or ../@vr='FL' or ../@vr='FD' or ../@vr='IS' or ../@vr='SL' or ../@vr='SS' or ./@vr='UL' or ../@vr='US'">
                <xsl:value-of select="text()"/>
            </xsl:when>
            <xsl:otherwise>"<xsl:value-of select="text()"/>"</xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$array and position()=last()">]</xsl:if>
    </xsl:template>

    <xsl:template match="Alphabetic|Ideographic|Phonetic">
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>":{</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="FamilyName|GivenName|MiddleName|NamePrefix|NameSuffix">
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>":"</xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="BulkData">
        <xsl:text>,</xsl:text>
        <xsl:text>"BulkData":{</xsl:text>
        <xsl:apply-templates select="@*"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

</xsl:stylesheet>
