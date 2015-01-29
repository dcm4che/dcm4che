    usage: wadors [options] <wadoRSURL>
    
    The wadors tool provides a way to query a DICOM WADO-RS service for
    Studies, Series and Instances, metadata and BulkData.
    The tool provides options for specifying accept type (Media Type)
    and transfer syntax. The response will be written into files of the 
    specified type in the accept type, where each file corresponds to 
    one part of the server response.
    -
    Options:
        --request-timeout <ms>                timeout in ms for receiving
                                              a WADO-RS response,no timeout
                                              by default
     -h,--help                                display this help and exit
     -I,--indent                              use additional whitespace in XML
                                              output.
        --out-dir <directory>                 specifies directory where
                                              files with file names
                                              specified by option --out-file
                                              will be stored.
        --out-file <name>                     specifies name of the files
                                              written to the directory 
                                              specified by out-dir
                                              (default: wadoResponse).
     -V,--version                             output version information and
                                              exit
     -x,--xsl <xsl-file>                      Provide XSL file to apply 
                                              transformation on the returned
                                              XML. No effect on JSON response.
     -K,--no-keyword                          do not include keyword attribute
                                              of DicomAttribute element in XML
                                              output.
        --accept-type  <mediaType;ts>         Includes the type of the 
                                              accepted multipart/related
                                              response, defined as type
                                              =MediaType;transfer-syntax
                                              ={tsuid}. Both the MediaType
                                              and the transfer-syntax
                                              have to be defined (no spaces).
                                              No default however there can
                                              be multiple accept-type. 
    -
    Examples:
    $ wadors --accept-type application/dicom+xml
    http://localhost:8080/dcm4che-arc/wado/DCM4CHEE/studies/1.2.3.4/metadata
    Retrieves the metadata for study with UID 1.2.3.4
    -
    Examples:
    $ wadors --accept-type image/dicom+jpeg;1.2.840.10008.1.2.4.50
    http://localhost:8080/dcm4che-arc/wado/DCM4CHEE/studies/1.2.3.4
    Retrieves the images (bulk data) for study with UID 1.2.3.4 as JPEG baseline