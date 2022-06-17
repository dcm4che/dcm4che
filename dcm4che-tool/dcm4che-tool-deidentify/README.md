```
usage: deidentify [Options] <infile> <outfile>
or deidentify [Options] <infile>... <outdir>
or deidentify [Options] <indir>... <outdir>

De-identify one or several DICOM files according the Basic Application
Level Confidentiality Profile specified in DICOM Part 15.
-
Options:
    --expl-item-len     encode sequence items with explicit length; at
                        default, non-empty sequence items are encoded with
                        undefined length
    --expl-seq-len      encode sequences with explicit length; at default,
                        non-empty sequences are encoded with undefined
                        length
    --group-len         include (gggg,0000) Group Length attributes; at
                        default, optional Group Length attributes are
                        excluded
 -h,--help              display this help and exit
    --retain-date       retain any dates and times present in the
                        Attributes
    --retain-dev        retain information about the identity of the
                        device in the Attributes
    --retain-org        retain information about the identity of the
                        institution in the Attributes
    --retain-pid-hash   retain hashed Patient ID in the Attributes
    --retain-uid        retain UIDs in the Attributes
 -s <attr=value>        specify dummy values for replaced Attributes. attr
                        can be specified by keyword or tag value (in hex),
                        e.g. PatientName or 00100010.
    --undef-item-len    encode all sequence items with undefined length;
                        at default, only non-empty sequence items are
                        encoded with undefined length
    --undef-seq-len     encode all sequences with undefined length; at
                        default, only non-empty sequences are encoded with
                        undefined length
 -V,--version           output version information and exit
Examples:
$ deidentify --retain-uid -sPatientName=ANONYMIZED -sPatientID=0815 --
in.dcm out.dcm
De-identify DICOM file in.dcm to out.dcm, retaining UIDs in the
Attributes, setting Patient Name und Patient ID to the specified values.
```
