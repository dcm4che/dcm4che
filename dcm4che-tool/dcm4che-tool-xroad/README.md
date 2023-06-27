```
usage: xroad -u <user> --url <url> [options] <patientID> [..]

XRoad SOAP Client to query Estonia National Patient Registry for Patient
Demographics
-
Options:
    --client.memberClass <value>       set XRoad Client Identifier
                                       property "memberClass", "NGO" by
                                       default.
    --client.memberCode <value>        set XRoad Client Identifier
                                       property "memberCode", "90007945"
                                       by default.
    --client.objectType <value>        set XRoad Client Identifier
                                       property "objectType", "SUBSYSTEM"
                                       by default.
    --client.subsystemCode <value>     set XRoad Client Identifier
                                       property "subsystemCode", "mia" by
                                       default.
    --client.xRoadInstance <value>     set XRoad Client Identifier
                                       property "xRoadInstance", "EE" by
                                       default.
    --csv <csv-file>                   write query results in CSV file.
    --csv-delim <csv-delim>            delimiter character for CSV file
                                       specified by --csv. Defaults to ,
                                       (comma).
    --csv-no-header                    write CSV file without header line.
    --csv-quote <csv-quote>            quote character for CSV file
                                       specified by --csv. By default,
                                       only values which contain the
                                       delimiter or " (quote) are quoted
                                       using " (quote).
 -h,--help                             display this help and exit
    --id <value>                       set unique identifier for this
                                       message.
    --protocolVersion <value>          set X-Road message protocol
                                       version, "4.0" by default.
    --rr441.cValjad <value>            set RR441Request property
                                       "cValjad", "1,2,6,7,9,10" by
                                       default.
    --service.memberClass <value>      set XRoad Service Identifier
                                       property "memberClass", "GOV" by
                                       default.
    --service.memberCode <value>       set XRoad Service Identifier
                                       property "memberCode", "70008440"
                                       by default.
    --service.objectType <value>       set XRoad Service Identifier
                                       property "objectType", "SERVICE" by
                                       default.
    --service.serviceCode <value>      set XRoad Service Identifier
                                       property "serviceCode", "RR441" by
                                       default.
    --service.serviceVersion <value>   set XRoad Service Identifier
                                       property "serviceVersion", "v1" by
                                       default.
    --service.subsystemCode <value>    set XRoad Service Identifier
                                       property "subsystemCode", "rr" by
                                       default.
    --service.xRoadInstance <value>    set XRoad Service Identifier
                                       property "xRoadInstance", "EE" by
                                       default.
 -U,--url <url>                        request URL.
 -u,--user <id>                        User ID passed in header element
                                       "userId".
 -V,--version                          output version information and exit
-
Examples:
=> xroad -u EE123 --url http://xtee1trt.pacs.ee 49202247013
Query Patient Registry with User ID EE123 at http://xtee1trt.pacs.ee for
Patient with Patient ID 49202247013.

=> xroad -u EE123 --url http://xtee1trt.pacs.ee --csv result.csv @pids.txt
Query Patient Registry for Patients with Patient IDs listed in file:
pids.txt and store results in CVS file: result.csv.
```
