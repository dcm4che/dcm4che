    usage: dcm2xml [<options>] <dicom-file>
    
    Convert <dicom-file> (or the standard input if <dicom-file> = '-') in XML
    presentation and optionally apply XSLT stylesheet on it. Writes result to
    standard output.
    -
    Options:
     -b,--with-bulkdata              include bulkdata directly in XML output;
                                     by default, only references to bulkdata
                                     are included.
     -B,--no-bulkdata                do not include bulkdata in XML output; by
                                     default, references to bulkdata are
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
     -I,--indent                     use additional whitespace in XML output
     -K,--no-keyword                 do not include keyword attribute of
                                     DicomAttribute element in XML output
     -V,--version                    output version information and exit
     -X,--blk-spec <xml-file>        specify bulkdata attributes explicitly by
                                     XML presentation in <xml-file>
     -x,--xsl <xsl-file>             apply specified XSLT stylesheet
        --xmlns                      include
                                     xmlns='http://dicom.nema.org/PS3.19/model
                                     s/NativeDICOM' attribute in root element
    Examples:
    $ dcm2xml image.dcm
    Write XML representation of DICOM file image.dcm to standard output,
    including only a reference to the pixel data in image.dcm
    $ dcm2xml --blk-file-dir=/tmp/pixeldata/ - < image.dcm
    Write XML representation of DICOM file image.dcm to standard output,
    including a reference to the extracted pixel data in file
    /tmp/pixeldata/blk#####.tmp
