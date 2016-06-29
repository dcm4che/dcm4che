<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/AuditMessage">
        <AuditMessage>
            <xsl:apply-templates select="@*|node()"/>
        </AuditMessage>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*/*/@csd-code">
        <xsl:attribute name="code">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="*/*/@originalText">
        <xsl:if test="string-length() &gt; 0">
            <xsl:attribute name="displayName">
                <xsl:value-of select="."/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*/*/@codeSystemName">
        <xsl:if test="string-length() &gt; 0">
            <xsl:attribute name="codeSystemName">
                <xsl:value-of select="."/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
