```
usage: json2props <schema-dir> <props-dir>
or json2props <schema-dir> <props-dir> <out-schema-dir>

The json2props utility converts Archive configuration schema JSON files to
key-value properties files and vice versa to ease translation of attribute
names and descriptions to languages other than English.
-
Options:
 -h,--help      display this help and exit
 -V,--version   output version information and exit
-
Example: json2props src/main/webapp/assets/schema src/props
=> Convert the English version of the JSON Schema files to create / update
key/value properties files
-
Example: json2props src/main/webapp/assets/schema src/props/hi
src/main/webapp/assets/schema/hi
=> Converts translated versions of the key/value properties files for one
language back to JSON Schema files. If the translated key/value properties
files miss some (new) entries already defined in the English version of
the JSON Schema files in src/main/webapp/assets/schema, the created
language specific JSON Schema file are supplemented with the entries from
the English version.
-
Example: json2props src/main/webapp/assets/schema/hi src/props/hi
=> Converts the so created JSON Schema files (example 2) back to the
language specific key/value properties files. It will add the previous
missing entries in the key/value properties files, where title and
description in English separated by | can be replaced by translated terms.
```
