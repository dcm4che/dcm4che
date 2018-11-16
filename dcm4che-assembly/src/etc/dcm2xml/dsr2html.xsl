<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8" />
  <xsl:param name="wado-url">http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/wado</xsl:param>
  <xsl:variable name="sopRefs" select="/NativeDicomModel/DicomAttribute[@tag='0040A375' or @tag='0040A385']/Item
                   /DicomAttribute[@tag='00081115']/Item/DicomAttribute[@tag='00081199']/Item"/>

  <xsl:include href="cuid2name.xsl" />

  <xsl:template match="/NativeDicomModel">
    <html>
      <head>
        <title>
          <xsl:call-template name="cuid2name">
            <xsl:with-param name="cuid" select="DicomAttribute[@tag='00080016']/Value"/>
          </xsl:call-template>
        </title>
        <style type="text/css">
          <xsl:value-of select="document('report.css')" disable-output-escaping="yes" />
        </style>
      </head>
      <body>
        <table>
          <xsl:call-template name="patient">
            <xsl:with-param name="name" select="DicomAttribute[@tag='00100010']/PersonName"/>
            <xsl:with-param name="id" select="DicomAttribute[@tag='00100020']/Value"/>
            <xsl:with-param name="dob" select="DicomAttribute[@tag='00100030']/Value"/>
            <xsl:with-param name="sex" select="DicomAttribute[@tag='00100040']/Value"/>
          </xsl:call-template>
          <xsl:variable name="refReq" select="DicomAttribute[@tag='0040A370']/Item"/>
          <xsl:variable name="accno" select="DicomAttribute[@tag='00080050']/Value"/>
          <xsl:if test="$accno != $refReq/DicomAttribute[@tag='00080050']/Value">
            <xsl:apply-templates mode="tr" select="$accno">
              <xsl:with-param name="label">(Study) Accession Number:</xsl:with-param>
            </xsl:apply-templates>
          </xsl:if>
          <xsl:variable name="referring" select="DicomAttribute[@tag='00080090']/PersonName"/>
          <xsl:if test="$referring/Alphabetic/FamilyName != $refReq/DicomAttribute[@tag='00080090']/PersonName/Alphabetic/FamilyName">
            <xsl:apply-templates mode="trpn" select="$referring">
              <xsl:with-param name="label">(Study) Referring Physician:</xsl:with-param>
            </xsl:apply-templates>
          </xsl:if>
          <xsl:variable name="studyDesc" select="DicomAttribute[@tag='00081030']/Value"/>
          <xsl:if test="$studyDesc != $refReq/DicomAttribute[@tag='00321060']/Value">
            <xsl:apply-templates mode="tr" select="$studyDesc">
              <xsl:with-param name="label">Study Description:</xsl:with-param>
            </xsl:apply-templates>
          </xsl:if>
          <xsl:apply-templates mode="refReq" select="$refReq"/>
          <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='00080070']/Value">
            <xsl:with-param name="label">Manufacturer:</xsl:with-param>
          </xsl:apply-templates>
          <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='0040A496']/Value">
            <xsl:with-param name="label">Preliminary Flag:</xsl:with-param>
          </xsl:apply-templates>
          <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='0040A491']/Value">
            <xsl:with-param name="label">Completion Flag:</xsl:with-param>
          </xsl:apply-templates>
          <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='0040A492']/Value">
            <xsl:with-param name="label">Completion Flag Description:</xsl:with-param>
          </xsl:apply-templates>
          <xsl:apply-templates
              mode="predecessorDocument"
              select="DicomAttribute[@tag='0040A360']/Item/DicomAttribute[@tag='00081115']/Item/DicomAttribute[@tag='00081199']/Item"/>
          <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='0040A493']/Value">
            <xsl:with-param name="label">Verification Flag:</xsl:with-param>
          </xsl:apply-templates>
          <xsl:apply-templates mode="verifyingObserver" select="DicomAttribute[@tag='0040A073']/Item"/>
          <xsl:apply-templates mode="authorObserver" select="DicomAttribute[@tag='0040A078']/Item"/>
          <xsl:apply-templates mode="participant" select="DicomAttribute[@tag='0040A07A']/Item"/>
          <xsl:call-template name="contentDateTime">
            <xsl:with-param name="date" select="DicomAttribute[@tag='00080023']/Value"/>
            <xsl:with-param name="time" select="DicomAttribute[@tag='00080033']/Value"/>
          </xsl:call-template>
        </table>
        <hr/>
        <xsl:call-template name="container">
          <xsl:with-param name="level" select="1"/>
        </xsl:call-template>
        <hr/>
        <div class="footnote">
          <small>
            <xsl:text>This page was generated from a DICOM Structured Reporting document by </xsl:text>
            <a href="https://github.com/dcm4che">dcm4chee-arc</a>
            <xsl:text> 5.x</xsl:text>
          </small>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="container">
    <xsl:param name="level"/>
    <xsl:variable name="conceptName" select="DicomAttribute[@tag='0040A043']/Item"/>
    <xsl:if test="$conceptName">
      <xsl:element name="{concat('h',$level)}">
        <xsl:call-template name="spanCodeMeaning">
          <xsl:with-param name="code" select="$conceptName"/>
        </xsl:call-template>
      </xsl:element>
    </xsl:if>
    <xsl:variable name="observationDateTime" select="DicomAttribute[@tag='0040A032']/Value"/>
    <xsl:if test="$observationDateTime">
      <p>
        <small>
          <xsl:text>(observed: </xsl:text>
          <xsl:call-template name="formatDT">
            <xsl:with-param name="dt" select="$observationDateTime"/>
          </xsl:call-template>
          <xsl:text>)</xsl:text>
        </small>
      </p>
    </xsl:if>
    <xsl:variable name="mods_arq_obs" select="DicomAttribute[@tag='0040A730']/Item[
              DicomAttribute[@tag='0040A010']/Value='HAS CONCEPT MOD' or
              DicomAttribute[@tag='0040A010']/Value='HAS ACQ CONTEXT' or
              DicomAttribute[@tag='0040A010']/Value='HAS OBS CONTEXT']"/>
    <xsl:if test="$mods_arq_obs">
      <p>
        <small>
          <xsl:for-each select="$mods_arq_obs">
            <xsl:if test="position() &gt; 1">
              <br/>
            </xsl:if>
            <span class="under">
              <xsl:variable name="rel" select="DicomAttribute[@tag='0040A010']/Value"/>
              <xsl:choose>
                <xsl:when test="$rel='HAS CONCEPT MOD'">Concept Modifier</xsl:when>
                <xsl:when test="$rel='HAS ACQ CONTEXT'">Acquisition Context</xsl:when>
                <xsl:when test="$rel='HAS OBS CONTEXT'">Observation Context</xsl:when>
              </xsl:choose>
            </span>
            <xsl:text>: </xsl:text>
            <xsl:call-template name="spanCodeMeaning">
              <xsl:with-param name="code" select="DicomAttribute[@tag='0040A043']/Item"/>
            </xsl:call-template>
            <xsl:text> = </xsl:text>
            <xsl:apply-templates mode="renderValue" select="." />
            <xsl:variable name="child" select="DicomAttribute[@tag='0040A730']/Item"/>
            <xsl:if test="$child">
              <xsl:text>^^^</xsl:text>
              <xsl:apply-templates mode="renderValue" select="$child" />
            </xsl:if>
          </xsl:for-each>
        </small>
      </p>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="DicomAttribute[@tag='0040A050']/Value='SEPARATE'">
        <xsl:apply-templates
            mode="separate"
            select="DicomAttribute[@tag='0040A730']/Item[DicomAttribute[@tag='0040A010']/Value='CONTAINS']">
          <xsl:with-param name="level" select="$level+1"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates
            mode="continuous"
            select="DicomAttribute[@tag='0040A730']/Item[DicomAttribute[@tag='0040A010']/Value='CONTAINS']">
          <xsl:with-param name="level" select="$level+1"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="separate" match="Item[DicomAttribute[@tag='0040A040']/Value='CONTAINER']" >
    <xsl:param name="level"/>
    <div>
      <xsl:call-template name="container">
        <xsl:with-param name="level" select="$level"/>
      </xsl:call-template>
    </div>
    <p/>
  </xsl:template>

  <xsl:template mode="separate" match="Item" >
    <div>
      <xsl:variable name="conceptName" select="DicomAttribute[@tag='0040A043']/Item"/>
      <xsl:if test="$conceptName">
        <b>
          <xsl:call-template name="spanCodeMeaning">
            <xsl:with-param name="code" select="$conceptName"/>
          </xsl:call-template>
        </b>
        <xsl:text>:</xsl:text>
        <br/>
      </xsl:if>
      <xsl:apply-templates mode="renderValue" select="." />
    </div>
    <p/>
  </xsl:template>

  <xsl:template mode="continuous" match="Item[DicomAttribute[@tag='0040A040']/Value='TEXT']" >
    <xsl:text>
</xsl:text>
    <xsl:apply-templates mode="renderValue" select="."/>
  </xsl:template>

  <xsl:template mode="continuous" match="Item" >
    <xsl:text>
</xsl:text>
    <span class="under">
      <xsl:apply-templates mode="renderValue" select="." />
    </span>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='CODE']">
    <xsl:call-template name="spanCodeMeaning">
      <xsl:with-param name="class">under</xsl:with-param>
      <xsl:with-param name="code" select="DicomAttribute[@tag='0040A168']/Item"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='PNAME']">
    <xsl:call-template name="formatPN">
      <xsl:with-param name="pnc" select="DicomAttribute[@tag='0040A123']/PersonName/Alphabetic"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='UIDREF']">
    <xsl:value-of select="DicomAttribute[@tag='0040A124']/Value"/>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='DATE']">
    <xsl:call-template name="formatDA">
      <xsl:with-param name="da" select="DicomAttribute[@tag='0040A121']/Value"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='NUM']">
    <xsl:variable name="measuredValue" select="DicomAttribute[@tag='0040A300']/Item"/>
    <span class="under">
      <xsl:value-of select="concat($measuredValue/DicomAttribute[@tag='0040A30A'],' ')"/>
      <xsl:call-template name="spanCodeValue">
        <xsl:with-param name="code" select="$measuredValue/DicomAttribute[@tag='004008EA']/Item"/>
      </xsl:call-template>
    </span>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='TEXT']">
    <xsl:call-template name="renderText">
      <xsl:with-param name="text" select="DicomAttribute[@tag='0040A160']/Value"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="renderComposite">
    <xsl:variable name="objectUID"
                  select="DicomAttribute[@tag='00081199']/Item/DicomAttribute[@tag='00081155']/Value"/>
    <xsl:call-template name="wado-link">
      <xsl:with-param name="sopRef" select="$sopRefs[DicomAttribute[@tag='00081155']/Value=$objectUID]"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='COMPOSITE']">
    <xsl:call-template name="renderComposite"/>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='WAVEFORM']">
    <xsl:call-template name="renderComposite"/>
  </xsl:template>

  <xsl:template mode="renderValue" match="Item[DicomAttribute[@tag='0040A040']/Value='IMAGE']">
    <xsl:call-template name="renderComposite"/>
  </xsl:template>

  <xsl:template name="renderText">
    <xsl:param name="text"/>
    <xsl:call-template name="replace_lf_by_br">
      <xsl:with-param name="text">
        <xsl:call-template name="replace_lf_by_br">
          <xsl:with-param name="text">
            <xsl:call-template name="replace_lf_by_br">
              <xsl:with-param name="text" select="$text"/>
              <xsl:with-param name="lf" select="'&#13;&#10;'"/>
              <xsl:with-param name="len">3</xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="lf" select="'&#13;'"/>
          <xsl:with-param name="len">2</xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="lf" select="'&#10;'"/>
      <xsl:with-param name="len">2</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="replace_lf_by_br">
    <xsl:param name="text"/>
    <xsl:param name="lf"/>
    <xsl:param name="len"/>
    <xsl:variable name="first" select="substring-before($text,$lf)"/>
    <xsl:choose>
      <xsl:when test="$first or starts-with($text,$lf)">
        <xsl:value-of select="$first"/>
        <br/>
        <xsl:call-template name="replace_lf_by_br">
          <xsl:with-param name="text" select="substring($text,string-length($first)+$len)"/>
          <xsl:with-param name="lf" select="$lf"/>
          <xsl:with-param name="len" select="$len"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="patient">
    <xsl:param name="name"/>
    <xsl:param name="id"/>
    <xsl:param name="dob"/>
    <xsl:param name="sex"/>
    <tr>
      <td><b>Patient:</b></td>
      <td>
        <xsl:call-template name="formatPN">
          <xsl:with-param name="pnc" select="$name/Alphabetic"/>
        </xsl:call-template>
        <xsl:variable name="sex1">
          <xsl:choose>
            <xsl:when test="$sex='M'">Male</xsl:when>
            <xsl:when test="$sex='F'">Female</xsl:when>
            <xsl:when test="$sex='O'">Other</xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="$id or $dob or $sex1">
          <xsl:text> (</xsl:text>
          <xsl:if test="$sex1">
            <xsl:value-of select="$sex1" />
            <xsl:if test="$dob or $id">, </xsl:if>
          </xsl:if>
          <xsl:if test="$dob">
            <xsl:text>*</xsl:text>
            <xsl:call-template name="formatDA">
              <xsl:with-param name="da" select="$dob"/>
            </xsl:call-template>
            <xsl:if test="$id">, </xsl:if>
          </xsl:if>
          <xsl:if test="$id">
            <xsl:value-of select="concat('#', $id)"/>
          </xsl:if>
          <xsl:text>)</xsl:text>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="PersonName" mode="trpn">
    <xsl:param name="label"/>
    <tr>
      <td><b><xsl:value-of select="$label"/></b></td>
      <td>
        <xsl:call-template name="formatPN">
          <xsl:with-param name="pnc" select="Alphabetic"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="Value" mode="tr">
    <xsl:param name="label"/>
    <tr>
      <td><b><xsl:value-of select="$label"/></b></td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="Item" mode="predecessorDocument">
    <tr>
      <td>
        <xsl:if test="position()=1">
          <b>Predecessor Docs:</b>
        </xsl:if>
      </td>
      <td>
        <xsl:call-template name="wado-link">
          <xsl:with-param name="sopRef" select="."/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="Item" mode="verifyingObserver">
    <tr>
      <td>
        <xsl:if test="position()=1">
          <b>Verifying Observers:</b>
        </xsl:if>
      </td>
      <td>
        <xsl:call-template name="formatIdPersonName">
          <xsl:with-param name="idTag" select="'0040A088'"/>
          <xsl:with-param name="nameTag" select="'0040A075'"/>
        </xsl:call-template>
        <xsl:variable name="org" select="DicomAttribute[@tag='0040A027']/Value"/>
        <xsl:if test="$org">
          <xsl:value-of select="concat(', ', $org)"/>
        </xsl:if>
        <xsl:call-template name="optFormatDT">
          <xsl:with-param name="dt" select="DicomAttribute[@tag='0040A030']/Value"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="Item" mode="participant">
    <tr>
      <td>
        <b>
          <xsl:variable name="participationType" select="DicomAttribute[@tag='0040A080']/Value"/>
          <xsl:choose>
            <xsl:when test="$participationType = 'ENT'">
              <xsl:text>Data Enterer:</xsl:text>
            </xsl:when>
            <xsl:when test="$participationType = 'ATTEST'">
              <xsl:text>Attestor:</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$participationType" />
            </xsl:otherwise>
          </xsl:choose>
        </b>
      </td>
      <td>
        <xsl:call-template name="formatIdPersonName">
          <xsl:with-param name="idTag" select="'00401101'"/>
          <xsl:with-param name="nameTag" select="'0040A123'"/>
        </xsl:call-template>
        <xsl:call-template name="optFormatDT">
          <xsl:with-param name="dt" select="DicomAttribute[@tag='0040A082']/Value"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="Item" mode="authorObserver">
    <tr>
      <td>
        <xsl:if test="position()=1">
          <b>Author Observers:</b>
        </xsl:if>
      </td>
      <td>
        <xsl:call-template name="formatIdPersonName">
          <xsl:with-param name="idTag" select="'00401101'"/>
          <xsl:with-param name="nameTag" select="'0040A123'"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="formatIdPersonName">
    <xsl:param name="idTag"/>
    <xsl:param name="nameTag"/>
    <xsl:call-template name="spanCode">
      <xsl:with-param name="class">under</xsl:with-param>
      <xsl:with-param name="code" select="DicomAttribute[@tag=$idTag]/Item"/>
      <xsl:with-param name="text">
        <xsl:call-template name="formatPN">
          <xsl:with-param name="pnc" select="DicomAttribute[@tag=$nameTag]/PersonName/Alphabetic"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="contentDateTime">
    <xsl:param name="date"/>
    <xsl:param name="time"/>
    <tr>
      <td><b>Content Date/Time:</b></td>
      <td>
        <xsl:call-template name="formatDA">
          <xsl:with-param name="da" select="$date"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:call-template name="formatTM">
          <xsl:with-param name="tm" select="$time"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="Item" mode="refReq">
    <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='00080050']/Value">
      <xsl:with-param name="label">Accession Number:</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates mode="trpn" select="DicomAttribute[@tag='00080090']/PersonName">
      <xsl:with-param name="label">Referring Physician:</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='00321060']/Value">
      <xsl:with-param name="label">Procedure Description:</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates mode="tr" select="DicomAttribute[@tag='00401002']/Value">
      <xsl:with-param name="label">Reason for Procedure:</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="formatPN">
    <xsl:param name="pnc"/>
    <xsl:variable name="px" select="$pnc/NamePrefix"/>
    <xsl:variable name="gn" select="$pnc/GivenName"/>
    <xsl:variable name="mn" select="$pnc/MiddleName"/>
    <xsl:variable name="fn" select="$pnc/FamilyName"/>
    <xsl:variable name="sx" select="$pnc/NameSuffix"/>
    <xsl:variable name="s" select="normalize-space(concat($px,' ',$gn,' ',$mn,' ',$fn))"/>
    <xsl:choose>
      <xsl:when test="$sx">
        <xsl:value-of select="concat($s, ', ', $sx)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$s"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="formatDA">
    <xsl:param name="da"/>
    <xsl:value-of select="concat(substring($da,1,4),'-',substring($da,5,2),'-',substring($da,7,2))"/>
  </xsl:template>

  <xsl:template name="formatTM">
    <xsl:param name="tm"/>
    <xsl:value-of select="substring($tm,1,2)"/>
    <xsl:choose>
      <xsl:when test="string-length($tm) &lt; 4">:00:00</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat(':',substring($tm,3,2))"/>
        <xsl:choose>
          <xsl:when test="string-length($tm) &lt; 6">:00</xsl:when>
          <xsl:otherwise><xsl:value-of select="concat(':',substring($tm,5,2))"/></xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="optFormatDT">
    <xsl:param name="dt"/>
    <xsl:if test="$dt">
      <small>
        <xsl:text> (</xsl:text>
        <xsl:call-template name="formatDT">
          <xsl:with-param name="dt" select="$dt"/>
        </xsl:call-template>
        <xsl:text>)</xsl:text>
      </small>
    </xsl:if>
  </xsl:template>

  <xsl:template name="formatDT">
    <xsl:param name="dt"/>
    <xsl:call-template name="formatDA">
      <xsl:with-param name="da" select="$dt"/>
    </xsl:call-template>
    <xsl:if test="string-length($dt) &gt;= 10">
      <xsl:text> </xsl:text>
      <xsl:call-template name="formatTM">
        <xsl:with-param name="tm" select="substring($dt,9)"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="formatCode">
    <xsl:param name="code"/>
    <xsl:variable name="value" select="$code/DicomAttribute[@tag='00080100']/Value"/>
    <xsl:variable name="scheme" select="$code/DicomAttribute[@tag='00080102']/Value"/>
    <xsl:variable name="version" select="$code/DicomAttribute[@tag='00080103']/Value"/>
    <xsl:variable name="meaning" select="$code/DicomAttribute[@tag='00080104']/Value"/>
    <xsl:variable name="schemex">
      <xsl:value-of select="$scheme"/>
      <xsl:if test="$version">
        <xsl:value-of select="concat(' [',$version,']')"/>
      </xsl:if>
    </xsl:variable>
    <xsl:value-of select="concat('(',$value,', ',$schemex, ', &quot;', $meaning, '&quot;)')"/>
  </xsl:template>

  <xsl:template name="spanCode">
    <xsl:param name="class"/>
    <xsl:param name="code"/>
    <xsl:param name="text"/>
    <span>
      <xsl:if test="$code">
        <xsl:if test="$class">
          <xsl:attribute name="class">
            <xsl:value-of select="$class"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="title">
          <xsl:call-template name="formatCode">
            <xsl:with-param name="code" select="$code"/>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:if>
      <xsl:value-of select="$text"/>
    </span>
  </xsl:template>

  <xsl:template name="spanCodeMeaning">
    <xsl:param name="class"></xsl:param>
    <xsl:param name="code"/>
    <xsl:call-template name="spanCode">
      <xsl:with-param name="class" select="$class"/>
      <xsl:with-param name="code" select="$code"/>
      <xsl:with-param name="text" select="$code/DicomAttribute[@tag='00080104']/Value"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="spanCodeValue">
    <xsl:param name="class"></xsl:param>
    <xsl:param name="code"/>
    <xsl:call-template name="spanCode">
      <xsl:with-param name="class" select="$class"/>
      <xsl:with-param name="code" select="$code"/>
      <xsl:with-param name="text" select="$code/DicomAttribute[@tag='00080100']/Value"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wado-link">
    <xsl:param name="sopRef"/>
    <xsl:variable name="studyUID" select="$sopRef/../../../../DicomAttribute[@tag='0020000D']/Value"/>
    <xsl:variable name="seriesUID" select="$sopRef/../../DicomAttribute[@tag='0020000E']/Value"/>
    <xsl:variable name="objectUID" select="$sopRef/DicomAttribute[@tag='00081155']/Value"/>
    <xsl:variable name="classUID" select="$sopRef/DicomAttribute[@tag='00081150']/Value"/>
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="concat($wado-url,'?requestType=WADO&amp;studyUID=',$studyUID,
          '&amp;seriesUID=',$seriesUID,'&amp;objectUID=', $objectUID)"/>
      </xsl:attribute>
      <xsl:call-template name="cuid2name">
        <xsl:with-param name="cuid" select="$classUID"/>
      </xsl:call-template>
    </a>
  </xsl:template>


</xsl:stylesheet>