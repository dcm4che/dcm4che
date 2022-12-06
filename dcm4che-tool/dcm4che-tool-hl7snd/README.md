```
usage: hl7snd [options] -c <host>:<port> [<file>..][<directory>..]

Reads HL7 V2 messages from specified files or directories and send them to
a HL7 Receiver application listening on specified host and port using
Minimal Lower Level Protocol (MLLP). Specify '-' as <file> to read the
message from standard input
-
Options:
 -b,--bind <ip>                           specify local address used to
                                          connect to the remote
                                          application; pick up any valid
                                          local address to bind the socket
                                          by default.
 -c,--connect <host:port>                 specify remote address and port
                                          of the HL7 Receiver.
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
Example: hl7snd -c localhost:2575 adt.hl7
=> Send HL7 V2 patient management ADT message adt.hl7 to HL7 Receiver
listening on local port 2575.
Sample HL7 ADT messages may be referred at
https://github.com/dcm4che/dcm4che/tree/master/dcm4che-assembly/src/etc/te
stdata/hl7
HL7 ADT messages to DICOM Patient entity level mappings may be referred at
https://dcm4chee-arc-hl7cs.readthedocs.io/en/latest/adt/inbound.html#hl7-a
dt-to-dicom-mapping
-
Example: hl7snd -c localhost:2575 order.hl7
=> Send HL7 V2 order message order.hl7 to HL7 Receiver listening on local
port 2575.
Sample HL7 order messages may be referred at
https://github.com/dcm4che/dcm4che/tree/master/dcm4che-assembly/src/etc/te
stdata/hl7
HL7 order messages to DICOM Modality Worklist entity level mappings may be
referred at
https://dcm4chee-arc-hl7cs.readthedocs.io/en/latest/orm/inbound.html#hl7-o
rder-to-dicom-mwl-mapping
-
Example: hl7snd -c localhost:2575 report.hl7
=> Send HL7 V2 ORU messages report.hl7 to HL7 Receiver listening on local
port 2575.
Sample HL7 ORU messages may be referred at
https://github.com/dcm4che/dcm4che/tree/master/dcm4che-assembly/src/etc/te
stdata/hl7
HL7 ORU messages to DICOM Basic Text SR or Encapsulated PDF entity level
mappings may be referred at
https://dcm4chee-arc-hl7cs.readthedocs.io/en/latest/oru/inbound.html#hl7-o
ru-to-dicom-mapping
-
Example: hl7snd -c localhost:2575 appointment.hl7
=> Send HL7 V2 SIU messages appointment.hl7 to HL7 Receiver listening on
local port 2575.
Sample HL7 SIU message may be referred at
https://github.com/dcm4che/dcm4che/tree/master/dcm4che-assembly/src/etc/te
stdata/hl7
HL7 SIU messages accepted by the archive may be referred at
https://dcm4chee-arc-hl7cs.readthedocs.io/en/latest/siu/index.html
-
```