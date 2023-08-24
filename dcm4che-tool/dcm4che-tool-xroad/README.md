```
usage: xroad -u <user> --url <url> [options] <patientID> [..]

XRoad SOAP Client to query Estonia National Patient Registry for Patient
Demographics and optionally write query results in a CSV file with header
names reflecting the Estonian property names in the SOAP messages.

Technically a response message may contain multiple patient records, which
is reflected by the first field "#", specifying the index of the patient
record in the response message contained by that CSV row:
- "1" marks the first patient record in the response message, "2" the
second, and so on.
- "0" indicates, there was no patient record in the response message at
all, typically because there is no patient record with the given
<patientID> in the National Patient Registry.
- "-1" signals an error receiving the response message, with last field
("faultString") containing an error message.

The second ("cIsikukoodid") and the third ("cValjad") field contain
properties of the request message, the remaining fields ("cIsikukood",
"cPerenimi", "cEesnimi", "cMPerenimed", "cMEesnimed", "cRiikKood",
"cRiik", "cIsanimi", "cSugu", "cSynniaeg", "cSurmKpv", "cTeoVoime",
"cIsStaatus", "cKirjeStaatus", "cEKRiik", "cEKMaak", "cEKVald",
"cEKAsula", "cEKTanav", "cEKIndeks", "cEKAlgKpv", "cEKVallaKpv",
"cEKAadress", "cSynniRiik", "cSaabusEestiKpv", "faultCode", "faultString")
contain properties of the response message.
-
Options:
 -c                                    continue on errors receiving
                                       response messages.
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
    --csv-delim <char>                 delimiter character for CSV file
                                       specified by --csv. Defaults to ,
                                       (comma).
    --csv-no-header                    write CSV file without header line.
    --csv-quote <char>                 quote character for CSV file
                                       specified by --csv. By default,
                                       only fields containing a
                                       line-break, double-quote or
                                       delimiters (commas) are quoted by a
                                       double-quote.
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

=> xroad -u EE123 --url http://xtee1trt.pacs.ee -c --csv out.csv @in.txt
Query Patient Registry for Patients with Patient IDs listed in file:
in.txt, continue on errors receiving response messages and store results
in CVS file: out.csv.
```
