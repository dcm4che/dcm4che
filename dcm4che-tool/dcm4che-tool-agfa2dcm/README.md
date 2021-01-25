```
usage: agfa2dcm [<options>] <agfa-blob-file>

Extract DICOM files from Agfa BLOB file.
-
Options:
 -d <out-dir>   directory to which extracted DICOM Files are stored, '.'
                by default
 -h,--help      display this help and exit
 -p <pattern>   file path of stored objects, '{ggggeeee}' will be replaced
                by the attribute value, e.g.:
                '{00100020}/{0020000D}/{0020000E}/{00080018}.dcm' will
                store extracted objects using the SOP Instance UID
                (0008,0018) as file name and '.dcm' as file name extension
                into sub-directories structured according its Patient ID
                (0010,0020), Study Instance UID (0020,000D} and Series
                Instance UID (0020,000E). At default, extracted objects
                are stored to the storage directory with the SOP Instance
                UID (0008,0018) as file name without extension.
 -V,--version   output version information and exit
Example:
=> agfa2dcm 3c951dcf.blob
Extract DICOM files from Agfa BLOB file 3c951dcf.blob to working directory
```
