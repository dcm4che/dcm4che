```
usage: qstar -u <user:password> --url <url> [options] [<file-path>..]

QStar SOAP Client to fetch file information of specified files or to
retrieve/prefetch the specified files.
-
Options:
 -h,--help                   display this help and exit
    --job <jobId>            get status of retrieve job with specified
                             jobId. Can't be used with option --retrieve.
    --retrieve <priority>    retrieve specified files with specified
                             priority (1 - LOW, 2 - NORMAL, 3 - HIGH). If
                             no target directory is specified, the files
                             will be prefetched to cache and no file will
                             be created in the filesystem.
    --target-dir <path>      directory to which retrieved files are
                             stored.
 -u,--user <user:password>   user name and password to use for server
                             authentication.
    --url <url>              request URL.
 -V,--version                output version information and exit
-
Examples:
=> qstar -u user:secret --url http://127.0.0.1:18083
Verify authentication of user with password.

=> qstar -u user:secret --url http://127.0.0.1:18083 /full/path/to/file
Get file information of file with full path /full/path/to/file

=> qstar -u user:secret --url http://127.0.0.1:18083 --retrieve 3
/full/path/to/file
Prefetch file with full path /full/path/to/file with high priority to the
QStar cache

=> qstar -u user:secret --url http://127.0.0.1:18083 --retrieve 1
--target-dir /tmp /full/path/to/file
Retrieve file with full path /full/path/to/file with low priority to
directory /tmp

=> qstar -u user:secret --url http://127.0.0.1:18083 --job 42
Get job status of retrieve job with jobId 42

=> qstar -u user:secret --url http://127.0.0.1:18083 --job 42
/full/path/to/file
Get job status of retrieve of file with full path /full/path/to/file by
job with jobId 42
```
