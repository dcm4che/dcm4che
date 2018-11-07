    usage: wadors [options]
    
    Wado RS client simulator.
    -
    Options:
     -a,--accept <arg>           Specify the value for Accept header. The
                                 value of Accept header will then be sent in
                                 request header.
     -h,--help                   display this help and exit
        --out-dir <directory>    specifies directory where the received MIME
                                 multipart messages will be unpacked into
                                 different parts.
     -u,--user <user:password>   Specify the user name and password to use for
                                 server authentication.
        --url <url>              Specify the request URL.
     -V,--version                output version information and exit
    -
    Example: wadors --url
    http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/rs/studies/{StudyIUID}
    => Send WADO RS request to Wado RS Receiver.