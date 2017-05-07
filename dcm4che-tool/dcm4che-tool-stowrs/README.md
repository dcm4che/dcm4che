    usage: stowrs [options]  -u <stowURL> [<fileToBeStored>..]
    
    For DICOM files : ability to send multiple dicom files to STOW-RS receiver at a time. 
    For metadata+bulkdata : Reads metadata from specified metadata file or from command line 
    and send them to a STOW-RS receiver. Attributes for bulkdata can be specified using 
    -m <metadata> or -f <file>. Note that if attributes are not specified at all by the user 
    then system generated values will be used for certain attributes. 
    Currently tool supports sending of only one bulkdata file at a time to STOW-RS receiver. 
    Supported extension types for bulkdata are pdf, jpg, jpeg, mpg, mpeg, mpg2. 
    User has to ensure that there are no spaces in the file names.
    -
    Options:
    -a,--accept <arg>               Specify the value for Accept header : xml or json.
                                    The value of Accept header will then be sent in request header 
                                    as application/dicom+xml or application/dicom+json.
                                    Note that for DICOM objects application/dicom+xml will always be used by default.
                                    If this flag is absent, for bulkdata type of objects 
                                    the value specified in -t option will be used to determine 
                                    application/dicom+xml or application/dicom+json. 
                                    If -t option is absent as well then application/dicom+xml will be used by default.                     
    -f,--file <arg>                 specify the file containing the metadata in XML format.
    -h,--help                       display this help and exit
    -m <[seq/]attr=value>           specify metadata attributes. attr can be
                                    specified by keyword or tag value (in hex),
                                    e.g. PatientName or 00100010. Attributes in
                                    nested Datasets can be specified by including
                                    the keyword/tag value of the sequence
                                    attribute, e.g. 00400275/00400009 for
                                    Scheduled Procedure Step ID in the Request.
    -na,--no-appn <arg>             Specify value as true if application segments APPn are to be excluded from JPEG stream; 
                                    encapsulate JPEG stream verbatim by default.
    -ph,--pixel-header <arg>        Specify value as true if metadata information is to be extracted from 
                                    header of pixel data for jpegs and mpegs.
                                    If absent pixel header will not be read; user has to then ensure that 
                                    pixel data related attributes should be present either in 
                                    metadata file or from command line to ensure seeing the images/videos correctly.
    -t,--type <arg>                 specify the value for Content-type header : xml or json. 
                                    The value of Content-type will then be sent in request header 
                                    as application/dicom+xml or application/dicom+json.
    -u,--url <arg>                  specify the request URL.
    -V,--version                    output version information and exit

    -
    Example: stowrs -m PatientName=John^Doe -u
    http://localhost/stow/studies[/{StudyInstanceUID}] img.jpeg
    => Send stow request to stowRS Receiver with the attribute given and
    img.jpeg bulkData.
    Example: stowrs -u http://localhost/stow/studies[/{StudyInstanceUID}] 
    object1.dcm object2.dcm
    => Send stow request to stowRS Receiver with the attribute given and
    img.jpeg bulkData.
