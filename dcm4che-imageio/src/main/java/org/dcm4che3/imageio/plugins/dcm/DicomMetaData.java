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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.imageio.plugins.dcm;

import java.util.HashSet;
import java.util.Set;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.w3c.dom.Node;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomMetaData extends IIOMetadata {
    private static final Set<String> VIDEO_TSUID = new HashSet<>();
    static {
        VIDEO_TSUID.add(UID.MPEG2);
        VIDEO_TSUID.add(UID.MPEG2MainProfileHighLevel);
        VIDEO_TSUID.add(UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
        VIDEO_TSUID.add(UID.MPEG4AVCH264HighProfileLevel41);
        VIDEO_TSUID.add(UID.MPEG4AVCH264HighProfileLevel42For2DVideo);
        VIDEO_TSUID.add(UID.MPEG4AVCH264HighProfileLevel42For3DVideo);
        VIDEO_TSUID.add(UID.MPEG4AVCH264StereoHighProfileLevel42);
        VIDEO_TSUID.add(UID.HEVCH265Main10ProfileLevel51);
        VIDEO_TSUID.add(UID.HEVCH265MainProfileLevel51);
    }

    private final Attributes fileMetaInformation;
    private final Attributes attributes;

    public DicomMetaData(Attributes fileMetaInformation, Attributes attributes) {
        this.fileMetaInformation = fileMetaInformation;
        this.attributes = attributes;
    }

    public final Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    public final Attributes getAttributes() {
        return attributes;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    public String getTransferSyntaxUID() {
    	return getFileMetaInformation().getString(Tag.TransferSyntaxUID);
    }

    public boolean bigEndian() {
        return getAttributes().bigEndian();
    }

    public boolean isVideo() {
        return isVideo(getTransferSyntaxUID());
    }
	
    public static boolean isVideo(String tsuid) {
        return VIDEO_TSUID.contains(tsuid);
    }

}
