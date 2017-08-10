    usage: syslog [options] -c <host>:<port> [<file>..]
    
    Send content of specified files as RFC 5424 Syslog messages via TCP/TLS or
    UDP to a Syslog Receiver listening on specified host and port. Specify '-'
    as <file> to read the message from standard input
    -
    Options:
        --app-name <name>                     specify APPNAME in emitted
                                              Syslog messages. 'syslog' by
                                              default.
     -b,--bind <ip>                           specify local address used to
                                              connect to the remote
                                              application; pick up any valid
                                              local address to bind the socket
                                              by default.
     -c,--connect <host:port>                 specify remote address and port
                                              of the Syslog Receiver.
        --connect-timeout <ms>                timeout in ms for TCP connect,
                                              no timeout by default
        --delay <ms>                          specify delay time in ms between
                                              sent messages
        --facility <facility>                 specify facility name of the
                                              emitted syslog messages. Valid
                                              facility names are: kern, user,
                                              mail, daemon, auth, syslog, lpr,
                                              news, uucp, cron, authpriv, ftp,
                                              ntp,  audit, console, cron2,
                                              local0 to local7. The default is
                                              authpriv.
     -h,--help                                display this help and exit
        --idle-timeout <ms>                   specify time in ms, after TCP
                                              connection is closed between
                                              sent messages. Only effective if
                                              a larger delay time between sent
                                              messages is specified by option
                                              --delay.
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
        --level <level>                       specify level name of the
                                              emitted Syslog messages. Valid
                                              level names are: emerg, alert,
                                              crit, err, warning, notice,
                                              info, debug. The default is
                                              notice.
        --msg-id <id>                         Specify MSGID in emitted Syslog
                                              messages. 'DICOM+RFC3881' by
                                              default.
        --no-bom                              do not prefix message content by
                                              BOM.
        --proxy <[user:password@]host:port>   specify host and port of the
                                              HTTP Proxy to tunnel the TCP
                                              connection.
        --retry <s>                           specify retry interval in s to
                                              sent messages to a temporary
                                              unreachable syslog receiver; no
                                              retry by default.
        --sorcv-buffer <length>               set SO_RCVBUF socket option to
                                              specified value
        --sosnd-buffer <length>               set SO_SNDBUF socket option to
                                              specified value
        --spool-dir <dir>                     directory to spool messages if
                                              the syslog receiver is not
                                              reachable and a a retry inteval
                                              was specified by --retry. Use
                                              system temporary directory by
                                              default.
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
        --udp                                 send Syslog messages over UDP;
                                              send Syslog messages over TCP by
                                              default.
        --utc                                 specifies time stamp in Syslog
                                              messages in UTC. Specify time
                                              stamp in local time zone by
                                              default.
     -V,--version                             output version information and
                                              exit
    -
    Example: syslog --tls -c localhost:6514 audit.xml
    => Send message audit.xml over TLS to Syslog Receiver listening on local
    port 6514.
