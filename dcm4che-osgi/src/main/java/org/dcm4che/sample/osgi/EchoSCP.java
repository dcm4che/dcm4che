package org.dcm4che.sample.osgi;

import java.net.URL;
import java.util.Properties;

import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.conf.ldap.LdapDicomConfiguration;
import org.dcm4che.net.DeviceService;
import org.dcm4che.net.service.BasicCEchoSCP;
import org.dcm4che.net.service.DicomServiceRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class EchoSCP extends DeviceService implements BundleActivator {

	private final static String DICOM_DEVICE_NAME="echoscp";
	
    private DicomConfiguration dicomConfig;
	
	public void start(BundleContext context) throws Exception 
	{		
		//loading ldap.properties as bundle resource, as class.getResource doesn't work from 
		//jboss modules to bundles
		URL ldapURL = context.getBundle().getEntry("ldap.properties");
		Properties props = new Properties();
		props.load(ldapURL.openStream());
		
		dicomConfig = (DicomConfiguration) new LdapDicomConfiguration(props);

		System.out.println("init device:"+DICOM_DEVICE_NAME);
		init(dicomConfig.findDevice(DICOM_DEVICE_NAME));
		
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        device.setDimseRQHandler(serviceRegistry);
		
        super.start();
	}

	public void stop(BundleContext context) throws Exception 
	{
		super.stop();
	}
}
