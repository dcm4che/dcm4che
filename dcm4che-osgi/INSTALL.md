Getting Started with DCM4CHE - OSGI examples - WILDFLY 
======================================================

Requirements
------------
- Java SE 6 or later - tested with [OpenJDK](http://openjdk.java.net/)
  and [Oracle JDK](http://java.com/en/download)

- [WildFly Application Server 8.0.0.Beta1](http://www.wildfly.org/)

- [JBoss OSGi installer 2.1.0](http://sourceforge.net/projects/jboss/files/JBossOSGi/2.1.0/jbosgi-installer-2.1.0.jar/download)

- [Apache Felix OSGi Web Console 4.2.0-all](http://tweedo.com/mirror/apache//felix/org.apache.felix.webconsole-4.2.0-all.jar)

- [Apache Felix Metatype 1.0.8](http://tweedo.com/mirror/apache//felix/org.apache.felix.metatype-1.0.8.jar)

- [Apache Aries Util](http://tweedo.com/mirror/apache/aries/org.apache.aries.util-1.0.0.jar)

- [Apache Aries Proxy](http://tweedo.com/mirror/apache/aries/org.apache.aries.proxy-1.0.0.jar)

- [Apache Aries Blueprint](http://tweedo.com/mirror/apache/aries/org.apache.aries.blueprint-1.0.0.jar)

- LDAP Server including a [sample DICOM Configuration](https://github.com/dcm4che/dcm4chee-arc/blob/master/INSTALL.md)

**Note:** The link above on "sample DICOM Configuration" points to a INSTALL.md file describing how to install a comprehensive, complete Archive. 
To run the OSGi tests is not necessary to set up the complete insfrastructure for an Archive, but just the part related to the LDAP configuration, 
i.e the sections "Setup LDAP Server" and "Import sample configuration into LDAP Server".

Setup Wildfly AS
----------------
0. $WILDFLY = WildFly AS home directory
   $DCM4CHE = dcm4che home directory

1. Download [WildFly AS 8.0.0.Beta1](http://wildfly.org/downloads/) and extract it in the $WILDFLY directory.

2. Download and launch JBoss OSGi installer 2.1.0 (select an installation 
   path of your choice and make sure to tick both "JBoss OSGi Distribution" 
   and "WildFly OSGi Integration" boxes).

        > java -jar jbosgi-installer-2.1.0.jar

3. Make Jboss OSGi modules reference sun.jdk modules (see https://community.jboss.org/thread/232938):
   add `<module name="sun.jdk"/>` to $WILDFLY/modules/system/layers/base/org/jboss/as/osgi/main/module.xml and
   $WILDFLY/modules/system/layers/base/org/jboss/osgi/framework/main/module.xml
		

4. Download Apache Felix Web Console and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/felix/webconsole/main	
        > cp org.apache.felix.webconsole-4.2.0-all.jar $WILDFLY/bundles/org/apache/felix/webconsole/main

5. Download Apache Felix Metatype and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/felix/metatype/main	
        > cp org.apache.felix.metatype-1.0.8.jar $WILDFLY/bundles/org/apache/felix/metatype/main

6. Download Apache Aries Util and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/aries/util/main	
        > cp org.apache.aries.util-1.0.0.jar $WILDFLY/bundles/org/apache/aries/util/main

7. Download Apache Aries Proxy and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/aries/proxy/main	
        > cp org.apache.aries.proxy-1.0.0.jar $WILDFLY/bundles/org/apache/aries/blueprint/main

8. Download Apache Aries Blueprint and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/aries/blueprint/main	
        > cp org.apache.aries.blueprint-1.0.0.jar $WILDFLY/bundles/org/apache/aries/blueprint/main

9. Make a copy of the original WildFly AS OSGi configuration file:

        > cp $WILDFLY/standalone/configuration/standalone-osgi.xml $WILDFLY/standalone/configuration/dcm4che-osgi.xml

10. Install DCM4CHE 3.3.0-SNAPSHOT bundles:

        > cd $WILDFLY
        > unzip $DCM4CHE/dcm4che-jboss-modules/target/dcm4che-jboss-bundles-3.3.0-SNAPSHOT.zip

11. Run Wildfly:

        > $WILDFLY/bin/standalone.sh -c dcm4che-osgi.xml [UNIX]
        > %WILDFLY%\bin\standalone.bat -c dcm4che-osgi.xml [Windows]

12. Register Apache bundles as capabilities, using JBoss CLI:

        > $WILDFLY/bin/jboss-cli.sh -c
        /subsystem=osgi/capability=org.apache.felix.metatypet:add(startlevel=1)
        /subsystem=osgi/capability=org.apache.felix.webconsole:add(startlevel=1)
        /subsystem=osgi/capability=org.apache.aries.blueprint:add(startlevel=1)
        /subsystem=osgi/capability=org.apache.aries.proxy:add(startlevel=1)
        /subsystem=osgi/capability=org.apache.aries.util:add(startlevel=1)

13. Register dcm4che bundles as OSGi capabilities, using JBoss CLI (as above):

        > $WILDFLY/bin/jboss-cli.sh -c
        /subsystem=osgi/capability=org.dcm4che.audit:add()
        /subsystem=osgi/capability=org.dcm4che.conf.api:add()
        /subsystem=osgi/capability=org.dcm4che.conf.api-hl7:add()
        /subsystem=osgi/capability=org.dcm4che.conf.ldap:add()
        /subsystem=osgi/capability=org.dcm4che.conf.ldap-audit:add()
        /subsystem=osgi/capability=org.dcm4che.conf.ldap-hl7:add()
        /subsystem=osgi/capability=org.dcm4che.conf.ldap-imageio:add()
        /subsystem=osgi/capability=org.dcm4che.conf.prefs:add()
        /subsystem=osgi/capability=org.dcm4che.conf.prefs-audit:add()
        /subsystem=osgi/capability=org.dcm4che.conf.prefs-hl7:add()
        /subsystem=osgi/capability=org.dcm4che.conf.prefs-imageio:add()
        /subsystem=osgi/capability=org.dcm4che.core:add()
        /subsystem=osgi/capability=org.dcm4che.emf:add()
        /subsystem=osgi/capability=org.dcm4che.hl7:add()
        /subsystem=osgi/capability=org.dcm4che.image:add()
        /subsystem=osgi/capability=org.dcm4che.imageio:add()
        /subsystem=osgi/capability=org.dcm4che.imageio-rle:add()
        /subsystem=osgi/capability=org.dcm4che.mime:add()
        /subsystem=osgi/capability=org.dcm4che.net:add()
        /subsystem=osgi/capability=org.dcm4che.net-audit:add()
        /subsystem=osgi/capability=org.dcm4che.net-hl7:add()
        /subsystem=osgi/capability=org.dcm4che.net-imageio:add()
        /subsystem=osgi/capability=org.dcm4che.soundex:add()


14. Reload WildFly
        > $WILDFLY/bin/jboss-cli.sh -c :reload

15. deploy OSGi example bundles in the deploy directory:

        > $WILDFLY/bin/jboss-cli.sh -c
        deploy $DCM4CHE/dcm4che-osgi/dcm4che-osgi-config/target/dcm4che-osgi-config-3.3.0-SNAPSHOT.jar
        deploy $DCM4CHE/dcm4che-osgi/dcm4che-osgi-device/target/dcm4che-osgi-device-3.3.0-SNAPSHOT.jar
        deploy $DCM4CHE/dcm4che-osgi/dcm4che-osgi-echo/target/dcm4che-osgi-echo-3.3.0-SNAPSHOT.jar
        deploy $DCM4CHE/dcm4che-osgi/dcm4che-osgi-rest/target/dcm4che-osgi-rest-3.3.0-SNAPSHOT.jar

Apache Felix Webconsole
-----------------------

Access Apache Felix Web Console at [http://localhost:8080/system/console]DCM4CHEDCM4CHE(http://localhost:8080/system/console) (user/pass:admin/admin)
Through the Web Console is possible to manage installed bundles, configure bundles and check active OSGi services.


Control dcm4che OSGI ECHOSCP sample - by HTTP GET
-----------------------------------------------------

1.  `HTTP GET http://localhost:8080/echoscp/running` 
    returns `true`, if the archive is running, otherwise `false`.

2.  `HTTP GET http://localhost:8080/echoscp/stop` 
     stops DCM4CHE

3.  `HTTP GET http://localhost:8080/echoscp/start` 
    starts DCM4CHE

4.  `HTTP GET http://localhost:8080/echoscp/reload` 
    reloads the configuration from the configuration backend.

*Note*: `start`, `stop` and `reload` returns `HTTP status: 204 No Content` 
on success,  which causes some HTTP clients (in particular `wget`) to hang.

