    usage: mppsscu [options] -c <aet>@<host>:<port> [<file>..][<directory>..]
    
    The mppsscu application implements a Service Class User (SCU) for the
    Modality Performed Procedure Step (MPPS) SOP Class and for the
    Verification SOP Class. DICOM files specified on the command line may
    contain MPPS or Composite Objects. Specified MPPS objects are sent
    verbatim by one MPPS N-CREATE and one MPPS N-SET message to a Service
    Class Provider (SCP) of the MPPS SOP Class. For Composite Objects for each
    different Study a MPPS N-CREATE and a MPPS N-SET message referencing the
    SOP Instances is sent to the MPPS SCP. If no DICOM file is specified, it
    sends a DICOM C-ECHO message and waits for a response.
    -
    Options:
        --accept-timeout <ms>                 timeout in ms for receiving
                                              A-ASSOCIATE-AC, no timeout by
                                              default
        --archive <YES|NO>                    specify value for Archive
                                              Requested (0040,A494) included
                                              in MPPS N-SET RQ, by default
                                              attribute Archive Requested
                                              (0040,A494) will not be
                                              included.
     -b,--bind <aet[@ip][:port]>              specify AE Title, local address
                                              of the Application Entity
                                              provided by this application;
                                              use MPPSSCU and pick up any
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
        --code-config <file|url>              file path or URL of list of
                                              configured Discontinuation
                                              Reasons Codes to be used instead
                                              of etc/mppsscu/code.properties
        --connect-timeout <ms>                timeout in ms for TCP connect,
                                              no timeout by default
        --dc                                  send MPPS N-SET RQ with status
                                              DISCONTINUED instead of
                                              COMPLETED
        --dc-reason <code-value>              include Discontinuation Reason
                                              Code in MPPS N-SET RQ with
                                              status DISCONTINUED - must be
                                              one of the values specified by
                                              etc/mppsscu/code.properties or
                                              --code-config
        --explicit-vr                         propose all uncompressed TS,
                                              explicit VR little endian first
                                              (default: implicit VR little
                                              endian first)
     -h,--help                                display this help and exit
        --idle-timeout <ms>                   timeout in ms for receiving
                                              DIMSE-RQ, no timeout by default
        --implicit-vr                         propose only implicit VR little
                                              endian (default: all
                                              uncompressed TS)
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
        --ppsid <id>                          specify fix value for Performed
                                              Procedure Step ID (0040,0253);
                                              generate value from an
                                              incrementing number by default
        --ppsid-format <pattern>              specify pattern for generated
                                              Performed Procedure Step ID
                                              (0040,0253) from an incrementing
                                              number, PPS-0000000000 at
                                              default
        --ppsid-new                           include new Performed Procedure
                                              Step ID (0040,0253) in MPPS
                                              N-CREATE RQ, copy Performed
                                              Procedure Step ID from SOP
                                              Instances by default
        --ppsid-start <num>                   specify start value of
                                              incrementing number for
                                              generated Performed Procedure
                                              Step ID (0040,0253) according
                                              option --ppsid-format, random
                                              number by default
        --ppsuid <uid>                        specify Affected SOP Instance
                                              UID (0000,1000) in MPPS N-CREATE
                                              RQ. If several MPPS N-CREATE
                                              messages are invoked, the value
                                              will be suffixed by '.1',
                                              '.2',... . If not specified, the
                                              original SOP Instance UID of the
                                              specified MPPS or, if created
                                              from specified Composite
                                              Objects, no SOP Instance UID
                                              will be supplied in the MPPS
                                              N-CREATE RQ so the MPPS SCP.
        --protocol <name>                     specify default value for
                                              Protocol Name (0018,1030)
                                              included in the MPPS N-SET RQ if
                                              the SOP Instances does not
                                              contain a Protocol Name; UNKNOWN
                                              by default
        --proxy <[user:password@]host:port>   specify host and port of the
                                              HTTP Proxy to tunnel the DICOM
                                              connection.
        --release-timeout <ms>                timeout in ms for receiving
                                              A-RELEASE-RP, no timeout by
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
    Example: mppsscu -c MPPSSCP@localhost:11112 path/to/study
    => Scan images in directory  path/to/study and send a MPPS N-CREATE RQ and
    a MPPS N-SET RQ referencing the images to MPPSSCP, listening on local port
    11112.
