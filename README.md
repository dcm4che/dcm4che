dcm4che DICOM Toolkit & Library
===============================
Sources: https://github.com/dcm4che/dcm4che  
Binaries: https://sourceforge.net/projects/dcm4che/files/dcm4che3  
Issue Tracker: https://github.com/dcm4che/dcm4che/issues  
Build Status: [![Build](https://github.com/dcm4che/dcm4che/actions/workflows/build.yml/badge.svg)](https://github.com/dcm4che/dcm4che/actions/workflows/build.yml)

This is a complete rewrite of [dcm4che-2.x](http://www.dcm4che.org/confluence/display/d2/).

One main focus was to minimize the memory footprint of the DICOM data sets.
It already provides modules to store/fetch configuration data to/from LDAP,
compliant to the DICOM Application Configuration Management Profile,
specified in [DICOM PS 3.15](http://dicom.nema.org/medical/dicom/current/output/html/part15.html#chapter_H), Annex H.

dcm4che uses a native library for the compression and decompression of images. Here is the list of supported systems and architectures:

| System  | Architecture | Package        | Requirement            |
|:--------|:-------------|:---------------|:-----------------------|
| Linux   | x86 64-bit   | linux-x86-64   | GLIBC_2.17             |
| Linux   | x86 32-bit   | linux-x86      | GLIBC_2.17             |
| Linux   | ARM 64-bit   | linux-aarch64  | GLIBC_2.27             |
| Linux   | ARM 32-bit   | linux-armv7a   | GLIBC_2.17             |
| Linux   | s390x        | linux-s390x    | GLIBC_2.17             |
| Windows | x86 64-bit   | windows-x86-64 | Windows 7 or higher    |
| Windows | x86 32-bit   | windows-x86    | Windows 7 or higher    |
| Mac OS  | x86 64-bit   | macosx-x86-64  | Mac OS 10.13 or higher |
| Mac OS  | ARM 64-bit   | macosx-aarch64 | Mac OS 11 or higher    |

Build
-----

Make sure you have Java 17 (JDK) or newer installed.

Run the [Maven Wrapper](https://maven.apache.org/wrapper/) script for building:

    ./mvnw install

or on Windows:

    .\mvnw install

Modules
-------
- dcm4che-audit
- dcm4che-audit-keycloak
- dcm4che-conf
  - dcm4che-conf-api
  - dcm4che-conf-api-hl7
  - dcm4che-conf-json
  - dcm4che-conf-json-schema
  - dcm4che-conf-ldap
  - dcm4che-conf-ldap-audit
  - dcm4che-conf-ldap-hl7
  - dcm4che-conf-ldap-imageio
  - dcm4che-conf-ldap-schema
- dcm4che-core
- dcm4che-dcmr
- dcm4che-deident
- dcm4che-dict
- dcm4che-dict-priv
- dcm4che-emf
- dcm4che-hl7
- dcm4che-image
- dcm4che-imageio
- dcm4che-imageio-opencv
- dcm4che-imageio-rle
- dcm4che-js-dict
- dcm4che-json
- dcm4che-mime
- dcm4che-net
- dcm4che-net-audit
- dcm4che-net-hl7
- dcm4che-net-imageio
- dcm4che-soundex
- dcm4che-ws-rs
- dcm4che-xdsi
- dcm4che-jboss-modules

Utilities
---------
- [agfa2dcm][]: Extract DICOM files from Agfa BLOB file
- [agfa2sr][]: Extract concatenated XML Agfa Reports and convert them to DICOM SR Documents
- [dcm2dcm][]: Transcode DICOM file according the specified Transfer Syntax
- [dcm2jpg][]: Convert DICOM image to JPEG or other image formats
- [dcm2json][]: Convert DICOM file in JSON presentation
- [dcm2pdf][]: Extract encapsulated PDF, CDA or STL from DICOM file
- [dcm2str][]: Apply Attributes Format Pattern to dicom file or command line parameters.
- [dcm2xml][]: Convert DICOM file in XML presentation
- [dcmbenchmark][]: Parse a DICOM file repetitively, measuring time and used memory
- [dcmdir][]: Dump, create or update DICOMDIR file
- [dcmdump][]: Dump DICOM file in textual form
- [dcmldap][]: Insert/remove configuration entries for Network AEs into/from LDAP server
- [dcmqrscp][]: Simple DICOM archive
- [dcmvalidate][]: Validate DICOM object according a specified Information Object Definition
- [deidentify][]: De-identify one or several DICOM files
- [emf2sf][]: Convert DICOM Enhanced Multi-frame image to legacy DICOM Single-frame images
- [findscu][]: Invoke DICOM C-FIND Query Request
- [fixlo2un][]: Fixes length of private tags truncated to 2 bytes on conversion from implicit VR to explicit VR Transfer Syntax
- [getscu][]: Invoke DICOM C-GET Retrieve Request
- [hl72xml][]: Convert HL7 v2.x message in XML presentation
- [hl7pdq][]: Query HL7 v2.x Patient Demographics Supplier
- [hl7pix][]: Query HL7 v2.x PIX Manager
- [hl7rcv][]: HL7 v2.x Receiver
- [hl7snd][]: Send HL7 v2.x message
- [ianscp][]: DICOM Instance Availability Notification receiver 
- [ianscu][]: Send DICOM Instance Availability Notification
- [jpg2dcm][]: Convert JPEG images or MPEG videos in DICOM files
- [json2dcm][]: Converts JSON file to DICOM file
- [json2index][]: Creates search index for UI configuration from JSON schema files
- [json2props][]: Convert Archive configuration schema JSON files to key/value properties files and vice versa
- [json2rst][]: Generate ReStructuredText files from Archive configuration schema JSON files
- [maskpxdata][]: Mask information burned into the Pixel Data
- [mkkos][]: Make DICOM Key Object Selection Document
- [modality][]: Simulates DICOM Modality
- [movescu][]: Invoke DICOM C-MOVE Retrieve request
- [mppsscp][]: DICOM Modality Performed Procedure Step Receiver
- [mppsscu][]: Send DICOM Modality Performed Procedure Step
- [pdf2dcm][]: Convert PDF file into DICOM file
- [planarconfig][]: Detects the actual planar configuration of uncompressed pixel data of color images with Photometric Interpretation RGB or YBR_FULL and optionally correct non matching values of attribute Planar Configuration of the image
- [qstar][]: QStar SOAP Client to prefetch/retrieve or fetch file information of specified files
- [stgcmtscu][]: Invoke DICOM Storage Commitment Request
- [storescp][]: DICOM Composite Object Receiver
- [storescu][]: Send DICOM Composite Objects
- [stowrs][]: Send DICOM Composite Objects or Bulkdata file over Web
- [stowrsd][]: STOW-RS Server
- [swappxdata][]: Swaps bytes of uncompressed pixel data in DICOM files
- [syslog][]: Send Syslog messages via TCP/TLS or UDP to a Syslog Receiver
- [syslogd][]: Receives RFC 5424 Syslog messages via TCP/TLS or UDP
- [tpl2xml][]: Converts private dictionaries present in template file(s) to xml file(s).
- [upsscu][]: Invokes services of Unified Procedure Step Service Class
- [wadors][]: Wado RS Client Simulator 
- [wadows][]: Wado WS Client Simulator 
- [xml2dcm][]: Create/Update DICOM file from/with XML presentation
- [xml2hl7][]: Create HL7 v2.x message from XML presentation
- [xroad][]: XRoad SOAP Client to query Estonia National Patient Registry for Patient Demographics

[agfa2dcm]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-agfa2dcm/README.md
[agfa2sr]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-agfa2sr/README.md
[dcm2dcm]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2dcm/README.md
[dcm2jpg]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2jpg/README.md
[dcm2json]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2json/README.md
[dcm2pdf]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2pdf/README.md
[dcm2str]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2str/README.md
[dcm2xml]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2xml/README.md
[dcmbenchmark]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcmbenchmark/README.md
[dcmdir]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcmdir/README.md
[dcmdump]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcmdump/README.md
[dcmldap]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcmldap/README.md
[dcmqrscp]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcmqrscp/README.md
[dcmvalidate]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcmvalidate/README.md
[deidentify]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-deidentify/README.md
[emf2sf]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-emf2sf/README.md
[findscu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-findscu/README.md
[fixlo2un]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-fixlo2un/README.md
[getscu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-getscu/README.md
[hl72xml]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-hl72xml/README.md
[hl7pdq]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-hl7pdq/README.md
[hl7pix]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-hl7pix/README.md
[hl7rcv]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-hl7rcv/README.md
[hl7snd]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-hl7snd/README.md
[ianscp]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-ianscp/README.md
[ianscu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-ianscu/README.md
[jpg2dcm]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-jpg2dcm/README.md
[json2dcm]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-json2dcm/README.md
[json2index]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-json2index/README.md
[json2props]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-json2props/README.md
[json2rst]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-json2rst/README.md
[maskpxdata]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-maskpxdata/README.md
[mkkos]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-mkkos/README.md
[modality]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-ihe/dcm4che-tool-ihe-modality/README.md
[movescu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-movescu/README.md
[mppsscp]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-mppsscp/README.md
[mppsscu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-mppsscu/README.md
[pdf2dcm]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-pdf2dcm/README.md
[planarconfig]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-planarconfig/README.md
[qstar]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-qstar/README.md
[stgcmtscu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-stgcmtscu/README.md
[storescp]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-storescp/README.md
[storescu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-storescu/README.md
[stowrs]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-stowrs/README.md
[stowrsd]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-stowrsd/README.md
[swappxdata]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-swappxdata/README.md
[syslog]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-syslog/README.md
[syslogd]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-syslogd/README.md
[tpl2xml]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-tpl2xml/README.md
[upsscu]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-upsscu/README.md
[wadors]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-wadors/README.md
[wadows]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-wadows/README.md
[xml2dcm]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-xml2dcm/README.md
[xml2hl7]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-xml2hl7/README.md
[xroad]: https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-xroad/README.md

License
-------
* [Mozilla Public License Version 1.1](http://www.mozilla.org/MPL/1.1/)

