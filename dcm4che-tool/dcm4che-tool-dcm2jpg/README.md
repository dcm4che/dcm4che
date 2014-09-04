    usage: dcm2jpg [<options>] <dicom-file> <jpeg-file>
                   or dcm2jpg [Options] <dicom-file>... <outdir>
                   or dcm2jpg [Options] <indir>... <outdir>
    
    Convert DICOM image(s) to JPEG(s) or other image formats
    -
    Options:
     -c,--windowCenter <center>   Window Center of linear VOI LUT function to
                                  apply
     -C <type>                    Compression Type of Image Writer to be used
     -E <class>                   ImageWriter class to be used for encoding,
                                  choose the first Image Writer found for
                                  given image format by default
     -F <format>                  output image format, JPEG by default
        --frame <number>          frame to convert, 1 (= first frame) by
                                  default
     -h,--help                    display this help and exit
        --lsE                     list available Image Writers for specified
                                  output image format
        --lsF                     list supported output image formats
        --noauto                  disable auto-windowing for images w/o VOI
                                  attributes
        --overlays <mask>         render overlays specified by bits 1-16 of
                                  <mask> in hex; FFFF by default.
        --ovlygray <value>        grayscale value of rendered overlays in hex;
                                  FFFF (= white) by default.
        --ps <file>               file path of presentation state to apply
     -q <quality>                 compression quality (0.0-1.0) of output
                                  image
        --suffix <suffix>         file extension used with destination
                                  directory argument,lower case format name by
                                  default
        --uselut                  use explicit VOI LUT in image, even if the
                                  image also specifies Window Center/Width;
                                  prefer applying Window Center/Width over
                                  explicit VOI LUT by default
     -V,--version                 output version information and exit
        --voilut <number>         use <number>. explicit VOI LUT, if the image
                                  provides several explicit VOI LUT; use 1. by
                                  default.
     -w,--windowWidth <width>     Window Width of linear VOI LUT function to
                                  apply
        --window <number>         use <number>. Window Center/Width value, if
                                  the image provides several Window
                                  Center/Width values; use 1. by default.
    -
    Example: dcm2jpg img.dcm img.jpg
    => Convert DICOM image 'img.dcm' to JPEG image 'img.jpg'
