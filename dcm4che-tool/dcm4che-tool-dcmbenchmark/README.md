```
usage: dcmbenchmark [<options>] <dicom-file>

The dcmbenchmark utility parse a DICOM file repetitively, printing parsing
time and used memory as tab-separated values (TSV) to standard output.
-
Options:
 -a             Accumulate parsed datasets in memory.
 -g             Runs the garbage collector to free memory.
 -h,--help      display this help and exit
 -m <no>        Number of measurements, default: 10.
 -n <no>        Number of parsing per measurement; default: 10.
 -p             Read Pixel Data from file.
 -V,--version   output version information and exit
Example:
$ dcmbenchmark image.dcm
Read DICOM file image.dcm without Pixel Data 100 times with one
measurement per 10 parsings.
```
