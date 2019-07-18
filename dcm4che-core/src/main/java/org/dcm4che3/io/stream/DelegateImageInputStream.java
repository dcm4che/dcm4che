//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2019                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//
package org.dcm4che3.io.stream;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Delegate to an underlying ImageInputStream.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DelegateImageInputStream implements ImageInputStream {

    private final ImageInputStream iis;

    public DelegateImageInputStream(ImageInputStream iis) {
        this.iis = iis;
    }

    @Override
    public void setByteOrder(ByteOrder byteOrder) {
        iis.setByteOrder(byteOrder);
    }

    @Override
    public ByteOrder getByteOrder() {
        return iis.getByteOrder();
    }

    @Override
    public int read() throws IOException {
        return iis.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return iis.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return iis.read(b,off,len);
    }

    @Override
    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        iis.readBytes(buf,len);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return iis.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return iis.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return iis.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return iis.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return iis.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return iis.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return iis.readInt();
    }

    @Override
    public long readUnsignedInt() throws IOException {
        return iis.readUnsignedInt();
    }

    @Override
    public long readLong() throws IOException {
        return iis.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return iis.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return iis.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return iis.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return iis.readUTF();
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        iis.readFully(b,off,len);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        iis.readFully(b);
    }

    @Override
    public void readFully(short[] s, int off, int len) throws IOException {
        iis.readFully(s,off,len);
    }

    @Override
    public void readFully(char[] c, int off, int len) throws IOException {
        iis.readFully(c,off,len);
    }

    @Override
    public void readFully(int[] i, int off, int len) throws IOException {
        iis.readFully(i,off,len);
    }

    @Override
    public void readFully(long[] l, int off, int len) throws IOException {
        iis.readFully(l,off,len);
    }

    @Override
    public void readFully(float[] f, int off, int len) throws IOException {
        iis.readFully(f,off,len);
    }

    @Override
    public void readFully(double[] d, int off, int len) throws IOException {
        iis.readFully(d,off,len);
    }

    @Override
    public long getStreamPosition() throws IOException {
        return this.iis.getStreamPosition();
    }

    @Override
    public int getBitOffset() throws IOException {
        return iis.getBitOffset();
    }

    @Override
    public void setBitOffset(int bitOffset) throws IOException {
        iis.setBitOffset(bitOffset);
    }

    @Override
    public int readBit() throws IOException {
        return iis.readBit();
    }

    @Override
    public long readBits(int numBits) throws IOException {
        return iis.readBits(numBits);
    }

    @Override
    public long length() throws IOException {
        return iis.length();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return iis.skipBytes(n);
    }

    @Override
    public long skipBytes(long n) throws IOException {
        return iis.skipBytes(n);
    }

    @Override
    public void seek(long pos) throws IOException {
        iis.seek(pos);
    }

    @Override
    public void mark() {
        iis.mark();
    }

    @Override
    public void reset() throws IOException {
        iis.reset();
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        iis.flushBefore(pos);
    }

    @Override
    public void flush() throws IOException {
        iis.flush();
    }

    @Override
    public long getFlushedPosition() {
        return iis.getFlushedPosition();
    }

    @Override
    public boolean isCached() {
        return iis.isCached();
    }

    @Override
    public boolean isCachedMemory() {
        return iis.isCachedMemory();
    }

    @Override
    public boolean isCachedFile() {
        return iis.isCachedFile();
    }

    @Override
    public void close() throws IOException {
        // Do nothing:  preserve the underlying class.
    }

    @Override
    public String toString() {
        return "Delegate("+iis+")";
    }
}

