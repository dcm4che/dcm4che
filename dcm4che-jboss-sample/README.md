dcm4che-jboss-sample
====================

Sample C-ECHO SCP in deployable _ejb-jar_.


    $ mvn clean install

builds a version using `PreferencesDicomConfiguration`.


    $ mvn -Dldap=slapd clean install

builds a version using `LdapDicomConfiguration` with LDAP connection parameters
specified in `src/main/filters/slapd.properties`:

    ldap-url=ldap://localhost:389/dc=nodomain
    user-dn=cn=admin,dc=nodomain
    password=admin

The device name can be specified by system property
`org.dcm4che.jboss.sample.deviceName`. Default: `echoscp`.

You have to import a corresponding device configuration into `Preferences`(e.g.:

    $ ~/dcm4che/bin/xml2prefs src/main/config/sample-config.xml

) or into LDAP (e.g.:

    $ ldapadd -xW -Dcn=admin,dc=nodomain -f src/main/config/sample-config.ldif

) before you can deploy the service (e.g.:

    $ ~/jboss7/bin/jboss-cli.sh -c "deploy target/dcm4che-jboss-sample-3.0.0-SNAPSHOT.jar"

)

The JMX name for the _jmx-view_ can be specified by system property
`org.dcm4che.jboss.sample.jmxName'. Default: `dcm4chee:service=echoSCP`.

For TLS connections the certificate of `dcm4che-tools-storescp`:

    $ keytool -list -v -keystore src/main/resources/key.jks -storepass secret
    
    Keystore type: JKS
    Keystore provider: SUN
    
    Your keystore contains 1 entry
    
    Alias name: storescp
    Creation date: May 9, 2011
    Entry type: PrivateKeyEntry
    Certificate chain length: 1
    Certificate[1]:
    Owner: CN=storescp, OU=dcm4che-tool, O=dcm4che, L=Vienna, ST=Vienna, C=AT
    Issuer: CN=storescp, OU=dcm4che-tool, O=dcm4che, L=Vienna, ST=Vienna, C=AT
    Serial number: 4dc7c9d3
    Valid from: Mon May 09 13:02:43 CEST 2011 until: Tue May 08 13:02:43 CEST 2012
    Certificate fingerprints:
             MD5:  77:E0:F7:C7:9F:29:2D:C4:CD:FD:F3:F3:E7:7F:74:2A
             SHA1: CC:2B:42:27:36:0B:E9:B1:F1:21:54:E3:4A:81:3F:44:B1:1A:87:23
             Signature algorithm name: SHA1withRSA
             Version: 3

is used by default. You may specify a different certificate by system
properties `org.dcm4che.jboss.sample.keyStoreType` (default: `JKS`),
`org.dcm4che.jboss.sample.keyStoreURL` (default: `resource:key.jks`),
`org.dcm4che.jboss.sample.storePassword` (default: `secret') and
`org.dcm4che.jboss.sample.keyPassword`
(default:`${org.dcm4che.jboss.sample.storePassword}`).

You may use JBoss's CLI to set system properties. e.g.:

    $ ~/jboss7/bin/jboss-cli.sh -c --file=src/main/config/sample-config.jboss-cli
