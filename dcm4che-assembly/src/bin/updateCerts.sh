#!/bin/sh
DIRNAME="`dirname "$0"`"
ETC=`cd "$DIRNAME"/../etc; pwd`
CACERTS=$ETC/dcm4chee-arc/cacerts.jks
if [ -f $CACERTS ]
    then rm $CACERTS
fi
for name in syslogd hl7snd stgcmtscu findscu hl7pix ianscp storescu mppsscu movescu ianscu dcmqrscp getscu hl7rcv storescp syslog mppsscp
do
    $DIRNAME/updateCert.sh $name dcm4che-tool $CACERTS
done
$DIRNAME/updateCert.sh dcm4chee-arc dcm4chee $CACERTS
for name in syslogd hl7snd stgcmtscu findscu hl7pix ianscp storescu mppsscu movescu ianscu dcmqrscp getscu hl7rcv storescp syslog mppsscp
do
    cp $CACERTS $ETC/$name/cacerts.jks
done
