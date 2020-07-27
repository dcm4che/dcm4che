    usage: dcm2pdf [<options>] <dicom-file> <pdf|cda|stl-file>
    or dcm2pdf [Options] <dicom-file>... <pdf|cda|stl-outdir>
    or dcm2pdf [Options] <dicom-dir>... <pdf|cda|stl-outdir>
    
    Convert DICOM file(s) to PDF (file extension pdf), CDA (file extension
    xml) or STL (file extension stl) file(s). By default DICOM file(s) will be
    checked for Encapsulated PDF Storage SOP Class and converted to PDF
    file(s). To convert Encapsulated CDA / MTL / OBJ / STL DICOM file(s)
    specify corresponding options as provided.
    -
    Options:
        --cda       Convert DICOM object(s) into CDA file(s)
     -h,--help      display this help and exit
        --mtl       Convert DICOM object(s) into MTL file(s)
        --obj       Convert DICOM object(s) into OBJ file(s)
        --stl       Convert DICOM object(s) into STL file(s)
     -V,--version   output version information and exit
    -
    Example 1: dcm2pdf object.dcm file.pdf
    => Convert Encapsulated PDF DICOM object to a pdf file.
    -
    Example 2: dcm2pdf --cda object.dcm cda-file.xml
    => Convert Encapsulated CDA DICOM object to a CDA file.
    -
    Example 3: dcm2pdf object1.dcm object2.dcm pdf-dir
    => Convert the specified Encapsulated PDF DICOM objects to pdf files in
    pdf-dir.
    -
    Example 4: dcm2pdf dicom-object-dir pdf-dir
    => Convert the Encapsulated PDF DICOM objects in dicom-object-dir to pdf
    files in pdf-dir.