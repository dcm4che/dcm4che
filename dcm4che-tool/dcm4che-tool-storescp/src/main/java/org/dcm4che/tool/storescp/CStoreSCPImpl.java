package org.dcm4che.tool.storescp;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.Association;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCStoreSCP;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.util.AttributesFormat;
import org.dcm4che.util.SafeClose;

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
    protected File createFile(Association as, Attributes rq, Object storage)
            throws DicomServiceException {
        return new File(storeSCP.getStorageDirectory(),
                rq.getString(Tag.AffectedSOPInstanceUID) + StoreSCP.PART_EXT);
    }

    @Override
    protected File process(Association as, PresentationContext pc, Attributes rq,
            Attributes rsp, Object storage, File file, MessageDigest digest)
            throws DicomServiceException {
        File dst;
        AttributesFormat filePathFormat = storeSCP.getStorageFilePathFormat();
        File storeDir = storeSCP.getStorageDirectory();
        if (filePathFormat == null) {
            String fname = file.getName();
            dst = new File(storeDir, fname.substring(0, fname.lastIndexOf('.')));
        } else {
            Attributes ds;
            DicomInputStream in = null;
            try {
                in = new DicomInputStream(file);
                in.setIncludeBulkData(false);
                ds = in.readDataset(-1, Tag.PixelData);
            } catch (IOException e) {
                LOG.warn(as + ": Failed to decode dataset:", e);
                throw new DicomServiceException(Status.CannotUnderstand);
            } finally {
                SafeClose.close(in);
            }
            dst = new File(storeDir, filePathFormat.format(ds));
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
        return null;
    }
}