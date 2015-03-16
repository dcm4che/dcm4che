package org.dcm4che.test.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;

public class TestUtils {

    public static void addDBCustomAttribute(Entity entity, DicomConfiguration remoteConfig
            , int tagToAdd, VR vr) throws ConfigurationException {
        Device dev = remoteConfig.findDevice("dcm4chee-arc");
        ArchiveDeviceExtension arcDevExt = dev
                .getDeviceExtension(ArchiveDeviceExtension.class);
        AttributeFilter filter = arcDevExt.getAttributeFilter(Entity.Instance);
        filter.setCustomAttribute1(new ValueSelector(null, tagToAdd, vr, 0, new ItemPointer(Tag.ConceptNameCodeSequence)));
        remoteConfig.merge(dev);
    }
    public static String reloadServerConfig(Properties defaultConfig, String url) throws MalformedURLException, IOException {
        String baseURL = defaultConfig!=null && defaultConfig.getProperty("remoteConn.url") != null
                ?defaultConfig.getProperty("remoteConn.url"):url;
        HttpURLConnection connection = (HttpURLConnection) 
                new URL(baseURL + (baseURL.endsWith("/")?"dcm4chee-arc/ctrl/reload":"/dcm4chee-arc/ctrl/reload"))
                .openConnection();
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("charset", "utf-8");
        String responseCode = connection.getResponseCode()+"";
        connection.disconnect();
        return responseCode;
    }
}
