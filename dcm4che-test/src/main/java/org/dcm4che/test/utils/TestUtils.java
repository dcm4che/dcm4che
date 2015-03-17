package org.dcm4che.test.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.dcm4che.test.annotations.RemoteConnectionParameters;
import org.dcm4che.test.common.BasicTest;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static List<Path> backedUpDevices = new ArrayList<Path>();

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);
    
    public static void backUpRemoteConfig( String devName, DicomConfiguration remoteConfig) throws ConfigurationException, IOException {
        Device devBackUP = remoteConfig.findDevice(devName);
        backupDevice(devBackUP);
    }

    public static void rollBackRemoteConfig(DicomConfiguration remoteConfig) throws ConfigurationException, ClassNotFoundException, IOException {
        
        for(Path devPath : backedUpDevices) {
            Device dev = readDevice(devPath);
            remoteConfig.merge(dev);
            Files.delete(devPath);
        }
        remoteConfig.sync();
    }

    public static void addDBCustomAttribute(String archiveDeviceName, Entity entity, DicomConfiguration remoteConfig
            , int tagToAdd, VR vr, int SequenceTag) throws ConfigurationException {

        Device dev = remoteConfig.findDevice(archiveDeviceName);
        ArchiveDeviceExtension arcDevExt = dev
                .getDeviceExtension(ArchiveDeviceExtension.class);
        AttributeFilter filter = arcDevExt.getAttributeFilter(Entity.Instance);
        if(SequenceTag>0) 
            filter.setCustomAttribute1(new ValueSelector(null, tagToAdd, vr, 0, new ItemPointer(SequenceTag)));
        else
            filter.setCustomAttribute1(new ValueSelector(null, tagToAdd, vr, 0));
        
        remoteConfig.merge(dev);
    }

    public static String reloadServerConfig(BasicTest test) throws MalformedURLException, IOException {
        Properties defaultParams = test.getDefaultProperties();
        //get remote connection parameters
        RemoteConnectionParameters remoteParams = 
                (RemoteConnectionParameters) test.getParams().get("RemoteConnectionParameters");
        String baseURL =  remoteParams==null?
                defaultParams.getProperty("remoteConn.url")
                :remoteParams.baseURL();
        String webContext = remoteParams==null?
                defaultParams.getProperty("remoteConn.webcontext")
                :remoteParams.webContext();
        baseURL+=webContext.endsWith("/")?webContext:"/"+webContext;
        HttpURLConnection connection = (HttpURLConnection) 
                new URL(baseURL + (baseURL.endsWith("/")?"ctrl/reload":"/ctrl/reload"))
                .openConnection();
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("charset", "utf-8");
        String responseCode = connection.getResponseCode()+"";
        connection.disconnect();
        return responseCode;
    }

    public static void backupDevice(Device d) throws IOException {
        Path file = Files.createTempFile("temp"+d.getDeviceName()+"backup", ".dev");
        FileOutputStream fileOut = new FileOutputStream(file.toFile());
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        try{
        out.writeObject(d);
        out.flush();
        }
        finally{
            out.close();
        }
        backedUpDevices.add(file);
    }

    public static Device readDevice(Path devPath) throws ClassNotFoundException, IOException {
        FileInputStream fileIn = new FileInputStream(devPath.toFile());
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Device dev;
        try{
            dev = (Device) in.readObject();
        }
        finally {
            in.close();
        }
        return dev;
    }

    public static List<Path> getBackedUpDevices() {
        return backedUpDevices;
    }

    public static void adjustRemoteConfigurationForDestinationSCP(String scpToAdjust, BasicTest test, String connectionToKeep) {
        DicomConfiguration remote = test.getRemoteConfig();
        Device scpDevice = null;
        try {
            scpDevice = remote.findDevice(scpToAdjust);
            if(scpDevice == null)
                throw new ConfigurationException();
        } catch (ConfigurationException e) {
            LOG.error("Unable to load scp device from remote configuration", e);
        }
        
        
        for (Iterator<Connection> iter = scpDevice.getConnections().iterator();iter.hasNext();) {
            Connection conn = iter.next();
            if(!conn.getCommonName().equalsIgnoreCase(connectionToKeep)) {
                for (ApplicationEntity ae : scpDevice.getApplicationEntities()) {
                    ae.removeConnection(conn);
                }
                iter.remove();
            }
        }

        try {
            remote.merge(scpDevice);
        } catch (ConfigurationException e) {
            LOG.error("Error merging device in the remote configuration", e);
        }
    }

}
