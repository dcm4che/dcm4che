```
usage: hl7rcv [options] -b [<ip>:]<port>

The hl7rcv application receives HL7 V2 messages from a HL7 Sender
application using Minimal Lower Level Protocol (MLLP).
-
Options:
 -b,--bind <[ip:]port>               specify the port on which the HL7
                                     Receiver shall listening for
                                     connection requests. If no local IP
                                     address of the network interface is
                                     specified, connections on any/all
                                     local addresses are accepted.
    --charset <name>                 HL7 Character Set used to decode
                                     message if not specified by MSH-18,
                                     ASCII by default
    --directory <path>               directory to which received HL7 V2
                                     messages are stored, using its
                                     Message Type as sub-directory name
                                     and its Message Control ID or a
                                     random uuid as file name. '.' by
                                     default
 -h,--help                           display this help and exit
    --idle-timeout <ms>              timeout in ms for receiving HL7
                                     messages, no timeout by default
    --ignore                         do not store received HL7 V2 messages
                                     in files
    --key-pass <password>            password for accessing the key in the
                                     key store, key store password by
                                     default
    --key-store <file|url>           file path or URL of key store
                                     containing the private key,
                                     resource:key.p12 by default
    --key-store-pass <password>      password for key store containing the
                                     private key, 'secret' by default
    --key-store-type <storetype>     type of key store containing the
                                     private key, PKCS12 by default
    --mllp2                          use MLLP Release 2 with Commit
                                     Acknowledgement Block
    --response-delay <ms>            delay in ms returning response
                                     message. No delay by default.
    --sorcv-buffer <length>          set SO_RCVBUF socket option to
                                     specified value
    --sosnd-buffer <length>          set SO_SNDBUF socket option to
                                     specified value
    --ssl2Hello                      send/accept SSLv3/TLS ClientHellos
                                     encapsulated in a SSLv2 ClientHello
                                     packet; equivalent to --tls-protocol
                                     SSLv2Hello --tls-protocol SSLv3
                                     --tls-protocol TLSv1 --tls-protocol
                                     TLSv1.1 --tls-protocol TLSv1.2
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
    --tls-eia-https                  enable server endpoint identification
                                     according RFC 2818: HTTP Over TLS
    --tls-eia-ldaps                  enable server endpoint identification
                                     according RFC 2830: LDAP Extension
                                     for TLS
    --tls-noauth                     disable client authentification for
                                     TLS
    --tls-null                       enable TLS connection without
                                     encryption; equivalent to
                                     --tls-cipher SSL_RSA_WITH_NULL_SHA
    --tls-protocol <protocol>        TLS/SSL protocol to use. Multiple
                                     TLS/SSL protocols may be enabled by
                                     multiple --tls-protocol options.
                                     Supported values by Java 11: TLSv1,
                                     TLSv1.1, TLSv1.2, TLSv1.3, SSLv3,
                                     SSLv2Hello. By default, only TLSv1.2
                                     is enabled.
    --tls1                           enable only TLS/SSL protocol TLSv1;
                                     equivalent to --tls-protocol TLSv1
    --tls11                          enable only TLS/SSL protocol TLSv1.1;
                                     equivalent to --tls-protocol TLSv1.1
    --tls12                          enable only TLS/SSL protocol TLSv1.2;
                                     equivalent to --tls-protocol TLSv1.2
    --tls13                          enable only TLS/SSL protocol TLSv1.3;
                                     equivalent to --tls-protocol TLSv1.3
    --trust-store <file|url>         file path of key store containing
                                     trusted certificates,
                                     resource:cacerts.p12 by default
    --trust-store-pass <password>    password for key store with trusted
                                     certificates, 'secret' by default
    --trust-store-type <storetype>   type of key store with trusted
                                     certificates, PKCS12 by default
    --uuid                           use a random uuid for message
                                     filename instead of MSH-9
 -V,--version                        output version information and exit
 -x,--xsl <xsl-file>                 generate response by applying
                                     specified XSLT stylesheet, return
                                     Application Accept message by default
    --xsl-param <name=value>         specify additional XSLT parameters,
                                     "MessageControlID" and
                                     "DateTimeOfMessage" are provided by
                                     default
-
Example: hl7rcv -b 2575
=> Starts receiver listening on port 2575.
```
