    usage: json2dcm [<options>] [-i <dicom-file>] -j <json-file> [-o
                    <dicom-file>]
    
    Convert <json-file> (or the standard input if <json-file> = '-') into a
    DICOM stream written to -o <dicom-file> or standard output if no -o option
    is specified. Optionally load DICOM file specified by -i <dicom-file> and
    merge attributes parsed from <json-file> with it.
    -
    Options:
     -B,--no-bulkdata                do not read bulkdata from -i <dicom-file>
     -b,--alloc-bulkdata             load bulkdata from -i <dicom-file> into
                                     memory; at default, bulkdata from -i
                                     <dicom-file> is streamed to -o
                                     <dicom-file> and not hold in memory
        --blk <[seq.]attr>           specify attribute by keyword or tag value
                                     (in hex) which shall be treated as
                                     bulkdata, e.g.
                                     --blk=IconImageSequence.PixelData or
                                     --blk=00880200.7FE00010. Multiple
                                     attributes can be specified by repeating
                                     the option for each attribute.
        --blk-file-prefix <prefix>   prefix for generating file names for
                                     extracted bulkdata; 'blk' by default
        --blk-file-suffix <suffix>   suffix for generating file names for
                                     extracted bulkdata; '.tmp' by default
        --blk-nodefs                 do NOT treat attributes listed by DICOM
                                     Composite Instance Retrieve Without Bulk
                                     Data Service Class as bulkdata, but only
                                     consider bulkdata attributes explicitly
                                     specified by --blk <[seq/]attr> and
                                     --blk-vr <vr[,...]=length>.
        --blk-vr <vr[,...]=length>   specify threshold for the value length
                                     for attributes with particular VRs which
                                     shall be treated as bulkdata, e.g.:
                                     --blk-vr LT,OB,OD,OF,OL,OW,UC,UN,UT=1024
                                     - treat all attributes with VR = LT or OB
                                     or OD or OF or OL or OW or UC or UN or UT
                                     which value length exceeds 1024 bytes as
                                     bulkdata.
     -c,--cat-blk-files              concatenate extracted bulkdata into one
                                     file
     -d,--blk-file-dir <directory>   directory were files with extracted
                                     bulkdata are stored if the DICOM stream
                                     to be merged is read from standard input;
                                     if not specified, files are stored into
                                     the default temporary-file directory
        --expl-item-len              encode sequence items with explicit
                                     length; at default, non-empty sequence
                                     items are encoded with undefined length
        --expl-seq-len               encode sequences with explicit length; at
                                     default, non-empty sequences are encoded
                                     with undefined length
     -F,--no-fmi                     store result always without File Meta
                                     Information. At default, the result is
                                     stored with File Meta Information if the
                                     JSON file or the input DICOM file
                                     specified by -i <dicom-file> contains a
                                     File Meta Information
     -f,--fmi                        store result always as DICOM Part 10 File
                                     with File Meta Information. At default,
                                     the result is only stored with File Meta
                                     Information if the JSON file or the input
                                     DICOM file specified by -i <dicom-file>
                                     contains a File Meta Information
        --group-len                  include (gggg,0000) Group Length
                                     attributes; at default, optional Group
                                     Length attributes are excluded
     -h,--help                       display this help and exit
     -i <dicom-file>                 load DICOM file to be merged with
                                     attributes parsed from -j <json-file>;
                                     set <dicom-file> = '-' to read DICOM
                                     stream from standard input
     -j <json-file>                  JSON file to convert to DICOM stream; set
                                     <json-file> = '-' to read JSON from
                                     standard input
        --keep-blk-files             do not delete extracted bulkdata after it
                                     was written into the generated DICOM
                                     stream.
     -o <dicom-file>                 store result into <dicom-file>; by
                                     default write DICOM stream to standard
                                     output
     -t,--transfer-syntax <uid>      store result with specified Transfer
                                     Syntax. At default use the Transfer
                                     Syntax of the input DICOM file or
                                     Explicit VR Little Endian if no input
                                     DICOM file was specified for generated
                                     DICOM Part 10 files, and Implicit VR
                                     Little Endian if no File Meta Information
                                     is included in the stored result
        --undef-item-len             encode all sequence items with undefined
                                     length; at default, only non-empty
                                     sequence items are encoded with undefined
                                     length
        --undef-seq-len              encode all sequences with undefined
                                     length; at default, only non-empty
                                     sequences are encoded with undefined
                                     length
     -V,--version                    output version information and exit
    -
    Examples:
    $ json2dcm -j in.json -o out.dcm
    Convert JSON presentation in.json to DICOM file out.dcm
