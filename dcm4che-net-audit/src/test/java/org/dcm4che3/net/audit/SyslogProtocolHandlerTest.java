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
 * Portions created by the Initial Developer are Copyright (C) 2020
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

package org.dcm4che3.net.audit;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Zhirong Liang <zhirong.liang@agfa.com>
 */
public class SyslogProtocolHandlerTest {
    private static final int MINIMUM_NUMBER_OF_THREADS = 4;
    private static final long THREAD_KEEP_ALIVE_TIME_SEC = 60;
    private static final int EXECUTOR_QUEUE_SIZE = 1000;
    private static final String AUDIT_SYSLOG_THREAD_NAME_PREFIX = "Audit-Syslog-";

    private static Executor executor;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // thread pool core size equals number of processors, or at least 4 if less
        int threadPoolCoreSize = Math.max( Runtime.getRuntime().availableProcessors(), MINIMUM_NUMBER_OF_THREADS );
        executor = new ThreadPoolExecutor(threadPoolCoreSize,
                       threadPoolCoreSize * 2,
                           THREAD_KEEP_ALIVE_TIME_SEC,
                           TimeUnit.SECONDS,
                           new LinkedBlockingQueue<>(EXECUTOR_QUEUE_SIZE),
                           new NamedThreadFactory(AUDIT_SYSLOG_THREAD_NAME_PREFIX));

        SyslogProtocolHandler.setExecutor(executor);
    }

    @Test
    public void setExecutor_ThreadNamePrefixesWithCustomizedName_WhenNewThreadIsCreated() {

        ThreadPoolExecutor executor = Whitebox.getInternalState(SyslogProtocolHandler.class, "executor");

        assertThat("Thread name should prefix with Audit-Syslog-",
                   executor.getThreadFactory()
                           .newThread(() -> {})
                           .getName(),
                   startsWith(AUDIT_SYSLOG_THREAD_NAME_PREFIX));
    }

    @Test
    public void setExecutor_ThreadPoolCoreSizeMatchesAvailableProcessorsOrMinimum4_ForSyslogProtocolHandlerThreadPool() {

        int expectedThreadPoolCoreSize = Math.max( Runtime.getRuntime().availableProcessors(), MINIMUM_NUMBER_OF_THREADS );

        ThreadPoolExecutor executor = Whitebox.getInternalState(SyslogProtocolHandler.class, "executor");

        assertThat("Thread pool core size should match available processors or minimum 4",
                   executor.getCorePoolSize(),
                   is(expectedThreadPoolCoreSize));
    }
}