usage: stowrs [options]  -u <stowURL> [<bulkDataFile>..]

Reads metadata from specified files or parameters and send them to a
stowRS Receiver Attributes can be specified using -m <metadata> or -f file
or if bulk data is DICOM thenno such attributes are to be specified.
 -f,--file <arg>            specify the file containing the metadata in
                            JSON or XML, In which case metadata-type
                            should be specified.
 -h,--help                  display this help and exit
 -m <[seq/]attr=value>      specify metadata attributes. attr can be
                            specified by keyword or tag value (in hex),
                            e.g. PatientName or 00100010. Attributes in
                            nested Datasets can be specified by including
                            the keyword/tag value of the sequence
                            attribute, e.g. 00400275/00400009 for
                            Scheduled Procedure Step ID in the Request.
 -t,--metadata-type <arg>   specify metadata type as JSON or XML.
 -u,--url <arg>             specify the request URL.
 -V,--version               output version information and exit
-
Example: stowrs -m PatientName=John^Doe -u
http://localhost/stow/studies[/{StudyInstanceUID}] img.jpeg
=> Send stow request to stowRS Receiver with the attribute given and
img.jpeg bulkData.
