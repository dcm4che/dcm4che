    usage: dcmvalidate --iod <iod-file> [<dicom-file>..][<directory>..]
    
    Utility to validate DICOM objects according a specified Information Object
    Definition.
    -
    Options:
     -h,--help             display this help and exit
        --iod <iod-file>   path to xml file with Information Object Definition
     -V,--version          output version information and exit
    Example:
    $ dcmvalidate --iod etc/dcmvalidate/dicomdir-iod.xml DICOMDIR
    Validate DICOMDIR against IOD specified in etc/dcmvalidate/dicomdir.xml
