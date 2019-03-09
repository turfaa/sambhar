package io.sentry.dsn;

import com.bumptech.glide.load.Key;
import io.sentry.config.Lookup;
import io.sentry.util.Util;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dsn {
    public static final String DEFAULT_DSN = "noop://localhost?async=false";
    private static final Logger logger = LoggerFactory.getLogger(Dsn.class);
    private String host;
    private Map<String, String> options;
    private String path;
    private int port;
    private String projectId;
    private String protocol;
    private Set<String> protocolSettings;
    private String publicKey;
    private String secretKey;
    private URI uri;

    public Dsn(String str) throws InvalidDsnException {
        this(URI.create(str));
    }

    public Dsn(URI uri) throws InvalidDsnException {
        if (uri != null) {
            this.options = new HashMap();
            this.protocolSettings = new HashSet();
            extractProtocolInfo(uri);
            extractUserKeys(uri);
            extractHostInfo(uri);
            extractPathInfo(uri);
            extractOptions(uri);
            makeOptionsImmutable();
            validate();
            try {
                this.uri = new URI(this.protocol, null, this.host, this.port, this.path, null, null);
                return;
            } catch (URISyntaxException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Impossible to determine Sentry's URI from the DSN '");
                stringBuilder.append(uri);
                stringBuilder.append("'");
                throw new InvalidDsnException(stringBuilder.toString(), e);
            }
        }
        throw new InvalidDsnException("DSN constructed with null value!");
    }

    public static String dsnLookup() {
        String lookup = Lookup.lookup("dsn");
        if (Util.isNullOrEmpty(lookup)) {
            lookup = Lookup.lookup("dns");
        }
        if (!Util.isNullOrEmpty(lookup)) {
            return lookup;
        }
        logger.warn("*** Couldn't find a suitable DSN, Sentry operations will do nothing! See documentation: https://docs.sentry.io/clients/java/ ***");
        return DEFAULT_DSN;
    }

    private void extractPathInfo(URI uri) {
        String path = uri.getPath();
        if (path != null) {
            int lastIndexOf = path.lastIndexOf("/") + 1;
            this.path = path.substring(0, lastIndexOf);
            this.projectId = path.substring(lastIndexOf);
        }
    }

    private void extractHostInfo(URI uri) {
        this.host = uri.getHost();
        this.port = uri.getPort();
    }

    private void extractProtocolInfo(URI uri) {
        String scheme = uri.getScheme();
        if (scheme != null) {
            String[] split = scheme.split("\\+");
            this.protocolSettings.addAll(Arrays.asList(split).subList(0, split.length - 1));
            this.protocol = split[split.length - 1];
        }
    }

    private void extractUserKeys(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            String[] split = userInfo.split(":");
            this.publicKey = split[0];
            if (split.length > 1) {
                this.secretKey = split[1];
            }
        }
    }

    private void extractOptions(URI uri) {
        String query = uri.getQuery();
        if (query != null && !query.isEmpty()) {
            String[] split = query.split("&");
            int length = split.length;
            int i = 0;
            while (i < length) {
                String str = split[i];
                try {
                    String[] split2 = str.split("=");
                    this.options.put(URLDecoder.decode(split2[0], Key.STRING_CHARSET_NAME), split2.length > 1 ? URLDecoder.decode(split2[1], Key.STRING_CHARSET_NAME) : null);
                    i++;
                } catch (UnsupportedEncodingException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Impossible to decode the query parameter '");
                    stringBuilder.append(str);
                    stringBuilder.append("'");
                    throw new IllegalArgumentException(stringBuilder.toString(), e);
                }
            }
        }
    }

    private void makeOptionsImmutable() {
        this.options = Collections.unmodifiableMap(this.options);
        this.protocolSettings = Collections.unmodifiableSet(this.protocolSettings);
    }

    private void validate() {
        LinkedList linkedList = new LinkedList();
        if (this.host == null) {
            linkedList.add("host");
        }
        if (!(this.protocol == null || this.protocol.equalsIgnoreCase("noop") || this.protocol.equalsIgnoreCase("out"))) {
            if (this.publicKey == null) {
                linkedList.add("public key");
            }
            if (this.projectId == null || this.projectId.isEmpty()) {
                linkedList.add("project ID");
            }
        }
        if (!linkedList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid DSN, the following properties aren't set '");
            stringBuilder.append(linkedList);
            stringBuilder.append("'");
            throw new InvalidDsnException(stringBuilder.toString());
        }
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPath() {
        return this.path;
    }

    public Set<String> getProtocolSettings() {
        return this.protocolSettings;
    }

    public Map<String, String> getOptions() {
        return this.options;
    }

    public URI getUri() {
        return this.uri;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Dsn dsn = (Dsn) obj;
        if (this.port != dsn.port || !this.host.equals(dsn.host) || !this.options.equals(dsn.options) || !this.path.equals(dsn.path) || !this.projectId.equals(dsn.projectId)) {
            return false;
        }
        if (this.protocol == null ? dsn.protocol == null : this.protocol.equals(dsn.protocol)) {
            return this.protocolSettings.equals(dsn.protocolSettings) && this.publicKey.equals(dsn.publicKey) && this.secretKey.equals(dsn.secretKey);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (((((((this.publicKey.hashCode() * 31) + this.projectId.hashCode()) * 31) + this.host.hashCode()) * 31) + this.port) * 31) + this.path.hashCode();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dsn{uri=");
        stringBuilder.append(this.uri);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
