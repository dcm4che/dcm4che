/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.tool.fixlo2un;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.tool.common.CLIUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since May 2019
 */
public class FixLO2UN extends SimpleFileVisitor<Path> {

    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.fixlo2un.messages");
    private final ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x55, 0x4e, 0, 0, 0, 0, 0, 0 })
            .order(ByteOrder.LITTLE_ENDIAN);
    private final Path srcPath;
    private final Path destPath;
    private final Dest dest;

    private FixLO2UN(Path srcPath, Path destPath, Dest dest) {
        this.srcPath = srcPath;
        this.destPath = destPath;
        this.dest = dest;
    }

    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, FixLO2UN.class);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            List<String> argList = cl.getArgList();
            Path destPath = Paths.get(argList.get(argList.size() - 1));
            if (!Files.isDirectory(destPath) && (argList.size() > 2 || !Files.isRegularFile(Paths.get(argList.get(0))))) {
                System.out.printf("target '%s' is not a directory%n", destPath);
                System.exit(-1);
            }
            Dest dest = Dest.of(destPath);
            for (int i = 0; i < argList.size() - 1; i++) {
                Path srcPath = Paths.get(argList.get(i));
                Files.walkFileTree(srcPath, new FixLO2UN(srcPath, destPath, dest));
            }
        } catch (ParseException e) {
            System.err.println("json2rst: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("json2rst: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) throws IOException {
        Path dstFile = dest.dstFile(srcFile, srcPath, destPath);
        Path dstDir = dstFile.getParent();
        if (dstDir != null) Files.createDirectories(dstDir);
        System.out.printf("%s -> %s%n", srcFile, dstFile);
        try (FileChannel ifc = (FileChannel) Files.newByteChannel(srcFile, EnumSet.of(StandardOpenOption.READ));
            FileChannel ofc = (FileChannel) Files.newByteChannel(dstFile,
                    EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
            MappedByteBuffer mbb = ifc.map(FileChannel.MapMode.READ_ONLY, 0, ifc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            mbb.mark();
            int length;
            while ((length = correctLength(mbb)) > 0) {
                int position = mbb.position();
                System.out.printf("  %d: (%02X%02X,%02X%02X) LO #%d -> UN #%d%n",
                        position - 6,
                        mbb.get(position - 5),
                        mbb.get(position - 6),
                        mbb.get(position - 3),
                        mbb.get(position - 4),
                        length & 0xfff,
                        length);
                mbb.reset().limit(position - 2);
                ofc.write(mbb);
                buffer.putInt(4, length).rewind();
                ofc.write(buffer);
                mbb.limit(position + 2 + length).position(position + 2);
                ofc.write(mbb);
                mbb.limit((int) ifc.size()).mark();
            }
            mbb.reset();
            ofc.write(mbb);
        }
        return FileVisitResult.CONTINUE;
    }

    private int correctLength(MappedByteBuffer mbb) {
        int length;
        while (mbb.remaining() > 8) {
            if (mbb.getShort() == 0x4f4c
                    && mbb.get(mbb.position() - 3) == 0
                    && mbb.get(mbb.position() - 6) % 2 != 0
                    && !isVRCode(mbb.getShort(mbb.position() + 6 +
                    (length = mbb.getShort(mbb.position()) & 0xffff))))
                return correctLength(mbb, length);
        }
        return 0;
    }

    private boolean isVRCode(int code) {
        switch (code) {
            case 0x4541:
            case 0x5341:
            case 0x5441:
            case 0x5343:
            case 0x4144:
            case 0x5344:
            case 0x5444:
            case 0x4446:
            case 0x4c46:
            case 0x5349:
            case 0x4f4c:
            case 0x544c:
            case 0x424f:
            case 0x444f:
            case 0x464f:
            case 0x4c4f:
            case 0x574f:
            case 0x4e50:
            case 0x4853:
            case 0x4c53:
            case 0x5153:
            case 0x5353:
            case 0x5453:
            case 0x4d54:
            case 0x4355:
            case 0x4955:
            case 0x4c55:
            case 0x4e55:
            case 0x5255:
            case 0x5355:
            case 0x5455:
                return true;
        }
        return false;
    }

    private int correctLength(MappedByteBuffer mbb, int length) {
        do {
            length += 0x10000;
        } while (!isVRCode(mbb.getShort(mbb.position() + 6 + length)));
        return length;
    }

    private enum Dest {
        FILE,
        DIRECTORY {
            @Override
            Path dstFile(Path srcFile, Path srcPath, Path destPath) {
                return destPath.resolve(srcFile == srcPath ? srcFile.getFileName() : srcPath.relativize(srcFile));
            }
        };

        static Dest of(Path destPath) {
            return Files.isDirectory(destPath) ? DIRECTORY : FILE;
        }

        Path dstFile(Path srcFile, Path srcPath, Path destPath) {
            return destPath;
        }
    }

}
