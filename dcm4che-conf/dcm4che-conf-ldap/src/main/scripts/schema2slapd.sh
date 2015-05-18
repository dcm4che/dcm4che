#!/bin/sh
cn=$(basename $1 .schema)
echo \# DICOM Application Configuration Data Model Hierarchy LDAP Schema
echo dn: cn=$cn,cn=schema,cn=config
echo objectClass: olcSchemaConfig
echo cn: $cn
sed '
/^#.*/d
/^ *$/d
s/attributetype/olcAttributeTypes:/
s/objectclass/olcObjectClasses:/' $1