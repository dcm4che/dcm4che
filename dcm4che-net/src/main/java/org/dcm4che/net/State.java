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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

package org.dcm4che.net;

import java.io.IOException;
import java.net.Socket;

import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.AssociationAC;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public enum State {
    Sta1("Sta1 - Idle") {

        @Override
        public AssociationAC connect(Association as, NetworkConnection local,
                NetworkConnection remote, AAssociateRQ rq)
                throws IOException, InterruptedException {
            return as.doConnect(local, remote, rq);
        }

        @Override
        public AssociationAC accept(Association as, NetworkConnection local,
                Socket sock) throws IOException, InterruptedException {
            return as.doAccept(local, sock);
        }
    },
    Sta2("Sta2 - Transport connection open") {

        @Override
        public void onAAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.handle(rq);
        }
    },
    Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
    Sta4("Sta4 - Awaiting transport connection opening to complete"),
    Sta5("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU") {

        @Override
        public void onAAssociateAC(Association as, AAssociateAC ac)
                throws IOException {
            as.handle(ac);
        }

        @Override
        public void onAAssociateRJ(Association as, AAssociateRJ rj)
                throws IOException {
            as.handle(rj);
        }
    },
    Sta6("Sta6 - Association established and ready for data transfer") {

        @Override
        public void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQ();
        }

        @Override
        public void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }
    },
    Sta7("Sta7 - Awaiting A-RELEASE-RP PDU") {

        @Override
        public void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }

        @Override
        public void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQCollision();
        }

        @Override
        public void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }
    },
    Sta8("Sta8 - Awaiting local A-RELEASE response primitive"),
    Sta9("Sta9 - Release collision requestor side; awaiting A-RELEASE response"),
    Sta10("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU"){

        @Override
        public void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRPCollision();
        }
    },
    Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU"){

        @Override
        public void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }
    },
    Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
    Sta13("Sta13 - Awaiting Transport Connection Close Indication");

    private String name;

    State(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    AssociationAC connect(Association as, NetworkConnection local,
            NetworkConnection remote, AAssociateRQ rq)
            throws IOException, InterruptedException {
        throw new IllegalStateException(name);
    }

    AssociationAC accept(Association as, NetworkConnection local,
            Socket sock) throws IOException, InterruptedException {
        throw new IllegalStateException(name);
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

}
