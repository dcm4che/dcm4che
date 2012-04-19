package org.dcm4che.tool.storescp;

import java.io.File;
import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.net.Association;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCStoreSCP;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.util.AttributesFormat;

class CStoreSCPImpl extends BasicCStoreSCP {

    private final StoreSCP storeSCP;

    CStoreSCPImpl(StoreSCP storeSCP) {
        super("*");
        this.storeSCP = storeSCP;
    }

    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq,
            PDVInputStream data, Attributes rsp)
            throws IOException {
        rsp.setInt(Tag.Status, VR.US, storeSCP.getStatus());
        if (storeSCP.getStorageDirectory() != null)
            super.store(as, pc, rq, data, rsp);
    }

    @Override
    protected File createFile(Association as, Attributes rq)
            throws DicomServiceException {
        return new File(storeSCP.getStorageDirectory(),
                rq.getString(Tag.AffectedSOPInstanceUID) + StoreSCP.PART_EXT);
    }

    @Override
    protected Attributes parse(Association as, File file)
            throws DicomServiceException {
        AttributesFormat filePathFormat = storeSCP.getStorageFilePathFormat();
        return (filePathFormat != null) ? super.parse(as, file) : null;
    }

    @Override
    protected File rename(Association as, File file, Attributes attrs)
            throws DicomServiceException {
        File dst;
        File storeDir = storeSCP.getStorageDirectory();
        AttributesFormat filePathFormat = storeSCP.getStorageFilePathFormat();
        if (filePathFormat == null) {
            String fname = file.getName();
            dst = new File(storeDir, fname.substring(0, fname.lastIndexOf('.')));
        } else {
            dst = new File(storeDir, filePathFormat.format(attrs));
        }
        File dir = dst.getParentFile();
        dir.mkdirs();
        dst.delete();
        if (file.renameTo(dst))
            LOG.info("{}: M-RENAME {} to {}", new Object[] {as, file, dst});
        else {
            LOG.warn("{}: Failed to M-RENAME {} to {}", new Object[] {as, file, dst});
            throw new DicomServiceException(Status.OutOfResources, "Failed to rename file");
        }
        return dst;
    }

}