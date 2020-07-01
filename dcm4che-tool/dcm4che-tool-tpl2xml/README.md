    usage: tpl2xml [options] [<template-file>..]
    
    Convert private dictionaries present in template file(s) to xml file(s).
    Converted xml file(s) shall be written to the same directory as template
    file(s), if not out-dir is specified. The xml file(s) names shall be that
    of the Private Creator ID (owner) of the dictionary. A single template
    file may contain dictionaries belonging to different owners. Invalid
    keywords shall be improved whereas any duplicate tags or keywords for a
    particular Private Creator ID (owner) shall be ignored and only logged.
    -
    Options:
     -h,--help                  display this help and exit
     -I,--indent                use additional whitespace in XML output
        --out-dir <directory>   specify directory where converted xml file(s)
                                containing private dictionary elements shall
                                be saved.
     -V,--version               output version information and exit
        --xml11                 set version in XML declaration to 1.1; 1.0 by
                                default
    -
    Example 1: tpl2xml -I --out-dir path-to-converted-xml-file-dir acuson.tpl
    => Convert private dictionaries present in acuson template file to
    corresponding xml files and write them into the specified directory.
    -
    Example 2: tpl2xml -I --out-dir path-to-converted-xml-file-dir acuson.tpl
    philips.tpl
    => Convert private dictionaries present in acuson and philips template
    files to corresponding xml files and write them into the specified
    directory.