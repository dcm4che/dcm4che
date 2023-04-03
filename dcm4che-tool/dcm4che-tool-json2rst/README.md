```
usage: json2rst <path-to-device.schema.json> <output-dir>
                [<tabular-columns>]

The json2rst utility generates ReStructuredText files from Archive
configuration schema JSON files used for documentation
of Archive configuration attributes in the DICOM Conformance Statement.
The <tabular-columns> sets the width of table columns in the generated rst
files, with default being as |p{4cm}|l|p{8cm}|
-
Options:
 -h,--help      display this help and exit
 -V,--version   output version information and exit
-
Example: json2rst
/work/dcm4chee-arc-lang/src/main/webapp/assets/schema/exportRule.schema.js
on /work/dcm4chee-arc-cs/docs/networking/config "|p{6cm}|l|p{10cm}|"
=> Convert configuration fields and their descriptions specified in
exportRule.schema.json file to its corresponding exportRule.rst file
created in the specified output directory, with specified column widths
for the table rendered in exportRule.html (on building html pages from rst
files)
```
