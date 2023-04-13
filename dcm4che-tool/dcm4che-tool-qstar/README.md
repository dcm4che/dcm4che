```
usage: qstar -u <user:password> <url> [<file-path>..]

QStar SOAP Client to fetch file information of specified files. If no
<file-path> is specified, only test user authentication.
-
Options:
 -h,--help                   display this help and exit
 -u,--user <user:password>   Specify the user name and password to use for
                             server authentication
 -V,--version                output version information and exit
-
Example: qstar -u user:secret http://127.0.0.1:18083 /full/path/to/file
```
