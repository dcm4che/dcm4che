```
usage: maskpxdata [options] -r <x>,<y>,<w>,<h> [...] --
                  <file>|<directory>...

The maskpxdata utility change the pixel value of rectangular regions of
uncompressed DICOM images to a particular value. It may be used to mask
(black out) information burned into the Pixel Data.
For each successfully updated file a dot (.) character is written to
stdout. For each file kept untouched, one of the characters:
p - no pixel data)
c - compressed pixel data)
is written to stdout. If an error occurs on updating a file, an E
character is written to stdout and a stack trace is written to stderr.
-
Options:
 -h,--help                     display this help and exit
    --pxval <arg>              pixel value to be set, either specified as
                               decimal value or - particularly for RGB
                               images - as hex HTML color code. 0
                               (#000000) by default.
 -r,--region <x>,<y>,<w>,<h>   rectangular region specified by top-left
                               corner <x>,<y> and width <w> and height <h>
                               inside which pixel values shall be changed.
 -V,--version                  output version information and exit
-
Example: maskpxdata -r 20,20,200,60 -r 500,20,200,60 -- image.dcm
=> Nullify the pixel values inside two rectangular regions of 200x60
pixels and top-left corners 20,20 and 500,20.
```
