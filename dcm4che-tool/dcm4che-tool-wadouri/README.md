    usage: wadouri [options] <wadoUriURL>
    
    The wadouri tool provides a way to query a DICOM WADO-URI service for
    a single DICOM object that can be retrieved as different MIME types
    depending on the contentType parameter specified in the request.
    Possible media types include DICOM, BulkData (JPEG, GIF, PNG and JP2)
    and SR documents (HTML, TEXT, RTF, PDF, DICOM and CDA[ if supported])
    The tool provides options for specifying the WADO-URI request parameters
    as specified by DICOM in part 18 section 8.
    -
    Options:
        --request-timeout <ms>                timeout in ms for receiving
                                              a WADO-URI response,no timeout
                                              by default.
        --object-id <studyUID:seriesUID:objectUID>
                                              Specifies the full hierarchical 
                                              id for the object to be retrieved.
        --content-type <mediaType>            Specifies the media type(s) 
                                              of the request.
                                              Default is application/dicom
                                              Multiple content type options
                                              can be specified per request.
        --charset <charset[,charset,...]>     Specifies one or more charsets
                                              to consider for encoding the 
                                              response data.
        --anonymize                           Ensure anonymization of names
                                              (i.e. PatientName) in the response.
        --annotation <item>                   Indicates that certain annotations
                                              need to be burned into the returned
                                              pixels. Annotations are only 
                                              applicable on retrieving image
                                              types. Possible types of 
                                              annotations include patient (
                                              for burning patient information
                                              and technique for burning technique
                                              information.
        --rows <numberOfRows>               Specifies the number of rows in
                                              the retrieved image. Will apply
                                              scaling if specified.
        --columns <numberOfColumns>         Specifies the number of columns
                                              on the retrieved image. Will
                                              apply scaling if specified.
        --region <xLeft,yLeft,xRight,yRight>  Specifies the region to be
                                              retrieved from the image.
                                              Can be used for magnification.
        --window-params <center:width>        Used to control luminosity (center)
                                              and contrast (width) of the image.
                                              Not applicable for application/dicom.
        --frame-number <number>               Specifies a frame in a multi-frame
                                              image. ignored for application/dicom.
        --image-quality <number>              Specifies a number in the range of 
                                              1-100, with 100 being best and 1 being
                                              worst quality. The number specified is
                                              that of the compression quality.
                                              Only applicable to lossy formats.
        --presentation-id <SeriesUID:SOPUID>  Specifies the Series Instance UID and
                                              the SopInstanceUID of the presentation
                                              state to apply on the retrieved image.
                                              Is applied before annotation or
                                              selection of region.
        --transfer-syntax <tsUID>             Transfer syntax UID for DICOM
                                              image retrieved.
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
    -
    Examples:
    $ wadouri --object-id 1.2.3:1.2.3.4:1.2.3.4.5 --content-type image/jpeg
    http://localhost:8080/dicom-web/DCM4CHEE
    Retrieves the bulk data for the instance 1.2.3.4.5 from series 1.2.3.4
    from study 1.2.3 as a JPEG image.
    -
    $ wadouri --object-id 1.2.3:1.2.3.4:1.2.3.4.5 --content-type application/dicom
    --transfer-syntax 1.2.840.10008.1.2.1
    http://localhost:8080/dicom-web/DCM4CHEE
    Retrieves the DICOM representation for the instance 1.2.3.4.5 from series 1.2.3.4
    from study 1.2.3 encoded as Explicit VR little Endian.