    usage: dcm2pdf <dicom-file> <pdf|cda|stl|mtl|obj|genozip|bzip2-file>
    or dcm2pdf <dicom-file>... <pdf|cda|stl|mtl|obj|genozip|bzip2-outdir>
    or dcm2pdf <dicom-dir>... <pdf|cda|stl|mtl|obj|genozip|bzip2-outdir>

    Convert DICOM file(s) to PDF (file extension pdf), CDA (file extension
    xml), STL (file extension stl), MTL (file extension mtl), OBJ (file
    extension obj) or Genozip compressed genomic (file extension genozip)
    file(s). Supported content types are application/pdf (for PDF files),
    text/xml (for CDA files), application/sla or model/stl or
    model/x.stl-binary (for STL files), model/mtl (for MTL files), model/obj
    (for OBJ files), application/vnd.genozip (for Genozip compressed genomic
    files), application/prs.vcfbzip2 (for Bzip2 compressed genomic data VCF
    files) and application/x-bzip2 (for Bzip2 compressed genomic data Document
    files).
    -
    Options:
     -h,--help      display this help and exit
     -V,--version   output version information and exit
    -
    Example 1: dcm2pdf object.dcm file.pdf
    => Convert encapsulated PDF DICOM object to a pdf file.
    -
    Example 2: dcm2pdf cda-object.dcm cda-file.xml
    => Convert encapsulated CDA DICOM object to a CDA file.
    -
    Example 3: dcm2pdf genozip-object.dcm genozip-file.genozip
    => Convert encapsulated Genozip compressed genomic DICOM object to a
    Genozip compressed genomic file.
    -
    Example 4: dcm2pdf stl-object.dcm stl-file.stl
    => Convert encapsulated STL DICOM object to a STL file.
    -
    Example 5: dcm2pdf mtl-object.dcm mtl-file.mtl
    => Convert encapsulated MTL DICOM object to a MTL file.
    -
    Example 6: dcm2pdf obj-object.dcm obj-file.obj
    => Convert encapsulated OBJ DICOM object to a OBJ file.
    -
    Example 7: dcm2pdf object1.dcm object2.dcm pdf-dir
    => Convert the specified encapsulated PDF DICOM objects to pdf files in
    pdf-dir.
    -
    Example 8: dcm2pdf dicom-object-dir pdf-dir
    => Convert the encapsulated PDF DICOM objects in dicom-object-dir to pdf
    files in pdf-dir.
    -
    Example 9: dcm2pdf vcfbzip2.object.dcm vcfbzip2-file.vcfbz2
    => Convert the specified encapsulated Bzip2 compressed genomic data VCF
    DICOM object to Bzip2 compressed genomic data VCF file.