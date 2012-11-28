    usage: hl72xml [<options>] <hl7-file>
    
    Convert <hl7-file> (or the standard input if <hl7-file> = '-') in XML
    presentation and optionally apply XSLT stylesheet on it. Writes result to
    standard output.
    -
    Options:
        --charset <name>   Character Set used to decode the message if not
                           specified by MSH-18, ASCII by default
     -h,--help             display this help and exit
     -I,--indent           use additional whitespace in XML output
     -V,--version          output version information and exit
     -x,--xsl <xsl-file>   apply specified XSLT stylesheet
        --xmlns            include xmlns='http://aurora.regenstrief.org/xhl7'
                           attribute in root element
    Examples:
    $ hl72xml message.hl7
    Write XML representation of HL7 file message.hl7 to standard output
