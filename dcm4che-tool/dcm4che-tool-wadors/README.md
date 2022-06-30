    usage: wadors [options] [URLs...]

    Wado RS client simulator. It supports retrieving Study, Series, Instance,
    Metadata and Bulkdata. One may choose to specify multiple urls as
    arguments. Each of the objects of the Study/series shall be saved to the
    current working directory or the directory selected by user as
    <uid>-001.dicom, <uid>-002.dicom and so on. The uid is determined based on
    the url(s) specified. For eg. if study is retrieved the Study IUID will be
    used, if the url is for series retrieval then Series IUID shall be used.
    The extension of individual parts is determined by content type of each
    part. Supported accept types for retrieving bulkdata into multiparts are
    application/pdf (for PDF files), text/xml (for CDA files), image/jpeg,
    video/mpeg, video/mp4, video/quicktime, image/jp2, image/png, image/gif,
    application/sla or model/stl or model/x.stl-binary (for STL files),
    model/mtl (for MTL files), model/obj (for OBJ files) and
    application/vnd.genozip (for Genozip compressed genomic files).
    -
    Options:
     -a,--accept <arg>           Specify Acceptable Media Types for the
                                 response payload. Default: */* For eg.
                                 multipart/related;type=application/dicom;tran
                                 sfer-syntax=* or
                                 multipart/related;type=application/dicom or
                                 multipart/related;type=image/jpeg or
                                 multipart/related;type=application/dicom+xml
                                 If specified, by default it will be appended
                                 to the URL as a query parameter.
        --allowAnyHost           If the other server requires HTTPS and this
                                 option is specified, the other server’s
                                 certificate is validated via the truststore,
                                 but host name validation is not done.
        --bearer <bearer>        Specify the bearer token to be used in
                                 Authorization header for server
                                 authentication.
        --disableTM              If the other server requires HTTPS and this
                                 option is specified, a truststore need not be
                                 specified.
     -h,--help                   display this help and exit
     -H <httpHeader:value>       Specify additional HTTP headers to be sent in
                                 the invoked request.
        --header                 If specified, Accept value shall be sent as
                                 HTTP Request Header instead being appended to
                                 url as query parameter.
        --out-dir <directory>    Specify directory where the received MIME
                                 multipart messages will be unpacked into
                                 different parts.
     -u,--user <user:password>   Specify the user name and password to use for
                                 server authentication.
     -V,--version                output version information and exit
    
    Examples:
    => wadors
    http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/rs/studies/{StudyIUID1
    }
    Send WADO RS request to Wado RS Receiver to retrieve studies with Study
    Instance UID StudyIUID1
    
    => wadors
    http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/rs/studies/{StudyIUID1
    }
    http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/rs/studies/{StudyIUID2
    }/series/{SeriesIUID21}
    Send WADO RS request to Wado RS Receiver to retrieve studies with Study
    Instance UID StudyIUID1 and to retrieve series of study with Study
    Instance UID StudyIUID2 and series instance UID as SeriesIUID21.
    
    => wadors -a "multipart/related;type=image/jpeg"
    http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/rs/studies/{StudyIUID}
    /series/{SeriesIUID}/instances/{SOPIUID}
    Send WADO RS request to Wado RS Receiver to retrieve specified instance as
    a jpeg file.
    
    => wadors -a "multipart/related;type=application/vnd.genozip"
    http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/rs/studies/{StudyIUID}
    Send WADO RS request to Wado RS Receiver to retrieve all instances as a
    Genozip compressed genomic files.