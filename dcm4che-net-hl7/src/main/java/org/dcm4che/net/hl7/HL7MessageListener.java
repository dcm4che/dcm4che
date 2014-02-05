package org.dcm4che.net.hl7;

import java.net.Socket;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Segment;
import org.dcm4che.net.Connection;

public interface HL7MessageListener {

    byte[] onMessage(HL7Application hl7App, Connection conn,
            Socket s, HL7Segment msh, byte[] msg, int off, int len, int mshlen)
            throws HL7Exception;

}