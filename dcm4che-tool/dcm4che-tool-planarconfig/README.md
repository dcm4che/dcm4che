```
usage: planarconfig [-v] [--uids] [--3x3 MIN MAX] [--fix[0|1]]
                    <file>|<directory>...

The planarconfig utility detects the actual planar configuration of
uncompressed pixel data of color images with Photometric Interpretation
RGB or YBR_FULL and optionally correct non matching values of attribute
Planar Configuration of the image.

The average chroma (s. https://en.wikipedia.org/wiki/HSL_and_HSV) over all
pixels and the sum of absolute differences of sample values of adjoining pixels
is calculated and resulting values on assuming color-by-pixel or color-by-plane
planar configuration are compared. A lower value for the calculated average
chroma and for the sum of absolute differences of sample values of adjoining
indicates that the assumed planar configuration is correct.

If the calculated values of the average chroma and the sum of absolute
differences of sample values of adjoining pixels indicate contradictory planar
configurations, the average difference of sample values between 3x3 tiles
assuming color-by-pixel is calculated additionally. A value lesser than the
specified lower threshold (default: 10) indicates a color-by-plane, a value
greater than the specified upper threshold (default: 20) a color-by-pixel
planar configuration.

Otherwise, if the significance of the difference in the average chroma is
greater than the significance of the difference in the sum of absolute
differences of sample values, the detected planar configuration is which
resulted in the lesser chroma value - otherwise the detected planar
configuration is which resulted in lesser differences of sample values of
adjoining pixels.

For each processed file one of the characters:
p - no pixel data
c - compressed pixel data
m - monochrome (or palette color) pixel data
0 - detected color-by-pixel planar configuration, matching with value 0
    of attribute Planar Configuration
O - detected color-by-pixel planar configuration, NOT matching with value 1
    of attribute Planar Configuration
1 - detected color-by-plane planar configuration, matching with value 1
    of attribute Planar Configuration
I - detected color-by-plane planar configuration, NOT matching with value 0
    of attribute Planar Configuration
is written to stdout.
If an error occurs on processing a file, an E character is written to stdout
and a stack trace is written to stderr.

Options:
--uids         log SOP Instance UIDs of files with not matching value of
               attribute Planar Configuration in file './uids.log'.
--3x3 MIN MAX  thresholds for 3x3 pattern detection; 10 20 by default
--fix          fix all files with NOT matching value of attribute Planar
               Configuration
--fix0         fix value of attribute Planar Configuration with detected
               color-by-pixel planar configuration to 0
--fix1         fix value of attribute Planar Configuration with detected
               color-by-plane planar configuration to 1
-v             displays average sample difference between 3x3 tiles, average
               chroma and sample differences of adjoining pixels in format:
               3x3=<avg-tile-diff>,
               chroma=[<color-by-pixel>, <color-by-plane>, <significance>],
               diff=[<color-by-pixel>, <color-by-plane>, <significance>]
               for each processed file.
```
