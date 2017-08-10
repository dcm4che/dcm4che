    usage: stgcmtscu [options] [-b <aet>[:<port>] -c <aet>@<host>:<port>
                     [<file>..][<directory>..]
    
    The stgcmtscu application implements a Service Class User (SCU) for the
    Storage Commitment Push Model SOP Class. DICOM files specified on the
    command line are scanned and one or several Storage Commitment Requests
    (N-ACTION RQ) for the SOP Instances in the scanned files are sent to a
    Service Class Provider (SCP) of the Storage Commitment Push Model SOP
    Class. If no DICOM file is specified, it sends a DICOM C-ECHO message and
    waits for a response.\n-
    Options:
        --accept-timeout <ms>                 timeout in ms for receiving
                                              A-ASSOCIATE-AC, no timeout by
                                              default
     -b,--bind <aet[@ip][:port]>              specify AE Title, local address
                                              of the Application Entity
                                              provided by this application;
                                              use STGCMTSCU and pick up any
                                              valid local address to bind the
                                              socket by default. If also a
                                              port is specified, the
                                              Application Entity will
                                              listening for incoming
                                              association requests on it.
        --big-endian                          propose all uncompressed TS,
                                              explicit VR big endian first
                                              (default: implicit VR little
                                              endian first)
     -c,--connect <aet@host:port>             specify AE Title, remote address
                                              and port of the remote
                                              Application Entity.
        --connect-timeout <ms>                timeout in ms for TCP connect,
                                              no timeout by default
        --directory <path>                    directory to which received
                                              Storage Commitment Results are
                                              stored, '.' by default
        --explicit-vr                         propose all uncompressed TS,
                                              explicit VR little endian first
                                              (default: implicit VR little
                                              endian first)
     -h,--help                                display this help and exit
        --idle-timeout <ms>                   timeout in ms for receiving
                                              DIMSE-RQ, no timeout by default
        --ignore                              do not store received Storage
                                              Commitment Results in files
        --implicit-vr                         propose only implicit VR little
                                              endian (default: all
                                              uncompressed TS)
        --keep-alive                          keep association open for
                                              receiving Storage Commitment
                                              Results on same Association on
                                              which the Storage Commitment
                                              Request was invoked
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
        --one-per-series                      send one Storage Commitment
                                              Requests for each different
                                              Series, by default SOP Instances
                                              of all scanned files are
                                              referenced in one Storage
                                              Commitment Request
        --one-per-study                       send one Storage Commitment
                                              Requests for each different
                                              Study, by default SOP Instances
                                              of all scanned files are
                                              referenced in one Storage
                                              Commitment Request
        --proxy <[user:password@]host:port>   specify host and port of the
                                              HTTP Proxy to tunnel the DICOM
                                              connection.
        --release-timeout <ms>                timeout in ms for receiving
                                              A-RELEASE-RP, no timeout by
                                              default
        --request-timeout <ms>                timeout in ms for receiving
                                              A-ASSOCIATE-RQ, no timeout by
                                              default
        --response-timeout <ms>               timeout in ms for receiving
                                              outstanding response messages,
                                              no timeout by default
     -s <[seq/]attr=value>                    specify attributes to overwrite
                                              referenced object(s). attr can
                                              be specified by keyword or tag
                                              value (in hex), e.g. PatientName
                                              or 00100010.
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
        --status <code>                       specifies status code in
                                              returned N-EVENT-REPORT RSPs,
                                              0000H by default.
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
                                              Instance UID of referenced
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
    Example: stgcmtscu -b STGCMTSCU:11114 -c DCMQRSCP@localhost:11112
    path/to/study
    => Starts server listening on port 11114, accepting association requests
    with STGCMTSCU as called AE title, for receiving Storage Commitment
    Results, before sending a Storage Commitment Request for SOP Instances in
    directory path/to/study to Storage Service Class Provider DCMQRSCP,
    listening on local port 11112.
