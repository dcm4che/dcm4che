    usage: storescu [options] -c <aet>@<host>:<port> [<file>..][<directory>..]
    
    The storescu application implements a Service Class User (SCU) for the
    Storage Service Class and for the Verification SOP Class. For each DICOM
    file on the command line it sends a C-STORE message to a Storage Service
    Class Provider (SCP) and waits for a response. If no DICOM file is
    specified, it sends a DICOM C-ECHO message and waits for a response. The
    application can be used to transmit DICOM images and other DICOM composite
    objects and to verify basic DICOM connectivity.
    -
    Options:
        --accept-timeout <ms>                 timeout in ms for receiving
                                              A-ASSOCIATE-AC, no timeout by
                                              default
     -b,--bind <aet[@ip][:port]>              specify AE Title, local address
                                              of the Application Entity
                                              provided by this application;
                                              use STORESCU and pick up any
                                              valid local address to bind the
                                              socket by default. If also a
                                              port is specified, the
                                              Application Entity will
                                              listening for incoming
                                              association requests on it.
     -c,--connect <aet@host:port>             specify AE Title, remote address
                                              and port of the remote
                                              Application Entity.
        --connect-timeout <ms>                timeout in ms for TCP connect,
                                              no timeout by default
     -h,--help                                display this help and exit
        --idle-timeout <ms>                   timeout in ms for receiving
                                              DIMSE-RQ, no timeout by default
        --input-file <file>                   file containing paths of files
                                              to be sent instead of reading
                                              them as arguments from the
                                              command line
        --key-pass <password>                 password for accessing the key
                                              in the key store, key store
                                              password by default
        --key-store <file|url>                file path or URL of key store
                                              containing the private key,
                                              resource:key.jks by default
        --key-store-pass <password>           password for key store
                                              containing the private key,
                                              'secret' by default
        --key-store-type <storetype>          type of key store containing the
                                              private key, JKS by default
        --max-ops-invoked <no>                maximum number of operations
                                              this AE may invoke
                                              asynchronously, unlimited by
                                              default
        --max-ops-performed <no>              maximum number of operations
                                              this AE may perform
                                              asynchronously, unlimited by
                                              default
        --max-pdulen-rcv <length>             specifies maximal length of
                                              received P-DATA TF PDUs
                                              communicated during association
                                              establishment. 0 indicates that
                                              no maximum length is specified.
                                              16378 by default
        --max-pdulen-snd <length>             specifies maximal length of sent
                                              P-DATA-TF PDUs by this AE. The
                                              actual maximum length of sent
                                              P-DATA-TF PDUs is also limited
                                              by the maximal length of
                                              received P-DATA-TF PDUs of the
                                              peer AE communicated during
                                              association establishment. 16378
                                              by default
        --not-async                           do not use asynchronous mode;
                                              equivalent to
                                              --max-ops-invoked=1 and
                                              --max-ops-performed=1
        --not-pack-pdv                        send only one PDV in one
                                              P-Data-TF PDU; pack command and
                                              data PDV in one P-DATA-TF PDU by
                                              default
        --prior-high                          set HIGH priority in invoked
                                              DIMSE-C operation, MEDIUM by
                                              default
        --prior-low                           set LOW priority in invoked
                                              DIMSE-C operation, MEDIUM by
                                              default
        --proxy <[user:password@]host:port>   specify host and port of the
                                              HTTP Proxy to tunnel the DICOM
                                              connection.
        --rel-ext-neg                         enable SOP Class Relationship
                                              Extended Negotiation
        --rel-sop-classes <file|url>          file path or URL of definition
                                              of Related General SOP Classes,
                                              resource:rel-sop-classes.propert
                                              ies by default
        --release-timeout <ms>                timeout in ms for receiving
                                              A-RELEASE-RP, no timeout by
                                              default
        --response-timeout <ms>               timeout in ms for receiving
                                              outstanding response messages,
                                              no timeout by default
     -s <[seq/]attr=value>                    specify attributes added to the
                                              sent object(s). attr can be
                                              specified by keyword or tag
                                              value (in hex), e.g. PatientName
                                              or 00100010. Attributes in
                                              nested Datasets can be specified
                                              by including the keyword/tag
                                              value of the sequence attribute,
                                              e.g. 00400275/00400009 for
                                              Scheduled Procedure Step ID in
                                              the Request Attributes Sequence.
        --soclose-delay <ms>                  delay in ms after sending
                                              A-ASSOCATE-RJ, A-RELEASE-RQ or
                                              A-ABORT before the socket is
                                              closed; 50ms by default
        --sorcv-buffer <length>               set SO_RCVBUF socket option to
                                              specified value
        --sosnd-buffer <length>               set SO_SNDBUF socket option to
                                              specified value
        --ssl2Hello                           send/accept SSLv3/TLS
                                              ClientHellos encapsulated in a
                                              SSLv2 ClientHello packet;
                                              equivalent to --tls-protocol
                                              SSLv2Hello --tls-protocol SSLv3
                                              --tls-protocol TLSv1
                                              --tls-protocol TLSv1.1
                                              --tls-protocol TLSv1.2
        --ssl3                                enable only TLS/SSL protocol
                                              SSLv3; equivalent to
                                              --tls-protocol SSLv3
        --tcp-delay                           set TCP_NODELAY socket option to
                                              false, true by default
        --tls                                 enable TLS connection without
                                              encryption or with AES or 3DES
                                              encryption; equivalent to
                                              --tls-cipher
                                              SSL_RSA_WITH_NULL_SHA
                                              --tls-cipher
                                              TLS_RSA_WITH_AES_128_CBC_SHA
                                              --tls-cipher
                                              SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-3des                            enable TLS connection with 3DES
                                              encryption; equivalent to
                                              --tls-cipher
                                              SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-aes                             enable TLS connection with AES
                                              or 3DES encryption; equivalent
                                              to --tls-cipher
                                              TLS_RSA_WITH_AES_128_CBC_SHA
                                              --tls-cipher
                                              SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-cipher <cipher>                 enable TLS connection with
                                              specified Cipher Suite. Multiple
                                              Cipher Suites may be enabled by
                                              multiple --tls-cipher options
        --tls-noauth                          disable client authentification
                                              for TLS
        --tls-null                            enable TLS connection without
                                              encryption; equivalent to
                                              --tls-cipher
                                              SSL_RSA_WITH_NULL_SHA
        --tls-protocol <protocol>             TLS/SSL protocol to use. Multiple
                                              TLS/SSL protocols may be enabled
                                              by multiple --tls-protocol
                                              options. Supported values by
                                              SunJSSE 1.8: TLSv1.2, TLSv1.1,
                                              TLSv1, SSLv3, SSLv2Hello. By
                                              default, TLSv1.2, TLSv1.1, TLSv1
                                              and SSLv3 are enabled.
        --tls1                                enable only TLS/SSL protocol
                                              TLSv1; equivalent to
                                              --tls-protocol TLSv1
        --tls11                               enable only TLS/SSL protocol
                                              TLSv1.1; equivalent to
                                              --tls-protocol TLSv1.1
        --tls12                               enable only TLS/SSL protocol
                                              TLSv1.2; equivalent to
                                              --tls-protocol TLSv1.2
        --tmp-file-dir <directory>            directory were temporary file
                                              with File Meta Information from
                                              scanned files is stored; if not
                                              specified, the file is stored
                                              into the default temporary-file
                                              directory
        --tmp-file-prefix <prefix>            prefix for generated file name
                                              for temporary file; 'storescu-'
                                              by default
        --tmp-file-suffix <suffix>            suffix for generated file name
                                              for temporary file; '.tmp' by
                                              default
        --trust-store <file|url>              file path of key store
                                              containing trusted certificates,
                                              resource:cacerts.jks by default
        --trust-store-pass <password>         password for key store with
                                              trusted certificates, 'secret'
                                              by default
        --trust-store-type <storetype>        type of key store with trusted
                                              certificates, JKS by default
        --uid-suffix <suffix>                 specify suffix to be appended to
                                              the Study, Series and SOP
                                              Instance UID of the sent
                                              object(s).
        --user <name>                         negotiate user identity with
                                              specified user name
        --user-pass <password>                negotiate user identity with
                                              specified password
        --user-rsp                            negotiate user identity with
                                              positive response requested
     -V,--version                             output version information and
                                              exit
    -
    Example: storescu -c STORESCP@localhost:11112 image.dcm
    => Send DICOM image image.dcm to Storage Service Class Provider STORESCP,
    listening on local port 11112.