package org.dcm4che.sample.osgi;

import org.dcm4che.net.Device;

public interface DeviceServiceInterface {

	public abstract Device getDevice();

	public abstract boolean isRunning();

	public abstract void start() throws Exception;

	public abstract void stop();

}