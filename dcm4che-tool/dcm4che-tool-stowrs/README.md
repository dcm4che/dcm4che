    usage: stowrs [options]  -u <stowURL> <file[(s)...]>
    
    Reads metadata from specified files or parameters and send them to a
    stowRS service. Supports sending DICOM files or metadata files in JSON or XML.
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
    http://localhost/stow/studies[/{StudyInstanceUID}] img.dcm
    => Send stow request to stowRS Receiver with the attribute given and
    img.dcm DICOM file while overriding PatientName.
    
    -
    Example: stowrs -m PatientName=John^Doe -u
    http://localhost/stow/studies[/{StudyInstanceUID}] img.dcm
    => Send stow request to stowRS Receiver with the attribute given and
    img.dcm DICOM file while overriding PatientName.
    
    -
    Example: stowrs -u http://localhost:8080/dicom-web/DCM4CHEE/studies
     -t XML -f /somepath/image-metadata.xml
    => Send stow request to stowRS Receiver as metadata in dicom xml and 
    bulk data referenced in the metadata file.
    
    -
    Example: stowrs -u http://localhost:8080/dicom-web/DCM4CHEE/studies
     -t JSON -f /somepath/image-metadata.json
    => Send stow request to stowRS Receiver as metadata in dicom json and 
    bulk data referenced in the metadata file.
