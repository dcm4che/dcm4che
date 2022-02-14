/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.net;

import java.io.IOException;

import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public enum State {
    Sta1("Sta1 - Idle") {

        @Override
        void write(Association as, AAbort aa) {
            // NO OP
        }

        @Override
        void closeSocket(Association as) {
            // NO OP
        }

        @Override
        void closeSocketDelayed(Association as) {
            // NO OP
        }
    },
    Sta2("Sta2 - Transport connection open") {

        @Override
        void onAAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.handle(rq);
        }

        @Override
        void write(Association as, AAbort aa) {
            as.doCloseSocket();
        }
    },
    Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
    Sta4("Sta4 - Awaiting transport connection opening to complete"),
    Sta5("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU") {

        @Override
        void onAAssociateAC(Association as, AAssociateAC ac)
                throws IOException {
            as.handle(ac);
        }

        @Override
        void onAAssociateRJ(Association as, AAssociateRJ rj)
                throws IOException {
            as.handle(rj);
        }
    },
    Sta6("Sta6 - Association established and ready for data transfer") {

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQ();
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }

        @Override
        void writeAReleaseRQ(Association as) throws IOException {
            as.writeAReleaseRQ();
        }

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    },
    Sta7("Sta7 - Awaiting A-RELEASE-RP PDU") {

        @Override
        public void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQCollision();
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }
    },
    Sta8("Sta8 - Awaiting local A-RELEASE response primitive") {

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    },
    Sta9("Sta9 - Release collision requestor side; awaiting A-RELEASE response"),
    Sta10("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU"){

        @Override
        void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRPCollision();
        }
    },
    Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU"){

        @Override
        void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }
    },
    Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
    Sta13("Sta13 - Awaiting Transport Connection Close Indication") {

        @Override
        public void onAReleaseRP(Association as) throws IOException {
            // NO OP
        }

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            // NO OP
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            // NO OP
        }

        @Override
        void write(Association as, AAbort aa) {
            // NO OP
        }

        @Override
        void closeSocketDelayed(Association as) {
            // NO OP
        }
    };

    private String name;

    State(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    void onAAssociateRQ(Association as, AAssociateRQ rq)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    void onAAssociateAC(Association as, AAssociateAC ac)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    void onAAssociateRJ(Association as, AAssociateRJ rj)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    void onPDataTF(Association as) throws IOException {
        as.unexpectedPDU("P-DATA-TF");
    }

    void onAReleaseRQ(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    void onAReleaseRP(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    void writeAReleaseRQ(Association as) throws IOException {
        throw new AssociationStateException(this);
    }

    void write(Association as, AAbort aa) {
        as.write(aa);
    }

    public void writePDataTF(Association as) throws IOException {
        throw new AssociationStateException(this);
    }

    void closeSocket(Association as) {
        as.doCloseSocket();
    }

    void closeSocketDelayed(Association as) {
        as.doCloseSocketDelayed();
    }
}
