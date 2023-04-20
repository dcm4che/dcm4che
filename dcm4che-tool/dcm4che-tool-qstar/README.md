```
usage: qstar -u <user:password> --url <url> [options] [<file-path>..]

QStar SOAP Client to fetch file information of specified files or to
retrieve/prefetch the specified files.
-
Options:
 -D,--target-dir <path>      directory to which retrieved files are
                             stored.
 -h,--help                   display this help and exit
 -j,--job <jobId>            get retrieve status of specified files
                             retrieved by job with specified jobId. If no
                             file-path is specified, get the overall
                             status of the retrieve job with specified
                             jobId. Can't be used with option --retrieve.
 -p,--progress <ms>          continue fetching the retrieve status of
                             specified files in specified interval after
                             invoking the retrieve request until the
                             retrieve job is completed. If no interval is
                             specified, fetch status continuously without
                             delay between successive object status
                             requests.
 -r,--retrieve <priority>    retrieve specified files with specified
                             priority (1 - LOW, 2 - NORMAL, 3 - HIGH). If
                             no target directory is specified, the files
                             will be prefetched to cache and no file will
                             be created in the filesystem.
 -u,--user <user:password>   user name and password to use for server
                             authentication.
 -U,--url <url>              request URL.
 -V,--version                output version information and exit
-
Examples:
=> qstar -u user:secret --url http://127.0.0.1:18083
Verify authentication of user with password.

=> qstar -u user:secret --url http://127.0.0.1:18083 /path/to/file
Get file information of file with full path /path/to/file.

=> qstar -u user:secret --url http://127.0.0.1:18083 --retrieve 1 -p 100
/path/to/file1 /path/to/file2
Prefetch files with full path /path/to/file1 and /path/to/file2 with low
priority to the QStar cache and continue fetching the retrieve status of
the specified files in 100ms interval until the prefetch job is completed.

=> qstar -u user:secret --url http://127.0.0.1:18083 --retrieve 3
--target-dir /path/to/dir /path/to/file1 /path/to/file2
Retrieve files with full path /path/to/file1 and /path/to/file2 with high
priority to directory /path/to/dir.

=> qstar -u user:secret --url http://127.0.0.1:18083 --job 42
Get job status of retrieve job with jobId 42.

=> qstar -u user:secret --url http://127.0.0.1:18083 --job 42
/path/to/file1 /path/to/file2
Get retrieve status of files with full path /path/to/file1 and
/path/to/file2 retrieved by job with jobId 42.
```
