    usage: dcm2str -p <pattern> [-s <[seq.]attr=value>].. --
                   [<file>..][<directory>..]
    
    Apply Attributes Format Pattern either to one or more DICOM files and/or
    DICOM file(s) in one or more directories. If individual attributes are
    specified, the attributes of the DICOM file(s) shall be overwritten by the
    specified attributes before the Attributes Format Pattern is applied.
    -
    Options:
     -h,--help               display this help and exit
     -p <pattern>            Specify Attributes Format Pattern to be applied.
     -s <[seq.]attr=value>   Specify attributes added to the object(s). It can
                             be specified by keyword or tag (in hex), e.g.
                             StudyInstanceUID=1.2.3 or 0020000D=1.2.3.
                             Attributes in nested Datasets can be specified by
                             including the keyword/tag value of the sequence
                             attribute, e.g. 00400275.00400009 for Scheduled
                             Procedure Step ID in the Request Attributes
                             Sequence.
     -V,--version            output version information and exit
    
    Examples:
    => dcm2str -p '{0020000D,hash}/{0020000E,hash}/{00080018,hash}/{rnd}'
    image.dcm
    Apply Attributes Format Pattern to the specified DICOM file.
    
    => dcm2str -p '{0020000D,hash}/{0020000E,hash}/{00080018,hash}/{rnd}'
    -sStudyInstanceUID=1.2.3 -sSeriesInstanceUID=1.2.3.4
    -sSOPInstanceUID=1.2.3.4.5 -- image.dcm
    Overwrite attributes of the specified DICOM file with specified DICOM
    attributes and then apply Attributes Format Pattern to the specified DICOM
    file.
    
    => dcm2str -p '{0020000D,hash}/{0020000E,hash}/{00080018,hash}/{rnd}'
    -sStudyInstanceUID=1.2.3 -sSeriesInstanceUID=1.2.3.4
    -sSOPInstanceUID=1.2.3.4.5 -- image.dcm
    /path-to-other-DICOM-files-directory
    Overwrite attributes of the specified DICOM file and of other DICOM files
    in the directory with the specified DICOM attributes and only then apply
    Attributes Format Pattern to DICOM file and to other DICOM files in the
    directory.
    
    => dcm2str -p '{0020000D,hash}/{0020000E,hash}/{00080018,hash}/{rnd}'
    image.dcm /path-to-other-DICOM-files-directory
    Apply Attributes Format Pattern to the specified DICOM file and also to
    the other DICOM files in the specified directory.
    
    => dcm2str -p '{0020000D,hash}/{0020000E,hash}/{00080018,hash}/{rnd}'
    -sStudyInstanceUID=1.2.3 -sSeriesInstanceUID=1.2.3.4
    -sSOPInstanceUID=1.2.3.4.5
    Apply Attributes Format Pattern to DICOM attributes passed as command line
    parameters.
