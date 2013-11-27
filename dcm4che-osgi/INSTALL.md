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
		

3. Download Apache Felix Web Console and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/felix/webconsole/main	
        > cp org.apache.felix.webconsole-4.2.0-all.jar $WILDFLY/bundles/org/apache/felix/webconsole/main

4. Download Apache Felix Metatype and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/felix/metatype/main	
        > cp org.apache.felix.metatype-1.0.8.jar $WILDFLY/bundles/org/apache/felix/metatype/main

4. Download Apache Aries Util and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/aries/util/main	
        > cp org.apache.aries.util-1.0.0.jar $WILDFLY/bundles/org/apache/aries/util/main

4. Download Apache Aries Proxy and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/aries/proxy/main	
        > cp org.apache.aries.proxy-1.0.0.jar $WILDFLY/bundles/org/apache/aries/blueprint/main

4. Download Apache Aries Blueprint and copy it in the bundles directory:

        > mkdir -p $WILDFLY/bundles/org/apache/aries/blueprint/main	
        > cp org.apache.aries.blueprint-1.0.0.jar $WILDFLY/bundles/org/apache/aries/blueprint/main

5. Make a copy of the original WildFly AS OSGi configuration file:

        > cp $WILDFLY/standalone/configuration/standalone-osgi.xml $WILDFLY/standalone/configuration/dcm4che-osgi.xml

5. Install DCM4CHE 3.2.1 bundles:

        > cd $WILDFLY
        > unzip $DCM4CHE/dcm4che-jboss-modules/target/dcm4che-jboss-bundles-3.3.0-SNAPSHOT.zip

6. Run Wildfly:

        > $WILDFLY/bin/standalone.sh -c dcm4che-osgi.xml [UNIX]
        > %WILDFLY%\bin\standalone.bat -c dcm4che-osgi.xml [Windows]

7. Add the Apache bundles as capabilities, usign JBoss CLI (the first command of the pipe lists all the bundles, 
   the second one places it each on a separate line, the third one constructs and executes the commands)

        > echo felix.webconsole felix.metatype aries.util aries.proxy aries.blueprint | xargs -n 1 | xargs -I to_add sh -c "$WILDFLY/bin/jboss-cli.sh -c  \"/subsystem=osgi/capability=org.apache."to_add":add(startlevel=1)\"" 
	
8. Add the DCM4CHE OSGi capabilities, usign JBoss CLI (as above):

        > echo audit conf.api  conf.api-hl7  conf.ldap  conf.ldap-audit  conf.ldap-hl7  conf.ldap-imageio  conf.prefs  conf.prefs-audit  conf.prefs-hl7  conf.prefs-imageio core  emf  hl7  image  imageio  imageio-rle  mime  net  net-audit  net-hl7  net-imageio  soundex | xargs -n 1 | xargs -I to_add sh -c "$WILDFLY/bin/jboss-cli.sh -c  \"/subsystem=osgi/capability=org.dcm4che."to_add":add(startlevel=1)\"" 

9. Restart WildFly

11. Copy OSGi example bundles in the deploy directory:

        > cp $DCM4CHE/dcm4che-osgi/dcm4che-osgi-config/target/dcm4che-osgi-config-3.3.0-SNAPSHOT.jar $WILDFLY/standalone/deployments
        > cp $DCM4CHE/dcm4che-osgi/dcm4che-osgi-device/target/dcm4che-osgi-device-3.3.0-SNAPSHOT.jar $WILDFLY/standalone/deployments
        > cp $DCM4CHE/dcm4che-osgi/dcm4che-osgi-echo/target/dcm4che-osgi-echo-3.3.0-SNAPSHOT.jar $WILDFLY/standalone/deployments	  
        > cp $DCM4CHE/dcm4che-osgi/dcm4che-osgi-rest/target/dcm4che-osgi-rest-3.3.0-SNAPSHOT.jar $WILDFLY/standalone/deployments
 
Apache Felix Webconsole
-----------------------

Access Apache Felix Web Console at [http://localhost:8080/system/console]DCM4CHEDCM4CHE(http://localhost:8080/system/console) (user/pass:admin/admin)
Through the Web Console is possible to manage installed bundles, configure bundles and check active OSGi services.

Configure LDAP
-----------------------

Under [http://localhost:8080/system/console/configMgr](http://localhost:8080/system/console/configMgr), configure the LDAP connection (dcm4che LDAP configuration) and Save. No need to restart.


Control DCM4CHE Archive - OSGI examples - by HTTP GET
-----------------------------------------------------

1.  `HTTP GET http://localhost:8080/dcm4chee-arc/rs/running` 
    returns `true`, if the archive is running, otherwise `false`.

2.  `HTTP GET http://localhost:8080/dcm4chee-arc/rs/stop` 
     stops DCM4CHE

3.  `HTTP GET http://localhost:8080/dcm4chee-arc/rs/start` 
    starts DCM4CHE

4.  `HTTP GET http://localhost:8080/dcm4chee-arc/rs/reload` 
    reloads the configuration from the configuration backend.

*Note*: `start`, `stop` and `reload` returns `HTTP status: 204 No Content` 
on success,  which causes some HTTP clients (in particular `wget`) to hang.


Testing  DCM4CHE - OSGI examples - WILDFLY 
------------------------------------------

For testing DCM4CHEE Archive 4.x, you may use DICOM and HL7 utilities provided by
[DCM4CHE 3.x](https://sourceforge.net/projects/dcm4che/files/dcm4che3/). After
extraction, you will find launcher scripts for Linux and Windows in directory 
`dcm4che-<version>/bin/`.

### Test Query for DICOM Composite Objects

Use DCM4CHE 3.x's `findscu` utility to perform an echo:

    > $DCM4CHE_HOME/bin/findscu -cECHOSCP@localhost:11112
