usage: jpg2dcm [Options] <jpgfile> <dcmfile>
Encapsulate JPEG Image into DICOM Object.
Options:
 -c <file>                    Specifies DICOM attributes included
                              additional to mandatory defaults
 -C <file>                    Specifies DICOM attributes included instead
                              of mandatory defaults
    --charset <code>          Specific Character Set code string, ISO_IR
                              100 by default
 -h,--help                    Print this message
    --mpeg                    Same as --transfer-syntax
                              1.2.840.10008.1.2.4.100 (MPEG2).
    --no-appn                 Exclude application segments APPn from JPEG
                              stream; encapsulate JPEG stream verbatim by
                              default.
    --transfer-syntax <uid>   Transfer Syntax; 1.2.840.10008.1.2.4.50
                              (JPEG Baseline) by default.
    --uid-prefix <prefix>     Generate UIDs with given prefix,
                              1.2.40.0.13.1.<host-ip> by default.
 -V,--version                 Print the version information and exit
--
Example 1: Encapulate JPEG Image verbatim with default values for
mandatory DICOM attributes into DICOM Secondary Capture Image:
$ jpg2dcm -c jpg2dcm.xml image.jpg image.dcm
--
Example 2: Encapulate JPEG Image without application segments and
additional DICOM attributes to mandatory defaults into DICOM Image Object:
$ jpg2dcm --no-appn -c patattrs.cfg homer.jpg image.dcm
--
Example 3: Encapulate MPEG2 Video with specified DICOM attributes into
DICOM Video Object:
$ jpg2dcm --mpeg -c mpg2dcm.xml video.mpg video.dcm
