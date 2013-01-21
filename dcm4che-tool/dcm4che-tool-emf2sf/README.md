    usage: emf2sf [<options>] <dicom-file>
    
    The emf2sf utility converts a DICOM Enhanced CT, MR or PET Multi-frame
    image to legacy DICOM Single-frame CT, MR, PET images.
    -
    Options:
     -f,--frame <no[,..]>       comma separated numbers of frames to convert;
                                convert all frames by default
     -h,--help                  display this help and exit
        --inst-no <format>      specifies instance number in created
                                Single-frame images as printf pattern. First %
                                will be replaced by the instance number of the
                                Enhanced Multi-frame image, second % by the
                                frame number (default: '%s%04d')
        --not-chseries          do not change Series Instance UID in created
                                Single-frame images
        --out-dir <directory>   directory to which extracted frames are stored
                                in DICOM files with file names specified by
                                option --out-file (default: '.')
        --out-file <name>       name of DICOM files of converted legacy DICOM
                                Single-frame images written to the directory
                                specified by out-dir. Zeros will be replaced
                                by the frame number (default:
                                <dicom-file>-000.dcm)
     -V,--version               output version information and exit
    Example:
    $ emf2sf -f 1,20,120 --out-file ct-000.dcm ct-emf.dcm
    Extract frame 1, 20 and 120 from Enhanced CT Multi-frame image ct-emf.dcm
    to legacy DICOM Single-frame CT images ct-001.dcm, ct-020.dcm and
    ct-120.dcm.
