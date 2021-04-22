```
usage: dcm2jpg [<options>] <dicom-file> <jpeg-file>
or dcm2jpg [Options] <dicom-file>... <outdir>
or dcm2jpg [Options] <indir>... <outdir>

Convert DICOM image(s) to JPEG(s) or other image formats.
-
Options:
 -C <type>                    Compression Type of Image Writer to be used
 -c,--windowCenter <center>   Window Center of linear VOI LUT function to
                              apply
 -E <class>                   ImageWriter class to be used for encoding,
                              com.sun.imageio.plugins.* (= JDK ImageIO
                              plugins) by default
 -F <format>                  output image format, JPEG by default
    --frame <number>          frame to convert, 1 (= first frame) by
                              default
 -h,--help                    display this help and exit
    --iccprofile <profile>    specifies the color characteristics of, and
                              inclusion of an ICC Profile in the rendered
                              image:
                              - no: include no ICC profile
                              - yes: include the ICC Profile specified in
                              the DICOM image, otherwise the sRGB ICC
                              profile
                              - srgb: include sRGB ICC profile and
                              transform original pixels to sRGB color
                              space if an ICC Profile is specified in the
                              DICOM image
                              - adobergb: include Adobe RGB ICC profile
                              and transform original pixels to Adobe RGB
                              color space
                              - rommrgb: include ROMM RGB ICC profile and
                              transform original pixels to ROMM RGB color
                              space
                              By default, include no ICC profile, but
                              transform original pixels to sRGB color
                              space if an ICC Profile is specified in the
                              DICOM image.
    --lsE                     list available Image Writers for specified
                              output image format
    --lsF                     list supported output image formats
    --noauto                  disable auto-windowing for images w/o VOI
                              attributes
    --noshape                 ignore present (2050,0020) Presentation LUT
                              Shape; prioritize value of (0028,0004)
                              Photometric Interpretation to determine if
                              minimum sample value is intended to be
                              displayed as white (=MONCHROME1) or as black
                              (=MONCHROME2)
    --overlays <mask>         render overlays specified by bits 1-16 of
                              <mask> in hex; FFFF by default.
    --ovlygray <value>        grayscale value of rendered overlays in hex;
                              FFFF (= white) by default.
    --ovlyrgb <value>         color of rendered overlays as RGB color
                              code; #ffffff (= white) by default.
    --ps <file>               file path of presentation state to apply
 -q <quality>                 compression quality (0.0-1.0) of output
                              image
    --suffix <suffix>         file extension used with destination
                              directory argument,lower case format name by
                              default
    --usedis                  use DicomInputStream for reading the DICOM
                              image. Supports deflated transfer syntaxes
                              (default, without option --frame <number>)
    --useiis                  use ImageInputStream for reading the DICOM
                              image (default, with option --frame
                              <number>)
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
```