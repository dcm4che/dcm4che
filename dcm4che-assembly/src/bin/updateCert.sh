#!/bin/sh
DIRNAME="`dirname "$0"`"
ETC=`cd "$DIRNAME"/../etc; pwd`
DN="CN=$1, OU=$2, O=dcm4che, L=Vienna, ST=Vienna, C=AT"
KEYSTORE=$ETC/$1/key.jks
if [ -f $KEYSTORE ]
    then rm $KEYSTORE
fi
keytool -genkeypair -v -alias $1 -keystore $KEYSTORE -storepass secret -keypass secret -keyalg RSA -validity 365 -dname "$DN"
keytool -exportcert -v -alias $1 -keystore $KEYSTORE -storepass secret | \
keytool -importcert -v -alias $1 -keystore $3 -storepass secret -noprompt
