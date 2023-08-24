/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2013-2021
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.tool.qstar;

import org.apache.commons.cli.*;
import org.dcm4che3.qstar.*;
import org.dcm4che3.tool.common.CLIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Apr 2023
 */
public class QStar {

    private static final Logger LOG = LoggerFactory.getLogger(QStar.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.qstar.messages");
    private final WSWebServiceSoapPort port;
    private WSUserLoginResponse userLogin;

    private QStar(String url) {
        port = QStarUtils.getWSWebServiceSoapPort(url);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            String[] user = cl.getOptionValues("u");
            if (user == null)
                throw new MissingOptionException(
                        rb.getString("missing-user-opt"));
            String url = cl.getOptionValue("U");
            if (url == null)
                throw new MissingOptionException(
                        rb.getString("missing-url-opt"));

            QStar qstar = new QStar(url);
            List<String> fileList = cl.getArgList();
            if (qstar.login(user[0], user[1])) {
                if (cl.hasOption("r")) {
                    if (cl.hasOption("s")) {
                        SortedSet<FilePosition> filePositions = new TreeSet<>();
                        for (String filePath : fileList) {
                            filePositions.add(new FilePosition(filePath, qstar.getFileInfo(filePath)));
                        }
                        fileList.clear();
                        for (FilePosition filePosition : filePositions) {
                            fileList.add(filePosition.filePath);
                        }
                    }
                    BigInteger jobId = qstar.batchFileRetrieve(
                            ((Number) cl.getParsedOptionValue("r")).longValue(),
                            fileList,
                            cl.getOptionValue("D", ""));
                    if (jobId != null && cl.hasOption("p")) {
                        Number delay = (Number) cl.getParsedOptionValue("p");
                        while (!fileList.isEmpty()) {
                            Iterator<String> iterator = fileList.iterator();
                            while (iterator.hasNext()) {
                                if (delay != null)
                                    Thread.sleep(delay.longValue());
                                switch (qstar.batchJobObjectStatus(jobId, iterator.next())) {
                                    case 1: // INQUEUE
                                    case 2: // PROCESSING
                                        break;
                                    default:
                                        iterator.remove();
                                }
                            }
                        }
                        qstar.batchJobStatus(jobId);
                     }
                } else if (cl.hasOption("j")) {
                    BigInteger jobId = BigInteger.valueOf(((Number) cl.getParsedOptionValue("j")).longValue());
                    if (fileList.isEmpty()) {
                        qstar.batchJobStatus(jobId);
                    } else {
                        for (String file : fileList) {
                            qstar.batchJobObjectStatus(jobId, file);
                        }
                    }
                } else if (cl.hasOption("P")) {
                    for (String file : fileList) {
                        qstar.purgeFile(file);
                    }
                } else {
                    for (String file : fileList) {
                        qstar.getFileInfo(file);
                    }
                }
                qstar.logout();
            }
        } catch (ParseException e) {
            System.err.println("qstar: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("qstar: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static class FilePosition implements Comparable<FilePosition> {
        private final String filePath;
        private long vol;
        private long pos;

        public FilePosition(String filePath, WSGetFileInfoResponse fileInfo) {
            this.filePath = filePath;
            if (fileInfo == null) return;
            List<WSFileExtentInfo> extent = fileInfo.getInfo().getFlocExtents().getExtent();
            if (extent.isEmpty()) return;
            WSFileExtentInfo wsFileExtentInfo = extent.get(0);
            this.vol = wsFileExtentInfo.getVol();
            this.pos = wsFileExtentInfo.getPos();
        }

        @Override
        public int compareTo(FilePosition o) {
            return vol < o.vol ? -1 : vol > o.vol ? 1 : pos < o.pos ? -1 : pos > o.pos ? 1 : filePath.compareTo(o.filePath);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilePosition that = (FilePosition) o;

            return filePath.equals(that.filePath);
        }

        @Override
        public int hashCode() {
            return filePath.hashCode();
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("u")
                .longOpt("user")
                .numberOfArgs(2)
                .valueSeparator(':')
                .argName("user:password")
                .desc(rb.getString("user"))
                .build());
        opts.addOption(Option.builder("U")
                .longOpt("url")
                .hasArg()
                .argName("url")
                .desc(rb.getString("url"))
                .build());
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("r")
                .longOpt("retrieve")
                .hasArg()
                .type(Number.class)
                .argName("priority")
                .desc(rb.getString("retrieve"))
                .build());
        group.addOption(Option.builder("j")
                .longOpt("job")
                .hasArg()
                .type(Number.class)
                .argName("jobId")
                .desc(rb.getString("job"))
                .build());
        group.addOption(Option.builder("P")
                .longOpt("purge")
                .desc(rb.getString("purge"))
                .build());
        opts.addOption(Option.builder("D")
                .longOpt("target-dir")
                .hasArg()
                .argName("path")
                .desc(rb.getString("target-dir"))
                .build());
        opts.addOption(Option.builder("p")
                .longOpt("progress")
                .hasArg()
                .optionalArg(true)
                .type(Number.class)
                .argName("ms")
                .desc(rb.getString("progress"))
                .build());
        opts.addOption(Option.builder("s")
                .longOpt("sort")
                .desc(rb.getString("sort"))
                .build());
        opts.addOptionGroup(group);
        return CLIUtils.parseComandLine(args, opts, rb, QStar.class);
    }

    private boolean login(String userName, String userPassword) {
        try {
            userLogin = QStarUtils.login(port, userName, userPassword);
            return true;
        } catch (Exception e) {
            LOG.info("Login Failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private void logout() {
        try {
            QStarUtils.logout(port, userLogin);
        } catch (Exception e) {
            LOG.info("Logout Failed: {}", e.getMessage(), e);
        }
    }

    private WSGetFileInfoResponse getFileInfo(String filePath) {
        try {
            return QStarUtils.getFileInfo(port, userLogin, filePath);
        } catch (Exception e) {
            LOG.info("GetFileInfo Failed: {}", e.getMessage(), e);
        }
        return null;
    }

    private BigInteger batchFileRetrieve(long jobPriority, List<String> fileList, String targetDir) {
        try {
            return QStarUtils.batchFileRetrieve(port, userLogin, jobPriority, fileList, targetDir).getJobId();
        } catch (Exception e) {
            LOG.info("BatchFileRetrieve Failed: {}", e.getMessage(), e);
        }
        return null;
    }

    private int batchJobStatus(BigInteger jobId) {
        try {
            return (int) QStarUtils.batchJobStatus(port, userLogin, jobId).getJobStatus();
        } catch (Exception e) {
            LOG.info("BatchJobStatus Failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private int batchJobObjectStatus(BigInteger jobId, String file) {
        try {
            return (int) QStarUtils.batchJobObjectStatus(port, userLogin, jobId, file).getJobObjectStatus();
        } catch (Exception e) {
            LOG.info("BatchJobObjectStatus Failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private void purgeFile(String filePath) {
        try {
            QStarUtils.purgeFile(port, userLogin, filePath);
        } catch (Exception e) {
            LOG.info("GetFileInfo Failed: {}", e.getMessage(), e);
        }
    }

}
