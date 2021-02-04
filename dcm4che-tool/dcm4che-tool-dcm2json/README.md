```
usage: dcm2json [<options>] <dicom-file>

Convert <dicom-file> (or the standard input if <dicom-file> = '-') in JSON
presentation. Writes result to standard output.
-
Options:
 -B,--no-bulkdata                do not include bulkdata in JSON output;
                                 by default, references to bulkdata are
                                 included.
 -b,--with-bulkdata              include bulkdata directly in JSON output;
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
                                 bulkdata are stored if the DICOM object
                                 is read from "standard input; if not
                                 specified, files are stored into the
                                 default temporary-file directory
 -h,--help                       display this help and exit
 -I,--indent                     use additional whitespace in JSON output
 -N,--encode-as-number           encode IS, SV and UV values in the range
                                 [-(2^53)+1, (2^53)-1] and valid DS values
                                 as JSON numbers; by default DS, IS, SV
                                 and UV values are encoded as JSON
                                 strings.
 -V,--version                    output version information and exit

Examples:
$ dcm2json image.dcm
Write JSON representation of DICOM file image.dcm to standard output,
including only a reference to the pixel data in image.dcm
$ dcm2json --blk-file-dir=/tmp/pixeldata/ - < image.dcm
Write JSON representation of DICOM file image.dcm to standard output,
including a reference to the extracted pixel data in file
/tmp/pixeldata/blk#####.tmp
```
