    usage: dcmldap [Options] -{ca} <aet@host:port>
                   or dcmldap  [Options] -d <aet>
    
    Utility to insert/remove configuration entries for Network AEs into/from
    LDAP server
    -
    Options:
     -a <aet@host:port>         add specified Network AE on specified
                                Connection to existing Device specified by
                                option --dev.
        --ae-desc <string>      specify Description of created Network AE.
     -c <aet@host:port>         create new Device providing the specified
                                Network AE on specified Connection.
        --conn <cn>             specify Common Name of created Network
                                Connection. If not specified, "dicom" or -
                                with TLS required - "tls-dicom" will be used.
     -d <aet>                   remove specified Network AE, including
                                associated Network Connection and the
                                providing Device - if no other Network AE is
                                provided from the Device
     -D <binddn>                specify Distinguished Name used to bind to the
                                LDAP server; default:
                                "cn=admin,dc=dcm4che,dc=org"
        --dev <name>            specify Device Name. Required with option -a.
                                If not specified the AE Title in lower case is
                                used.
        --dev-desc <string>     specify Description of created Device.
        --dev-type <string>     specify Primary Device Type of created Device.
     -h,--help                  display this help and exit
     -H <ldapuri>               specify URI referring to the LDAP server;
                                default:
                                "ldap://localhost:389/dc=dcm4che,dc=org"
        --tls                   requires TLS connection without encryption or
                                with AES or 3DES encryption; equivalent to
                                --tls-cipher SSL_RSA_WITH_NULL_SHA
                                --tls-cipher TLS_RSA_WITH_AES_128_CBC_SHA
                                --tls-cipher SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-3des              requires TLS connection with 3DES encryption;
                                equivalent to --tls-cipher
                                SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-aes               requires TLS connection with AES or 3DES
                                encryption; equivalent to --tls-cipher
                                TLS_RSA_WITH_AES_128_CBC_SHA --tls-cipher
                                SSL_RSA_WITH_3DES_EDE_CBC_SHA
        --tls-cipher <cipher>   requires TLS connection with specified Cipher
                                Suite. Multiple Cipher Suites may be enabled
                                by multiple --tls-cipher options
        --tls-null              requires TLS connection without encryption;
                                equivalent to --tls-cipher
                                SSL_RSA_WITH_NULL_SHA
     -V,--version               output version information and exit
     -w <passwd>                specify password used to bind to the LDAP
                                server; default: "secret"
    Examples:
    $ dcmldap -c STORESCP@storescp:11112
    create new Device 'storescp' providing Network AE 'STORESCP' on Network
    Connection 'dicom' listing on host 'storescp' at port '11112', using
    Distinguished Name 'cn=admin,dc=dcm4che,dc=org' and password 'secret' to
    bind to LDAP server 'ldap://localhost:389/dc=dcm4che,dc=org'.
    -
    $ dcmldap -a STORESCP_TLS@localhost:2762 --tls --dev=storescp
    add Network AE 'STORESCP_TLS' on Network Connection 'dicom-tls', requiring
    TLS, listing on host 'localhost' at port '11112', providing to existing
    Device 'storescp', using Distinguished Name 'cn=admin,dc=dcm4che,dc=org'
    and password 'secret' to bind to LDAP server
    'ldap://localhost:389/dc=dcm4che,dc=org'.
    -
    $ dcmldap -d STORESCP_TLS
    remove Network AE 'STORESCP_TLS' using Distinguished Name
    'cn=admin,dc=dcm4che,dc=org' and password 'secret' to bind to LDAP server
    'ldap://localhost:389/dc=dcm4che,dc=org'.
