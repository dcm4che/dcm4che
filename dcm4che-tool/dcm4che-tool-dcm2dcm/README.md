    usage: dcm2dcm [<options>] [-t <uid>] <infile> <outfile>
                   or dcm2dcm [Options] [-t <uid>] <uid> <infile>... <outdir>
                   or dcm2dcm [Options] [-t <uid>] <uid> <indir>... <outdir>
    
    Transcode one or several DICOM files according the specified Transfer
    Syntax.
    -
    Options:
     -C <name=value>              specify additional compression parameter
        --expl-item-len           encode sequence items with explicit length;
                                  at default, non-empty sequence items are
                                  encoded with undefined length
        --expl-seq-len            encode sequences with explicit length; at
                                  default, non-empty sequences are encoded
                                  with undefined length
     -f,--retain-fmi              retain File Meta Information from source
                                  files if available. At default the File Meta
                                  Information is replaced by one referencing
                                  dcm4che-3.x
     -F,--no-fmi                  transcode sources to Implicit VR Little
                                  Endian and store it without File Meta
                                  Information
        --group-len               include (gggg,0000) Group Length attributes;
                                  at default, optional Group Length attributes
                                  are excluded
     -h,--help                    display this help and exit
        --j2ki                    compress JPEG 2000 Lossy; equivalent to -t
                                  1.2.840.10008.1.2.4.91
        --j2kr                    compress JPEG 2000 Lossless; equivalent to
                                  -t 1.2.840.10008.1.2.4.90
        --jpeg                    compress JPEG Lossy; equivalent to -t
                                  1.2.840.10008.1.2.4.50 or .51
        --jpll                    compress JPEG Lossless; equivalent to -t
                                  1.2.840.10008.1.2.4.70
        --jpls                    compress JPEG LS Lossless; equivalent to -t
                                  1.2.840.10008.1.2.4.80
        --legacy                  use legacy Compressor/Decompressor instead
                                  Transcoder
     -Q <encoding-rate>           encoding rate in bits per pixel of JPEG 2000
                                  Lossy compression
     -q <quality>                 compression quality (0.0-1.0) of JPEG Lossy
                                  compression
     -t,--transfer-syntax <uid>   transcode sources to specified Transfer
                                  Syntax. At default use Explicit VR Little
                                  Endian
        --undef-item-len          encode all sequence items with undefined
                                  length; at default, only non-empty sequence
                                  items are encoded with undefined length
        --undef-seq-len           encode all sequences with undefined length;
                                  at default, only non-empty sequences are
                                  encoded with undefined length
     -V,--version                 output version information and exit
        --verify <max-error>      verify compression if decompressed pixel
                                  values does not differ from original pixel
                                  values more than <max-error>
        --verify-block <size>     verify compression by comparing average
                                  pixel values of specified block size; 1 by
                                  default
    Examples:
    $ dcm2dcm --jpll img.dcm jpll.dcm
    Compress DICOM image in.dcm to jpll.dcm with JPEG Lossless,
    Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1])
    Transfer Syntax
    $ dcm2dcm jpll.dcm out.dcm
    Decompress DICOM image jpll.dcm to out.dcm with Explicit VR Little Endian
    Transfer Syntax
