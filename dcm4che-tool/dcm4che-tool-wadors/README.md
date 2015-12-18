    usage: wadors [options] <wadoRSURL>

    The wadors tool provides a way to query a DICOM WADO-RS service for
    Studies, Series and Instances, metadata and BulkData. The tool provides
    options for specifying accept type (Media Type) and transfer syntax. The
    response will be written into files of the specified type in the accept
    type, where each file corresponds to one part of the server response.
    -
    Options:
        --accept-type <mediaType;ts>   Includes the type of the accepted
                                       response, defined as {MediaType} or
                                       {MediaType};{TransferSyntaxUID}. Please
                                       note that the "multipart/related" type
                                       does NOT need to be specified but will
                                       be automatically added if appropriate.
                                       Multiple accept-type options can be
                                       specified. There is no default.
        --dump-headers                 Specifies whether to dump request
                                       header and response header for each
                                       multipart.
     -h,--help                         display this help and exit
     -I,--indent                       use additional whitespace in XML output
     -K,--no-keyword                   do not include keyword attribute of
                                       DicomAttribute element in XML output
        --out-dir <directory>          specifies directory where files with
                                       file names specified by option
                                       --out-file will be stored.
        --request-timeout <arg>        Timeout in ms for receiving a WADO-RS
                                       response,no timeout by default
     -V,--version                      output version information and exit
     -x,--xsl <xsl-file>               Provide XSL file to apply
                                       transformation on the returned XML. No
                                       effect on JSON response
    -
    Examples:
    $ wadors --accept-type application/dicom+xml
    http://localhost:8080/dicom-web/DCM4CHEE/studies/1.2.3.4/metadata
    Retrieves the metadata for study with UID 1.2.3.4
    -
    Examples:
    $ wadors --accept-type "image/dicom+jpeg;1.2.840.10008.1.2.4.50"
    http://localhost:8080/dicom-web/DCM4CHEE/studies/1.2.3.4
    Retrieves the images (bulk data) for study with UID 1.2.3.4 as JPEG
    baseline
