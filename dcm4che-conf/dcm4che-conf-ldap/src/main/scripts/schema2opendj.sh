#!/bin/sh
echo \# DICOM Application Configuration Data Model Hierarchy LDAP Schema
echo dn: cn=schema
echo objectClass: top
echo objectClass: ldapSubentry
echo objectClass: subschema
sed '
/^#.*/d
/^ *$/d
s/attributetype/attributeTypes:/
s/objectclass/objectClasses:/' $1
