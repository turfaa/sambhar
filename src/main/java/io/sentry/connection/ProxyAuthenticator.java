package io.sentry.connection;

import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.PasswordAuthentication;

public class ProxyAuthenticator extends Authenticator {
    private String pass;
    private String user;

    public ProxyAuthenticator(String str, String str2) {
        this.user = str;
        this.pass = str2;
    }

    /* Access modifiers changed, original: protected */
    public PasswordAuthentication getPasswordAuthentication() {
        return getRequestorType() == RequestorType.PROXY ? new PasswordAuthentication(this.user, this.pass.toCharArray()) : null;
    }
}
