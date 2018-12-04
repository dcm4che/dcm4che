    usage: wadows [options]
    
    Wado WS client simulator. It supports RetrieveImagingDocumentSet request
    which shall retrieve a set of DICOM instances and other objects. It also
    supports RetrieveRenderedImagingDocumentSet request which shall retrieve a
    set of DICOM instances that have been rendered into the requested format.
    For example, if rendering into JPEG was requested, these will be the JPEG
    renderings of the requested set of DICOM Objects. The UIDs can be
    specified either by KOS file (using -f option) or by specifying
    independently (using --study option)
    -
    Options:
        --columns <arg>                specify columns for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st.
     -f,--file <file>                  specify DICOM file containing Key
                                       Object Selection Document.
        --frame-number <arg>           specify frame number for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st.
     -h,--help                         display this help and exit
        --image-quality <arg>          specify image quality for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st.
        --out-dir <directory>          specifies directory where the received
                                       MIME multipart messages will be
                                       unpacked into different parts.
        --rendered                     indicates if request should be
                                       RetrieveRenderedImagingDocumentSetReque
                                       st. If absent
                                       RetrieveImagingDocumentSetRequest will
                                       be used by default.
        --repository-unique-id <uid>   specify Repository Unique ID.
        --rows <arg>                   specify rows for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st.
        --study <arg>                  Specify Study/Series/Instance UIDs in
                                       the format eg.
                                       StudyIUID[SeriesIUID1[SOPIUID11,SOPIUID
                                       12],SeriesIUID2[SOPIUID21,SOPIUID22]].
                                       This example shows 2 series of a study
                                       each with 2 instances shall be
                                       retrieved. One may specify multiple
                                       instances of multiple series of a study
                                       similar to example shown above. The
                                       value shall include at least one SOP
                                       Instance UID i.e.
                                       StudyIUID[SeriesIUID[SOPIUID]]
     -t,--contentType <arg>            specify one or more content types for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st. If none are specified, system will
                                       use image/jpeg by default.
        --tsuid <arg>                  specify one or more transfer syntax
                                       UIDs for
                                       RetrieveImagingDocumentSetRequest. If
                                       none are specified, system will use
                                       Explicit VR Little Endian by default.
        --url <url>                    Specify the request URL.
     -V,--version                      output version information and exit
        --window-center <arg>          specify window center for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st.
        --window-width <arg>           specify window width for
                                       RetrieveRenderedImagingDocumentSetReque
                                       st.
    -
    Example: wadows --url
    http://<host>:<port>/dcm4chee-arc/xdsi/ImagingDocumentSource --rendered
    --study
    '1.113654.3.13.1026[1.113654.5.14.1035[1.113654.5.15.1504,1.113654.5.15.15
    12]]'
    => Send RetrieveRenderedImagingDocumentSetRequest to Wado WS Receiver with
    specified StudyIUID[SeriesIUID[SOPIUID1,SOPIUID2]] with default content
    type as image/jpeg.
    -
    Example: wadows --url
    http://<host>:<port>/dcm4chee-arc/xdsi/ImagingDocumentSource -f kos.dcm
    => Send RetrieveImagingDocumentSet to Wado WS Receiver with default
    Transfer Syntax UID as Explicit VR Little Endian and Key Object Selection
    Document having Current Requested Procedure Evidence Sequence which
    contains StudyIUID, SeriesIUID(s) and DocumentUniqueID(s).