```
usage: stowrsd -b [<ip>:]<port>

The stowrsd application implements a STOW-RS server. Objects in received
multipart requests are extracted in files.
-
Options:
 -b,--bind <[ip:]port>   specify the port on which the STOW-RS Server
                         shall listening for connection requests. If no
                         local IP address of the network interface is
                         specified, connections on any/all local addresses
                         are accepted.
    --directory <path>   directory under which received objects are stored
                         in sub-directories encoding the date-time of the
                         request. '.' by default.
 -h,--help               display this help and exit
    --ignore             do not store received payloads in files.
 -V,--version            output version information and exit
-
Example: stowrsd -b 8080
=> Starts server listening on port 8080.
```
