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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.tool.common;

import java.io.File;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class DicomFiles {

    private static SAXParser saxParser;

    public interface Callback {
        boolean dicomFile(File f, Attributes fmi, long dsPos, Attributes ds)
                throws Exception;
    }

    public static void scan(List<String> fnames, Callback scb) {
        for (String fname : fnames)
            scan(new File(fname), scb);
    }

    private static void scan(File f, Callback scb) {
        if (f.isDirectory()) {
            for (String s : f.list())
                scan(new File(f, s), scb);
            return;
        }
        if (f.getName().endsWith(".xml")) {
            try {
                SAXParser p = saxParser;
                if (p == null)
                    saxParser = p = SAXParserFactory.newInstance().newSAXParser();
                Attributes ds = new Attributes();
                ContentHandlerAdapter ch = new ContentHandlerAdapter(ds);
                p.parse(f, ch);
                Attributes fmi = ch.getFileMetaInformation();
                if (fmi == null)
                    fmi = ds.createFileMetaInformation(UID.ExplicitVRLittleEndian);
                boolean b = scb.dicomFile(f, fmi, -1, ds);
                System.out.print(b ? '.' : 'I');
            } catch (Exception e) {
                System.out.println();
                System.out.println("Failed to parse file " + f + ": " + e.getMessage());
                e.printStackTrace(System.out);
            }
        } else {
            DicomInputStream in = null;
            try {
                in = new DicomInputStream(f);
                in.setIncludeBulkData(IncludeBulkData.NO);
                Attributes fmi = in.readFileMetaInformation();
                long dsPos = in.getPosition();
                Attributes ds = in.readDataset(-1, Tag.PixelData);
                if (fmi == null || !fmi.containsValue(Tag.TransactionUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPClassUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPInstanceUID))
                    fmi = ds.createFileMetaInformation(in.getTransferSyntax());
                boolean b = scb.dicomFile(f, fmi, dsPos, ds);
                System.out.print(b ? '.' : 'I');
            } catch (Exception e) {
                System.out.println();
                System.out.println("Failed to scan file " + f + ": " + e.getMessage());
                e.printStackTrace(System.out);
            } finally {
                SafeClose.close(in);
            }
        }
    }
}
