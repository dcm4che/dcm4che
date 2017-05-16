    usage: jpg2dcm [Options] <jpgfile> <dcmfile>

    Supports conversion of pixel data (with extensions jpg, jpeg, mpg, mpeg, mpg2) to DICOM. 
    Metadata can be specified via command line (using -m option) or a file (using -f option).
    Character set ISO_IR 100 will be used as default charset, if not specified in metadata.
    File names should not contain spaces.
    -
    Options:
    -f,--file <file>                Specify the file (in XML format) containing the metadata 
                                    or DICOM attributes.
    -h,--help                       Print this message
    -m,--metadata                   Specify metadata attributes. Attribute can be specified 
                                    by keyword or tag value (in hex), 
                                    e.g. PatientName or 00100010. Attributes in nested 
                                    Datasets can be specified by including the keyword/tag 
                                    value of the sequence attribute, 
                                    e.g. 00400275/00400009 for 
                                    Scheduled Procedure Step ID in the Request.
    -na,--no-appn                   Exclude application segments APPn from JPEG
                                    stream; encapsulate JPEG stream verbatim by
                                    default.
    -V,--version                    Print the version information and exit
    --
    Example 1: Encapulate JPEG Image verbatim with DICOM attributes or metadata 
    specified in the xml file into DICOM Image Object:
    $ jpg2dcm -f jpg2dcm.xml image.jpg image.dcm
    --
    Example 2: Encapulate JPEG Image without application segments and with system generated 
    DICOM attributes into DICOM Image Object:
    $ jpg2dcm -na true homer.jpg image.dcm
    --
    Example 3: Encapulate MPEG2 Video with specified DICOM attributes into
    DICOM Video Object:
    $ jpg2dcm -f mpg2dcm.xml video.mpg video.dcm