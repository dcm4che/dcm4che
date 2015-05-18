    usage: dcmdict [options] <lookUpKey>
    
    The DICOM dictionary tool provides a way to lookup DICOM attribute and UID
    information by specifying the type of the query and a lookUpKey. The lookup-
    Key does not have to be complete, but the use can specify a prefix of the 
    attribute or uid, or even specify the abbreviation (Keyword camel case) of
    the queried attribute/uid and the tool will provide a suggestion list for
    the user to use again to do a correct lookup.
    -
    Options:
     -h,--help                                display this help and exit
       ,--private-creator                     specify a private creator.
                                              Used to get proprietary dictionary.
                                              default is standard DICOM dictionary.
     -V,--version                             output version information and
                                              exit
    -
    Examples:
    $ dcmdict StudyInstanceUID
    Lookup for attribute StudyInstanceUID.
    -
    Examples:
    $ dcmdict Study
    Lookup for suggestions on attributes whose name start with prefix "Study".
    -
    Examples:
    $ dcmdict SIUID
    Lookup for suggestions on attributes whose name is abbreviated 
    "SIUID" (i.e. StudyInstanceUID).