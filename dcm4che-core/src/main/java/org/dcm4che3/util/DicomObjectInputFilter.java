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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.util;

import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java deserialization allowlist for dcm4che's {@code ObjectInputStream}
 * sinks (issue #1581).
 *
 * <p>Java's serialization runs {@code readObject}/{@code readResolve}
 * callbacks before any cast or post-read validation completes, so a stream
 * containing a hostile {@code Serializable} class triggers attacker code
 * before dcm4che can react. This class installs a JEP 290
 * {@code ObjectInputFilter} that rejects every class not on a small
 * allowlist of dcm4che data/container types, primitive boxes, common
 * collections, and time-zone types — i.e. the classes the legitimate read
 * path actually produces.</p>
 *
 * <h2>How it is wired up</h2>
 * <ul>
 *   <li>On JDK 17+ a JVM-wide {@code ObjectInputFilter.Config} <em>filter
 *       factory</em> is installed once, at class initialization, so every
 *       {@link ObjectInputStream} constructed after that point carries a
 *       composed filter (host-application filter wins on {@code REJECTED} /
 *       {@code ALLOWED}; ours runs on {@code UNDECIDED}).</li>
 *   <li>On JDK 8u121–16 the per-stream filter API
 *       ({@code ObjectInputStream#setObjectInputFilter}, or the 8u121
 *       {@code sun.misc} equivalent) is used directly inside
 *       {@link #install(ObjectInputStream)}, since the older API permits
 *       installing a filter mid-stream.</li>
 *   <li>The dcm4che allowlist is <em>scoped</em>: it only takes effect on a
 *       thread that has called {@link #install(ObjectInputStream)} without a
 *       matching {@link #uninstall(ObjectInputStream)} yet. Outside the
 *       guarded {@code readObject} bodies the filter returns
 *       {@code UNDECIDED}, so non-dcm4che deserialization elsewhere in the
 *       host application is unaffected.</li>
 * </ul>
 *
 * <h2>Idempotency and never-throws</h2>
 * <p>{@link #install} is best-effort, may be called repeatedly on the same
 * stream, and never propagates exceptions. Failure paths log at DEBUG; a
 * single WARN is logged once if neither filter API is available
 * (pre-8u121 JVMs), preserving pre-fix behavior on those runtimes.</p>
 *
 * <p>Host applications that need to widen the allowlist should install
 * their own outer filter on the {@code ObjectInputStream} before triggering
 * deserialization — the dcm4che filter respects any pre-existing filter.</p>
 */
public final class DicomObjectInputFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DicomObjectInputFilter.class);

    private static final long MAX_ARRAY_LENGTH = 100_000L;
    private static final long MAX_DEPTH = 64L;
    private static final long MAX_REFERENCES = 1_000_000L;

    private static final Set<String> ALLOWED_CLASSES;
    static {
        Set<String> s = new HashSet<>();
        // dcm4che data/container types
        s.add("org.dcm4che3.data.Attributes");
        s.add("org.dcm4che3.data.BulkData");
        s.add("org.dcm4che3.data.BulkDataWithPrefix");
        s.add("org.dcm4che3.data.Sequence");
        s.add("org.dcm4che3.data.Fragments");
        s.add("org.dcm4che3.data.VR");
        s.add("org.dcm4che3.data.ItemPointer");
        s.add("org.dcm4che3.data.Code");
        s.add("org.dcm4che3.data.Issuer");
        s.add("org.dcm4che3.data.DateRange");
        s.add("org.dcm4che3.data.AttributeSelector");
        s.add("org.dcm4che3.data.ValueSelector");
        s.add("org.dcm4che3.data.IOD");
        s.add("org.dcm4che3.data.IOD$DataElement");
        // dcm4che util types
        s.add("org.dcm4che3.util.IntHashMap");
        s.add("org.dcm4che3.util.Property");
        // Java primitive boxes and core types
        s.add("java.lang.Boolean");
        s.add("java.lang.Byte");
        s.add("java.lang.Character");
        s.add("java.lang.Short");
        s.add("java.lang.Integer");
        s.add("java.lang.Long");
        s.add("java.lang.Float");
        s.add("java.lang.Double");
        s.add("java.lang.Number");
        s.add("java.lang.String");
        s.add("java.lang.Enum");
        s.add("java.lang.Object");
        // Common collections used in Attributes.properties and as enclosing types
        s.add("java.util.HashMap");
        s.add("java.util.LinkedHashMap");
        s.add("java.util.TreeMap");
        s.add("java.util.Hashtable");
        s.add("java.util.Properties");
        s.add("java.util.ArrayList");
        s.add("java.util.LinkedList");
        s.add("java.util.HashSet");
        s.add("java.util.LinkedHashSet");
        s.add("java.util.TreeSet");
        // JDK-internal types the deserializer probes for table allocations of
        // the collections above (e.g. HashMap's Node[] allocation goes through
        // the filter with component class java.util.Map$Entry).
        s.add("java.util.Map$Entry");
        s.add("java.util.Map");
        s.add("java.util.AbstractMap");
        s.add("java.util.AbstractMap$SimpleEntry");
        s.add("java.util.AbstractMap$SimpleImmutableEntry");
        s.add("java.util.Comparator");
        s.add("java.io.Serializable");
        // Time zone (defaultTimeZone field on Attributes)
        s.add("java.util.TimeZone");
        s.add("java.util.SimpleTimeZone");
        s.add("sun.util.calendar.ZoneInfo");
        // Date/calendar types occasionally serialized
        s.add("java.util.Date");
        s.add("java.util.Calendar");
        s.add("java.util.GregorianCalendar");
        ALLOWED_CLASSES = Collections.unmodifiableSet(s);
    }

    /**
     * Tracks how many {@link #install} calls are unmatched on the current
     * thread. When {@code > 0} the allowlist is active for that thread.
     */
    private static final ThreadLocal<int[]> ACTIVE_DEPTH = new ThreadLocal<int[]>() {
        @Override protected int[] initialValue() { return new int[1]; }
    };

    private static final PerStreamBridge BRIDGE = createPerStreamBridge();
    /** True if a JVM-wide filter factory was successfully installed at class init. */
    private static final boolean FACTORY_INSTALLED = installFilterFactory();
    private static final AtomicBoolean WARNED_UNAVAILABLE = new AtomicBoolean(false);

    private DicomObjectInputFilter() {}

    /**
     * Touch the class so its static initializer runs eagerly. Called from
     * the static initializers of the dcm4che data classes that act as
     * deserialization sinks; this ensures the JVM-wide filter factory is
     * installed before those classes (or anything that loads them) ever
     * constructs an {@code ObjectInputStream}.
     *
     * @return always {@code true}; the return type makes it usable as the
     *         right-hand side of a {@code static final boolean} field.
     */
    public static boolean touch() {
        return true;
    }

    /**
     * Activate the dcm4che allowlist for subsequent reads on the current
     * thread. Best-effort, idempotent (nesting-safe), and never throws.
     *
     * <p>Pair with {@link #uninstall(ObjectInputStream)} in a {@code
     * try/finally} block. A top-level caller that does not pair the call is
     * safe at runtime — subsequent benign reads on the same thread still
     * succeed for allowlisted classes — but unit tests that toggle thread
     * state should reset between methods.</p>
     */
    public static void install(ObjectInputStream ois) {
        ACTIVE_DEPTH.get()[0]++;
        if (ois != null) {
            try {
                BRIDGE.install(ois);
            } catch (Throwable t) {
                LOG.debug("Per-stream filter install failed on {} — JVM-wide factory still applies",
                        ois.getClass().getName(), t);
            }
        }
        if (!FACTORY_INSTALLED && !BRIDGE.isReal()
                && WARNED_UNAVAILABLE.compareAndSet(false, true)) {
            LOG.warn("Java deserialization filter API unavailable on this JVM; "
                    + "dcm4che cannot enforce a class allowlist. "
                    + "Upgrade to JDK 8u121+ for protection.");
        }
    }

    /**
     * Match a previous {@link #install(ObjectInputStream)} on the same
     * thread. Safe to call without a matching install; underflow is clamped
     * at zero. Never throws.
     */
    public static void uninstall(ObjectInputStream ois) {
        int[] cell = ACTIVE_DEPTH.get();
        if (cell[0] > 0) cell[0]--;
    }

    /** True iff the allowlist is currently active for the calling thread. */
    static boolean isActive() {
        return ACTIVE_DEPTH.get()[0] > 0;
    }

    /** Allowlist membership check; package-private so tests can probe it. */
    static boolean isClassAllowed(Class<?> clazz) {
        if (clazz == null) return true;
        Class<?> c = clazz;
        while (c.isArray()) c = c.getComponentType();
        if (c.isPrimitive()) return true;
        return ALLOWED_CLASSES.contains(c.getName());
    }

    // ------------------------------------------------------------------
    // JVM-wide filter factory (JDK 9+)
    // ------------------------------------------------------------------

    private static boolean installFilterFactory() {
        try {
            Class<?> configCls = Class.forName("java.io.ObjectInputFilter$Config");
            Class<?> filterCls = Class.forName("java.io.ObjectInputFilter");
            Method setFactory;
            try {
                setFactory = configCls.getMethod(
                        "setSerialFilterFactory", java.util.function.BinaryOperator.class);
            } catch (NoSuchMethodException nsme) {
                // JDK 9-16: factory API does not exist; fall back to per-stream install only.
                return false;
            }
            Object factory = newFactoryProxy(filterCls);
            setFactory.invoke(null, factory);
            return true;
        } catch (Throwable t) {
            LOG.debug("Could not install JVM-wide serial filter factory", t);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object newFactoryProxy(final Class<?> filterCls) throws Exception {
        final Class<?> infoCls = Class.forName("java.io.ObjectInputFilter$FilterInfo");
        final Class<?> statusCls = Class.forName("java.io.ObjectInputFilter$Status");
        final Object allowed   = enumConstant(statusCls, "ALLOWED");
        final Object rejected  = enumConstant(statusCls, "REJECTED");
        final Object undecided = enumConstant(statusCls, "UNDECIDED");
        final Method serialClassM = infoCls.getMethod("serialClass");
        final Method arrayLenM    = infoCls.getMethod("arrayLength");
        final Method depthM       = infoCls.getMethod("depth");
        final Method referencesM  = infoCls.getMethod("references");
        final Method checkInputM  = filterCls.getMethod("checkInput", infoCls);

        // factory: (current, next) -> composed filter
        InvocationHandler factoryHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (!"apply".equals(method.getName()) || args == null || args.length != 2) {
                    return null;
                }
                final Object current = args[0];
                final Object next = args[1];
                return Proxy.newProxyInstance(filterCls.getClassLoader(),
                        new Class<?>[] { filterCls },
                        new ComposedFilterHandler(current, next,
                                allowed, rejected, undecided,
                                serialClassM, arrayLenM, depthM, referencesM,
                                checkInputM));
            }
        };
        return Proxy.newProxyInstance(filterCls.getClassLoader(),
                new Class<?>[] { java.util.function.BinaryOperator.class },
                factoryHandler);
    }

    private static final class ComposedFilterHandler implements InvocationHandler {
        private final Object current;
        private final Object next;
        private final Object allowed;
        private final Object rejected;
        private final Object undecided;
        private final Method serialClassM;
        private final Method arrayLenM;
        private final Method depthM;
        private final Method referencesM;
        private final Method checkInputM;

        ComposedFilterHandler(Object current, Object next,
                              Object allowed, Object rejected, Object undecided,
                              Method serialClassM, Method arrayLenM,
                              Method depthM, Method referencesM,
                              Method checkInputM) {
            this.current = current;
            this.next = next;
            this.allowed = allowed;
            this.rejected = rejected;
            this.undecided = undecided;
            this.serialClassM = serialClassM;
            this.arrayLenM = arrayLenM;
            this.depthM = depthM;
            this.referencesM = referencesM;
            this.checkInputM = checkInputM;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String n = method.getName();
            if ("toString".equals(n)) return "DicomObjectInputFilter(factory)";
            if ("equals".equals(n))   return Boolean.valueOf(proxy == args[0]);
            if ("hashCode".equals(n)) return Integer.valueOf(System.identityHashCode(proxy));
            if (!"checkInput".equals(n) || args == null || args.length != 1) {
                return undecided;
            }
            Object info = args[0];
            // 1. Defer to a per-stream filter set by the host application.
            if (next != null) {
                Object s = checkInputM.invoke(next, info);
                if (s == rejected || s == allowed) return s;
            }
            // 2. Defer to the previous global filter, if any.
            if (current != null) {
                Object s = checkInputM.invoke(current, info);
                if (s == rejected || s == allowed) return s;
            }
            // 3. Apply the dcm4che allowlist only when the calling thread is
            //    inside a guarded readObject (or after install() but before
            //    uninstall()).
            if (!isActive()) {
                return undecided;
            }
            long arrayLen = ((Long) arrayLenM.invoke(info)).longValue();
            long depth = ((Long) depthM.invoke(info)).longValue();
            long refs = ((Long) referencesM.invoke(info)).longValue();
            if (arrayLen >= 0 && arrayLen > MAX_ARRAY_LENGTH) return rejected;
            if (depth > MAX_DEPTH) return rejected;
            if (refs > MAX_REFERENCES) return rejected;
            Class<?> cls = (Class<?>) serialClassM.invoke(info);
            if (cls == null) return undecided;
            return isClassAllowed(cls) ? undecided : rejected;
        }
    }

    // ------------------------------------------------------------------
    // Per-stream install bridge (JDK 8u121+ and JDK 9+ fallback)
    // ------------------------------------------------------------------

    private interface PerStreamBridge {
        void install(ObjectInputStream ois) throws Exception;
        boolean isReal();
    }

    private static PerStreamBridge createPerStreamBridge() {
        try {
            return new Jdk9PerStreamBridge();
        } catch (Throwable ignored) {
            // fall through
        }
        try {
            return new Jdk8PerStreamBridge();
        } catch (Throwable ignored) {
            // fall through
        }
        return new NoOpPerStreamBridge();
    }

    private static final class NoOpPerStreamBridge implements PerStreamBridge {
        @Override public void install(ObjectInputStream ois) {}
        @Override public boolean isReal() { return false; }
    }

    /**
     * Installs a per-stream {@code java.io.ObjectInputFilter}. The filter
     * is the same scoped-by-{@link #isActive} allowlist used by the
     * JVM-wide factory; for streams constructed before the factory was
     * installed (or in JDK 9-16, where the factory API does not exist),
     * this is the primary mechanism.
     *
     * <p>JDK 17+ rejects the call once any object has been read from the
     * stream; that failure is caught and logged at DEBUG by
     * {@link #install(ObjectInputStream)}.</p>
     */
    private static final class Jdk9PerStreamBridge implements PerStreamBridge {
        private final Class<?> filterCls;
        private final Class<?> infoCls;
        private final Class<?> statusCls;
        private final Object allowed;
        private final Object rejected;
        private final Object undecided;
        private final Method serialClassM;
        private final Method arrayLenM;
        private final Method depthM;
        private final Method referencesM;
        private final Method checkInputM;
        private final Method setFilterM;
        private final Method getFilterM;

        Jdk9PerStreamBridge() throws Exception {
            this.filterCls    = Class.forName("java.io.ObjectInputFilter");
            this.infoCls      = Class.forName("java.io.ObjectInputFilter$FilterInfo");
            this.statusCls    = Class.forName("java.io.ObjectInputFilter$Status");
            this.allowed      = enumConstant(statusCls, "ALLOWED");
            this.rejected     = enumConstant(statusCls, "REJECTED");
            this.undecided    = enumConstant(statusCls, "UNDECIDED");
            this.serialClassM = infoCls.getMethod("serialClass");
            this.arrayLenM    = infoCls.getMethod("arrayLength");
            this.depthM       = infoCls.getMethod("depth");
            this.referencesM  = infoCls.getMethod("references");
            this.checkInputM  = filterCls.getMethod("checkInput", infoCls);
            this.setFilterM   = ObjectInputStream.class.getMethod(
                    "setObjectInputFilter", filterCls);
            this.getFilterM   = ObjectInputStream.class.getMethod(
                    "getObjectInputFilter");
        }

        @Override
        public void install(ObjectInputStream ois) throws Exception {
            Object existing = getFilterM.invoke(ois);
            if (existing != null) {
                // Either the factory or a host-application filter is already in
                // place; both already incorporate the host filter, so don't try
                // to overwrite. (Overwriting throws "filter can not be set more
                // than once" on every recent JDK.)
                return;
            }
            Object filter = Proxy.newProxyInstance(filterCls.getClassLoader(),
                    new Class<?>[] { filterCls },
                    new ComposedFilterHandler(null, null,
                            allowed, rejected, undecided,
                            serialClassM, arrayLenM, depthM, referencesM,
                            checkInputM));
            setFilterM.invoke(ois, filter);
        }

        @Override public boolean isReal() { return true; }
    }

    /** JDK 8u121 path via {@code sun.misc.ObjectInputFilter$Config}. */
    private static final class Jdk8PerStreamBridge implements PerStreamBridge {
        private final Class<?> filterCls;
        private final Class<?> infoCls;
        private final Class<?> statusCls;
        private final Object allowed;
        private final Object rejected;
        private final Object undecided;
        private final Method serialClassM;
        private final Method arrayLenM;
        private final Method depthM;
        private final Method referencesM;
        private final Method checkInputM;
        private final Method setFilterM;
        private final Method getFilterM;

        Jdk8PerStreamBridge() throws Exception {
            this.filterCls    = Class.forName("sun.misc.ObjectInputFilter");
            this.infoCls      = Class.forName("sun.misc.ObjectInputFilter$FilterInfo");
            this.statusCls    = Class.forName("sun.misc.ObjectInputFilter$Status");
            this.allowed      = enumConstant(statusCls, "ALLOWED");
            this.rejected     = enumConstant(statusCls, "REJECTED");
            this.undecided    = enumConstant(statusCls, "UNDECIDED");
            this.serialClassM = infoCls.getMethod("serialClass");
            this.arrayLenM    = infoCls.getMethod("arrayLength");
            this.depthM       = infoCls.getMethod("depth");
            this.referencesM  = infoCls.getMethod("references");
            this.checkInputM  = filterCls.getMethod("checkInput", infoCls);
            Class<?> configCls = Class.forName("sun.misc.ObjectInputFilter$Config");
            this.setFilterM = configCls.getMethod(
                    "setObjectInputFilter", ObjectInputStream.class, filterCls);
            this.getFilterM = configCls.getMethod(
                    "getObjectInputFilter", ObjectInputStream.class);
        }

        @Override
        public void install(ObjectInputStream ois) throws Exception {
            Object existing = getFilterM.invoke(null, ois);
            if (existing != null) return;
            Object filter = Proxy.newProxyInstance(filterCls.getClassLoader(),
                    new Class<?>[] { filterCls },
                    new ComposedFilterHandler(null, null,
                            allowed, rejected, undecided,
                            serialClassM, arrayLenM, depthM, referencesM,
                            checkInputM));
            setFilterM.invoke(null, ois, filter);
        }

        @Override public boolean isReal() { return true; }
    }

    // ------------------------------------------------------------------
    // shared helpers
    // ------------------------------------------------------------------

    private static Object enumConstant(Class<?> enumClass, String name) {
        Object[] constants = enumClass.getEnumConstants();
        if (constants != null) {
            for (Object c : constants) {
                if (name.equals(((Enum<?>) c).name())) return c;
            }
        }
        throw new IllegalStateException("No " + enumClass.getName() + "." + name);
    }

    // package-private accessors for tests
    static Set<String> allowedClassNames() {
        return ALLOWED_CLASSES;
    }
}
