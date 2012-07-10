dcm4che-jboss-sample
====================

Sample C-ECHO SCP in deployable _ejb-jar_.


Description
-----------

Deploys an implementation of `org.dcm4che.conf.api.DicomConfiguration` as
_Singleton EJB_ by EJB deployment descriptor `META-INF/ejb-jar.xml`:

```xml
<ejb-jar
  xmlns="http://java.sun.com/xml/ns/javaee" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"
  version="3.1">
  <enterprise-beans>
    <session>
      <ejb-name>DicomConfiguration</ejb-name>
      <business-local>org.dcm4che.conf.api.DicomConfiguration</business-local>
      <ejb-class>${ejb-class}</ejb-class>
      <session-type>Singleton</session-type>
      <init-on-startup>true</init-on-startup>
      <pre-destroy>
        <lifecycle-callback-method>close</lifecycle-callback-method>
      </pre-destroy>
    </session>
  </enterprise-beans>
  <assembly-descriptor>
    <container-transaction>
      <method>
      <ejb-name>DicomConfiguration</ejb-name>
      <method-name>*</method-name>
      </method>
      <trans-attribute>NotSupported</trans-attribute>
    </container-transaction>
  </assembly-descriptor>
</ejb-jar>
```

to get injected by `DeviceService` _Singleton EJB_ `EchoSCP`:
```java
@Singleton
@DependsOn("DicomConfiguration")
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EchoSCP extends DeviceService implements EchoSCPMBean {

    static final String DEVICE_NAME = "org.dcm4che.jboss.sample.deviceName";
    static final String JMX_NAME = "org.dcm4che.jboss.sample.jmxName";
    static final String KS_TYPE = "org.dcm4che.jboss.sample.keyStoreType";
    static final String KS_URL = "org.dcm4che.jboss.sample.keyStoreURL";
    static final String KS_PASSWORD = "org.dcm4che.jboss.sample.storePassword";
    static final String KEY_PASSWORD = "org.dcm4che.jboss.sample.keyPassword";

    @EJB(name="DicomConfiguration")
    DicomConfiguration dicomConfiguration;

    private ObjectInstance mbean;

    @PostConstruct
    void init() {
        try {
            super.init(dicomConfiguration.findDevice(
                    System.getProperty(DEVICE_NAME, "echoscp")));
            mbean = ManagementFactory.getPlatformMBeanServer()
                    .registerMBean(this, new ObjectName(
                            System.getProperty(JMX_NAME, "dcm4chee:service=echoSCP")));
            start();
        } catch (Exception e) {
            destroy();
            throw new RuntimeException(e);
        }
        
    }

    @PreDestroy
    void destroy() {
        stop();
        if (mbean != null)
            try {
                ManagementFactory.getPlatformMBeanServer()
                    .unregisterMBean(mbean.getObjectName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        mbean = null;
        device = null;
    }

    @Override
    public Device unwrapDevice() {
        return device;
    }

    @Override
    public void reloadConfiguration() throws Exception {
        device.reconfigure(dicomConfiguration.findDevice(device.getDeviceName()));
    }

    protected KeyManager keyManager() throws Exception {
        String url = System.getProperty(KS_URL, "resource:key.jks");
        String kstype = System.getProperty(KS_TYPE, "JKS");
        String kspw = System.getProperty(KS_PASSWORD, "secret");
        String keypw = System.getProperty(KEY_PASSWORD, kspw);
        return SSLManagerFactory.createKeyManager(kstype, url, kspw, keypw);
    }

}
```
with _buisness interface_ and _jmx-view_ `EchoSCPMBean`:
```java
public interface EchoSCPMBean {

    boolean isRunning();

    void start() throws Exception;

    void stop();

    void reloadConfiguration() throws Exception;

    Device unwrapDevice();
}
```


Build
-----

    $ mvn clean install

builds a version using `org.dcm4che.conf.prefs.PreferencesDicomConfiguration`.


    $ mvn -Dldap=slapd clean install

builds a version using `org.dcm4che.conf.ldap.ExtendedLdapDicomConfiguration`
with LDAP connection parameters specified in `src/main/filters/slapd.properties`:

    ldap-url=ldap://localhost:389/dc=nodomain
    user-dn=cn=admin,dc=nodomain
    password=admin


Configuration
-------------

The device name can be specified by system property (default)
- `org.dcm4che.jboss.sample.deviceName` (`echoscp`).

You have to import a corresponding device configuration into `Preferences`(e.g.:

    $ ~/dcm4che/bin/xml2prefs src/main/config/sample-config.xml

) or into LDAP (e.g.:

    $ ldapadd -xW -Dcn=admin,dc=nodomain -f src/main/config/sample-config.ldif

) before you can deploy the service (e.g.:

    $ ~/jboss7/bin/jboss-cli.sh -c "deploy target/dcm4che-jboss-sample-3.0.0-SNAPSHOT.jar"

)

The JMX name for the _jmx-view_ can be specified by system property (default)
- `org.dcm4che.jboss.sample.jmxName` (`dcm4chee:service=echoSCP`).

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

is used by default. You may specify a different certificate by system properties (default):
- `org.dcm4che.jboss.sample.keyStoreType` (`JKS`)
- `org.dcm4che.jboss.sample.keyStoreURL` (`resource:key.jks`)
- `org.dcm4che.jboss.sample.storePassword` (`secret`)
- `org.dcm4che.jboss.sample.keyPassword` (`${org.dcm4che.jboss.sample.storePassword}`)

You may use JBoss's CLI to set system properties. e.g.:

    $ ~/jboss7/bin/jboss-cli.sh -c --file=src/main/config/sample-config.jboss-cli
