    usage: qidors [options] <serviceURL>
    
    The qidors tool provides a way to query a DICOM QIDO-RS service for
    Studies, Series and Instances. The tool provides options for applying
    Search filters, matching options, matching attribute(s) and maximum
    Result allowed in the response. It is also possible to define whither
    the response is to be in DICOM XML or in DICOM JSON formats.
    -
    Options:
        --request-timeout <ms>                timeout in ms for receiving
                                              a QIDO-RS response,no timeout
                                              by default
     -h,--help                                display this help and exit
     -I,--indent                              use additional whitespace in XML
                                              output
     -i,--includefield <attr>                 specifies which extra attribute(s)
                                              will be included in the response.
                                              If specified, fields are added to
                                              the default returned fields otherwise
                                              all fields are returned 
                                              if not specified.
     -m,match <[seq.]attr=value>              Specify matching key. attr can
                                              be specified by keyword or tag
                                              value (in hex), e.g. PatientName
                                              or 00100010. Attributes in
                                              nested Datasets can be specified
                                              by including the keyword.tag
                                              value of the sequence attribute,
                                              e.g. 00400275.00400009 for
                                              Scheduled Procedure Step ID in
                                              the Request Attributes Sequence.
        --out-dir <directory>                 specifies directory where
                                              attributes of received matches
                                              are stored  files with file names
                                              specified by option --out-file.
        --out-file <name>                     specifies name of the files
                                              with received matches written to
                                              the directory specified by
                                              out-dir(default: qidoResponse).
        --timezone                            negotiate timezone adjustment of
                                              queries
        --fuzzy                               negotiate fuzzy semantic person 
                                              name attribute matching.
     -V,--version                             output version information and
                                              exit
     -J,--json                                write received matches as XML
                                              Infoset specified in DICOM Part
                                              19, default is XML if not specified.
     -x,--xsl <xsl-file>                      Provide XSL file to apply 
                                              transformation on the returned XML.
                                              No effect on JSON response.
     -K,--no-keyword                          do not include keyword attribute
                                              of DicomAttribute element in XML
                                              output
        --limit <results>                     Maximum number of results to be \
                                              returned by the server
        --offset <index>                      Index from which results will start \
                                              (i.e. offset=1 means result 0 is skipped)
    -
    Examples:
    $ qidors --fuzzy --timezone -i MYPROPRIETARYATTR -m PatientName=Doe^John -m
    StudyDate=20110510 -m OtherPatientIDsSequence.00100020=11235813 
    http://localhost:8080/dicom-web/DCM4CHEE/studies
    Issues a QIDO-RS query against endpoint http://localhost:8080/dicom-web/DCM4CHEE/
    and specifies that the response should be in XML