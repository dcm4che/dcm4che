    usage: dcm2json [<options>] <dicom-file>
    
    Convert <dicom-file> (or the standard input if <dicom-file> = '-') in JSON
    presentation. Writes result to standard output.
    -
    Options:
     -b,--with-bulkdata              include bulkdata directly in JSON output;
                                     by default, only references to bulkdata
                                     are included.
     -B,--no-bulkdata                do not include bulkdata in JSON output;
                                     by default, references to bulkdata are
                                     included.
        --blk-file-prefix <prefix>   prefix for generating file names for
                                     extracted bulkdata; 'blk' by default
        --blk-file-suffix <suffix>   directory were files with extracted
                                     bulkdata are stored if the DICOM object
                                     is read from "standard input; if not
                                     specified, files are stored into the
                                     default temporary-file directory
     -c,--cat-blk-files              concatenate extracted bulkdata into one
                                     file
     -d,--blk-file-dir <directory>   directory were files with extracted
                                     bulkdata are stored if the DICOM object
                                     is read from "standard input; if not
                                     specified, files are stored into the
                                     default temporary-file directory
     -h,--help                       display this help and exit
     -I,--indent                     use additional whitespace in JSON output
     -J,--blk-spec <json-file>       specify bulkdata attributes explicitly by
                                     JSON presentation in <xml-file>
     -V,--version                    output version information and exit
    
    Examples:
    $ dcm2json image.dcm
    Write JSON representation of DICOM file image.dcm to standard output,
    including only a reference to the pixel data in image.dcm
    $ dcm2json --blk-file-dir=/tmp/pixeldata/ - < image.dcm
    Write JSON representation of DICOM file image.dcm to standard output,
    including a reference to the extracted pixel data in file
    /tmp/pixeldata/blk#####.tmp
