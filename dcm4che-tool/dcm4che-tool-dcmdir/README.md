    usage: dcmdir -{cdlpuz} <dicomdir> [Options] [<file>..][<directory>..]
    
    Utility to dump, create or update a DICOMDIR file referencing DICOM
    filesof a DICOM file-set.
    -
    Options:
     -c <dicomdir>                             create new directory file
                                               <dicomdir> with references to
                                               DICOM files specified by file..
                                               or directory.. arguments
        --csv <csv-file>                       import records from CSV file
                                               with hex encoded tag values or
                                               keywords of the DICOM
                                               Attributes as headers. The CSV
                                               may also contain the additional
                                               Query/Retrieve Attributes :
                                               Number of Patient Related
                                               Studies (0020,1200), Number of
                                               Patient Related (0020,1202),
                                               Number of Patient Related
                                               Instances (0020,1204), Number
                                               of Study Related Series
                                               (0020,1206), Number of Study
                                               Related Instances (0020,1208),
                                               Number of Series Related
                                               Instances | (0020,1209)
        --csv-delim <csv-delim>                delimiter character for CSV
                                               file specified by --csv.
                                               Defaults to , (comma).
        --csv-quote <csv-quote>                quote character for CSV file
                                               specified by --csv. Defaults to
                                               " (quote).
     -d <dicomdir>                             delete records referring DICOM
                                               files specified by file.. or
                                               directory.. arguments from
                                               existing directory file
                                               <dicomdir> by setting its
                                               Record In-use Flag = 0
        --expl-item-len                        encode sequence items with
                                               explicit length; at default,
                                               non-empty sequence items are
                                               encoded with undefined length
        --expl-seq-len                         encode sequences with explicit
                                               length; at default, non-empty
                                               sequences are encoded with
                                               undefined length
        --fs-desc <txtfile>                    specify File-set Descriptor
                                               File
        --fs-desc-cs <code>                    Character Set used in File-set
                                               Descriptor File ("ISO_IR 100" =
                                               ISO Latin 1)
        --fs-id <id>                           specify File-set ID
        --fs-uid <uid>                         specify File-set UID
        --group-len                            include (gggg,0000) Group
                                               Length attributes; at default,
                                               optional Group Length
                                               attributes are excluded
     -h,--help                                 display this help and exit
        --in-use                               only list directory records
                                               with Record In-use Flag != 0
     -l <dicomdir>                             list content of directory file
                                               <dicomdir> to standard out
        --orig-seq-len                         preserve encoding of sequence
                                               length from the original file
     -p <dicomdir>                             purge records without file
                                               references from directory file
                                               <dicomdir> by setting its
                                               Record In-use Flag = 0
        --record-config <record-config-file>   file path or URL to
                                               configuration of directory
                                               record attributes. At default
                                               only mandatory directory record
                                               attributes are included.
     -u <dicomdir>                             update existing directory file
                                               <dicomdir> "with references to
                                               DICOM files specified by file..
                                               or  directory.. arguments
        --undef-item-len                       encode all sequence items with
                                               undefined length; at default,
                                               only non-empty sequence items
                                               are encoded with undefined
                                               length
        --undef-seq-len                        encode all sequences with
                                               undefined length; at default,
                                               only non-empty sequences are
                                               encoded with undefined length
     -V,--version                              output version information and
                                               exit
     -w,--width <col>                          set line length; default: 78
     -z <dicomdir>                             compact existing directory file
                                               <dicomdir> by removing records
                                               with Record In-use Flag != 0
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
    $ dcmdir -l /media/cdrom/DICOMDIR
    list content of DICOMDIR to stdout
    -
    $ dcmdir -c disk99/DICOMDIR --fs-id DISK99 --fs-desc disk99/README
    disk99/DICOM
    create a new directory file with specified File-set ID and Descriptor
    File, referencing all DICOM Files in directory disk99/DICOM
    -
    $ dcmdir -u disk99/DICOMDIR disk99/DICOM/CT1
    add directory records referencing all DICOM files in directory
    disk99/DICOM/CT1 to existing directory file
    -
    $ dcmdir -d disk99/DICOMDIR disk99/DICOM/CT2
    delete/deactivate directory records referencing DICOM files in directory
    disk99/DICOM/CT2
    -
    $ dcmdir -p disk99/DICOMDIR
    delete/deactivate directory records without child records referencing any
    DICOM file
    -
    $ dcmdir -z disk99/DICOMDIR
    compact DICOMDIR by removing inactive records
    -
    $ dcmdir -c disk99/DICOMDIR --csv /path-to-csv-file.csv --record-config
    /dcm4che-assembly/src/etc/dcmdir/RecordFactory.xml disk99/DICOMDIR
    create a new directory file referencing all DICOM Files in directory
    disk99/DICOM and also referencing all records present in csv file
    -
