```
usage: hl7pdq [options] -c <app>^<fac>@<host>:<port> <qip>[..]
              [<domain>..]

The hl7pdq application queries a Patient Demographics Supplier for patient
demographic information matching specified Demographics Fields, for all or
for a specified list of domains using a HL7 V2.5 QBP^Q22 message according
IHE ITI-21 Transaction: Patient Demographics Query.

<qip> shall be formatted as HL7 QIP data type:
@PID.<field no>.<component no>.<subcomponent no>^<value>, e.g.:
@PID.3.1^<patient ID>
@PID.3.4.1^<namespace ID of assigning authority>
@PID.3.4.2^<universal ID of assigning authority>
@PID.3.4.3^<universal ID type of assigning authority>
@PID.5.1.1^<patient family name>
@PID.5.2^<patient given name>
@PID.6.1.1^<mother maiden family name>
@PID.6.2^<mother maiden given name>
@PID.7.1^<date/time of birth>
@PID.8^<administrative sex>
@PID.11.1.1^<street or mailing address>
@PID.11.1.3^<city>
@PID.11.1.5^<zip>
@PID.13.1^<phone number>
@PID.18.1^<patient account number>
@PID.18.4.1^<namespace ID of assigning authority>
@PID.18.4.2^<universal ID of assigning authority>
@PID.18.4.3^<universal ID type of assigning authority>

<domain> shall be formatted as HL7 CX data type:^^^<namespace
ID>&<universal ID>&<universal ID type>
-
Options:
 -b,--bind <app^fac[@ip]>                 specify Sending Application and
                                          Facility and local address used
                                          to connect to the remote
                                          application; use hl7pdq^dcm4che
                                          and pick up any valid local
                                          address to bind the socket by
                                          default.
 -c,--connect <app^fac@host:port>         specify Receiving Application
                                          and Facility, remote address and
                                          port of the HL7 Receiver acting
                                          as Patient Demographics
                                          Supplier.
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
Examples:
hl7pdq -c XREF^XYZ@localhost:2575 @PID.3.1^XYZ10515W @PID.3.4.1^XREF2005
=> Query Patient Demographics Supplier XREF^XYZ listening on local port
2575 for a patient with Patient ID XYZ10515W with Assigning Authority
Namespace ID XREF2005
hl7pdq -c XREF^XYZ@localhost:2575 @PID.5.1.1^SMITH @PID.8^F ^^^XREF2005
=> Query Patient Demographics Supplier XREF^XYZ listening on local port
2575 for patients whose family name matches the value ‘SMITH’ and whose
sex matches the value female, restricting to domain XREF2005
```
