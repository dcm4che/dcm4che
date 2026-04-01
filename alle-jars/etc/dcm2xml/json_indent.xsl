<?xml version="1.1" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>

    <xsl:variable name="HEX0">0123456769ABCDEF</xsl:variable>
    <xsl:variable name="HEX1">123456769ABCDEF0</xsl:variable>

    <xsl:template match="/NativeDicomModel">
        <xsl:text>{</xsl:text>
        <xsl:apply-templates select="DicomAttribute">
            <xsl:with-param name="br">
                <xsl:text>&#xA; </xsl:text>
            </xsl:with-param>
        </xsl:apply-templates>
        <xsl:text>&#xA;}&#xA;</xsl:text>
    </xsl:template>

    <xsl:template match="DicomAttribute">
        <xsl:param name="br"/>
        <xsl:variable name="br2" select="concat($br,' ')"/>
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:value-of select="$br"/>
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="@privateCreator">
                <xsl:variable name="preceding" select="preceding-sibling::*[1]"/>  
                <xsl:variable name="gggg" select="substring(@tag,1,4)"/>
                <xsl:variable name="tag">
                    <xsl:call-template name="unifyPrivateTag">
                        <xsl:with-param name="gggg" select="$gggg"/>
                        <xsl:with-param name="xx" select="10"/>
                        <xsl:with-param name="ee" select="substring(@tag,7,2)"/>
                        <xsl:with-param name="creator" select="@privateCreator"/>
                        <xsl:with-param name="preceding" select="$preceding"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="substring($preceding/@tag,1,4)!=$gggg or $preceding/@privateCreator!=@privateCreator">
                    <xsl:value-of select="concat($gggg,'00',substring($tag,5,2))"/>
                    <xsl:text>" : {</xsl:text>
                    <xsl:value-of select="$br2"/>
                    <xsl:text>"vr" : "LO",</xsl:text>
                    <xsl:value-of select="$br2"/>
                    <xsl:text>"Value" : [</xsl:text>
                    <xsl:value-of select="concat($br2,' ')"/>
                    <xsl:text>"</xsl:text>
                    <xsl:value-of select="@privateCreator"/>
                    <xsl:text>"</xsl:text>
                    <xsl:value-of select="concat($br2,']',$br,'},',$br)"/>
                    <xsl:text>"</xsl:text>     
                </xsl:if>
                <xsl:value-of select="$tag"/>
           </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@tag"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>" : {</xsl:text>
        <xsl:value-of select="$br2"/>
        <xsl:text>"vr" : "</xsl:text>
        <xsl:value-of select="@vr"/>
        <xsl:text>"</xsl:text>
        <xsl:apply-templates select="*">
            <xsl:with-param name="br" select="$br2"/>
        </xsl:apply-templates>
        <xsl:value-of select="$br"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template name="unifyPrivateTag">
        <xsl:param name="gggg"/>
        <xsl:param name="xx"/>
        <xsl:param name="ee"/>
        <xsl:param name="creator"/>
        <xsl:param name="preceding"/>
        <xsl:choose>
            <xsl:when test="substring($preceding/@tag,1,4)=$gggg">
                <xsl:variable name="precedingCreator" select="$preceding/@privateCreator"/>
                <xsl:call-template name="unifyPrivateTag">
                    <xsl:with-param name="gggg" select="$gggg"/>
                    <xsl:with-param name="xx">
                        <xsl:choose>
                            <xsl:when test="$precedingCreator=$creator">
                                <xsl:value-of select="$xx"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="substring($xx,2,1)='F'">
                                       <xsl:value-of select="translate($xx, $HEX0, $HEX1)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="substring($xx,1,1)"/>
                                        <xsl:value-of select="translate(substring($xx,2,1), $HEX0, $HEX1)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="ee" select="$ee"/>
                    <xsl:with-param name="creator" select="$precedingCreator"/>
                    <xsl:with-param name="preceding" select="$preceding/preceding-sibling::*[1]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($gggg,$xx,$ee)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="Value">
        <xsl:param name="br"/>
        <xsl:text>,</xsl:text>
        <xsl:if test="position()=1">
            <xsl:value-of select="$br"/>
            <xsl:text>"Value" : [</xsl:text>
        </xsl:if>
        <xsl:value-of select="concat($br,' ')"/>
        <xsl:choose>
            <xsl:when test="not(text())">
                <xsl:text>null</xsl:text>
            </xsl:when>
            <xsl:when test="../@vr='DS' or ../@vr='FL' or ../@vr='FD' or ../@vr='IS' or ../@vr='SL' or ../@vr='SS' or ../@vr='UL' or ../@vr='US'">
                 <xsl:call-template name="number">
                    <xsl:with-param name="text" select="text()"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>"</xsl:text>
                <xsl:call-template name="escape">
                    <xsl:with-param name="text" select="text()"/>
                </xsl:call-template>
                <xsl:text>"</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="position()=last()">
            <xsl:value-of select="concat($br,']')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="number">
        <xsl:param name="text"/>
        <xsl:variable name="ch1" select="substring($text,1,1)"/>
        <xsl:if test="$ch1='-'">-</xsl:if>
        <xsl:call-template name="skipLeadingZeros">
            <xsl:with-param name="text">
                <xsl:choose>
                    <xsl:when test="$ch1='-' or $ch1='+'">
                        <xsl:value-of select="substring($text,2)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$text"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="skipLeadingZeros">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="substring($text,1,1)='0' and string-length($text)>1 and substring($text,2,1)!='.'">
                <xsl:call-template name="skipLeadingZeros">
                    <xsl:with-param name="text" select="substring($text,2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
 
    <xsl:template name="escape">
        <xsl:param name="text"/>
        <xsl:call-template name="replaceAll">
            <xsl:with-param name="text">
            <xsl:call-template name="replaceAll">
                <xsl:with-param name="text">
                    <xsl:call-template name="replaceAll">
                        <xsl:with-param name="text">
                            <xsl:call-template name="replaceAll">
                                <xsl:with-param name="text">
                                    <xsl:call-template name="replaceAll">
                                        <xsl:with-param name="text">
                                            <xsl:call-template name="replaceAll">
                                                <xsl:with-param name="text" select="$text"/>
                                                <xsl:with-param name="replace">\</xsl:with-param>
                                                <xsl:with-param name="by">\\</xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:with-param>
                                        <xsl:with-param name="replace">"</xsl:with-param>
                                        <xsl:with-param name="by">\"</xsl:with-param>
                                    </xsl:call-template>
                                </xsl:with-param>
                                <xsl:with-param name="replace" select="'&#xA;'"/>
                                <xsl:with-param name="by" select="'\n'"/>
                            </xsl:call-template>
                        </xsl:with-param>
                        <xsl:with-param name="replace" select="'&#xC;'"/>
                        <xsl:with-param name="by" select="'\f'"/>
                    </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="replace" select="'&#xD;'"/>
                <xsl:with-param name="by" select="'\r'"/>
             </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="replace" select="'&#x1B;'"/>
            <xsl:with-param name="by" select="'\u001B'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="replaceAll">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="by"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)" />
                <xsl:value-of select="$by" />
                <xsl:call-template name="replaceAll">
                    <xsl:with-param name="text" select="substring-after($text,$replace)" />
                    <xsl:with-param name="replace" select="$replace" />
                    <xsl:with-param name="by" select="$by" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="InlineBinary">
        <xsl:param name="br"/>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="$br"/>
        <xsl:text>"InlineBinary" : "</xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="InlineBinary" mode="DataFragment">
        <xsl:param name="br"/>
        <xsl:value-of select="$br"/>
        <xsl:text>"InlineBinary" : "</xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="PersonName">
        <xsl:param name="br"/>
        <xsl:variable name="br2" select="concat($br,' ')"/>
        <xsl:text>,</xsl:text>
        <xsl:if test="position()=1">
            <xsl:value-of select="$br"/>
            <xsl:text>"Value" : [</xsl:text>
        </xsl:if>
        <xsl:value-of select="$br2"/>
        <xsl:text>{</xsl:text>
        <xsl:apply-templates select="*">
            <xsl:with-param name="br" select="concat($br2,' ')"/>
        </xsl:apply-templates>
        <xsl:value-of select="$br2"/>
        <xsl:text>}</xsl:text>
        <xsl:if test="position()=last()">
            <xsl:value-of select="concat($br,']')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Alphabetic|Ideographic|Phonetic">
        <xsl:param name="br"/>
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:value-of select="$br"/>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>" : "</xsl:text>
        <xsl:call-template name="makePN">
            <xsl:with-param name="familyName" select="FamilyName"/>
            <xsl:with-param name="givenName" select="GivenName"/>
            <xsl:with-param name="middleName" select="MiddleName"/>
            <xsl:with-param name="namePrefix" select="NamePrefix"/>
            <xsl:with-param name="nameSuffix" select="NameSuffix"/>
        </xsl:call-template>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template name="makePN">
        <xsl:param name="familyName"/>
        <xsl:param name="givenName"/>
        <xsl:param name="middleName"/>
        <xsl:param name="namePrefix"/>
        <xsl:param name="nameSuffix"/>
        <xsl:value-of select="$familyName"/>
        <xsl:if test="$givenName or $middleName or $namePrefix or $nameSuffix">
            <xsl:value-of select="concat('^',$givenName)"/>
            <xsl:if test="$middleName or $namePrefix or $nameSuffix">
                <xsl:value-of select="concat('^',$middleName)"/>
                <xsl:if test="$namePrefix or $nameSuffix">
                    <xsl:value-of select="concat('^',$namePrefix)"/>
                    <xsl:if test="$nameSuffix">
                        <xsl:value-of select="concat('^',$nameSuffix)"/>
                    </xsl:if>
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Item">
        <xsl:param name="br"/>
        <xsl:variable name="br2" select="concat($br,' ')"/>
        <xsl:text>,</xsl:text>
        <xsl:if test="position()=1">
            <xsl:value-of select="$br"/>
            <xsl:text>"Value" : [</xsl:text>
        </xsl:if>
        <xsl:value-of select="$br2"/>
        <xsl:text>{</xsl:text>
        <xsl:apply-templates select="*">
            <xsl:with-param name="br" select="concat($br2,' ')"/>
        </xsl:apply-templates>
        <xsl:value-of select="$br2"/>
        <xsl:text>}</xsl:text>
        <xsl:if test="position()=last()">
            <xsl:value-of select="concat($br,']')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="DataFragment">
        <xsl:param name="br"/>
        <xsl:variable name="br2" select="concat($br,' ')"/>
        <xsl:text>,</xsl:text>
        <xsl:if test="position()=1">
            <xsl:value-of select="$br"/>
            <xsl:text>"DataFragment" : [</xsl:text>
        </xsl:if>
        <xsl:value-of select="$br2"/>
        <xsl:text>{</xsl:text>
        <xsl:apply-templates select="*" mode="DataFragment">
            <xsl:with-param name="br" select="concat($br2,' ')"/>
        </xsl:apply-templates>
        <xsl:value-of select="$br2"/>
        <xsl:text>}</xsl:text>
        <xsl:if test="position()=last()">
            <xsl:value-of select="concat($br,']')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="BulkData">
        <xsl:param name="br"/>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="$br"/>
        <xsl:text>"BulkDataURI" : "</xsl:text>
        <xsl:value-of select="@uri"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="BulkData" mode="DataFragment">
        <xsl:param name="br"/>
        <xsl:value-of select="$br"/>
        <xsl:text>"BulkDataURI" : "</xsl:text>
        <xsl:value-of select="@uri"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

</xsl:stylesheet>
