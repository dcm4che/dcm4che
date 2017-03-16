    usage: syslogd [options] -b [<ip>:]<port>
    
    The syslogd application receives RFC 5424 Syslog messages via TCP/TLS or
    UDP and stores them in files.
    -
    Options:
     -b,--bind <[ip:]port>               specify the port on which the Syslog
                                         Receiver shall listening for
                                         connection requests. If no local IP
                                         address of the network interface is
                                         specified, connections on any/all
                                         local addresses are accepted.
        --directory <path>               directory to which received Syslog
                                         messages are stored. '.' by default
     -h,--help                           display this help and exit
        --idle-timeout <ms>              timeout in ms for receiving Syslog
                                         messages via TCP, no timeout by
                                         default
        --ignore                         do not store received Syslog messages
                                         in files
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
        --udp                            receive Syslog messages over UDP;
                                         receive Syslog messages over TCP by
                                         default.
     -V,--version                        output version information and exit
    -
    Example: syslogd --tls -b 6514
    => Starts TLS/TCP Syslog receiver listening on port 6514.
