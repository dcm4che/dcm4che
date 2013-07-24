<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:variable name="indent" select="'  '"/>
    <xsl:template match="/NativeDicomModel">
        <xsl:text>{</xsl:text>
        <xsl:apply-templates select="DicomAttribute">
            <xsl:with-param name="br">
                <xsl:text>&#xA;</xsl:text>
                <xsl:value-of select="$indent"/>
            </xsl:with-param>
        </xsl:apply-templates>
        <xsl:text>&#xA;}&#xA;</xsl:text>
    </xsl:template>

    <xsl:template match="DicomAttribute">
        <xsl:param name="br"/>
        <xsl:variable name="br2" select="concat($br,$indent)"/>
        <xsl:variable name="br3" select="concat(',',$br2)"/>
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:value-of select="$br"/>
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="@keyword"><xsl:value-of select="@keyword"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="@tag"/></xsl:otherwise>
        </xsl:choose>
        <xsl:text>" : {</xsl:text>
        <xsl:apply-templates select="@tag">
            <xsl:with-param name="br" select="$br2"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="@privateCreator">
            <xsl:with-param name="br" select="$br3"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="@vr">
            <xsl:with-param name="br" select="$br3"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="*">
            <xsl:with-param name="br" select="$br2"/>
        </xsl:apply-templates>
        <xsl:value-of select="$br"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="@tag|@privateCreator|@vr|@uri|@uuid">
        <xsl:param name="br"/>
        <xsl:value-of select="$br"/>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>" : "</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="Value|PersonName|Item">
        <xsl:param name="br"/>
        <xsl:variable name="array" select="last()>1"/>
        <xsl:variable name="br2">
            <xsl:value-of select="$br"/>
            <xsl:if test="$array">
                <xsl:value-of select="$indent"/>
            </xsl:if>
        </xsl:variable>
        <xsl:text>,</xsl:text>
        <xsl:choose>
            <xsl:when test="position()=1">
                <xsl:value-of select="$br"/>
                <xsl:text>"</xsl:text>
                <xsl:value-of select="name()"/>
                <xsl:text>" : </xsl:text>
                <xsl:if test="$array">
                    <xsl:value-of select="concat('[',$br2)"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$br2"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="../@vr='SQ' or ../@vr='PN'">
                <xsl:text>{</xsl:text>
                <xsl:apply-templates select="*">
                    <xsl:with-param name="br" select="concat($br2,$indent)"/>
                </xsl:apply-templates>
                <xsl:value-of select="$br2"/>
                <xsl:text>}</xsl:text>
            </xsl:when>
            <xsl:when test="../@vr='DS' or ../@vr='FL' or ../@vr='FD' or ../@vr='IS' or ../@vr='SL' or ../@vr='SS' or ./@vr='UL' or ../@vr='US'">
                <xsl:value-of select="text()"/>
            </xsl:when>
            <xsl:otherwise>"<xsl:value-of select="text()"/>"</xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$array and position()=last()">
            <xsl:value-of select="concat($br,']')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Alphabetic|Ideographic|Phonetic">
        <xsl:param name="br"/>
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:value-of select="$br"/>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>" : {</xsl:text>
        <xsl:apply-templates select="*">
            <xsl:with-param name="br" select="concat($br,$indent)"/>
        </xsl:apply-templates>
        <xsl:value-of select="$br"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="FamilyName|GivenName|MiddleName|NamePrefix|NameSuffix">
        <xsl:param name="br"/>
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:value-of select="$br"/>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>" : "</xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="BulkData">
        <xsl:param name="br"/>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="$br"/>
        <xsl:text>"BulkData" : {</xsl:text>
        <xsl:apply-templates select="@*">
            <xsl:with-param name="br" select="concat($br,$indent)"/>
         </xsl:apply-templates>
        <xsl:value-of select="$br"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

</xsl:stylesheet>
