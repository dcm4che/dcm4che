    usage: movescu [options] -c <aet>@<host>:<port> --dest <aet>
                   [dcmfile_in...]
    
    The movescu application implements a Service Class User (SCU) for the Query/Retrieve, the 
    Composite Instance Root Retrieve, the Composite Instance Retrieve Without Bulk Data, the 
    Hanging Protocol Query/Retrieve and the Color Palette Query/Retrieve Service Class. movescu 
    only supports retrieve functionality using the C-MOVE message. It sends matching keys to an 
    Service Class Provider (SCP) and waits for responses. Matching keys can be specified in 
    DICOM file(s) dcmfile_in or by options -m.
    -
    Options:
        --accept-timeout <ms>                 timeout in ms for receiving
                                              A-ASSOCIATE-AC, no timeout by
                                              default
     -b,--bind <aet[@ip][:port]>              specify AE Title, local address
                                              of the Application Entity
                                              provided by this application;
                                              use MOVESCU and pick up any
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
        --dest <aet>                          specifies AE title of the Move
                                              Destination.
        --explicit-vr                         propose all uncompressed TS,
                                              explicit VR little endian first
                                              (default: implicit VR little
                                              endian first)
     -h,--help                                display this help and exit
     -i <attr>                                specifies which attribute(s) of
                                              given DICOM file(s) dcmfile_in
                                              will be included in the C-MOVE
                                              RQ. attr can be specified by its
                                              keyword or tag value (in hex),
                                              e.g.: StudyInstanceUID or
                                              00100020. By default, Study
                                              Instance UID, Series Instance
                                              UID and SOP Instance UID from
                                              the file(s) will be included.
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
     -L <PATIENT|STUDY|SERIES|IMAGE|FRAME>    specifies retrieve level. Use
                                              STUDY for PatientRoot,
                                              StudyRoot, PatientStudyOnly, and
                                              IMAGE for CompositeInstanceRoot
                                              by default.
     -M <name>                                specifies Information Model.
                                              Supported names: PatientRoot,
                                              StudyRoot, PatientStudyOnly,
                                              CompositeInstanceRoot,
                                              WithoutBulkData, HangingProtocol
                                              or ColorPalette. If no
                                              Information Model is specified,
                                              StudyRoot will be used.
     -m <attr=value>                          specify matching key. attr can
                                              be specified by keyword or tag
                                              value (in hex), e.g.
                                              StudyInstanceUID or 00200000D.
                                              Overrides matching keys
                                              specified in DICOM file(s).
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
        --relational                          negotiate relational-retrieve
                                              support
        --release-timeout <ms>                timeout in ms for receiving
                                              A-RELEASE-RP, no timeout by
                                              default
        --retrieve-timeout <ms>               timeout in ms for receiving
                                              outstanding C-GET-RSP or
                                              C-MOVE-RSP, no timeout by
                                              default
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
        --tls-protocol <protocol>             TLS/SSL protocol to use.
                                              Multiple TLS/SSL protocols may
                                              be enabled by multiple
                                              --tls-protocol options.
                                              Supported values by SunJSSE 1.6:
                                              TLSv1, SSLv3, SSLv2Hello. By
                                              default, TLSv1 and SSLv3 are
                                              enabled.
        --tls1                                enable only TLS/SSL protocol
                                              TLSv1; equivalent to
                                              --tls-protocol TLSv1
        --trust-store <file|url>              file path of key store
                                              containing trusted certificates,
                                              resource:cacerts.jks by default
        --trust-store-pass <password>         password for key store with
                                              trusted certificates, 'secret'
                                              by default
        --trust-store-type <storetype>        type of key store with trusted
                                              certificates, JKS by default
        --user <name>                         negotiate user identity with
                                              specified user name
        --user-pass <password>                negotiate user identity with
                                              specified password
        --user-rsp                            negotiate user identity with
                                              positive response requested
     -V,--version                             output version information and
                                              exit
    -
    Examples:
    $ movescu -c DCMQRSCP@localhost:11112 -m StudyInstanceUID=1.2.3.4 --dest
    STORESCP
    Retrieve from Query/Retrieve Service Class Provider DCMQRSCP listening on
    local port 11112 the Study with Study Instance UID = 1.2.3.4 to the
    Storage Service Class Provider STORESCP
