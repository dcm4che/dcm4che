```
usage: agfa2sr [<options>] <agfa-reports-file>

Extract concatenated XML Agfa Reports from specified file, converting them
to DICOM SR Documents by applying a XSL stylesheet and storing them as
DICOM Part 10 files into a specified directory.
-
Options:
 -d <out-dir>            directory to which resulting DICOM Files are
                         stored, '.' by default.
 -h,--help               display this help and exit
 -I,--indent             use additional whitespace in XML output.
    --lang <code>        Language of Content Item and Descendants in
                         format (CV, CSD, "CM"), '(en, RFC5646,
                         "English")' by default.
    --org <name>         Value of Verifying Organization (0040,A027) in
                         Verifying Observer Sequence (0040,A073) of
                         verified reports, reports, 'N/A' by default.
 -p <pattern>            file path of created DICOM Part 10 files,
                         '{ggggeeee}' will be replaced by the attribute
                         value, e.g.: '{00100020}/{0020000D}.dcm' will
                         store extracted objects using the Study Instance
                         UID (0020,000D) as file name and '.dcm' as file
                         name extension into sub-directories according its
                         Patient ID (0010,0020). At default, extracted
                         objects are stored to the storage directory with
                         the Study Instance UID (0020,000D) as file name
                         without extension.
 -s <[seq.]attr=value>   specify attributes added to created DICOM Part 10
                         files. attr can be specified by keyword or tag
                         value (in hex).
    --title <code>       Document Title in format (CV, CSD, "CM"),
                         '(18748-4, LN, "Diagnostic Imaging Report")' by
                         default.
 -V,--version            output version information and exit
 -x,--xsl <xsl-file>     apply XSLT stylesheet specified by file path or
                         URL or resource in the class path, agfa2sr.xsl by
                         default. Specify 'none' to disable XSLT, but
                         store extracted Agfa Reports verbatim using the
                         Study Instance UID as file name and '.xml' as
                         file name extension.
    --xml                disable conversion of output of XSLT into DICOM
                         Part 10 file, but store it verbatim using the
                         Study Instance UID as file name and '.xml' as
                         file name extension.
    --xml11              set version in XML declaration to 1.1; 1.0 by
                         default.
Example:
=> agfa2sr -s '00080005=ISO_IR 100' -s 'Manufacturer=AGFA' -- agfa_reports
Convert Agfa Reports from file agfa_reports to DICOM SR Documents, encoded
with Latin alphabet No.1 and with Manufacturer (0008,0070) 'AGFA', into
working directory.
```
