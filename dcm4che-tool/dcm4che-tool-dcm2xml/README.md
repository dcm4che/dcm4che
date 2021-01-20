    usage: dcm2xml [<options>] <dicom-file>
    
    Convert <dicom-file> (or the standard input if <dicom-file> = '-') in XML
    presentation and optionally apply XSLT stylesheet on it. Writes result to
    standard output.
    -
    Options:
     -B,--no-bulkdata                do not include bulkdata in XML output; by
                                     default, references to bulkdata are
                                     included.
     -b,--with-bulkdata              include bulkdata directly in XML output;
                                     by default, only references to bulkdata
                                     are included.
        --blk <[seq.]attr>           specify attribute by keyword or tag value
                                     (in hex) which shall be treated as
                                     bulkdata, e.g.
                                     --blk=IconImageSequence.PixelData or
                                     --blk=00880200.7FE00010. Multiple
                                     attributes can be specified by repeating
                                     the option for each attribute.
        --blk-file-prefix <prefix>   prefix for generating file names for
                                     extracted bulkdata; 'blk' by default
        --blk-file-suffix <suffix>   directory were files with extracted
                                     bulkdata are stored if the DICOM object
                                     is read from "standard input; if not
                                     specified, files are stored into the
                                     default temporary-file directory
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
                                     bulkdata are stored if the DICOM object
                                     is read from "standard input; if not
                                     specified, files are stored into the
                                     default temporary-file directory
     -h,--help                       display this help and exit
     -I,--indent                     use additional whitespace in XML output
     -K,--no-keyword                 do not include keyword attribute of
                                     DicomAttribute element in XML output
     -V,--version                    output version information and exit
     -x,--xsl <xsl-file>             apply XSLT stylesheet specified by file
                                     path or URL
        --xml11                      set version in XML declaration to 1.1;
                                     1.0 by default
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
