    usage: wadows [options]
    
    Wado WS client simulator.
    -
    Options:
        --columns <columns>                             specify columns for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
     -f,--file <file>                                   specify DICOM file
                                                        containing Key Object
                                                        Selection Document.
        --frame-number <frame-number>                   specify frame number
                                                        for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
     -h,--help                                          display this help and
                                                        exit
        --image-quality <image-quality>                 specify image quality
                                                        for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
        --out-dir <directory>                           specifies directory
                                                        where the received
                                                        MIME multipart
                                                        messages will be
                                                        unpacked into
                                                        different parts.
        --repository-unique-id <repository-unique-id>   specify Repository
                                                        Unique ID.
        --request-type <request-type>                   specify request type
                                                        if it should be
                                                        RetrieveImagingDocumen
                                                        tSetRequest or
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
        --rows <rows>                                   specify rows for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
        --study <study>                                 specify
                                                        Study/Series/Instance
                                                        UIDs.
     -t,--contentType <arg>                             specify one or more
                                                        content types for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
                                                        If none are specified,
                                                        system will use
                                                        image/jpeg by default.
        --tsuid <arg>                                   specify one or more
                                                        transfer syntax UIDs
                                                        for
                                                        RetrieveImagingDocumen
                                                        tSetRequest. If none
                                                        are specified, system
                                                        will use Explicit VR
                                                        Little Endian by
                                                        default.
        --url <url>                                     Specify the request
                                                        URL.
     -V,--version                                       output version
                                                        information and exit
        --window-center <window-center>                 specify window center
                                                        for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
        --window-width <window-width>                   specify window width
                                                        for
                                                        RetrieveRenderedImagin
                                                        gDocumentSetRequest.
    -
    Example: wadows --url
    http://<host>:<port>/dcm4chee-arc/xdsi/ImagingDocumentSource
    --request-type RetrieveRenderedImagingDocumentSetRequest --study
    '1.113654.3.13.1026(1.113654.5.14.1035(1.113654.5.15.1504,1.113654.5.15.15
    12))'
    => Send RetrieveRenderedImagingDocumentSetRequest to Wado WS Receiver with
    specified StudyIUID(SeriesIUID(SOPIUID1,SOPIUID2)) with default content
    type as image/jpeg.
    -
    Example: wadows --url
    http://<host>:<port>/dcm4chee-arc/xdsi/ImagingDocumentSource
    --request-type RetrieveImagingDocumentSetRequest -f kos.dcm
    => Send RetrieveImagingDocumentSet to Wado WS Receiver with default
    Transfer Syntax UID as Explicit VR Little Endian and Key Object Selection
    Document having Current Requested Procedure Evidence Sequence which
    contains StudyIUID, SeriesIUID(s) and DocumentUniqueID(s).