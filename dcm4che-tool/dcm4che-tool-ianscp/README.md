    usage: ianscp [options] -b [<aet>[@<ip>]:]<port>
    
    The ianscp application implements a Service Class Provider (SCP) for the
    Instance Available Notification (IAN) SOP Class. It listens on a specific
    TCP/IP port for incoming association requests from a Service Class User
    (SCU) of the IAN SOP Class. Instance Available Notifications received in
    N-CREATE requests are stored in DICOM files.ianscp application also
    supports the Verification Service Class as a SCP.
    -
    Options:
     -b,--bind <[aet[@ip]:]port>         specify the port on which the
                                         Application Entity shall listening
                                         for incoming association requests. If
                                         no local IP address of the network
                                         interface is specified, connections
                                         on any/all local addresses are
                                         accepted. If an AE Title is
                                         specified, only requests with
                                         matching Called AE Title will be
                                         accepted.
        --directory <path>               directory to which received Instance
                                         Available Notifications are stored,
                                         '.' by default
     -h,--help                           display this help and exit
        --idle-timeout <ms>              timeout in ms for receiving DIMSE-RQ,
                                         no timeout by default
        --ignore                         do not store received Instance
                                         Available Notifications in files
        --key-pass <password>            password for accessing the key in the
                                         key store, key store password by
                                         default
        --key-store <file|url>           file path or URL of key store
                                         containing the private key,
                                         resource:key.jks by default
        --key-store-pass <password>      password for key store containing the
                                         private key, 'secret' by default
        --key-store-type <storetype>     type of key store containing the
                                         private key, JKS by default
        --max-ops-invoked <no>           maximum number of operations this AE
                                         may invoke asynchronously, unlimited
                                         by default
        --max-ops-performed <no>         maximum number of operations this AE
                                         may perform asynchronously, unlimited
                                         by default
        --max-pdulen-rcv <length>        specifies maximal length of received
                                         P-DATA TF PDUs communicated during
                                         association establishment. 0
                                         indicates that no maximum length is
                                         specified. 16378 by default
        --max-pdulen-snd <length>        specifies maximal length of sent
                                         P-DATA-TF PDUs by this AE. The actual
                                         maximum length of sent P-DATA-TF PDUs
                                         is also limited by the maximal length
                                         of received P-DATA-TF PDUs of the
                                         peer AE communicated during
                                         association establishment. 16378 by
                                         default
        --not-async                      do not use asynchronous mode;
                                         equivalent to --max-ops-invoked=1 and
                                         --max-ops-performed=1
        --not-pack-pdv                   send only one PDV in one P-Data-TF
                                         PDU; pack command and data PDV in one
                                         P-DATA-TF PDU by default
        --release-timeout <ms>           timeout in ms for receiving
                                         A-RELEASE-RP, no timeout by default
        --request-timeout <ms>           timeout in ms for receiving
                                         A-ASSOCIATE-RQ, no timeout by default
        --soclose-delay <ms>             delay in ms after sending
                                         A-ASSOCATE-RJ, A-RELEASE-RQ or
                                         A-ABORT before the socket is closed;
                                         50ms by default
        --sop-classes <file|url>         file path or URL of list of accepted
                                         SOP Classes,
                                         resource:sop-classes.properties by
                                         default
        --sorcv-buffer <length>          set SO_RCVBUF socket option to
                                         specified value
        --sosnd-buffer <length>          set SO_SNDBUF socket option to
                                         specified value
        --ssl2Hello                      send/accept SSLv3/TLS ClientHellos
                                         encapsulated in a SSLv2 ClientHello
                                         packet; equivalent to --tls-protocol
                                         SSLv2Hello --tls-protocol SSLv3
                                         --tls-protocol TLSv1
                                         --tls-protocol TLSv1.1
                                         --tls-protocol TLSv1.2
        --ssl3                           enable only TLS/SSL protocol SSLv3;
                                         equivalent to --tls-protocol SSLv3
        --status <code>                  specifies status code in returned
                                         N-CREATE RSPs, 0000H by default.
        --tcp-delay                      set TCP_NODELAY socket option to
                                         false, true by default
        --tls                            enable TLS connection without
                                         encryption or with AES or 3DES
                                         encryption; equivalent to
                                         --tls-cipher SSL_RSA_WITH_NULL_SHA
                                         --tls-cipher
                                         TLS_RSA_WITH_AES_128_CBC_SHA
                                         --tls-cipher
                                         SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-3des                       enable TLS connection with 3DES
                                         encryption; equivalent to
                                         --tls-cipher
                                         SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-aes                        enable TLS connection with AES or
                                         3DES encryption; equivalent to
                                         --tls-cipher
                                         TLS_RSA_WITH_AES_128_CBC_SHA
                                         --tls-cipher
                                         SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-cipher <cipher>            enable TLS connection with specified
                                         Cipher Suite. Multiple Cipher Suites
                                         may be enabled by multiple
                                         --tls-cipher options
        --tls-noauth                     disable client authentification for
                                         TLS
        --tls-null                       enable TLS connection without
                                         encryption; equivalent to
                                         --tls-cipher SSL_RSA_WITH_NULL_SHA
        --tls-protocol <protocol>        TLS/SSL protocol to use. Multiple
                                         TLS/SSL protocols may be enabled
                                         by multiple --tls-protocol
                                         options. Supported values by
                                         SunJSSE 1.8: TLSv1.2, TLSv1.1,
                                         TLSv1, SSLv3, SSLv2Hello. By
                                         default, TLSv1.2, TLSv1.1, TLSv1
                                         and SSLv3 are enabled.
        --tls1                           enable only TLS/SSL protocol
                                         TLSv1; equivalent to
                                         --tls-protocol TLSv1
        --tls11                          enable only TLS/SSL protocol
                                         TLSv1.1; equivalent to
                                         --tls-protocol TLSv1.1
        --tls12                          enable only TLS/SSL protocol
                                         TLSv1.2; equivalent to
                                         --tls-protocol TLSv1.2
        --trust-store <file|url>         file path of key store containing
                                         trusted certificates,
                                         resource:cacerts.jks by default
        --trust-store-pass <password>    password for key store with trusted
                                         certificates, 'secret' by default
        --trust-store-type <storetype>   type of key store with trusted
                                         certificates, JKS by default
     -V,--version                        output version information and exit
    -
    Example: ianscp -b IANSCP:11112
    => Starts server listening on port 11112, accepting association requests
    with IANSCP as called AE title Instance Available Notifications will be
    stored in the working directory.
