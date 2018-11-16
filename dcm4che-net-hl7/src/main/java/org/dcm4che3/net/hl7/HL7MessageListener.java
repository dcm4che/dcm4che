package org.dcm4che3.net.hl7;

import org.dcm4che3.hl7.HL7Exception;
import org.dcm4che3.net.Connection;

import java.net.Socket;

public interface HL7MessageListener {

    UnparsedHL7Message onMessage(HL7Application hl7App, Connection conn, Socket s, UnparsedHL7Message msg)
            throws HL7Exception;

}