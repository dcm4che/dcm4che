    usage: mkkos [options] --title <code> -o <file> [<file>..][<directory>..]
    
    Create DICOM Key Object Selection Document (KOS) with specified Document
    Title -title <code> flagging DICOM Composite objects in specified <file>..
    and <directory>.. and store it into DICOM file -o <file>.
    -
    Options:
        --code-config <file|url>   file path or URL of list of configured
                                   Codes to be used instead of
                                   etc/mkkos/code.properties
        --desc <text>              optional Key Object Description of created
                                   KOS
        --expl-item-len            encode sequence items with explicit length;
                                   at default, non-empty sequence items are
                                   encoded with undefined length
        --expl-seq-len             encode sequences with explicit length; at
                                   default, non-empty sequences are encoded
                                   with undefined length
     -F,--no-fmi                   store KOS without File Meta Information
                                   with Implicit VR Little Endian. At default,
                                   create DICOM Part 10 file with File Meta
                                   Information
        --group-len                include (gggg,0000) Group Length
                                   attributes; at default, optional Group
                                   Length attributes are excluded
     -h,--help                     display this help and exit
        --inst-no <no>             Instance Number of created KOS (default: 1)
        --location-uid <uid>       optional Retrieve Location UID
        --modifier <code>          optional Document Title Modifier of created
                                   KOS - must be one of the values specified
                                   by etc/mkkos/code.properties or
                                   --code-config
     -o <file>                     created DICOM file
        --retrieve-aet <aet>       optional Retrieve AE Title
        --retrieve-url <url>       optional Retrieve URL
     -s <[seq/]attr=value>         specify attributes to overwrite referenced
                                   object(s). attr can be specified by keyword
                                   or tag value (in hex), e.g. PatientName or
                                   00100010.
        --series-no <no>           Series Number of created KOS (default: 999)
     -t,--transfer-syntax <uid>    store result with specified Transfer
                                   Syntax. At default use Explicit VR Little
                                   Endian for generated DICOM Part 10 file.
        --title <code>             Document Title of created KOS - must be one
                                   of the values specified by
                                   etc/mkkos/code.properties or --code-config
        --uid-suffix <suffix>      specify suffix to be appended to the Study,
                                   Series and SOP Instance UID of referenced
                                   object(s).
        --undef-item-len           encode all sequence items with undefined
                                   length; at default, only non-empty sequence
                                   items are encoded with undefined length
        --undef-seq-len            encode all sequences with undefined length;
                                   at default, only non-empty sequences are
                                   encoded with undefined length
     -V,--version                  output version information and exit
    -
    Example: mkkos --title DCM-113000 -o kos.dcm path/to/study
    => Create DICOM Key Object Selection Document with Document Title (113000,
    DCM, "Of Interest") flagging Composite objects in directory path/to/study
    and store it into DICOM file kos.dcm.
