package org.dcm4che.sample.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.conf.ldap.LdapDicomConfiguration;
import org.dcm4che.net.DeviceService;
import org.dcm4che.net.service.DicomServiceRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class EchoDevice extends DeviceService implements DeviceServiceInterface {

	private final static String DICOM_DEVICE_NAME="echoscp";
	
    private DicomConfiguration dicomConfig;
    
	public BundleContext bcontext;
    
	public void setBcontext(BundleContext bcontext) {
		this.bcontext = bcontext;
	}

	/**
	 * start of the service
	 * 
	 * @see org.dcm4che.net.DeviceService#start()
	 */
	public void start()
	{
		System.out.println("starting Echo Dicom Device....");
		
		try {
			//loading ldap.properties as bundle resource, as class.getResource doesn't work from 
			//jboss modules to bundles
			URL ldapURL = this.bcontext.getBundle().getEntry("ldap.properties");
			Properties props = new Properties();
			props.load(ldapURL.openStream());
			
			dicomConfig = (DicomConfiguration) new LdapDicomConfiguration(props);

			System.out.println("init device:"+DICOM_DEVICE_NAME);
			init(dicomConfig.findDevice(DICOM_DEVICE_NAME));
			
			DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
			this.device.setDimseRQHandler(serviceRegistry);
			
			System.out.println("this.device:"+this.device);
			
			super.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop()
	{
		super.stop();
	}
}
