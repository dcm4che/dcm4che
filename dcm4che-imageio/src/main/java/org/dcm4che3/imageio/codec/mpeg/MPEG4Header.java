package org.dcm4che3.imageio.codec.mpeg;

import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MPEG4Header {
    public static void main (String[] args) throws Exception {
        Parser.parse();
    }
}

class Parser {
    private static Logger LOG = LoggerFactory.getLogger(Parser.class);
    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 10485768; // 10MiB
    private static byte[] buffer = {};
    private static int headerLength;


    private int numFrames;
    private float frameRate;

    private static int offset = 0;
    private static byte[] moov = {};
    private static byte[] trackBox = {};
    private static int moovOffset = 0;

    static void parse() throws Exception {
        File file = new File("/home/vrinda/Downloads/small.mp4");
        int grow = INIT_BUFFER_SIZE;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            while (headerLength == buffer.length && headerLength < MAX_BUFFER_SIZE) {
                buffer = Arrays.copyOf(buffer, grow += headerLength);
                headerLength += StreamUtils.readAvailable(bis, buffer, headerLength, buffer.length - headerLength);
                parseHeader(buffer);
            }
            parseMoovBox();
        }
    }

    private static void parseHeader(byte[] buffer) {
        if (offset > buffer.length)
            return;

        ByteBuffer byteBuffer = getByteBuffer(buffer, offset);
        long contentsize;
        long size = IsoTypeReader.readUInt32(byteBuffer);
        String type = IsoTypeReader.read4cc(byteBuffer);
        //TODO : size == 1; size == 0; type.equals("uuid"). For all other cases content size is as below
        contentsize = size - 8;

        if (!type.equals("moov")) {
            offset = offset + (int)size;
            parseHeader(buffer);
        } else {
            moov = Arrays.copyOf(moov, (int) contentsize);
            moov = Arrays.copyOfRange(buffer, offset+8, offset+(int)contentsize);
        }
    }

    private static void parseMoovBox() {
        ByteBuffer byteBuffer = getByteBuffer(moov, moovOffset);
        long size = IsoTypeReader.readUInt32(byteBuffer);
        String type = IsoTypeReader.read4cc(byteBuffer);
        long contentsize = size - 8;

        if (type.equals("trak")) {
            trackBox = Arrays.copyOf(trackBox, (int) contentsize);
            trackBox = Arrays.copyOfRange(moov, moovOffset+8, moovOffset+(int)contentsize);
            ByteBuffer trackHeaderBoxBytes = getByteBuffer(trackBox, 0);
            long sizeTrak = IsoTypeReader.readUInt32(trackHeaderBoxBytes);
            String typeTrak = IsoTypeReader.read4cc(trackHeaderBoxBytes);
            long contentsizeTrak = sizeTrak - 8;
            ByteBuffer trackHeaderBytes = getByteBufferContent(trackBox, 8, (int)contentsizeTrak);
            TrackHeaderParser.parseTrackHeaderDetails(trackHeaderBytes);
        } else {
            moovOffset = moovOffset + (int) size;
            parseMoovBox();
        }
    }

    private static ByteBuffer getByteBuffer(byte[] buffer, int offset) {
        byte[] boxHeader = Arrays.copyOfRange(buffer, offset, offset + 8);  //most boxes have headers of length 8
        return ByteBuffer.wrap(boxHeader);
    }

    private static ByteBuffer getByteBufferContent(byte[] buffer, int offset, int contentSize) {
        byte[] boxHeader = Arrays.copyOfRange(buffer, offset, offset+contentSize);  //most boxes have headers of length 8
        return ByteBuffer.wrap(boxHeader);
    }
}

class TrackHeaderParser {
    private static double rows;
    private static double columns;
    private static int version;
    private static long duration;

    private static Logger LOG = LoggerFactory.getLogger(TrackHeaderParser.class);

    static void parseTrackHeaderDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (version == 1) {
            IsoTypeReader.readUInt64(content);//creation time
            IsoTypeReader.readUInt64(content);//modification time
            IsoTypeReader.readUInt32(content);//trackId
            IsoTypeReader.readUInt32(content);
            duration = content.getLong();
        } else {
            IsoTypeReader.readUInt32(content);//creation time
            IsoTypeReader.readUInt32(content);//modification time
            IsoTypeReader.readUInt32(content);//trackId
            IsoTypeReader.readUInt32(content);
            duration = content.getInt();
        }

        if (duration < -1) {
            LOG.warn("tkhd duration is not in expected range");
        }

        IsoTypeReader.readUInt32(content);
        IsoTypeReader.readUInt32(content);
        IsoTypeReader.readUInt16(content);  //layer  // 204
        IsoTypeReader.readUInt16(content);//alternate group
        IsoTypeReader.readFixedPoint88(content);//volume
        IsoTypeReader.readUInt16(content);     // 212
        Matrix.fromByteBuffer(content);
        rows = IsoTypeReader.readFixedPoint1616(content);    // 248
        columns = IsoTypeReader.readFixedPoint1616(content);


        System.out.println("duration: " + duration);
        System.out.println("rows: " + rows);
        System.out.println("columns: " + columns);
    }

    static long parseVersionAndFlags(ByteBuffer content) {
        version = IsoTypeReader.readUInt8(content);
        IsoTypeReader.readUInt24(content); //flags
        return 4;
    }
}

class IsoTypeReader {
    static long readUInt32BE(ByteBuffer bb) {
        long ch1 = readUInt8(bb);
        long ch2 = readUInt8(bb);
        long ch3 = readUInt8(bb);
        long ch4 = readUInt8(bb);
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));

    }

    static long readUInt32(ByteBuffer bb) {
        long i = bb.getInt();
        if (i < 0) {
            i += 1L << 32;
        }
        return i;
    }

    static int readUInt24(ByteBuffer bb) {
        int result = 0;
        result += readUInt16(bb) << 8;
        result += byte2int(bb.get());
        return result;
    }


    static int readUInt16(ByteBuffer bb) {
        int result = 0;
        result += byte2int(bb.get()) << 8;
        result += byte2int(bb.get());
        return result;
    }

    static int readUInt16BE(ByteBuffer bb) {
        int result = 0;
        result += byte2int(bb.get());
        result += byte2int(bb.get()) << 8;
        return result;
    }

    static int readUInt8(ByteBuffer bb) {
        return byte2int(bb.get());
    }

    static int byte2int(byte b) {
        return b < 0 ? b + 256 : b;
    }


    /**
     * Reads a zero terminated UTF-8 string.
     *
     * @param byteBuffer the data source
     * @return the string readByte
     * @throws Error in case of an error in the underlying stream
     */
    static String readString(ByteBuffer byteBuffer) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = byteBuffer.get()) != 0) {
            out.write(read);
        }
        return Utf8.convert(out.toByteArray());
    }

    static String readString(ByteBuffer byteBuffer, int length) {
        byte[] buffer = new byte[length];
        byteBuffer.get(buffer);
        return Utf8.convert(buffer);

    }

    static long readUInt64(ByteBuffer byteBuffer) {
        long result = 0;
        // thanks to Erik Nicolas for finding a bug! Cast to long is definitivly needed
        result += readUInt32(byteBuffer) << 32;
        if (result < 0) {
            throw new RuntimeException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
        }
        result += readUInt32(byteBuffer);
        return result;
    }

    static double readFixedPoint1616(ByteBuffer bb) {
        byte[] bytes = new byte[4];
        bb.get(bytes);

        int result = 0;
        result |= ((bytes[0] << 24) & 0xFF000000);
        result |= ((bytes[1] << 16) & 0xFF0000);
        result |= ((bytes[2] << 8) & 0xFF00);
        result |= ((bytes[3]) & 0xFF);
        return ((double) result) / 65536;

    }


    static double readFixedPoint0230(ByteBuffer bb) {
        byte[] bytes = new byte[4];
        bb.get(bytes);

        int result = 0;
        result |= ((bytes[0] << 24) & 0xFF000000);
        result |= ((bytes[1] << 16) & 0xFF0000);
        result |= ((bytes[2] << 8) & 0xFF00);
        result |= ((bytes[3]) & 0xFF);
        return ((double) result) / (1 << 30);

    }

    static float readFixedPoint88(ByteBuffer bb) {
        byte[] bytes = new byte[2];
        bb.get(bytes);
        short result = 0;
        result |= ((bytes[0] << 8) & 0xFF00);
        result |= ((bytes[1]) & 0xFF);
        return ((float) result) / 256;
    }

    static String readIso639(ByteBuffer bb) {
        int bits = readUInt16(bb);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int c = (bits >> (2 - i) * 5) & 0x1f;
            result.append((char) (c + 0x60));
        }
        return result.toString();
    }


    static String read4cc(ByteBuffer bb) {
        byte[] codeBytes = new byte[4];
        bb.get(codeBytes);

        try {
            return new String(codeBytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }


    }

    static long readUInt48(ByteBuffer byteBuffer) {
        long result = (long) readUInt16(byteBuffer) << 32;
        if (result < 0) {
            throw new RuntimeException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
        }
        result += readUInt32(byteBuffer);

        return result;
    }
}

class Matrix {
    static final Matrix ROTATE_0 = new Matrix(1, 0, 0, 1, 0, 0, 1, 0, 0);
    static final Matrix ROTATE_90 = new Matrix(0, 1, -1, 0, 0, 0, 1, 0, 0);
    static final Matrix ROTATE_180 = new Matrix(-1, 0, 0, -1, 0, 0, 1, 0, 0);
    static final Matrix ROTATE_270 = new Matrix(0, -1, 1, 0, 0, 0, 1, 0, 0);
    double u, v, w;
    double a, b, c, d, tx, ty;

    public Matrix(double a, double b, double c, double d, double u, double v, double w, double tx, double ty) {
        this.u = u;
        this.v = v;
        this.w = w;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.tx = tx;
        this.ty = ty;
    }

    static Matrix fromFileOrder(double a, double b, double u, double c, double d, double v, double tx, double ty, double w) {
        return new Matrix(a, b, c, d, u, v, w, tx, ty);
    }

    static Matrix fromByteBuffer(ByteBuffer byteBuffer) {
        return fromFileOrder(
                IsoTypeReader.readFixedPoint1616(byteBuffer),
                IsoTypeReader.readFixedPoint1616(byteBuffer),
                IsoTypeReader.readFixedPoint0230(byteBuffer),
                IsoTypeReader.readFixedPoint1616(byteBuffer),
                IsoTypeReader.readFixedPoint1616(byteBuffer),
                IsoTypeReader.readFixedPoint0230(byteBuffer),
                IsoTypeReader.readFixedPoint1616(byteBuffer),
                IsoTypeReader.readFixedPoint1616(byteBuffer),
                IsoTypeReader.readFixedPoint0230(byteBuffer)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matrix matrix = (Matrix) o;

        if (Double.compare(matrix.a, a) != 0) return false;
        if (Double.compare(matrix.b, b) != 0) return false;
        if (Double.compare(matrix.c, c) != 0) return false;
        if (Double.compare(matrix.d, d) != 0) return false;
        if (Double.compare(matrix.tx, tx) != 0) return false;
        if (Double.compare(matrix.ty, ty) != 0) return false;
        if (Double.compare(matrix.u, u) != 0) return false;
        if (Double.compare(matrix.v, v) != 0) return false;
        if (Double.compare(matrix.w, w) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(u);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(v);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(w);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(a);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(b);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(c);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(d);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(tx);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(ty);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        if (this.equals(ROTATE_0)) {
            return "Rotate 0°";
        }
        if (this.equals(ROTATE_90)) {
            return "Rotate 90°";
        }
        if (this.equals(ROTATE_180)) {
            return "Rotate 180°";
        }
        if (this.equals(ROTATE_270)) {
            return "Rotate 270°";
        }
        return "Matrix{" +
                "u=" + u +
                ", v=" + v +
                ", w=" + w +
                ", a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", tx=" + tx +
                ", ty=" + ty +
                '}';
    }

    public void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, a);
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, b);
        IsoTypeWriter.writeFixedPoint0230(byteBuffer, u);

        IsoTypeWriter.writeFixedPoint1616(byteBuffer, c);
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, d);
        IsoTypeWriter.writeFixedPoint0230(byteBuffer, v);

        IsoTypeWriter.writeFixedPoint1616(byteBuffer, tx);
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, ty);
        IsoTypeWriter.writeFixedPoint0230(byteBuffer, w);

    }
}

class IsoTypeWriter {

    static void writeUInt64(ByteBuffer bb, long u) {
        assert u >= 0 : "The given long is negative";
        bb.putLong(u);
    }

    static void writeUInt32(ByteBuffer bb, long u) {
        assert u >= 0 && u <= 1L << 32 : "The given long is not in the range of uint32 (" + u + ")";
        bb.putInt((int) u);

    }

    static void writeUInt32BE(ByteBuffer bb, long u) {
        assert u >= 0 && u <= 1L << 32 : "The given long is not in the range of uint32 (" + u + ")";
        writeUInt16BE(bb, (int) u & 0xFFFF);
        writeUInt16BE(bb, (int) ((u >> 16) & 0xFFFF));

    }


    static void writeUInt24(ByteBuffer bb, int i) {
        i = i & 0xFFFFFF;
        writeUInt16(bb, i >> 8);
        writeUInt8(bb, i);

    }

    static void writeUInt48(ByteBuffer bb, long l) {
        l = l & 0xFFFFFFFFFFFFL;
        writeUInt16(bb, (int) (l >> 32));
        writeUInt32(bb, l & 0xFFFFFFFFL);

    }


    static void writeUInt16(ByteBuffer bb, int i) {
        i = i & 0xFFFF;
        writeUInt8(bb, i >> 8);
        writeUInt8(bb, i & 0xFF);
    }

    static void writeUInt16BE(ByteBuffer bb, int i) {
        i = i & 0xFFFF;
        writeUInt8(bb, i & 0xFF);
        writeUInt8(bb, i >> 8);
    }

    static void writeUInt8(ByteBuffer bb, int i) {
        i = i & 0xFF;
        bb.put((byte) i);
    }


    static void writeFixedPoint1616(ByteBuffer bb, double v) {
        int result = (int) (v * 65536);
        bb.put((byte) ((result & 0xFF000000) >> 24));
        bb.put((byte) ((result & 0x00FF0000) >> 16));
        bb.put((byte) ((result & 0x0000FF00) >> 8));
        bb.put((byte) ((result & 0x000000FF)));
    }

    static void writeFixedPoint0230(ByteBuffer bb, double v) {
        int result = (int) (v * (1 << 30));
        bb.put((byte) ((result & 0xFF000000) >> 24));
        bb.put((byte) ((result & 0x00FF0000) >> 16));
        bb.put((byte) ((result & 0x0000FF00) >> 8));
        bb.put((byte) ((result & 0x000000FF)));
    }

    static void writeFixedPoint88(ByteBuffer bb, double v) {
        short result = (short) (v * 256);
        bb.put((byte) ((result & 0xFF00) >> 8));
        bb.put((byte) ((result & 0x00FF)));
    }

    static void writeIso639(ByteBuffer bb, String language) {
        if (language.getBytes().length != 3) {
            throw new IllegalArgumentException("\"" + language + "\" language string isn't exactly 3 characters long!");
        }
        int bits = 0;
        for (int i = 0; i < 3; i++) {
            bits += (language.getBytes()[i] - 0x60) << (2 - i) * 5;
        }
        writeUInt16(bb, bits);
    }

    static void writePascalUtfString(ByteBuffer bb, String string) {
        byte[] b = Utf8.convert(string);
        assert b.length < 255;
        writeUInt8(bb, b.length);
        bb.put(b);
    }

    static void writeZeroTermUtf8String(ByteBuffer bb, String string) {
        byte[] b = Utf8.convert(string);
        bb.put(b);
        writeUInt8(bb, 0);
    }

    static void writeUtf8String(ByteBuffer bb, String string) {

        bb.put(Utf8.convert(string));
        writeUInt8(bb, 0);
    }
}

class Utf8 {
    public static byte[] convert(String s) {
        try {
            if (s != null) {
                return s.getBytes("UTF-8");
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static String convert(byte[] b) {
        try {
            if (b != null) {
                return new String(b, "UTF-8");
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static int utf8StringLengthInBytes(String utf8) {
        try {
            if (utf8 != null) {
                return utf8.getBytes("UTF-8").length;
            } else {
                return 0;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }
}