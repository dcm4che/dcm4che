    usage: qc <qcurl> [OPTIONS] <SPLIT|MERGE|SEGMENT|UPDATE|DELETE|REJECT|RESTORE> <targetstudyuid> \
                                <codevalue:codeschemedesignator:codemeaning:codeversion>
    
    The QC tool provides a way to perform QC operations on a dcm4che archive. 
    These operations include, merge, split, segment, update, delete and reject or restore. 
    Other operations not supported by this tool include patient operations.
    -
    Options:
     -u,--url                                 Server url for qc services.
        --updatescope <STUDY|SERIES|PATIENT|INSTANCE|NONE>  
                                              An update scope for update operations,
                                              used to clarify to which scope the update
                                              data belong possible values include 
                                              STUDY,SERIES,PATIENT, INSTANCE and 
                                              NONE for no update.
        --moveuids  <uid[, uid, ...]>        if a move operation is specified 
                                              (SPLIT or SEGMENT) then one or move 
                                              uids are to be specified.
        --mergeuids  <uid[, uid, ...]>       if a MERGE operation is specified
                                              then one or merge source stydt 
                                              uids are to be specified.
        --restorerejectuids  <uid[, uid, ...]> 
                                              if a a reject or restore
                                              operation is specified
                                              then one or uids are to be specified.
        --cloneuids  <uid[, uid, ...]>       if SEGMENT is specified then zero
                                              or move uids are to be specified.
        --overridetargetstudy   <[seq/]attr=value>              
                                              specify override attributes for target
                                              study attr can be specified by keyword
                                              or tag value (in hex), e.g. PatientName
                                              or 00100010. Attributes in nested 
                                              Datasets can be specified by including
                                              the keyword/tag value of the sequence 
                                              attribute, e.g. 00400275/00400009 for
                                              Scheduled Procedure Step ID in the 
                                              Request Attributes Sequence.
        --overridetargetseries   <[seq/]attr=value>              
                                              specify override attributes for target
                                              series attr can be specified by keyword
                                              or tag value (in hex), e.g. Modality 
                                              or 00080060. Attributes in nested 
                                              Datasets can be specified by including
                                              the keyword/tag value of the sequence 
                                              attribute, e.g. 00400275/00400009 for
                                              Scheduled Procedure Step ID in the 
                                              Request Attributes Sequence.
        --updatedata    <[seq/]attr=value>    specify update attributes for target
                                              of update with a certain scope attr 
                                              can be specified by keyword or tag 
                                              value (in hex), e.g. Modality or 00080060.
                                              Attributes in nested Datasets can be 
                                              specified by including the 
                                              keyword/tag value of the sequence 
                                              attribute, e.g. 00400275/00400009 
                                              for Scheduled Procedure Step ID
                                              in the Request Attributes Sequence.
        --pid  <patientid:localentityid[:universalentityid:universalentityidtype]>
                                              specifies patient id, required for
                                              SEGMENT and SPLIT operations.
        --deleteobject <studyuid[:seriesuid[:instanceuid]]]>
                                              specifies the delete object.
     -h,--help                                display this help and exit
     -V,--version                             output version information and
                                              exit.
    -
    Examples:\n\
    $ qc http://localhost:8080/dcm4che-arc/qc --mergeuids 1.2,1.2.3.4 MERGE 
    1.2.3.4.5 113001:Rejected for Quality Reasons:DCM
    Merges studies 1.2 and 1.2.3.4 with study 1.2.3.4.5 with a rejection code of 113001 by DCM.