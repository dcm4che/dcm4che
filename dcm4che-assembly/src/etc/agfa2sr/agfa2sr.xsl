<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:agfa="http://www.agfa.com/hc"
                exclude-result-prefixes="agfa"
                version="1.0">
    <xsl:output xmlns:xsl="http://www.w3.org/1999/XSL/Transform" method="xml"/>
    <xsl:param name="langCodeValue"/>
    <xsl:param name="langCodingSchemeDesignator"/>
    <xsl:param name="langCodeMeaning"/>
    <xsl:param name="docTitleCodeValue"/>
    <xsl:param name="docTitleCodingSchemeDesignator"/>
    <xsl:param name="docTitleCodeMeaning"/>
    <xsl:param name="VerifyingOrganization"/>
    <xsl:param name="Manufacturer"/>
    <xsl:param name="SpecificCharacterSet"/>
    <xsl:param name="SeriesNumber">0</xsl:param>
    <xsl:param name="InstanceNumber">1</xsl:param>
    <xsl:param name="SeriesInstanceUID"/>
    <xsl:param name="SOPClassUID">1.2.840.10008.5.1.4.1.1.88.11</xsl:param>
    <xsl:param name="SOPInstanceUID"/>
    <xsl:variable name="ABC">AÄBCDEFGHIJKLMNOÖÕPQRSTUÜVWXYZ</xsl:variable>
    <xsl:variable name="abc">aäbcdefghijklmnoöõpqrstuüvwxyz</xsl:variable>

    <xsl:template match="/agfa:DiagnosticRadiologyReport">
        <xsl:variable name="accNo" select="OrderDetails/AccessionNumber"/>
        <xsl:variable name="referringPhysicianName" select="OrderDetails/ReferringPhysician/Name"/>
        <xsl:variable name="studyIUID" select="OrderDetails/StudyDetails/StudyInstanceUID"/>
        <xsl:variable name="studyDesc" select="translate(OrderDetails/StudyDetails/StudyDescription, ' ', ' ')"/>
        <xsl:variable name="reasonFor" select="translate(OrderDetails/StudyDetails/ReasonForStudy,' -',' ')"/>
        <NativeDicomModel>
            <xsl:if test="$SpecificCharacterSet">
                <!-- Specific Character Set -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'00080005'"/>
                    <xsl:with-param name="vr" select="'CS'"/>
                    <xsl:with-param name="val" select="$SpecificCharacterSet"/>
                </xsl:call-template>
            </xsl:if>
            <!-- SOP Class UID -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00080016'"/>
                <xsl:with-param name="vr" select="'UI'"/>
                <xsl:with-param name="val" select="'1.2.840.10008.5.1.4.1.1.88.11'"/>
            </xsl:call-template>
            <xsl:if test="$SOPInstanceUID">
                <!-- SOP Instance UID -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'00080018'"/>
                    <xsl:with-param name="vr" select="'UI'"/>
                    <xsl:with-param name="val" select="$SOPInstanceUID"/>
                </xsl:call-template>
            </xsl:if>
            <!-- Accession Number -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00080050'"/>
                <xsl:with-param name="vr" select="'SH'"/>
                <xsl:with-param name="val" select="$accNo"/>
            </xsl:call-template>
            <!-- Modality -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00080060'"/>
                <xsl:with-param name="vr" select="'CS'"/>
                <xsl:with-param name="val" select="'SR'"/>
            </xsl:call-template>
            <!-- Manufacturer -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00080070'"/>
                <xsl:with-param name="vr" select="'LO'"/>
                <xsl:with-param name="val" select="$Manufacturer"/>
            </xsl:call-template>
            <!-- Referring Physician's Name -->
            <xsl:call-template name="pnAttrs">
                <xsl:with-param name="tag" select="'00080090'"/>
                <xsl:with-param name="val" select="$referringPhysicianName"/>
            </xsl:call-template>
            <!-- Study Description -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00081030'"/>
                <xsl:with-param name="vr" select="'LO'"/>
                <xsl:with-param name="val" select="$studyDesc"/>
            </xsl:call-template>
            <!-- Referenced Performed Procedure Step Sequence -->
            <DicomAttribute tag="00081111" vr="SQ"/>
            <!-- Patient's Name -->
            <xsl:call-template name="pnAttrs">
                <xsl:with-param name="tag" select="'00100010'"/>
                <xsl:with-param name="val" select="PatientDetails/Name"/>
            </xsl:call-template>
            <!-- Patient ID -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00100020'"/>
                <xsl:with-param name="vr" select="'LO'"/>
                <xsl:with-param name="val" select="PatientDetails/Id/IdText"/>
            </xsl:call-template>
            <!-- Issuer of Patient ID -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00100021'"/>
                <xsl:with-param name="vr" select="'LO'"/>
                <xsl:with-param name="val" select="PatientDetails/Id/IdDomain"/>
            </xsl:call-template>
            <!-- Patient's Birth Date -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00100030'"/>
                <xsl:with-param name="vr" select="'DA'"/>
                <xsl:with-param name="val" select="translate(PatientDetails/BirthDate, '-', '')"/>
            </xsl:call-template>
            <!-- Patient's Sex -->
            <xsl:variable name="patSex1" select="substring(PatientDetails/Sex, 1, 1)"/>
            <xsl:choose>
                <xsl:when test="$patSex1 = 'M' or $patSex1 = 'F'">
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'00100040'"/>
                        <xsl:with-param name="vr" select="'CS'"/>
                        <xsl:with-param name="val" select="$patSex1"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <DicomAttribute tag="00100040" vr="CS"/>
                </xsl:otherwise>
            </xsl:choose>
            <!-- Study Instance UID -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'0020000D'"/>
                <xsl:with-param name="vr" select="'UI'"/>
                <xsl:with-param name="val" select="$studyIUID"/>
            </xsl:call-template>
            <xsl:if test="$SeriesInstanceUID">
                <!-- Series Instance UID -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'0020000E'"/>
                    <xsl:with-param name="vr" select="'UI'"/>
                    <xsl:with-param name="val" select="$SeriesInstanceUID"/>
                </xsl:call-template>
            </xsl:if>
            <!-- Study ID -->
            <DicomAttribute tag="00200010" vr="SH"/>
            <!-- Series Number -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00200011'"/>
                <xsl:with-param name="vr" select="'IS'"/>
                <xsl:with-param name="val" select="$SeriesNumber"/>
            </xsl:call-template>
            <!-- Instance Number -->
            <xsl:call-template name="attr">
                <xsl:with-param name="tag" select="'00200013'"/>
                <xsl:with-param name="vr" select="'IS'"/>
                <xsl:with-param name="val" select="$InstanceNumber"/>
            </xsl:call-template>
            <!-- Performed Procedure Code Sequence -->
            <DicomAttribute tag="0040A372" vr="SQ"/>
            <xsl:call-template name="requestSeq">
                <xsl:with-param name="accNo" select="$accNo"/>
                <xsl:with-param name="referringPhysicianName" select="$referringPhysicianName"/>
                <xsl:with-param name="studyIUID" select="$studyIUID"/>
                <xsl:with-param name="studyDesc" select="$studyDesc"/>
                <xsl:with-param name="reasonFor" select="$reasonFor"/>
            </xsl:call-template>
            <xsl:call-template name="contentTemplateSq"/>
            <xsl:call-template name="codeItem">
                <xsl:with-param name="sqtag" select="'0040A043'"/>
                <xsl:with-param name="code" select="$docTitleCodeValue"/>
                <xsl:with-param name="scheme" select="$docTitleCodingSchemeDesignator"/>
                <xsl:with-param name="meaning" select="$docTitleCodeMeaning"/>
            </xsl:call-template>
            <xsl:call-template name="containerValueType"/>
            <xsl:call-template name="continuityOfContent"/>
            <xsl:apply-templates select="ReportDetails"/>
            <xsl:call-template name="contentSeq">
            </xsl:call-template>
        </NativeDicomModel>
    </xsl:template>

    <xsl:template name="contentTemplateSq">
        <DicomAttribute tag="0040A504" vr="SQ">
            <Item number="1">
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'00080105'"/>
                    <xsl:with-param name="vr" select="'CS'"/>
                    <xsl:with-param name="val" select="'DCMR'"/>
                </xsl:call-template>
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'0040DB00'"/>
                    <xsl:with-param name="vr" select="'CS'"/>
                    <xsl:with-param name="val" select="'2000'"/>
                </xsl:call-template>
            </Item>
        </DicomAttribute>
    </xsl:template>

    <xsl:template name="requestSeq">
        <xsl:param name="accNo"/>
        <xsl:param name="referringPhysicianName"/>
        <xsl:param name="studyIUID"/>
        <xsl:param name="studyDesc"/>
        <xsl:param name="reasonFor"/>
        <!-- Referenced Request Sequence -->
        <DicomAttribute tag="0040A370" vr="SQ">
            <Item number="1">
                <!-- Accession Number -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'00080050'"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="val" select="$accNo"/>
                </xsl:call-template>
                <!-- Referring Physician's Name -->
                <xsl:call-template name="pnAttrs">
                    <xsl:with-param name="tag" select="'00080090'"/>
                    <xsl:with-param name="val" select="$referringPhysicianName"/>
                </xsl:call-template>
                <!-- Referenced Study Sequence -->
                <DicomAttribute tag="00081110" vr="SQ"/>
                <!-- Study Instance UID -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'0020000D'"/>
                    <xsl:with-param name="vr" select="'UI'"/>
                    <xsl:with-param name="val" select="$studyIUID"/>
                </xsl:call-template>
                <!-- Requested Procedure Description -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="tag" select="'00321060'"/>
                    <xsl:with-param name="vr" select="'LO'"/>
                    <xsl:with-param name="val" select="$studyDesc"/>
                </xsl:call-template>
                <!-- Requested Procedure Sequence -->
                <DicomAttribute tag="00321064" vr="SQ"/>
                <!-- Requested Procedure ID -->
                <DicomAttribute tag="00401001" vr="SH"/>
                <!-- Reason for the Requested Procedure -->
                <xsl:if test="$reasonFor">
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'00401002'"/>
                        <xsl:with-param name="vr" select="'LO'"/>
                        <xsl:with-param name="val" select="$reasonFor"/>
                    </xsl:call-template>
                </xsl:if>
                <!-- Placer Order Number / Imaging Service Request -->
                <DicomAttribute tag="00402016" vr="LO"/>
                <!-- Filler Order Number / Imaging Service Request -->
                <DicomAttribute tag="00402017" vr="LO"/>
            </Item>
        </DicomAttribute>
    </xsl:template>

    <xsl:template match="ReportDetails">
        <xsl:variable name="resultStatus" select="ReportStatus"/>
        <!-- Completion Flag -->
        <xsl:variable name="completionFlag">
            <xsl:choose>
                <xsl:when test="$resultStatus='Finalized'">COMPLETE</xsl:when>
                <xsl:otherwise>PARTIAL</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'0040A491'"/>
            <xsl:with-param name="vr" select="'CS'"/>
            <xsl:with-param name="val" select="$completionFlag"/>
        </xsl:call-template>
        <!-- Verification Flag -->
        <xsl:variable name="verifyingObserver" select="InterpretationApprover"/>
        <xsl:variable name="interpretationRecordDate" select="translate(InterpretationRecordDate, '-', '')"/>
        <xsl:variable name="interpretationRecordTime" select="translate(InterpretationRecordTime, ':', '')"/>
        <xsl:variable name="interpretationApprovedDate" select="translate(InterpretationApprovedDate, '-', '')"/>
        <xsl:variable name="interpretationApprovedTime" select="translate(InterpretationApprovedTime, ':', '')"/>
        <xsl:variable name="verificationFlag">
            <xsl:choose>
                <xsl:when test="$resultStatus='Finalized' and $verifyingObserver/SingleByteName">VERIFIED</xsl:when>
                <xsl:otherwise>UNVERIFIED</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'0040A493'"/>
            <xsl:with-param name="vr" select="'CS'"/>
            <xsl:with-param name="val" select="$verificationFlag"/>
        </xsl:call-template>
        <!-- Content Date/Time -->
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'00080023'"/>
            <xsl:with-param name="vr" select="'DA'"/>
            <xsl:with-param name="val" select="$interpretationRecordDate"/>
        </xsl:call-template>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'00080033'"/>
            <xsl:with-param name="vr" select="'TM'"/>
            <xsl:with-param name="val" select="$interpretationRecordTime"/>
        </xsl:call-template>
        <!-- Study Date/Time -->
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'00080020'"/>
            <xsl:with-param name="vr" select="'DA'"/>
            <xsl:with-param name="val" select="$interpretationRecordDate"/>
        </xsl:call-template>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'00080030'"/>
            <xsl:with-param name="vr" select="'TM'"/>
            <xsl:with-param name="val" select="'0000'"/>
        </xsl:call-template>
        <xsl:if test="$verificationFlag = 'VERIFIED'">
            <!-- Verifying Observer Sequence -->
            <DicomAttribute tag="0040A073" vr="SQ">
                <Item number="1">
                    <!-- Verifying Organization -->
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'0040A027'"/>
                        <xsl:with-param name="vr" select="'LO'"/>
                        <xsl:with-param name="val" select="$VerifyingOrganization"/>
                    </xsl:call-template>
                    <!-- Verification DateTime -->
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'0040A030'"/>
                        <xsl:with-param name="vr" select="'DT'"/>
                        <xsl:with-param name="val">
                            <xsl:value-of select="concat($interpretationApprovedDate, $interpretationApprovedTime)"/>
                        </xsl:with-param>
                    </xsl:call-template>
                    <!-- Verifying Observer Name -->
                    <xsl:call-template name="pnAttrs">
                        <xsl:with-param name="tag" select="'0040A075'"/>
                        <xsl:with-param name="val" select="$verifyingObserver"/>
                    </xsl:call-template>
                    <!-- Verifying Observer Identification Code Sequence -->
                    <DicomAttribute tag="0040A088" vr="SQ"/>
                </Item>
            </DicomAttribute>
        </xsl:if>

        <!-- Author Observer Sequence -->
        <xsl:if test="ReportAuthor/Name/SingleByteName">
            <DicomAttribute tag="0040A078" vr="SQ">
                <Item number="1">
                    <!-- Institution Name -->
                    <DicomAttribute tag="00080080" vr="LO"/>
                    <!-- Institution Code Sequence -->
                    <DicomAttribute tag="00080082" vr="SQ"/>
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'0040A084'"/>
                        <xsl:with-param name="vr" select="'CS'"/>
                        <xsl:with-param name="val" select="'PSN'"/>
                    </xsl:call-template>
                    <xsl:call-template name="pnAttrs">
                        <xsl:with-param name="tag" select="'0040A123'"/>
                        <xsl:with-param name="val" select="ReportAuthor/Name"/>
                    </xsl:call-template>
                    <!-- Person Identification Code Sequence -->
                    <xsl:if test="ReportAuthor/Id/IdText">
                        <xsl:call-template name="codeItem">
                            <xsl:with-param name="sqtag" select="'00401101'"/>
                            <xsl:with-param name="code" select="ReportAuthor/Id/IdText"/>
                            <xsl:with-param name="scheme" select="ReportAuthor/Id/IdDomain"/>
                            <xsl:with-param name="meaning">
                                <xsl:call-template name="personName">
                                    <xsl:with-param name="val" select="ReportAuthor/Name"/>
                                </xsl:call-template>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>
                </Item>
            </DicomAttribute>
        </xsl:if>

        <!-- Participant Sequence -->
        <xsl:if test="InterpretationRecorder/SingleByteName/FirstName">
            <DicomAttribute tag="0040A07A" vr="SQ">
                <Item number="1">
                    <!-- Institution Name -->
                    <DicomAttribute tag="00080080" vr="LO"/>
                    <!-- Institution Code Sequence -->
                    <DicomAttribute tag="00080082" vr="SQ"/>
                    <!-- Person Identification Code Sequence -->
                    <DicomAttribute tag="00401101" vr="SQ"/>
                    <!-- Participation Type -->
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'0040A080'"/>
                        <xsl:with-param name="vr" select="'CS'"/>
                        <xsl:with-param name="val" select="'ENT'"/>
                    </xsl:call-template>
                    <!-- Participation DateTime -->
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'0040A082'"/>
                        <xsl:with-param name="vr" select="'DT'"/>
                        <xsl:with-param name="val">
                            <xsl:value-of select="concat($interpretationRecordDate, $interpretationRecordTime)"/>
                        </xsl:with-param>
                    </xsl:call-template>
                    <!-- Observer Type -->
                    <xsl:call-template name="attr">
                        <xsl:with-param name="tag" select="'0040A084'"/>
                        <xsl:with-param name="vr" select="'CS'"/>
                        <xsl:with-param name="val" select="'PSN'"/>
                    </xsl:call-template>
                    <!-- Person Name -->
                    <xsl:call-template name="pnAttrs2">
                        <xsl:with-param name="tag" select="'0040A123'"/>
                        <xsl:with-param name="FamilyName" select="InterpretationRecorder/SingleByteName/FirstName"/>
                        <xsl:with-param name="GivenName" select="InterpretationRecorder/SingleByteName/MiddleName"/>
                    </xsl:call-template>
                </Item>
            </DicomAttribute>
        </xsl:if>
    </xsl:template>

    <xsl:template name="contentSeq">
        <DicomAttribute tag="0040A730" vr="SQ">
            <xsl:call-template name="language">
                <xsl:with-param name="itemNo">1</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="reportItem">
                <xsl:with-param name="itemNo">2</xsl:with-param>
                <xsl:with-param name="parentCode">
                    <xsl:value-of select="'121070'"/>
                </xsl:with-param>
                <xsl:with-param name="parentCodeMeaning">
                    <xsl:value-of select="'Findings'"/>
                </xsl:with-param>
                <xsl:with-param name="childCode">
                    <xsl:value-of select="'121071'"/>
                </xsl:with-param>
                <xsl:with-param name="childCodeMeaning">
                    <xsl:value-of select="'Finding'"/>
                </xsl:with-param>
                <xsl:with-param name="val">
                    <xsl:value-of select="ReportDetails/ReportBody/text()"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="reportItem">
                <xsl:with-param name="itemNo">3</xsl:with-param>
                <xsl:with-param name="parentCode">
                    <xsl:value-of select="'121076'"/>
                </xsl:with-param>
                <xsl:with-param name="parentCodeMeaning">
                    <xsl:value-of select="'Conclusions'"/>
                </xsl:with-param>
                <xsl:with-param name="childCode">
                    <xsl:value-of select="'121077'"/>
                </xsl:with-param>
                <xsl:with-param name="childCodeMeaning">
                    <xsl:value-of select="'Conclusion'"/>
                </xsl:with-param>
                <xsl:with-param name="val">
                    <xsl:value-of select="ReportDetails/Conclusions/text()"/>
                </xsl:with-param>
            </xsl:call-template>
        </DicomAttribute>
    </xsl:template>

    <xsl:template name="language">
        <xsl:param name="itemNo"/>
        <Item number="{$itemNo}">
            <xsl:call-template name="conceptModRelation"/>
            <xsl:call-template name="codeValueType"/>
            <xsl:call-template name="conceptNameCodeSq">
                <xsl:with-param name="code" select="'121049'"/>
                <xsl:with-param name="meaning" select="'Language of Content Item and Descendants'"/>
            </xsl:call-template>
            <xsl:call-template name="codeItem">
                <xsl:with-param name="sqtag">0040A168</xsl:with-param>
                <xsl:with-param name="code" select="$langCodeValue"/>
                <xsl:with-param name="scheme" select="$langCodingSchemeDesignator"/>
                <xsl:with-param name="meaning" select="$langCodeMeaning"/>
            </xsl:call-template>
        </Item>
    </xsl:template>

    <xsl:template name="reportItem">
        <xsl:param name="itemNo"/>
        <xsl:param name="parentCode"/>
        <xsl:param name="parentCodeMeaning"/>
        <xsl:param name="childCode"/>
        <xsl:param name="childCodeMeaning"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <Item number="{$itemNo}">
                <xsl:call-template name="containsRelation"/>
                <xsl:call-template name="containerValueType"/>
                <xsl:call-template name="conceptNameCodeSq">
                    <xsl:with-param name="code" select="$parentCode"/>
                    <xsl:with-param name="meaning" select="$parentCodeMeaning"/>
                </xsl:call-template>
                <xsl:call-template name="continuityOfContent"/>
                <DicomAttribute tag="0040A730" vr="SQ">
                    <Item number="1">
                        <xsl:call-template name="containsRelation"/>
                        <xsl:call-template name="textValueType"/>
                        <xsl:call-template name="conceptNameCodeSq">
                            <xsl:with-param name="code" select="$childCode"/>
                            <xsl:with-param name="meaning" select="$childCodeMeaning"/>
                        </xsl:call-template>
                        <xsl:call-template name="text">
                            <xsl:with-param name="val" select="$val"/>
                        </xsl:call-template>
                    </Item>
                </DicomAttribute>
            </Item>
        </xsl:if>
    </xsl:template>

    <xsl:template name="continuityOfContent">
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'0040A050'"/>
            <xsl:with-param name="vr" select="'CS'"/>
            <xsl:with-param name="val" select="'SEPARATE'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="valueType">
        <xsl:param name="val"/>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'0040A040'"/>
            <xsl:with-param name="vr" select="'CS'"/>
            <xsl:with-param name="val" select="$val"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="conceptNameCodeSq">
        <xsl:param name="code"/>
        <xsl:param name="meaning"/>
        <xsl:call-template name="codeItem">
            <xsl:with-param name="sqtag" select="'0040A043'"/>
            <xsl:with-param name="code" select="$code"/>
            <xsl:with-param name="scheme" select="'DCM'"/>
            <xsl:with-param name="meaning" select="$meaning"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="text">
        <xsl:param name="val"/>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'0040A160'"/>
            <xsl:with-param name="vr" select="'UT'"/>
            <xsl:with-param name="val" select="$val"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="codeValueType">
        <xsl:call-template name="valueType">
            <xsl:with-param name="val" select="'CODE'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="containerValueType">
        <xsl:call-template name="valueType">
            <xsl:with-param name="val" select="'CONTAINER'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="textValueType">
        <xsl:call-template name="valueType">
            <xsl:with-param name="val" select="'TEXT'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="conceptModRelation">
        <xsl:call-template name="relationshipType">
            <xsl:with-param name="val" select="'HAS CONCEPT MOD'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="containsRelation">
        <xsl:call-template name="relationshipType">
            <xsl:with-param name="val" select="'CONTAINS'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="obsRelation">
        <xsl:call-template name="relationshipType">
            <xsl:with-param name="val" select="'HAS OBS CONTEXT'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="relationshipType">
        <xsl:param name="val"/>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="'0040A010'"/>
            <xsl:with-param name="vr" select="'CS'"/>
            <xsl:with-param name="val" select="$val"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="personName">
        <xsl:param name="val"/>
        <xsl:variable name="lastName" select="$val/SingleByteName/LastName"/>
        <xsl:if test="$lastName">
            <xsl:value-of select="concat($lastName, '^', $val/SingleByteName/FirstName)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="pnAttrs">
        <xsl:param name="tag"/>
        <xsl:param name="val"/>
        <xsl:call-template name="pnAttrs2">
            <xsl:with-param name="tag" select="$tag"/>
            <xsl:with-param name="FamilyName" select="$val/SingleByteName/LastName"/>
            <xsl:with-param name="GivenName" select="$val/SingleByteName/FirstName"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="pnAttrs2">
        <xsl:param name="tag"/>
        <xsl:param name="FamilyName"/>
        <xsl:param name="GivenName"/>
        <DicomAttribute tag="{$tag}" vr="PN">
            <xsl:if test="$FamilyName">
                <PersonName number="1">
                    <Alphabetic>
                        <xsl:call-template name="pnComp">
                            <xsl:with-param name="name">FamilyName</xsl:with-param>
                            <xsl:with-param name="val" select="$FamilyName"/>
                        </xsl:call-template>
                        <xsl:if test="$GivenName">
                            <xsl:call-template name="pnComp">
                                <xsl:with-param name="name">GivenName</xsl:with-param>
                                <xsl:with-param name="val" select="$GivenName"/>
                            </xsl:call-template>
                        </xsl:if>
                    </Alphabetic>
                </PersonName>
            </xsl:if>
        </DicomAttribute>
    </xsl:template>

    <xsl:template name="attr">
        <xsl:param name="tag"/>
        <xsl:param name="vr"/>
        <xsl:param name="val"/>
        <DicomAttribute tag="{$tag}" vr="{$vr}">
            <xsl:if test="$val">
                <Value number="1">
                    <xsl:value-of select="$val"/>
                </Value>
            </xsl:if>
        </DicomAttribute>
    </xsl:template>

    <xsl:template name="pnComp">
        <xsl:param name="name"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <xsl:element name="{$name}">
                <xsl:value-of select="$val"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="codeItem">
        <xsl:param name="sqtag"/>
        <xsl:param name="code"/>
        <xsl:param name="scheme"/>
        <xsl:param name="meaning"/>
        <xsl:if test="$code and $scheme and $meaning">
            <DicomAttribute tag="{$sqtag}" vr="SQ">
                <xsl:call-template name="codeItem1">
                    <xsl:with-param name="itemNo" select="'1'"/>
                    <xsl:with-param name="code" select="$code"/>
                    <xsl:with-param name="scheme" select="$scheme"/>
                    <xsl:with-param name="meaning" select="$meaning"/>
                </xsl:call-template>
            </DicomAttribute>
        </xsl:if>
    </xsl:template>

    <xsl:template name="codeItem1">
        <xsl:param name="itemNo"/>
        <xsl:param name="code"/>
        <xsl:param name="scheme"/>
        <xsl:param name="meaning"/>
        <xsl:if test="$code and $scheme and $meaning">
            <Item number="{$itemNo}">
                <!-- Code Value -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="val" select="substring($code,1,16)"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="tag" select="'00080100'"/>
                </xsl:call-template>
                <!-- Coding Scheme Designator -->
                <xsl:call-template name="attr">
                    <xsl:with-param name="val" select="substring($scheme,1,16)"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="tag" select="'00080102'"/>
                </xsl:call-template>
                <!-- Code Meaning -->
                <xsl:call-template name="trimmedAttr">
                    <xsl:with-param name="tag" select="'00080104'"/>
                    <xsl:with-param name="vr" select="'LO'"/>
                    <xsl:with-param name="val" select="$meaning"/>
                </xsl:call-template>
            </Item>
        </xsl:if>
    </xsl:template>

    <xsl:template name="trimmedAttr">
        <xsl:param name="tag"/>
        <xsl:param name="vr"/>
        <xsl:param name="val"/>
        <xsl:call-template name="attr">
            <xsl:with-param name="tag" select="$tag"/>
            <xsl:with-param name="vr" select="$vr"/>
            <xsl:with-param name="val" select="substring($val, 1, 64)"/>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
