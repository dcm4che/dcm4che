```
usage: hl7pix [options] -c <app>^<fac>@<host>:<port> <pid> [<domain>..]

The hl7pix application queries a PIX Manager for Patient IDs corresponding
to the specified Patient ID, for all or for a specified list of domains
using a HL7 V2.5 QBP^Q23 message according IHE ITI-9 Transaction: PIX
Query.
<pid> and <domain> shall be formatted as HL7 CX data type:
<pid>: <ID>^^^<namespace ID>&<universal ID>&<universal ID type>
<domain>: ^^^<namespace ID>&<universal ID>&<universal ID type>
-
Options:
 -b,--bind <app^fac[@ip]>                 specify Sending Application and
                                          Facility and local address used
                                          to connect to the remote
                                          application; use hl7pix^dcm4che
                                          and pick up any valid local
                                          address to bind the socket by
                                          default.
 -c,--connect <app^fac@host:port>         specify Receiving Application
                                          and Facility, remote address and
                                          port of the HL7 Receiver acting
                                          as PIX Manager.
    --charset <name>                      HL7 Character Set used to encode
                                          the message, ASCII by default
    --connect-timeout <ms>                timeout in ms for TCP connect,
                                          no timeout by default
 -h,--help                                display this help and exit
    --key-pass <password>                 password for accessing the key
                                          in the key store, key store
                                          password by default
    --key-store <file|url>                file path or URL of key store
                                          containing the private key,
                                          resource:key.p12 by default
    --key-store-pass <password>           password for key store
                                          containing the private key,
                                          'secret' by default
    --key-store-type <storetype>          type of key store containing the
                                          private key, PKCS12 by default
    --mllp2                               use MLLP Release 2 with Commit
                                          Acknowledgement Block
    --proxy <[user:password@]host:port>   specify host and port of the
                                          HTTP Proxy to tunnel the HL7
                                          connection.
    --response-timeout <ms>               timeout in ms for receiving
                                          other outstanding DIMSE RSPs
                                          than C-MOVE or C-GET RSPs, no
                                          timeout by default
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
    --tls-eia-https                       enable server endpoint
                                          identification according RFC
                                          2818: HTTP Over TLS
    --tls-eia-ldaps                       enable server endpoint
                                          identification according RFC
                                          2830: LDAP Extension for TLS
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
                                          Supported values by Java 11:
                                          TLSv1, TLSv1.1, TLSv1.2,
                                          TLSv1.3, SSLv3, SSLv2Hello. By
                                          default, only TLSv1.2 is
                                          enabled.
    --tls1                                enable only TLS/SSL protocol
                                          TLSv1; equivalent to
                                          --tls-protocol TLSv1
    --tls11                               enable only TLS/SSL protocol
                                          TLSv1.1; equivalent to
                                          --tls-protocol TLSv1.1
    --tls12                               enable only TLS/SSL protocol
                                          TLSv1.2; equivalent to
                                          --tls-protocol TLSv1.2
    --tls13                               enable only TLS/SSL protocol
                                          TLSv1.3; equivalent to
                                          --tls-protocol TLSv1.3
    --trust-store <file|url>              file path of key store
                                          containing trusted certificates,
                                          resource:cacerts.p12 by default
    --trust-store-pass <password>         password for key store with
                                          trusted certificates, 'secret'
                                          by default
    --trust-store-type <storetype>        type of key store with trusted
                                          certificates, PKCS12 by default
 -V,--version                             output version information and
                                          exit
-
Example: hl7pix -c XREF^XYZ@localhost:2575 XYZ10515W^^^XREF2005
=> Query PIX Manager XREF^XYZ listening on local port 2575 for Patient IDs
for all domains that correspond Patient ID XYZ10515W with Assigning
Authority Namespace ID XREF2005
```
