package io.sentry;

import io.sentry.buffer.Buffer;
import io.sentry.buffer.DiskBuffer;
import io.sentry.config.Lookup;
import io.sentry.connection.AsyncConnection;
import io.sentry.connection.BufferedConnection;
import io.sentry.connection.Connection;
import io.sentry.connection.HttpConnection;
import io.sentry.connection.NoopConnection;
import io.sentry.connection.OutputStreamConnection;
import io.sentry.connection.ProxyAuthenticator;
import io.sentry.connection.RandomEventSampler;
import io.sentry.context.ContextManager;
import io.sentry.context.ThreadLocalContextManager;
import io.sentry.dsn.Dsn;
import io.sentry.event.interfaces.DebugMetaInterface;
import io.sentry.event.interfaces.ExceptionInterface;
import io.sentry.event.interfaces.HttpInterface;
import io.sentry.event.interfaces.MessageInterface;
import io.sentry.event.interfaces.StackTraceInterface;
import io.sentry.event.interfaces.UserInterface;
import io.sentry.jvmti.FrameCache;
import io.sentry.marshaller.Marshaller;
import io.sentry.marshaller.json.DebugMetaInterfaceBinding;
import io.sentry.marshaller.json.ExceptionInterfaceBinding;
import io.sentry.marshaller.json.HttpInterfaceBinding;
import io.sentry.marshaller.json.JsonMarshaller;
import io.sentry.marshaller.json.MessageInterfaceBinding;
import io.sentry.marshaller.json.StackTraceInterfaceBinding;
import io.sentry.marshaller.json.UserInterfaceBinding;
import io.sentry.util.Util;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSentryClientFactory extends SentryClientFactory {
    public static final String ASYNC_GRACEFUL_SHUTDOWN_OPTION = "async.gracefulshutdown";
    public static final String ASYNC_OPTION = "async";
    public static final String ASYNC_PRIORITY_OPTION = "async.priority";
    public static final String ASYNC_QUEUE_DISCARDNEW = "discardnew";
    public static final String ASYNC_QUEUE_DISCARDOLD = "discardold";
    public static final String ASYNC_QUEUE_OVERFLOW_DEFAULT = "discardold";
    public static final String ASYNC_QUEUE_OVERFLOW_OPTION = "async.queue.overflow";
    public static final String ASYNC_QUEUE_SIZE_OPTION = "async.queuesize";
    public static final String ASYNC_QUEUE_SYNC = "sync";
    public static final long ASYNC_SHUTDOWN_TIMEOUT_DEFAULT = TimeUnit.SECONDS.toMillis(1);
    public static final String ASYNC_SHUTDOWN_TIMEOUT_OPTION = "async.shutdowntimeout";
    public static final String ASYNC_THREADS_OPTION = "async.threads";
    public static final String BUFFER_DIR_OPTION = "buffer.dir";
    public static final boolean BUFFER_ENABLED_DEFAULT = true;
    public static final String BUFFER_ENABLED_OPTION = "buffer.enabled";
    public static final long BUFFER_FLUSHTIME_DEFAULT = 60000;
    public static final String BUFFER_FLUSHTIME_OPTION = "buffer.flushtime";
    public static final String BUFFER_GRACEFUL_SHUTDOWN_OPTION = "buffer.gracefulshutdown";
    public static final long BUFFER_SHUTDOWN_TIMEOUT_DEFAULT = TimeUnit.SECONDS.toMillis(1);
    public static final String BUFFER_SHUTDOWN_TIMEOUT_OPTION = "buffer.shutdowntimeout";
    public static final int BUFFER_SIZE_DEFAULT = 10;
    public static final String BUFFER_SIZE_OPTION = "buffer.size";
    public static final String COMPRESSION_OPTION = "compression";
    public static final String DIST_OPTION = "dist";
    public static final String ENVIRONMENT_OPTION = "environment";
    @Deprecated
    public static final String EXTRATAGS_OPTION = "extratags";
    public static final String EXTRA_OPTION = "extra";
    private static final String FALSE = Boolean.FALSE.toString();
    public static final String HIDE_COMMON_FRAMES_OPTION = "stacktrace.hidecommon";
    public static final String HTTP_PROXY_HOST_OPTION = "http.proxy.host";
    public static final String HTTP_PROXY_PASS_OPTION = "http.proxy.password";
    public static final int HTTP_PROXY_PORT_DEFAULT = 80;
    public static final String HTTP_PROXY_PORT_OPTION = "http.proxy.port";
    public static final String HTTP_PROXY_USER_OPTION = "http.proxy.user";
    public static final String IN_APP_FRAMES_OPTION = "stacktrace.app.packages";
    public static final String MAX_MESSAGE_LENGTH_OPTION = "maxmessagelength";
    public static final String MDCTAGS_OPTION = "mdctags";
    public static final String NAIVE_PROTOCOL = "naive";
    public static final int QUEUE_SIZE_DEFAULT = 50;
    private static final Map<String, RejectedExecutionHandler> REJECT_EXECUTION_HANDLERS = new HashMap();
    public static final String RELEASE_OPTION = "release";
    public static final String SAMPLE_RATE_OPTION = "sample.rate";
    public static final String SERVERNAME_OPTION = "servername";
    public static final String TAGS_OPTION = "tags";
    public static final int TIMEOUT_DEFAULT = ((int) TimeUnit.SECONDS.toMillis(1));
    public static final String TIMEOUT_OPTION = "timeout";
    public static final String UNCAUGHT_HANDLER_ENABLED_OPTION = "uncaught.handler.enabled";
    private static final Logger logger = LoggerFactory.getLogger(DefaultSentryClientFactory.class);

    protected static final class DaemonThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;
        private final int priority;
        private final AtomicInteger threadNumber;

        private DaemonThreadFactory(int i) {
            this.threadNumber = new AtomicInteger(1);
            SecurityManager securityManager = System.getSecurityManager();
            this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sentry-pool-");
            stringBuilder.append(POOL_NUMBER.getAndIncrement());
            stringBuilder.append("-thread-");
            this.namePrefix = stringBuilder.toString();
            this.priority = i;
        }

        public Thread newThread(Runnable runnable) {
            ThreadGroup threadGroup = this.group;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.namePrefix);
            stringBuilder.append(this.threadNumber.getAndIncrement());
            Thread thread = new Thread(threadGroup, runnable, stringBuilder.toString(), 0);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }
            if (thread.getPriority() != this.priority) {
                thread.setPriority(this.priority);
            }
            return thread;
        }
    }

    static {
        REJECT_EXECUTION_HANDLERS.put(ASYNC_QUEUE_SYNC, new CallerRunsPolicy());
        REJECT_EXECUTION_HANDLERS.put(ASYNC_QUEUE_DISCARDNEW, new DiscardPolicy());
        REJECT_EXECUTION_HANDLERS.put("discardold", new DiscardOldestPolicy());
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:4:0x0024 */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public io.sentry.SentryClient createSentryClient(io.sentry.dsn.Dsn r5) {
        /*
        r4 = this;
        r0 = new io.sentry.SentryClient;	 Catch:{ Exception -> 0x0038 }
        r1 = r4.createConnection(r5);	 Catch:{ Exception -> 0x0038 }
        r2 = r4.getContextManager(r5);	 Catch:{ Exception -> 0x0038 }
        r0.<init>(r1, r2);	 Catch:{ Exception -> 0x0038 }
        r1 = "javax.servlet.ServletRequestListener";
        r2 = 0;
        r3 = r4.getClass();	 Catch:{ ClassNotFoundException -> 0x0024 }
        r3 = r3.getClassLoader();	 Catch:{ ClassNotFoundException -> 0x0024 }
        java.lang.Class.forName(r1, r2, r3);	 Catch:{ ClassNotFoundException -> 0x0024 }
        r1 = new io.sentry.event.helper.HttpEventBuilderHelper;	 Catch:{ ClassNotFoundException -> 0x0024 }
        r1.<init>();	 Catch:{ ClassNotFoundException -> 0x0024 }
        r0.addBuilderHelper(r1);	 Catch:{ ClassNotFoundException -> 0x0024 }
        goto L_0x002b;
    L_0x0024:
        r1 = logger;	 Catch:{ Exception -> 0x0038 }
        r2 = "The current environment doesn't provide access to servlets, or provides an unsupported version.";
        r1.debug(r2);	 Catch:{ Exception -> 0x0038 }
    L_0x002b:
        r1 = new io.sentry.event.helper.ContextBuilderHelper;	 Catch:{ Exception -> 0x0038 }
        r1.<init>(r0);	 Catch:{ Exception -> 0x0038 }
        r0.addBuilderHelper(r1);	 Catch:{ Exception -> 0x0038 }
        r5 = r4.configureSentryClient(r0, r5);	 Catch:{ Exception -> 0x0038 }
        return r5;
    L_0x0038:
        r5 = move-exception;
        r0 = logger;
        r1 = "Failed to initialize sentry, falling back to no-op client";
        r0.error(r1, r5);
        r5 = new io.sentry.SentryClient;
        r0 = new io.sentry.connection.NoopConnection;
        r0.<init>();
        r1 = new io.sentry.context.ThreadLocalContextManager;
        r1.<init>();
        r5.<init>(r0, r1);
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.DefaultSentryClientFactory.createSentryClient(io.sentry.dsn.Dsn):io.sentry.SentryClient");
    }

    /* Access modifiers changed, original: protected */
    public SentryClient configureSentryClient(SentryClient sentryClient, Dsn dsn) {
        String release = getRelease(dsn);
        if (release != null) {
            sentryClient.setRelease(release);
        }
        release = getDist(dsn);
        if (release != null) {
            sentryClient.setDist(release);
        }
        release = getEnvironment(dsn);
        if (release != null) {
            sentryClient.setEnvironment(release);
        }
        release = getServerName(dsn);
        if (release != null) {
            sentryClient.setServerName(release);
        }
        Map tags = getTags(dsn);
        if (!tags.isEmpty()) {
            for (Entry entry : tags.entrySet()) {
                sentryClient.addTag((String) entry.getKey(), (String) entry.getValue());
            }
        }
        Set<String> mdcTags = getMdcTags(dsn);
        if (!mdcTags.isEmpty()) {
            for (String addMdcTag : mdcTags) {
                sentryClient.addMdcTag(addMdcTag);
            }
        }
        tags = getExtra(dsn);
        if (!tags.isEmpty()) {
            for (Entry entry2 : tags.entrySet()) {
                sentryClient.addExtra((String) entry2.getKey(), entry2.getValue());
            }
        }
        if (getUncaughtHandlerEnabled(dsn)) {
            sentryClient.setupUncaughtExceptionHandler();
        }
        for (String release2 : getInAppFrames(dsn)) {
            FrameCache.addAppPackage(release2);
        }
        return sentryClient;
    }

    /* Access modifiers changed, original: protected */
    public Connection createConnection(Dsn dsn) {
        Connection createHttpConnection;
        String protocol = dsn.getProtocol();
        if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")) {
            logger.debug("Using an {} connection to Sentry.", protocol.toUpperCase());
            createHttpConnection = createHttpConnection(dsn);
        } else if (protocol.equalsIgnoreCase("out")) {
            logger.debug("Using StdOut to send events.");
            createHttpConnection = createStdOutConnection(dsn);
        } else if (protocol.equalsIgnoreCase("noop")) {
            logger.debug("Using noop to send events.");
            createHttpConnection = new NoopConnection();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Couldn't create a connection for the protocol '");
            stringBuilder.append(protocol);
            stringBuilder.append("'");
            throw new IllegalStateException(stringBuilder.toString());
        }
        Connection connection = createHttpConnection;
        BufferedConnection bufferedConnection = null;
        if (getBufferEnabled(dsn)) {
            Buffer buffer = getBuffer(dsn);
            if (buffer != null) {
                bufferedConnection = new BufferedConnection(connection, buffer, getBufferFlushtime(dsn), getBufferedConnectionGracefulShutdownEnabled(dsn), Long.valueOf(getBufferedConnectionShutdownTimeout(dsn)).longValue());
                connection = bufferedConnection;
            }
        }
        if (getAsyncEnabled(dsn)) {
            connection = createAsyncConnection(dsn, connection);
        }
        return bufferedConnection != null ? bufferedConnection.wrapConnectionWithBufferWriter(connection) : connection;
    }

    /* Access modifiers changed, original: protected */
    public Connection createAsyncConnection(Dsn dsn, Connection connection) {
        BlockingQueue linkedBlockingDeque;
        int asyncThreads = getAsyncThreads(dsn);
        int asyncPriority = getAsyncPriority(dsn);
        int asyncQueueSize = getAsyncQueueSize(dsn);
        if (asyncQueueSize == -1) {
            linkedBlockingDeque = new LinkedBlockingDeque();
        } else {
            linkedBlockingDeque = new LinkedBlockingDeque(asyncQueueSize);
        }
        return new AsyncConnection(connection, new ThreadPoolExecutor(asyncThreads, asyncThreads, 0, TimeUnit.MILLISECONDS, linkedBlockingDeque, new DaemonThreadFactory(asyncPriority), getRejectedExecutionHandler(dsn)), getAsyncGracefulShutdownEnabled(dsn), getAsyncShutdownTimeout(dsn));
    }

    /* Access modifiers changed, original: protected */
    public Connection createHttpConnection(Dsn dsn) {
        Proxy proxy;
        URL sentryApiUrl = HttpConnection.getSentryApiUrl(dsn.getUri(), dsn.getProjectId());
        String proxyHost = getProxyHost(dsn);
        String proxyUser = getProxyUser(dsn);
        String proxyPass = getProxyPass(dsn);
        int proxyPort = getProxyPort(dsn);
        if (proxyHost != null) {
            Proxy proxy2 = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            if (!(proxyUser == null || proxyPass == null)) {
                Authenticator.setDefault(new ProxyAuthenticator(proxyUser, proxyPass));
            }
            proxy = proxy2;
        } else {
            proxy = null;
        }
        Double sampleRate = getSampleRate(dsn);
        HttpConnection httpConnection = new HttpConnection(sentryApiUrl, dsn.getPublicKey(), dsn.getSecretKey(), proxy, sampleRate != null ? new RandomEventSampler(sampleRate.doubleValue()) : null);
        httpConnection.setMarshaller(createMarshaller(dsn));
        httpConnection.setConnectionTimeout(getTimeout(dsn));
        httpConnection.setBypassSecurity(getBypassSecurityEnabled(dsn));
        return httpConnection;
    }

    /* Access modifiers changed, original: protected */
    public Connection createStdOutConnection(Dsn dsn) {
        OutputStreamConnection outputStreamConnection = new OutputStreamConnection(System.out);
        outputStreamConnection.setMarshaller(createMarshaller(dsn));
        return outputStreamConnection;
    }

    /* Access modifiers changed, original: protected */
    public Marshaller createMarshaller(Dsn dsn) {
        int maxMessageLength = getMaxMessageLength(dsn);
        JsonMarshaller createJsonMarshaller = createJsonMarshaller(maxMessageLength);
        StackTraceInterfaceBinding stackTraceInterfaceBinding = new StackTraceInterfaceBinding();
        stackTraceInterfaceBinding.setRemoveCommonFramesWithEnclosing(getHideCommonFramesEnabled(dsn));
        stackTraceInterfaceBinding.setInAppFrames(getInAppFrames(dsn));
        createJsonMarshaller.addInterfaceBinding(StackTraceInterface.class, stackTraceInterfaceBinding);
        createJsonMarshaller.addInterfaceBinding(ExceptionInterface.class, new ExceptionInterfaceBinding(stackTraceInterfaceBinding));
        createJsonMarshaller.addInterfaceBinding(MessageInterface.class, new MessageInterfaceBinding(maxMessageLength));
        createJsonMarshaller.addInterfaceBinding(UserInterface.class, new UserInterfaceBinding());
        createJsonMarshaller.addInterfaceBinding(DebugMetaInterface.class, new DebugMetaInterfaceBinding());
        createJsonMarshaller.addInterfaceBinding(HttpInterface.class, new HttpInterfaceBinding());
        createJsonMarshaller.setCompression(getCompressionEnabled(dsn));
        return createJsonMarshaller;
    }

    /* Access modifiers changed, original: protected */
    public JsonMarshaller createJsonMarshaller(int i) {
        return new JsonMarshaller(i);
    }

    /* Access modifiers changed, original: protected */
    public ContextManager getContextManager(Dsn dsn) {
        return new ThreadLocalContextManager();
    }

    /* Access modifiers changed, original: protected */
    public Collection<String> getInAppFrames(Dsn dsn) {
        String lookup = Lookup.lookup(IN_APP_FRAMES_OPTION, dsn);
        if (Util.isNullOrEmpty(lookup)) {
            if (lookup == null) {
                logger.warn("No 'stacktrace.app.packages' was configured, this option is highly recommended as it affects stacktrace grouping and display on Sentry. See documentation: https://docs.sentry.io/clients/java/config/#in-application-stack-frames");
            }
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (String str : lookup.split(",")) {
            if (!str.trim().equals("")) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    /* Access modifiers changed, original: protected */
    public boolean getAsyncEnabled(Dsn dsn) {
        return FALSE.equalsIgnoreCase(Lookup.lookup(ASYNC_OPTION, dsn)) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public RejectedExecutionHandler getRejectedExecutionHandler(Dsn dsn) {
        Object obj = "discardold";
        String lookup = Lookup.lookup(ASYNC_QUEUE_OVERFLOW_OPTION, dsn);
        if (!Util.isNullOrEmpty(lookup)) {
            obj = lookup.toLowerCase();
        }
        RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler) REJECT_EXECUTION_HANDLERS.get(obj);
        if (rejectedExecutionHandler != null) {
            return rejectedExecutionHandler;
        }
        lookup = Arrays.toString(REJECT_EXECUTION_HANDLERS.keySet().toArray());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("RejectedExecutionHandler not found: '");
        stringBuilder.append(obj);
        stringBuilder.append("', valid choices are: ");
        stringBuilder.append(lookup);
        throw new RuntimeException(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public long getBufferedConnectionShutdownTimeout(Dsn dsn) {
        return Util.parseLong(Lookup.lookup(BUFFER_SHUTDOWN_TIMEOUT_OPTION, dsn), Long.valueOf(BUFFER_SHUTDOWN_TIMEOUT_DEFAULT)).longValue();
    }

    /* Access modifiers changed, original: protected */
    public boolean getBufferedConnectionGracefulShutdownEnabled(Dsn dsn) {
        return FALSE.equalsIgnoreCase(Lookup.lookup(BUFFER_GRACEFUL_SHUTDOWN_OPTION, dsn)) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public long getBufferFlushtime(Dsn dsn) {
        return Util.parseLong(Lookup.lookup(BUFFER_FLUSHTIME_OPTION, dsn), Long.valueOf(BUFFER_FLUSHTIME_DEFAULT)).longValue();
    }

    /* Access modifiers changed, original: protected */
    public long getAsyncShutdownTimeout(Dsn dsn) {
        return Util.parseLong(Lookup.lookup(ASYNC_SHUTDOWN_TIMEOUT_OPTION, dsn), Long.valueOf(ASYNC_SHUTDOWN_TIMEOUT_DEFAULT)).longValue();
    }

    /* Access modifiers changed, original: protected */
    public boolean getAsyncGracefulShutdownEnabled(Dsn dsn) {
        return FALSE.equalsIgnoreCase(Lookup.lookup(ASYNC_GRACEFUL_SHUTDOWN_OPTION, dsn)) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public int getAsyncQueueSize(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(ASYNC_QUEUE_SIZE_OPTION, dsn), Integer.valueOf(50)).intValue();
    }

    /* Access modifiers changed, original: protected */
    public int getAsyncPriority(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(ASYNC_PRIORITY_OPTION, dsn), Integer.valueOf(1)).intValue();
    }

    /* Access modifiers changed, original: protected */
    public int getAsyncThreads(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(ASYNC_THREADS_OPTION, dsn), Integer.valueOf(Runtime.getRuntime().availableProcessors())).intValue();
    }

    /* Access modifiers changed, original: protected */
    public boolean getBypassSecurityEnabled(Dsn dsn) {
        return dsn.getProtocolSettings().contains(NAIVE_PROTOCOL);
    }

    /* Access modifiers changed, original: protected */
    public Double getSampleRate(Dsn dsn) {
        return Util.parseDouble(Lookup.lookup(SAMPLE_RATE_OPTION, dsn), null);
    }

    /* Access modifiers changed, original: protected */
    public int getProxyPort(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(HTTP_PROXY_PORT_OPTION, dsn), Integer.valueOf(80)).intValue();
    }

    /* Access modifiers changed, original: protected */
    public String getProxyHost(Dsn dsn) {
        return Lookup.lookup(HTTP_PROXY_HOST_OPTION, dsn);
    }

    /* Access modifiers changed, original: protected */
    public String getProxyUser(Dsn dsn) {
        return Lookup.lookup(HTTP_PROXY_USER_OPTION, dsn);
    }

    /* Access modifiers changed, original: protected */
    public String getProxyPass(Dsn dsn) {
        return Lookup.lookup(HTTP_PROXY_PASS_OPTION, dsn);
    }

    /* Access modifiers changed, original: protected */
    public String getRelease(Dsn dsn) {
        return Lookup.lookup("release", dsn);
    }

    /* Access modifiers changed, original: protected */
    public String getDist(Dsn dsn) {
        return Lookup.lookup("dist", dsn);
    }

    /* Access modifiers changed, original: protected */
    public String getEnvironment(Dsn dsn) {
        return Lookup.lookup("environment", dsn);
    }

    /* Access modifiers changed, original: protected */
    public String getServerName(Dsn dsn) {
        return Lookup.lookup(SERVERNAME_OPTION, dsn);
    }

    /* Access modifiers changed, original: protected */
    public Map<String, String> getTags(Dsn dsn) {
        return Util.parseTags(Lookup.lookup("tags", dsn));
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public Set<String> getExtraTags(Dsn dsn) {
        return getMdcTags(dsn);
    }

    /* Access modifiers changed, original: protected */
    public Set<String> getMdcTags(Dsn dsn) {
        String lookup = Lookup.lookup(MDCTAGS_OPTION, dsn);
        if (Util.isNullOrEmpty(lookup)) {
            lookup = Lookup.lookup(EXTRATAGS_OPTION, dsn);
            if (!Util.isNullOrEmpty(lookup)) {
                logger.warn("The 'extratags' option is deprecated, please use the 'mdctags' option instead.");
            }
        }
        return Util.parseMdcTags(lookup);
    }

    /* Access modifiers changed, original: protected */
    public Map<String, String> getExtra(Dsn dsn) {
        return Util.parseExtra(Lookup.lookup("extra", dsn));
    }

    /* Access modifiers changed, original: protected */
    public boolean getCompressionEnabled(Dsn dsn) {
        return FALSE.equalsIgnoreCase(Lookup.lookup(COMPRESSION_OPTION, dsn)) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public boolean getHideCommonFramesEnabled(Dsn dsn) {
        return FALSE.equalsIgnoreCase(Lookup.lookup(HIDE_COMMON_FRAMES_OPTION, dsn)) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public int getMaxMessageLength(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(MAX_MESSAGE_LENGTH_OPTION, dsn), Integer.valueOf(1000)).intValue();
    }

    /* Access modifiers changed, original: protected */
    public int getTimeout(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(TIMEOUT_OPTION, dsn), Integer.valueOf(TIMEOUT_DEFAULT)).intValue();
    }

    /* Access modifiers changed, original: protected */
    public boolean getBufferEnabled(Dsn dsn) {
        String lookup = Lookup.lookup(BUFFER_ENABLED_OPTION, dsn);
        return lookup != null ? Boolean.parseBoolean(lookup) : true;
    }

    /* Access modifiers changed, original: protected */
    public Buffer getBuffer(Dsn dsn) {
        String lookup = Lookup.lookup(BUFFER_DIR_OPTION, dsn);
        return lookup != null ? new DiskBuffer(new File(lookup), getBufferSize(dsn)) : null;
    }

    /* Access modifiers changed, original: protected */
    public int getBufferSize(Dsn dsn) {
        return Util.parseInteger(Lookup.lookup(BUFFER_SIZE_OPTION, dsn), Integer.valueOf(10)).intValue();
    }

    /* Access modifiers changed, original: protected */
    public boolean getUncaughtHandlerEnabled(Dsn dsn) {
        return FALSE.equalsIgnoreCase(Lookup.lookup(UNCAUGHT_HANDLER_ENABLED_OPTION, dsn)) ^ 1;
    }
}
