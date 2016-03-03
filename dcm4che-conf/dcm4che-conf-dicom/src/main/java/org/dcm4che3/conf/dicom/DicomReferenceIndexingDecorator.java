package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.util.PathPattern;
import org.dcm4che3.net.Device;
import org.slf4j.Logger;

import java.util.*;

/**
 *
 *
 * @author rawmahn
 */
public class DicomReferenceIndexingDecorator extends ReferenceIndexingDecorator {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DicomReferenceIndexingDecorator.class);


    public DicomReferenceIndexingDecorator(Configuration delegate, HashMap<String, Path> uuidToSimplePathCache) {
        super(delegate, uuidToSimplePathCache);
    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {

        PathPattern.PathParser pp = DicomPath.DeviceUUIDByAnyUUID.parseIfMatches(liteXPathExpression);
        if (pp != null) {
            return handleDeviceUUIDByAnyUUID(pp);
        }

        pp = DicomPath.DeviceNameByUUID.parseIfMatches(liteXPathExpression);

        if (pp != null) {
            return handleDeviceNameByUUID(pp);
        }

        pp = DicomPath.DeviceNameByAEUUID.parseIfMatches(liteXPathExpression);

        if (pp != null) {
            return handleDeviceNameByAEUUID(pp);
        }

        return super.search(liteXPathExpression);
    }

    private Iterator handleDeviceNameByAEUUID(PathPattern.PathParser pp) {

        String uuid = pp.getParam("aeUUID");

        Path path = uuidToReferableIndex.get(uuid);

        if (path == null)
            return Collections.emptyList().iterator();

        if (!validateDevicePath(path)) {
            log.error("Unexpected path to device:" + path);
            return Collections.emptyList().iterator();
        }

        return Collections.singletonList(getConfigurationNode(path.subPath(0, 3).toSimpleEscapedXPath() + "/dicomDeviceName", null)).iterator();
    }

    private Iterator handleDeviceNameByUUID(PathPattern.PathParser pp) {
        String uuid = pp.getParam("deviceUUID");

        Path path = uuidToReferableIndex.get(uuid);

        if (path == null)
            return Collections.emptyList().iterator();

        if (!validateDevicePath(path) || path.getPathItems().size() != 3) {
            log.error("Unexpected path to device:" + path);
            return Collections.emptyList().iterator();
        }

        return Collections.singletonList(getConfigurationNode(path.toSimpleEscapedXPath() + "/dicomDeviceName", null)).iterator();
    }

    private Iterator handleDeviceUUIDByAnyUUID(PathPattern.PathParser devUUIDByAny) {
        String uuid = devUUIDByAny.getParam("UUID");

        Path path = uuidToReferableIndex.get(uuid);

        if (path == null)
            return Collections.emptyList().iterator();

        if (!validateDevicePath(path)) {
            log.error("Unexpected path to device:" + path);
            return Collections.emptyList().iterator();
        }

        // we need to get to the parent device path
        // so just take lvl 3

        // return device uuid
        return Collections.singletonList(getConfigurationNode(path.subPath(0, 3).toSimpleEscapedXPath() + "/_.uuid", null)).iterator();
    }

    private boolean validateDevicePath(Path path) {
        return "dicomConfigurationRoot".equals(path.getPathItems().get(0))
                && "dicomDevicesRoot".equals(path.getPathItems().get(1))
                && (path.getPathItems().get(2) instanceof String);
    }
}
