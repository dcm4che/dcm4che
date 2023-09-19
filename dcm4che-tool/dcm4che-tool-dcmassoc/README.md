```
usage: dcmassoc [options] -c <aet>@<host>:<port>

The dcmassoc application creates an association request to a remote AE,
allowing full control over the presentation contexts offered.
-
Options:
    --accept-timeout <ms>                 timeout in ms for receiving
                                          A-ASSOCIATE-AC, no timeout by
                                          default
 -b,--bind <aet[@ip]>                     specify AE Title, local address
                                          of the Application Entity
                                          provided by this application;
                                          use DCMASSOC and pick up any
                                          valid local address to bind the
                                          socket by default.
 -c,--connect <aet@host:port>             specify AE Title, remote address
                                          and port of the remote
                                          Application Entity.
    --connect-timeout <ms>                timeout in ms for TCP connect,
                                          no timeout by default
 -h,--help                                display this help and exit
    --idle-timeout <ms>                   timeout in ms for aborting idle
                                          Associations, no timeout by
                                          default
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
    --pc <number:cuid:tsuid[(,|;)...]>    specifies Presentation Contexts
                                          to be negotiated probing the
                                          Association Acceptance Policy of
                                          the DICOM SCP. SOP Class and
                                          Transfer Syntaxes can be
                                          specified by its UID or its name
                                          in camel-case (e.g.
                                          1.2.840.10008.5.1.4.1.1.2 or
                                          CTImageStorage). Semicolon
                                          separated Transfer Syntaxes will
                                          be offered in separate
                                          Presentation Contexts, where
                                          comma separated Transfer
                                          Syntaxes will be offered in one
                                          Presentation Context.
    --proxy <[user:password@]host:port>   specify host and port of the
                                          HTTP Proxy to tunnel the DICOM
                                          connection.
    --release-timeout <ms>                timeout in ms for receiving
                                          A-RELEASE-RP, no timeout by
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
    --user <name>                         negotiate user identity with
                                          specified user name
    --user-jwt <token>                    negotiate user identity with
                                          specified JSON Web Token
    --user-pass <password>                negotiate user identity with
                                          specified password
    --user-rsp                            negotiate user identity with
                                          positive response requested
    --user-saml <assertion>               negotiate user identity with
                                          specified SAML Assertion
 -V,--version                             output version information and
                                          exit
-
Example: dcmassoc -c STORESCP@localhost:11112 -pc
1:CTImageStorage:ImplicitVRLittleEndian -pc
3:MRImageStorage:ImplicitVRLittleEndian,ExplicitVRLittleEndian
=> Probe association to Storage Service Class Provider STORESCP listening
on remote port 11112 offering CTImageStorage with ImplicitVRLittleEndian
in first presentation context and MRImageStorage with
ImplicitVRLittleEndian and ExplicitVRLittleEndian in third presentation
context.
```
