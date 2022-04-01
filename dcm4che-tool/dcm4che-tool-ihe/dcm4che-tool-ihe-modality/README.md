```
usage: modality [options] [--kos-title <code> -o <file>] [--dc --dc-reason
                <code>] -b [<aet>[@<ip>]:]<port> -c <aet>@<host>:<port>
                <file|directory>..

The modality application provides a configurable work-flow tool
integrating Key Objects, MPPS, C-STORE, and Storage Commitment.
The tool can:
* Create a DICOM Key Object Selection Document (KOS) with specified
Document Title -title <code> flagging DICOM Composite objects in specified
<file>.. and <directory>.. and store it into DICOM file -o <file>.
* Send a MPPS N-CREATE and MPPS N-SET message referencing the SOP
Instances in the scanned files to a Service Class Provider (SCP) of the
MPPS SOP Class.
* Send a C-STORE message to a Storage Service Class Provider (SCP) for
each DICOM file on the command line and wait for a response.
* Send a Storage Commitment Requests (N-ACTION RQ) for the SOP Instances
in the scanned files to a Service Class Provider (SCP) of the Storage
Commitment Push Model SOP Class.
-
Options:
    --accept-timeout <ms>                 timeout in ms for receiving
                                          A-ASSOCIATE-AC, no timeout by
                                          default
 -b,--bind <aet[@ip][:port]>              specify AE Title, local address
                                          of the Application Entity
                                          provided by this application;
                                          use IOCMTEST and pick up any
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
    --code-config <file>                  file path or URL of list of
                                          configured Discontinuation
                                          Reasons Codes to be used instead
                                          of
                                          etc/modality/koscode.properties
    --connect-timeout <ms>                timeout in ms for TCP connect,
                                          no timeout by default
    --dc                                  send MPPS N-SET RQ with status
                                          DISCONTINUED instead of
                                          COMPLETED
    --dc-reason <code-value>              include Discontinuation Reason
                                          Code in MPPS N-SET RQ with
                                          status DISCONTINUED - must be
                                          one of the values specified by
                                          etc/modality/mppscode.properties
                                          or --code-config
    --explicit-vr                         propose all uncompressed TS,
                                          explicit VR little endian first
                                          (default: implicit VR little
                                          endian first)
 -h,--help                                display this help and exit
    --idle-timeout <ms>                   timeout in ms for aborting idle
                                          Associations, no timeout by
                                          default
    --implicit-vr                         propose only implicit VR little
                                          endian (default: all
                                          uncompressed TS)
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
    --kos-title <code-value>              Document Title of created KOS -
                                          must be one of the values
                                          specified by
                                          etc/modality/koscode.properties
                                          or --code-config
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
    --mpps                                send MPPS before sending objects
    --mpps-late                           send MPPS after sending objects
    --not-async                           do not use asynchronous mode;
                                          equivalent to
                                          --max-ops-invoked=1 and
                                          --max-ops-performed=1
    --not-pack-pdv                        send only one PDV in one
                                          P-Data-TF PDU; pack command and
                                          data PDV in one P-DATA-TF PDU by
                                          default
 -o <file>                                created DICOM file
    --proxy <[user:password@]host:port>   specify host and port of the
                                          HTTP Proxy to tunnel the DICOM
                                          connection.
    --release-timeout <ms>                timeout in ms for receiving
                                          A-RELEASE-RP, no timeout by
                                          default
    --response-timeout <ms>               timeout in ms for receiving
                                          other outstanding DIMSE RSPs
                                          than C-MOVE or C-GET RSPs, no
                                          timeout by default
 -s <[seq.]attr=value>                    specify attributes added to the
                                          sent object(s). attr can be
                                          specified by keyword or tag
                                          value (in hex), e.g. PatientName
                                          or 00100010. Attributes in
                                          nested Datasets can be specified
                                          by including the keyword/tag
                                          value of the sequence attribute,
                                          e.g. 00400275.00400009 for
                                          Scheduled Procedure Step ID in
                                          the Request Attributes Sequence.
    --send-timeout <ms>                   timeout in ms for sending other
                                          DIMSE RQs than C-STORE RQs, no
                                          timeout by default
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
    --stgcmt                              send storage commitment after
                                          sending objects
    --store-timeout <ms>                  timeout in ms for sending
                                          C-STORE sRQ, no timeout by
                                          default
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
    --tmp-file-dir <directory>            directory were temporary file
                                          with File Meta Information from
                                          scanned files is stored; if not
                                          specified, the file is stored
                                          into the default temporary-file
                                          directory
    --tmp-file-prefix <prefix>            prefix for generated file name
                                          for temporary file; 'storescu-'
                                          by default
    --tmp-file-suffix <suffix>            suffix for generated file name
                                          for temporary file; '.tmp' by
                                          default
    --trust-store <file|url>              file path of key store
                                          containing trusted certificates,
                                          resource:cacerts.p12 by default
    --trust-store-pass <password>         password for key store with
                                          trusted certificates, 'secret'
                                          by default
    --trust-store-type <storetype>        type of key store with trusted
                                          certificates, PKCS12 by default
    --uid-suffix <suffix>                 specify suffix to be appended to
                                          the Study, Series and SOP
                                          Instance UID of the sent
                                          object(s).
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
Example: modality --stgcmt -b MODALITY:11114 -c DCM4CHEE@localhost:11112
path/to/study
=> i) Start server listening on port 11114, accepting association requests
with MODALITY as called AE title (for receiving Storage Commitment
Results). ii) Scan images in directory  path/to/study and send a MPPS
N-CREATE RQ and MPPS N-SET RQ referencing the images to DCM4CHEE,
listening on local port 11112 (to send MPPS N-SET RQ after sending the
DICOM objects use option --mpps-late). iii) Send DICOM objects to
DCM4CHEE@localhost:11112. iv) Send a Storage Commitment Request for SOP
Instances in directory path/to/study to DCM4CHEE@localhost:11112.
```
