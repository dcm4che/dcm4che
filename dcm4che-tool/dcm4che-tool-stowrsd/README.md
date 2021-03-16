```
usage: stowrsd -b [<ip>:]<port>

The stowrsd application implements a STOW-RS server. Objects in received
multipart requests may be unpacked and stored in individual files.
-
Options:
 -b,--bind <[ip:]port>   specify the port on which the STOW-RS Server
                         shall listening for connection requests. If no
                         local IP address of the network interface is
                         specified, connections on any/all local addresses
                         are accepted.
    --backlog <no>       maximum number of queued incoming connections.
                         Use system default if not specified.
 -d,--directory <path>   directory under which received data is stored.
                         With --unpack, extracted objects are stored in
                         sub-directories. Sub-directory / file names
                         encodes the date-time of the request. '.' by
                         default.
 -h,--help               display this help and exit
    --ignore             do not store received data in files.
 -t,--threads <no>       maximum number of concurrently handled requests,
                         1 by default.
 -u,--unpack             unpack objects from received multipart requests.
 -V,--version            output version information and exit
-
Example: stowrsd -b 8080
=> Starts server listening on port 8080.
```
