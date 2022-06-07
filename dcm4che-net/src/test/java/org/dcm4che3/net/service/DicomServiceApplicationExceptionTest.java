package org.dcm4che3.net.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DicomServiceApplicationExceptionTest {

	@Test
	public void constructorWithStatus_returnsStatus() {
		int status = 111;
		assertEquals(status, new DicomServiceApplicationException(status).getStatus());
	}

	@Test
	public void constructorWithStatusAndCause_returnsStatusAndCause() {
		int status = 111;
		Throwable cause = new Throwable();
		DicomServiceApplicationException exception = new DicomServiceApplicationException(status, cause);
		assertEquals(status, exception.getStatus());
		assertEquals(cause, exception.getCause());
	}

	@Test
	public void constructorWithStatusAndMessage_returnsStatusAndMessage() {
		int status = 111;
		String message = "This is a message";
		DicomServiceApplicationException exception = new DicomServiceApplicationException(status, message);
		assertEquals(status, exception.getStatus());
		assertEquals(message, exception.getMessage());
	}

	@Test
	public void constructorWithStatusAndMessageAndCause_returnsStatusAndMessageAndCause() {
		int status = 111;
		Throwable cause = new Throwable();
		String message = "This is a message";
		DicomServiceApplicationException exception = new DicomServiceApplicationException(status, message, cause);
		assertEquals(status, exception.getStatus());
		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}
}
