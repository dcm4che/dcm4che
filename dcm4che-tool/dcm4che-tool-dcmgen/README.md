    usage: dcmgen [options] <numberOfInstance[:numberofSeries]> <dicomFile> [outDir]
    
    The DICOM generation tool provides a way to generate DICOM files. 
    The generated files depends on the specified DICOM file.
    Typically UIDs would be randomly generated, however override attributes
    can be specified using the --override option which can include study UIDs.
    SeriesInstanceUIDs and SopInstanceUIDs are randomly generated.
    Default output directory is current directory.
    -
    Options:
     -h,--help                                display this help and exit
       ,--override <[seq/]attr=value>         Specify attributes to override
                                              in the generated object(s).
                                              Default is randomly generated.
                                              attr can be specified by 
                                              keyword or tag value (in hex),
                                              e.g. PatientName or 00100010.
                                              Attributes in nested Datasets
                                              can be specified by including
                                              the keyword/tag value of the 
                                              sequence attribute,
                                              e.g. 00400275/00400009 for
                                              Scheduled Procedure Step ID in
                                              the Request Attributes Sequence.
     -V,--version                             output version information and
                                              exit
    -
    Examples:
    $ dcmgen --override StudyInstanceUID=1.2.3 10 2 seedFile.dcm
    Creates a study with uid 1.2.3 2 series each holding 5 
    instances with uids randomly generated and output directory
    is current directory. 
    -
    Examples:
    $ dcmgen 10 seedFile.dcm /tmp
    Creates a study with a randomly generated uid with 1 series
    holding 10 instances and output directory is current directory.