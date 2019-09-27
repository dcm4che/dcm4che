    usage: upsscu [options] -c <aet>@<host>:<port> [--] [<xml-file>]
    
    The upsscu application implements a Service Class User (SCU) for the
    Unified Procedure Step Service (UPS) SOP Class. Attributes to be sent in
    dataset for N-CREATE and N-SET requests can be specified in XML File --
    <xml-file>
    -
    Options:
        --accept-timeout <ms>                 timeout in ms for receiving
                                              A-ASSOCIATE-AC, no timeout by
                                              default
     -b,--bind <aet[@ip][:port]>              specify AE Title, local address
                                              of the Application Entity
                                              provided by this application;
                                              use UPSSCU and pick up any valid
                                              local address to bind the socket
                                              by default. If also a port is
                                              specified, the Application
                                              Entity will listening for
                                              incoming association requests on
                                              it.
        --big-endian                          propose all uncompressed TS,
                                              explicit VR big endian first
                                              (default: implicit VR little
                                              endian first)
     -c,--connect <aet@host:port>             specify AE Title, remote address
                                              and port of the remote
                                              Application Entity.
     -C,--command <arg>                       specifies Command. Supported
                                              names: Create, Update, Get,
                                              GetPull, GetWatch, Find,
                                              FindWatch ChangeState,
                                              RequestCancel,
                                              RequestCancelWatch, Subscribe,
                                              Unsubscribe, Suspend, Receive.
                                              If no Command is specified, Find
                                              will be used.
        --connect-timeout <ms>                timeout in ms for TCP connect,
                                              no timeout by default
        --contact <name>                      Specify Contact Display Name of
                                              Cancellation Request.
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
        --proxy <[user:password@]host:port>   specify host and port of the
                                              HTTP Proxy to tunnel the DICOM
                                              connection.
     -r <[seq/]attr>                          specify return key. key can be
                                              specified by keyword or tag
                                              value (in hex), e.g.
                                              NumberOfStudyRelatedSeries or
                                              00201206.
        --reason <reason>                     Specify Reason of Cancellation
                                              Request.
        --reason-code <code>                  Specify Reason Code in format
                                              <id>^<text>^<scheme> of
                                              Cancellation Request.
        --release-timeout <ms>                timeout in ms for receiving
                                              A-RELEASE-RP, no timeout by
                                              default
        --response-timeout <ms>               timeout in ms for receiving
                                              outstanding response messages,
                                              no timeout by default
     -S,--state <arg>                         specifies value for changing
                                              state of UPS. Supported state
                                              changes: Complete, Process,
                                              Discontinue.
     -s <[seq/]attr=value>                    Set element of dataset in format
                                              <attribute=value>.
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
        --tls-protocol <protocol>             TLS/SSL protocol to use.
                                              Multiple TLS/SSL protocols may
                                              be enabled by multiple
                                              --tls-protocol options.
                                              Supported values by SunJSSE 1.8:
                                              TLSv1, TLSv1.1, TLSv1.2, SSLv3,
                                              SSLv2Hello. By default, TLSv1,
                                              TLSv1.1 and TLSv1.2 are enabled.
        --tls1                                enable only TLS/SSL protocol
                                              TLSv1; equivalent to
                                              --tls-protocol TLSv1
        --tls11                               enable only TLS/SSL protocol
                                              TLSv1.1; equivalent to
                                              --tls-protocol TLSv1.1
        --tls12                               enable only TLS/SSL protocol
                                              TLSv1.2; equivalent to
                                              --tls-protocol TLSv1.2
        --truid <uid>                         Specify transaction UID for
                                              changing state of UPS
        --trust-store <file|url>              file path of key store
                                              containing trusted certificates,
                                              resource:cacerts.jks by default
        --trust-store-pass <password>         password for key store with
                                              trusted certificates, 'secret'
                                              by default
        --trust-store-type <storetype>        type of key store with trusted
                                              certificates, JKS by default
        --upsiuid <uid>                       Specify UPS Instance UID to be
                                              used in UPS requests as Affected
                                              SOP InstanceUID (0000,1000).
        --user <name>                         negotiate user identity with
                                              specified user name
        --user-pass <password>                negotiate user identity with
                                              specified password
        --user-rsp                            negotiate user identity with
                                              positive response requested
     -V,--version                             output version information and
                                              exit
    
    Examples:
    => upsscu -c UPSSCP@localhost:11112 -C Create
    Send UPS N-CREATE RQ listening on localport 11112. Use
    /etc/upsscu/create.xml to set attributes in the dataset.
    
    => upsscu -c UPSSCP@localhost:11112 -C Create --
    /path-to-custom-create.xml
    Send UPS N-CREATE RQ listening on localport 11112. Set attributes in the
    dataset from /path-to-custom-create.xml.
    
    => upsscu -c UPSSCP@localhost:11112 -C Update --upsiuid 1.2.3.4.5.6.7.8
    Send UPS N-SET RQ listening on localport 11112 with UPS Instance UID as
    1.2.3.4.5.6.7.8
    
    => upsscu -c UPSSCP@localhost:11112 -C Get --upsiuid 1.2.3.4.5.6.7.8
    Send UPS N-GET RQ listening on localport 11112 with UPS Instance UID and
    Negotiating SOP Class UID as Unified Procedure Step Push Sop Class.
    
    => upsscu -c UPSSCP@localhost:11112 -C GetPull --upsiuid 1.2.3.4.5.6.7.8
    Send UPS N-GET RQ listening on localport 11112 with UPS Instance UID as
    1.2.3.4.5.6.7.8 and Negotiating SOP Class UID as Unified Procedure Step
    Pull Sop Class.