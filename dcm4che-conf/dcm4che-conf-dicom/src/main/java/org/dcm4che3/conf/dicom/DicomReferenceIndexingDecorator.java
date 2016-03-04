package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.util.PathPattern;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Extension to shortcut DicomPath lookups through reference index
 *
 * @author rawmahn
 */
public class DicomReferenceIndexingDecorator extends ReferenceIndexingDecorator {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DicomReferenceIndexingDecorator.class);


    public DicomReferenceIndexingDecorator(Configuration delegate, Map<String, Path> uuidToSimplePathCache) {
        super(delegate, uuidToSimplePathCache);
    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        PathPattern.PathParser pp;

        pp = DicomPath.DeviceUUIDByAnyUUID.parseIfMatches(liteXPathExpression);
        if (pp != null) {
            return handleRefShortCut(pp, DicomPath.DeviceUUIDByAnyUUID);
        }

        pp = DicomPath.DeviceNameByUUID.parseIfMatches(liteXPathExpression);
        if (pp != null) {
            return handleRefShortCut(pp, DicomPath.DeviceNameByUUID);
        }

        pp = DicomPath.DeviceNameByAEUUID.parseIfMatches(liteXPathExpression);
        if (pp != null) {
            return handleRefShortCut(pp, DicomPath.DeviceNameByAEUUID);
        }

        return super.search(liteXPathExpression);
    }

    private Iterator handleRefShortCut(PathPattern.PathParser pp, DicomPath pathType) {

        String param;
        int validLen = -1;
        String suffix;
        switch (pathType) {
            case DeviceNameByUUID:
                param = "deviceUUID";
                validLen = 3;
                suffix = "/dicomDeviceName";
                break;
            case DeviceUUIDByAnyUUID:
                param = "UUID";
                suffix = "/_.uuid";
                break;
            case DeviceNameByAEUUID:
                param = "UUID";
                suffix = "/dicomDeviceName";
                break;
            default:
                throw new IllegalArgumentException();

        }

        String uuid = pp.getParam(param);

        Path path = getPathByUUIDFromIndex(uuid);


        if (path == null) {

            // double check
            Object nodeByUUID = getNodeByUUID(null, uuid);

            if (nodeByUUID!=null) {
                // reference index out of sync
                //TODO:!!!
            }


            return Collections.emptyList().iterator();
        }

        if (!validateDevicePath(path, validLen)) {
            log.error("Unexpected path to device:" + path);
            return Collections.emptyList().iterator();
        }

        return Collections.singletonList(getConfigurationNode(path.toSimpleEscapedXPath() + suffix, null)).iterator();
    }

    private boolean validateDevicePath(Path path, int len) {

        boolean lengthValid = len == -1 || path.getPathItems().size() == len;

        return "dicomConfigurationRoot".equals(path.getPathItems().get(0))
                && "dicomDevicesRoot".equals(path.getPathItems().get(1))
                && (path.getPathItems().get(2) instanceof String)
                && lengthValid;
    }
}
