package io.sentry.connection;

import com.bumptech.glide.load.Key;
import io.sentry.event.Event;
import io.sentry.marshaller.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class OutputStreamConnection extends AbstractConnection {
    private static final Charset UTF_8 = Charset.forName(Key.STRING_CHARSET_NAME);
    private Marshaller marshaller;
    private final OutputStream outputStream;

    public OutputStreamConnection(OutputStream outputStream) {
        super(null, null);
        this.outputStream = outputStream;
    }

    /* Access modifiers changed, original: protected|declared_synchronized */
    public synchronized void doSend(Event event) throws ConnectionException {
        try {
            this.outputStream.write("Sentry event:\n".getBytes(UTF_8));
            this.marshaller.marshall(event, this.outputStream);
            this.outputStream.write("\n".getBytes(UTF_8));
            this.outputStream.flush();
        } catch (IOException e) {
            throw new ConnectionException("Couldn't sent the event properly", e);
        }
    }

    public void close() throws IOException {
        this.outputStream.close();
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }
}
