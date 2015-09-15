#!/bin/sed -nf
# Disclaimer and Terms: You may use these scripts for commercial or
# non-commercial use at your own risk, as long as you retain the
# copyright statements in the source code. These scripts are provided
# "AS IS" with no warranty whatsoever and are FREE for as long as you
# want to use them. You can edit and adapt them to your requirements
# without seeking permission from me. I only ask that you retain the
# credits where they are due.
#
# Author: Vishal Goenka <vgoenka@hotmail.com>
#
# Unfold LDIF (LDAP Data Interchange Format) lines
# Version 1.0
#
# Usage: unldif.sed <ldif file>
# or
# cat <ldif file> | unldif.sed
# and if /bin/sed is not available,
# sed -nf unldif.sed <ldif file>
#
# Most LDIF generators will fold a long field on multiple lines by
# inserting a line separator (either a linefeed or carriage
# return/linefeed pair) followed by a space. Processing such ldif
# files through another script becomes much easier if such lines were
# unfolded. That is exactly what this script does. It unfolds ldif
# entries that are folded (broken) across lines and writes them on a
# single line.
#
{
    1{
h;
n;
    }
    /^ /!{
H;
g;
s,\n.*,,p;
g;
s,.*\n,,;
h;
    }
    /^ /{
H;
g;
s,\n ,,;
h;
    }
    ${
g;
s,\n ,,;
p;
    }
}

