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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4che3.ws.rs;

import javax.ws.rs.core.MediaType;

import org.dcm4che3.data.UID;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MediaTypes {

    /**
     * "application/dicom"
     */
    public final static String APPLICATION_DICOM = "application/dicom";

    /**
     * "application/dicom"
     */
    public final static MediaType APPLICATION_DICOM_TYPE =
            new MediaType("application", "dicom");

    /**
     * "application/dicom+xml"
     */
    public final static String APPLICATION_DICOM_XML = "application/dicom+xml";

    /**
     * "application/dicom+xml"
     */
    public final static MediaType APPLICATION_DICOM_XML_TYPE =
            new MediaType("application", "dicom+xml");

    /**
     * "application/dicom+json"
     */
    public final static String APPLICATION_DICOM_JSON = "application/dicom+json";

    /**
     * "application/dicom+json"
     */
    public final static MediaType APPLICATION_DICOM_JSON_TYPE =
            new MediaType("application", "dicom+json");

    /**
     * "image/gif"
     */
    public final static String IMAGE_GIF = "image/gif";

    /**
     * "image/gif"
     */
    public final static MediaType IMAGE_GIF_TYPE =
            new MediaType("image", "gif");


    /**
     * "image/png"
     */
    public final static String IMAGE_PNG = "image/png";

    /**
     * "image/png"
     */
    public final static MediaType IMAGE_PNG_TYPE =
            new MediaType("image", "png");

    /**
     * "image/jpeg"
     */
    public final static String IMAGE_JPEG = "image/jpeg";

    /**
     * "image/jpeg"
     */
    public final static MediaType IMAGE_JPEG_TYPE =
            new MediaType("image", "jpeg");

    /**
     * "image/x-jls"
     */
    public final static String IMAGE_X_JLS = "image/x-jls";

    /**
     * "image/x-jls"
     */
    public final static MediaType IMAGE_X_JLS_TYPE =
            new MediaType("image", "x-jls");

    /**
     * "image/jp2"
     */
    public final static String IMAGE_JP2 = "image/jp2";

    /**
     * "image/jp2"
     */
    public final static MediaType IMAGE_JP2_TYPE =
            new MediaType("image", "jp2");

    /**
     * "image/jpx"
     */
    public final static String IMAGE_JPX = "image/jpx";

    /**
     * "image/dicom+jpeg-jpx"
     */
    public final static MediaType IMAGE_JPX_TYPE =
            new MediaType("image", "jpx");

    /**
     * "image/dicom+rle"
     */
    public final static String IMAGE_X_DICOM_RLE = "image/x-dicom+rle";

    /**
     * "image/dicom+rle"
     */
    public final static MediaType IMAGE_X_DICOM_RLE_TYPE =
            new MediaType("image", "x-dicom+rle");

    /**
     * "video/mpeg"
     */
    public final static String VIDEO_MPEG = "video/mpeg";

    /**
     * "video/mpeg"
     */
    public final static MediaType VIDEO_MPEG_TYPE =
            new MediaType("video", "mpeg");

    /**
     * "video/mp4"
     */
    public final static String VIDEO_MP4 = "video/mp4";

    /**
     * "video/mp4"
     */
    public final static MediaType VIDEO_MP4_TYPE =
            new MediaType("video", "mp4");

    /**
     * "application/pdf"
     */
    public final static String APPLICATION_PDF = "application/pdf";

    /**
     * "application/pdf"
     */
    public final static MediaType APPLICATION_PDF_TYPE =
            new MediaType("application", "pdf");

    /**
     * "text/rtf"
     */
    public final static String TEXT_RTF = "text/rtf";

    /**
     * "text/rtf"
     */
    public final static MediaType TEXT_RTF_TYPE =
            new MediaType("text", "rtf");

    /**
     * "text/csv"
     */
    public final static String TEXT_CSV = "text/csv";

    /**
     * "text/csv"
     */
    public final static MediaType TEXT_CSV_TYPE =
            new MediaType("text", "csv");

    /**
     * "text/csv;charset=utf-8"
     */
    public final static String TEXT_CSV_UTF8 = "text/csv;charset=utf-8";

    /**
     * "text/csv;charset=utf-8"
     */
    public final static MediaType TEXT_CSV_UTF8_TYPE =
            new MediaType("text", "csv", "utf-8");

    /**
     * "application/zip"
     */
    public final static String APPLICATION_ZIP = "application/zip";

    /**
     * "application/zip"
     */
    public final static MediaType APPLICATION_ZIP_TYPE =
            new MediaType("application", "zip");

    /**
     * "multipart/related"
     */
    public final static String MULTIPART_RELATED = "multipart/related";

    /**
     * "multipart/related"
     */
    public final static MediaType MULTIPART_RELATED_TYPE =
            new MediaType("multipart", "related");

    public static MediaType forTransferSyntax(String ts) {
        MediaType type;
        switch (ts) {
            case UID.ExplicitVRLittleEndian:
            case UID.ImplicitVRLittleEndian:
                return MediaType.APPLICATION_OCTET_STREAM_TYPE;
            case UID.JPEGLossless:
                return IMAGE_JPEG_TYPE;
            case UID.JPEGLSLossless:
                return IMAGE_X_JLS_TYPE;
            case UID.JPEG2000LosslessOnly:
                return IMAGE_JP2_TYPE;
            case UID.JPEG2000Part2MultiComponentLosslessOnly:
                return IMAGE_JPX_TYPE;
            case UID.RLELossless:
                return IMAGE_X_DICOM_RLE_TYPE;
            case UID.JPEGBaseline1:
            case UID.JPEGExtended24:
            case UID.JPEGLosslessNonHierarchical14:
                type = IMAGE_JPEG_TYPE;
                break;
            case UID.JPEGLSLossyNearLossless:
                type = IMAGE_X_JLS_TYPE;
                break;
            case UID.JPEG2000:
                type = IMAGE_JP2_TYPE;
                break;
            case UID.JPEG2000Part2MultiComponent:
                type = IMAGE_JPX_TYPE;
                break;
            case UID.MPEG2:
            case UID.MPEG2MainProfileHighLevel:
                type = VIDEO_MPEG_TYPE;
                break;
            case UID.MPEG4AVCH264HighProfileLevel41:
            case UID.MPEG4AVCH264BDCompatibleHighProfileLevel41:
                type = VIDEO_MP4_TYPE;
                break;
            default:
                throw new IllegalArgumentException("ts: " + ts);
        }
        return new MediaType(type.getType(), type.getSubtype(), Collections.singletonMap("transfer-syntax", ts));
    }

    public static String transferSyntaxOf(MediaType bulkdataMediaType) {
        String tsuid = bulkdataMediaType.getParameters().get("transfer-syntax");
        if (tsuid != null)
            return tsuid;

        String type = bulkdataMediaType.getType().toLowerCase();
        String subtype = bulkdataMediaType.getSubtype().toLowerCase();
        if (type.equals("image")) {
            if (subtype.equals("jpeg"))
                return UID.JPEGLossless;
            else if (subtype.equals("x-jls"))
                return UID.JPEGLSLossless;
            else if (subtype.equals("jp2"))
                return UID.JPEG2000LosslessOnly;
            else if (subtype.equals("jpx"))
                return UID.JPEG2000Part2MultiComponentLosslessOnly;
            else if (subtype.equals("x-dicom+rle"))
                return UID.RLELossless;
        } else if (type.equals("video")) {
            if (subtype.equals("mpeg"))
                return UID.MPEG2;
            else if (subtype.equals("mp4"))
                return UID.MPEG4AVCH264HighProfileLevel41;
        }
        return UID.ExplicitVRLittleEndian;
    }

    public static String sopClassOf(MediaType bulkdataMediaType) {
        String type = bulkdataMediaType.getType().toLowerCase();
        return type.equals("image") ? UID.SecondaryCaptureImageStorage
                : type.equals("video") ? UID.VideoPhotographicImageStorage
                : equalsIgnoreParameters(bulkdataMediaType, APPLICATION_PDF_TYPE) ? UID.EncapsulatedPDFStorage
                : null;
    }

    public static boolean equalsIgnoreParameters(MediaType type1, MediaType type2) {
        return type1.getType().equalsIgnoreCase(type2.getType())
                &&  type1.getSubtype().equalsIgnoreCase(type2.getSubtype());
    }


    public static MediaType getMultiPartRelatedType(MediaType type) {
        return equalsIgnoreParameters(MULTIPART_RELATED_TYPE, type)
                ? MediaType.valueOf(type.getParameters().get("type"))
                : null;
    }

    public static String getTransferSyntax(MediaType type) {
        return type != null && equalsIgnoreParameters(APPLICATION_DICOM_TYPE, type)
                ? type.getParameters().get("transfer-syntax")
                : null;
    }

}
