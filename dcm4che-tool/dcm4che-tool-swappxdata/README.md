```
usage: swappxdata [--uids] [--ifBigEndian [--testAll]] <file>|<directory>...

The swappxdata utility swaps bytes of uncompressed pixel data with Value
Representation OW.
For each successfully updated file a dot (.) character is written to stdout.
If an error occurs on updating a file, an E character is written to stdout
and a stack trace is written to stderr.
For each file kept untouched, one of the characters:
p - no pixel data
c - compressed pixel data
b - pixel data with Value Representation OB
l - little endian encoded pixel data
8 - pixel data with 8 bits allocated
is written to stdout.

Options:
--uids           log SOP Instance UIDs from updated files in file 'uids.log'
                 in working directory.
--ifBigEndian    test encoding of pixel data; keep files untouched, if the
                 pixel data is encoded with little endian or 8 bits allocated.
                 By default, bytes of uncompressed pixel data with Value
                 Representation OW will be swapped, independent of its
                 encoding.
--testAll        test encoding of pixel data of each file. By default, if one
                 file of a directory is detected as not big endian encoded,
                 all remaining files of the directory are kept also untouched
                 without loading them in memory for testing.
```