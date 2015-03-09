package org.dcm4che.test.utils;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.BeanVitalizer;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.core.util.ConfigNodeUtil;
import org.dcm4che3.conf.dicom.CommonDicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.conf.dicom.DicomPath;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchiveHL7ApplicationExtension;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Roman K
 */
public class RemoteDicomConfigFactory {


    public static DicomConfiguration createRemoteDicomConfiguration(String remoteEndpointURL) {

        RemoteConfiguration remoteConfiguration = new RemoteConfiguration(remoteEndpointURL);
        try {
            DicomConfigurationBuilder builder = new DicomConfigurationBuilder().
                    registerCustomConfigurationStorage(remoteConfiguration).
                    persistDefaults(true);

            builder.registerDeviceExtension(HL7DeviceExtension.class);
            builder.registerDeviceExtension(AuditLogger.class);
            builder.registerDeviceExtension(AuditRecordRepository.class);
            builder.registerDeviceExtension(ImageReaderExtension.class);
            builder.registerDeviceExtension(ImageWriterExtension.class);

            builder.registerDeviceExtension(ArchiveDeviceExtension.class);
            builder.registerDeviceExtension(StorageDeviceExtension.class);
            builder.registerAEExtension(ArchiveAEExtension.class);
            builder.registerHL7ApplicationExtension(ArchiveHL7ApplicationExtension.class);

            return builder.build();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Can't initialize remote configuration", e);
        }
    }

    private static class RemoteConfiguration implements Configuration {

        @Path("/config")
        private static interface RESTDicomConfigAPI {

            @GET
            @Path("/device/{deviceName}")
            @Produces(MediaType.APPLICATION_JSON)
            public Map<String, Object> getDeviceConfig(@PathParam(value = "deviceName") String deviceName) throws ConfigurationException;


            @POST
            @Path("/device/{deviceName}")
            @Produces(MediaType.APPLICATION_JSON)
            @Consumes(MediaType.APPLICATION_JSON)
            public Response modifyDeviceConfig(@Context UriInfo ctx, @PathParam(value = "deviceName") String deviceName, Map<String, Object> config) throws ConfigurationException;
        }

        /**
         * jax rs client
         */
        RESTDicomConfigAPI remoteEndpoint;

        public RemoteConfiguration(String remoteEndpointURL) {

            // create jax-rs client
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(remoteEndpointURL);
            ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
            remoteEndpoint = rtarget.proxy(RESTDicomConfigAPI.class);
        }


        @Override
        public Map<String, Object> getConfigurationRoot() throws ConfigurationException {
            return null;
        }

        @Override
        public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {

            // if connection
            try {
                String deviceName = DicomPath.ConnectionByCnRef.parse(path).getParam("deviceName");
                Map<String, Object> deviceConfig = remoteEndpoint.getDeviceConfig(deviceName);

                // make dummy config tree with this one device
                Map<String, Object> dummyRoot = new BeanVitalizer().createConfigNodeFromInstance(new CommonDicomConfiguration.DicomConfigurationRootNode());
                ConfigNodeUtil.replaceNode(dummyRoot, DicomPath.DeviceByName.set("deviceName", deviceName).path(), deviceConfig);

                // get connection from dummy
                return ConfigNodeUtil.getNode(dummyRoot, path);

            } catch (IllegalArgumentException e) {
                //noop
            }


            try {

                return remoteEndpoint.getDeviceConfig(DicomPath.DeviceByName.parse(path).getParam("deviceName"));

            } catch (IllegalArgumentException e) {
                throw new ConfigurationException("This action is not supported when using the remote config", e);
            }
        }

        @Override
        public Class getConfigurationNodeClass(String path) throws ConfigurationException, ClassNotFoundException {
            return null;
        }

        @Override
        public boolean nodeExists(String path) throws ConfigurationException {
            if (path.equals(DicomPath.ConfigRoot.path())) return true;

            return remoteEndpoint.getDeviceConfig(DicomPath.DeviceByName.parse(path).getParam("deviceName")) != null;
        }

        @Override
        public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
            try {

                remoteEndpoint.modifyDeviceConfig(null, DicomPath.DeviceByName.parse(path).getParam("deviceName"), configNode);

            } catch (IllegalArgumentException e) {
                throw new ConfigurationException("This action is not supported when using the remote config", e);
            }
        }

        @Override
        public void refreshNode(String path) throws ConfigurationException {

        }

        @Override
        public void removeNode(String path) throws ConfigurationException {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
            throw new RuntimeException("Not supported");
        }
    }


}
