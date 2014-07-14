    Usage: prefs2xml [<options>] [<pathName>|<deviceName>]
    
    Export subtree of specified preference node as XML document to standard output.
    If no argument is specified, the whole DICOM Configuration at node 
    '/dicomConfigurationRoot' will be exported. If the argument does not start with
    '/', it is interpreted as DICOM Device Name which configuration at node
    '/dicomConfigurationRoot/dicomDevicesRoot/'<deviceName> will be exported. If
    the argument starts with '/', it specifies the path name of the exported node.
    
    Options:
    -s, --system    export System preferences; export User preferences by default
    -h,  --help     display this help and exit
    -V,  --version  output version information and exit
