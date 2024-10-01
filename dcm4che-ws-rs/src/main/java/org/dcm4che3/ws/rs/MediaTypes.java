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

import jakarta.ws.rs.core.MediaType;
import org.dcm4che3.data.UID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
     * "image/*"
     */
    public final static String IMAGE_WILDCARD = "image/*";

    /**
     * "image/*"
     */
    public final static MediaType IMAGE_WILDCARD_TYPE =
            new MediaType("image", "*");

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
     * "image/jls"
     */
    public final static String IMAGE_JLS = "image/jls";

    /**
     * "image/jls"
     */
    public final static MediaType IMAGE_JLS_TYPE =
            new MediaType("image", "jls");

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
     * "image/j2c"
     */
    public final static String IMAGE_J2C = "image/j2c";

    /**
     * "image/j2c"
     */
    public final static MediaType IMAGE_J2C_TYPE =
            new MediaType("image", "j2c");

    /**
     * "image/jpx"
     */
    public final static String IMAGE_JPX = "image/jpx";

    /**
     * "image/jpx"
     */
    public final static MediaType IMAGE_JPX_TYPE =
            new MediaType("image", "jpx");

    /**
     * "image/jph"
     */
    public final static String IMAGE_JPH = "image/jph";

    /**
     * "image/jph"
     */
    public final static MediaType IMAGE_JPH_TYPE =
            new MediaType("image", "jph");

    /**
     * "image/jphc"
     */
    public final static String IMAGE_JPHC = "image/jphc";

    /**
     * "image/jphc"
     */
    public final static MediaType IMAGE_JPHC_TYPE =
            new MediaType("image", "jphc");

    /**
     * "image/dicom-rle"
     */
    public final static String IMAGE_DICOM_RLE = "image/dicom-rle";

    /**
     * "image/dicom-rle"
     */
    public final static MediaType IMAGE_DICOM_RLE_TYPE =
            new MediaType("image", "dicom-rle");

    /**
     * "video/*"
     */
    public final static String VIDEO_WILDCARD = "video/*";

    /**
     * "video/*"
     */
    public final static MediaType VIDEO_WILDCARD_TYPE =
            new MediaType("video", "*");

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
     * "video/quicktime"
     */
    public final static String VIDEO_QUICKTIME = "video/quicktime";

    /**
     * "video/quicktime"
     */
    public final static MediaType VIDEO_QUICKTIME_TYPE =
            new MediaType("video", "quicktime");

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

    /**
     * "multipart/related;type=\"application/dicom\""
     */
    public final static String MULTIPART_RELATED_APPLICATION_DICOM = "multipart/related;type=\"application/dicom\"";

    /**
     * "multipart/related;type=\"application/dicom\""
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_TYPE =
            new MediaType("multipart", "related", Collections.singletonMap("type", APPLICATION_DICOM));

    /**
     * "multipart/related;type=\"application/dicom+xml\""
     */
    public final static String MULTIPART_RELATED_APPLICATION_DICOM_XML = "multipart/related;type=\"application/dicom+xml\"";

    /**
     * "multipart/related;type=\"application/dicom+xml\""
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE =
            new MediaType("multipart", "related", Collections.singletonMap("type", APPLICATION_DICOM_XML));

    /**
     * "model/stl"
     */
    public final static String MODEL_STL = "model/stl";

    /**
     * "model/stl"
     */
    public final static MediaType MODEL_STL_TYPE =
            new MediaType("model", "stl");

    /**
     * "model/x.stl-binary"
     */
    public final static String MODEL_X_STL_BINARY = "model/x.stl-binary";

    /**
     * "model/x.stl-binary"
     */
    public final static MediaType MODEL_X_STL_BINARY_TYPE =
            new MediaType("model", "x.stl-binary");

    /**
     * "application/sla"
     */
    public final static String APPLICATION_SLA = "application/sla";

    /**
     * "application/sla"
     */
    public final static MediaType APPLICATION_SLA_TYPE =
            new MediaType("application", "sla");

    /**
     * "model/obj"
     */
    public final static String MODEL_OBJ = "model/obj";

    /**
     * "model/obj"
     */
    public final static MediaType MODEL_OBJ_TYPE =
            new MediaType("model", "obj");

    /**
     * "model/mtl"
     */
    public final static String MODEL_MTL = "model/mtl";

    /**
     * "model/mtl"
     */
    public final static MediaType MODEL_MTL_TYPE =
            new MediaType("model", "mtl");

    /**
     * "application/vnd.genozip"
     */
    public final static String APPLICATION_VND_GENOZIP = "application/vnd.genozip";

    /**
     * "application/vnd.genozip"
     */
    public final static MediaType APPLICATION_VND_GENOZIP_TYPE =
            new MediaType("application", "vnd.genozip");

    /**
     * "application/x-bzip2"
     */
    public final static String APPLICATION_X_BZIP2 = "application/x-bzip2";

    /**
     * "application/x-bzip2"
     */
    public final static MediaType APPLICATION_X_BZIP2_TYPE =
            new MediaType("application", "x-bzip2");

    /**
     * "application/prs.vcfbzip2"
     */
    public final static String APPLICATION_PRS_VCFBZIP2 = "application/prs.vcfbzip2";

    /**
     * "application/prs.vcfbzip"
     */
    public final static MediaType APPLICATION_PRS_VCFBZIP2_TYPE =
            new MediaType("application", "prs.vcfbzip2");


    public static MediaType forTransferSyntax(String ts) {
        MediaType type;
        switch (ts) {
            case UID.ExplicitVRLittleEndian:
            case UID.ImplicitVRLittleEndian:
                return MediaType.APPLICATION_OCTET_STREAM_TYPE;
            case UID.RLELossless:
                return IMAGE_DICOM_RLE_TYPE;
            case UID.JPEGBaseline8Bit:
            case UID.JPEGExtended12Bit:
            case UID.JPEGLossless:
            case UID.JPEGLosslessSV1:
                type = IMAGE_JPEG_TYPE;
                break;
            case UID.JPEGLSLossless:
            case UID.JPEGLSNearLossless:
                type = IMAGE_JLS_TYPE;
                break;
            case UID.JPEG2000Lossless:
            case UID.JPEG2000:
                type = IMAGE_JP2_TYPE;
                break;
            case UID.JPEG2000MCLossless:
            case UID.JPEG2000MC:
                type = IMAGE_JPX_TYPE;
                break;
            case UID.HTJ2KLossless:
            case UID.HTJ2KLosslessRPCL:
            case UID.HTJ2K:
                type = IMAGE_JPHC_TYPE;
                break;
            case UID.MPEG2MPML:
            case UID.MPEG2MPHL:
                type = VIDEO_MPEG_TYPE;
                break;
            case UID.MPEG4HP41:
            case UID.MPEG4HP41BD:
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
        switch (type) {
            case "image":
                switch (subtype) {
                    case "jpeg":
                        return UID.JPEGLosslessSV1;
                    case "jls":
                    case "x-jls":
                        return UID.JPEGLSLossless;
                    case "jp2":
                        return UID.JPEG2000Lossless;
                    case "jpx":
                        return UID.JPEG2000MCLossless;
                    case "x-dicom-rle":
                    case "dicom-rle":
                        return UID.RLELossless;
                }
            case "video":
                switch (subtype) {
                    case "mpeg":
                        return UID.MPEG2MPML;
                    case "mp4":
                    case "quicktime":
                        return UID.MPEG4HP41;
                }
        }
        return UID.ExplicitVRLittleEndian;
    }

    public static String sopClassOf(MediaType bulkdataMediaType) {
        String type = bulkdataMediaType.getType().toLowerCase();
        return type.equals("image") ? UID.SecondaryCaptureImageStorage
                : type.equals("video") ? UID.VideoPhotographicImageStorage
                : equalsIgnoreParameters(bulkdataMediaType, APPLICATION_PDF_TYPE) ? UID.EncapsulatedPDFStorage
                : equalsIgnoreParameters(bulkdataMediaType, MediaType.APPLICATION_XML_TYPE) ? UID.EncapsulatedCDAStorage
                : isSTLType(bulkdataMediaType) ? UID.EncapsulatedSTLStorage
                : equalsIgnoreParameters(bulkdataMediaType, MODEL_OBJ_TYPE) ? UID.EncapsulatedOBJStorage
                : equalsIgnoreParameters(bulkdataMediaType, MODEL_MTL_TYPE) ? UID.EncapsulatedMTLStorage
                : null;
    }

    public static boolean isSTLType(MediaType mediaType) {
        return equalsIgnoreParameters(mediaType, MODEL_STL_TYPE)
                || equalsIgnoreParameters(mediaType, MODEL_X_STL_BINARY_TYPE)
                || equalsIgnoreParameters(mediaType, APPLICATION_SLA_TYPE);
    }

    public static boolean isSTLType(String type) {
        return MODEL_STL.equalsIgnoreCase(type)
                || MODEL_X_STL_BINARY.equalsIgnoreCase(type)
                || APPLICATION_SLA.equalsIgnoreCase(type);
    }

    public static boolean equalsIgnoreParameters(MediaType type1, MediaType type2) {
        return type1.getType().equalsIgnoreCase(type2.getType())
                &&  type1.getSubtype().equalsIgnoreCase(type2.getSubtype());
    }

    public static MediaType getMultiPartRelatedType(MediaType mediaType) {
        if (!MediaTypes.MULTIPART_RELATED_TYPE.isCompatible(mediaType))
            return null;

        String type = mediaType.getParameters().get("type");
        if (type == null)
            return MediaType.WILDCARD_TYPE;

        MediaType partType = MediaType.valueOf(type);
        if (mediaType.getParameters().size() > 1) {
            Map<String, String> params = new HashMap<>(mediaType.getParameters());
            params.remove("type");
            partType = new MediaType(partType.getType(), partType.getSubtype(), params);
        }
        return partType;
    }

    public static String getTransferSyntax(MediaType type) {
        return type != null && equalsIgnoreParameters(APPLICATION_DICOM_TYPE, type)
                ? type.getParameters().get("transfer-syntax")
                : null;
    }

    public static MediaType applicationDicomWithTransferSyntax(String tsuid) {
        return new MediaType("application", "dicom", Collections.singletonMap("transfer-syntax", tsuid));
    }
}
