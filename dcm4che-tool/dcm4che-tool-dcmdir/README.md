    usage: dcmdir -{cdlpuz} <dicomdir> [Options] [<file>..][<directory>..]
    
    Utility to dump, create or update a DICOMDIR file referencing DICOM
    filesof a DICOM file-set.
    -
    Options:
     -c <dicomdir>            create new directory file <dicomdir> with
                              references to DICOM files specified by file.. or
                              directory.. arguments
     -d <dicomdir>            delete records referring DICOM files specified
                              by file.. or directory.. arguments from
                              existing directory file <dicomdir> by setting
                              its Record In-use Flag = 0
        --expl-item-len       encode sequence items with explicit length; at
                              default, non-empty sequence items are encoded
                              with undefined length
        --expl-seq-len        encode sequences with explicit length; at
                              default, non-empty sequences are encoded with
                              undefined length
        --fs-desc <txtfile>   specify File-set Descriptor File
        --fs-desc-cs <code>   Character Set used in File-set Descriptor File
                              ("ISO_IR 100" = ISO Latin 1)
        --fs-id <id>          specify File-set ID
        --fs-uid <uid>        specify File-set UID
        --group-len           include (gggg,0000) Group Length attributes; at
                              default, optional Group Length attributes are
                              excluded
     -h,--help                display this help and exit
        --in-use              only list directory records with Record In-use
                              Flag != 0
     -l <dicomdir>            list content of directory file <dicomdir> to
                              standard out
        --orig-seq-len        preserve encoding of sequence length from the
                              original file
     -p <dicomdir>            purge records without file references from
                              directory file <dicomdir> by setting its Record
                              In-use Flag = 0
     -u <dicomdir>            update existing directory file <dicomdir> "with
                              references to DICOM files specified by file.. or
                              directory.. arguments
        --undef-item-len      encode all sequence items with undefined length;
                              at default, only non-empty sequence items are
                              encoded with undefined length
        --undef-seq-len       encode all sequences with undefined length; at
                              default, only non-empty sequences are encoded
                              with undefined length
     -V,--version             output version information and exit
     -w,--width <col>         set line length; default: 78
     -z <dicomdir>            compact existing directory file <dicomdir> by
                              removing records with Record In-use Flag != 0
    -
    -Prompts:
    '.' - add record(s) referring regular DICOM Part 10 file
    'F' - add record(s) referring file without File Meta Information
    'p' - add record(s) referring instance without Patient ID, using the Study
    Instance UID as Patient ID in the PATIENT record
    'P' - add record(s) referring file without File Meta Information with
    instance without Patient ID, using the Study Instance UID as Patient ID in
    the PATIENT record
    'r' - add root record referring instance without Study Instance UID
    'R' - add root record referring file without File Meta Information with
    instance without Study Instance UID
    '-' - do not add any record for already referenced file
    'x' - delete record referring one file
    -
    Examples:
    $ dicomdir -l /media/cdrom/DICOMDIR
    list content of DICOMDIR to stdout
    -
    $ dicomdir -c disk99/DICOMDIR -I DISK99 -D disk99/README disk99/DICOM
    create a new directory file with specified File-set ID and Descriptor
    File, referencing all DICOM Files in directory disk99/DICOM
    -
    $ dicomdir -u disk99/DICOMDIR disk99/DICOM/CT1
    add directory records referencing all DICOM files in directory
    disk99/DICOM/CT1 to existing directory file
    -
    $ dicomdir -d disk99/DICOMDIR disk99/DICOM/CT2
    delete/deactivate directory records referencing DICOM files in directory
    disk99/DICOM/CT2
    -
    $ dicomdir -p disk99/DICOMDIR
    delete/deactivate directory records without child records referencing any
    DICOM file
    -
    $ dicomdir -z disk99/DICOMDIR
    compact DICOMDIR by removing inactive records
