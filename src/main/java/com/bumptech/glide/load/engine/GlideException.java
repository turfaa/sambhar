package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GlideException extends Exception {
    private static final StackTraceElement[] EMPTY_ELEMENTS = new StackTraceElement[0];
    private static final long serialVersionUID = 1;
    private final List<Throwable> causes;
    private Class<?> dataClass;
    private DataSource dataSource;
    private String detailMessage;
    private Key key;

    private static final class IndentedAppendable implements Appendable {
        private static final String EMPTY_SEQUENCE = "";
        private static final String INDENT = "  ";
        private final Appendable appendable;
        private boolean printedNewLine = true;

        @NonNull
        private CharSequence safeSequence(@Nullable CharSequence charSequence) {
            return charSequence == null ? "" : charSequence;
        }

        IndentedAppendable(Appendable appendable) {
            this.appendable = appendable;
        }

        public Appendable append(char c) throws IOException {
            boolean z = false;
            if (this.printedNewLine) {
                this.printedNewLine = false;
                this.appendable.append(INDENT);
            }
            if (c == 10) {
                z = true;
            }
            this.printedNewLine = z;
            this.appendable.append(c);
            return this;
        }

        public Appendable append(@Nullable CharSequence charSequence) throws IOException {
            charSequence = safeSequence(charSequence);
            return append(charSequence, 0, charSequence.length());
        }

        public Appendable append(@Nullable CharSequence charSequence, int i, int i2) throws IOException {
            charSequence = safeSequence(charSequence);
            boolean z = false;
            if (this.printedNewLine) {
                this.printedNewLine = false;
                this.appendable.append(INDENT);
            }
            if (charSequence.length() > 0 && charSequence.charAt(i2 - 1) == 10) {
                z = true;
            }
            this.printedNewLine = z;
            this.appendable.append(charSequence, i, i2);
            return this;
        }
    }

    public Throwable fillInStackTrace() {
        return this;
    }

    public GlideException(String str) {
        this(str, Collections.emptyList());
    }

    public GlideException(String str, Throwable th) {
        this(str, Collections.singletonList(th));
    }

    public GlideException(String str, List<Throwable> list) {
        this.detailMessage = str;
        setStackTrace(EMPTY_ELEMENTS);
        this.causes = list;
    }

    /* Access modifiers changed, original: 0000 */
    public void setLoggingDetails(Key key, DataSource dataSource) {
        setLoggingDetails(key, dataSource, null);
    }

    /* Access modifiers changed, original: 0000 */
    public void setLoggingDetails(Key key, DataSource dataSource, Class<?> cls) {
        this.key = key;
        this.dataSource = dataSource;
        this.dataClass = cls;
    }

    public List<Throwable> getCauses() {
        return this.causes;
    }

    public List<Throwable> getRootCauses() {
        ArrayList arrayList = new ArrayList();
        addRootCauses(this, arrayList);
        return arrayList;
    }

    public void logRootCauses(String str) {
        List rootCauses = getRootCauses();
        int size = rootCauses.size();
        int i = 0;
        while (i < size) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Root cause (");
            int i2 = i + 1;
            stringBuilder.append(i2);
            stringBuilder.append(" of ");
            stringBuilder.append(size);
            stringBuilder.append(")");
            Log.i(str, stringBuilder.toString(), (Throwable) rootCauses.get(i));
            i = i2;
        }
    }

    private void addRootCauses(Throwable th, List<Throwable> list) {
        if (th instanceof GlideException) {
            for (Throwable addRootCauses : ((GlideException) th).getCauses()) {
                addRootCauses(addRootCauses, list);
            }
            return;
        }
        list.add(th);
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream printStream) {
        printStackTrace((Appendable) printStream);
    }

    public void printStackTrace(PrintWriter printWriter) {
        printStackTrace((Appendable) printWriter);
    }

    private void printStackTrace(Appendable appendable) {
        appendExceptionMessage(this, appendable);
        appendCauses(getCauses(), new IndentedAppendable(appendable));
    }

    public String getMessage() {
        StringBuilder stringBuilder;
        String stringBuilder2;
        StringBuilder stringBuilder3 = new StringBuilder(71);
        stringBuilder3.append(this.detailMessage);
        if (this.dataClass != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(", ");
            stringBuilder.append(this.dataClass);
            stringBuilder2 = stringBuilder.toString();
        } else {
            stringBuilder2 = "";
        }
        stringBuilder3.append(stringBuilder2);
        if (this.dataSource != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(", ");
            stringBuilder.append(this.dataSource);
            stringBuilder2 = stringBuilder.toString();
        } else {
            stringBuilder2 = "";
        }
        stringBuilder3.append(stringBuilder2);
        if (this.key != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(", ");
            stringBuilder.append(this.key);
            stringBuilder2 = stringBuilder.toString();
        } else {
            stringBuilder2 = "";
        }
        stringBuilder3.append(stringBuilder2);
        List<Throwable> rootCauses = getRootCauses();
        if (rootCauses.isEmpty()) {
            return stringBuilder3.toString();
        }
        if (rootCauses.size() == 1) {
            stringBuilder3.append("\nThere was 1 cause:");
        } else {
            stringBuilder3.append("\nThere were ");
            stringBuilder3.append(rootCauses.size());
            stringBuilder3.append(" causes:");
        }
        for (Throwable th : rootCauses) {
            stringBuilder3.append(10);
            stringBuilder3.append(th.getClass().getName());
            stringBuilder3.append('(');
            stringBuilder3.append(th.getMessage());
            stringBuilder3.append(')');
        }
        stringBuilder3.append("\n call GlideException#logRootCauses(String) for more detail");
        return stringBuilder3.toString();
    }

    private static void appendExceptionMessage(Throwable th, Appendable appendable) {
        try {
            appendable.append(th.getClass().toString()).append(": ").append(th.getMessage()).append(10);
        } catch (IOException unused) {
            throw new RuntimeException(th);
        }
    }

    private static void appendCauses(List<Throwable> list, Appendable appendable) {
        try {
            appendCausesWrapped(list, appendable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void appendCausesWrapped(List<Throwable> list, Appendable appendable) throws IOException {
        int size = list.size();
        int i = 0;
        while (i < size) {
            int i2 = i + 1;
            appendable.append("Cause (").append(String.valueOf(i2)).append(" of ").append(String.valueOf(size)).append("): ");
            Throwable th = (Throwable) list.get(i);
            if (th instanceof GlideException) {
                ((GlideException) th).printStackTrace(appendable);
            } else {
                appendExceptionMessage(th, appendable);
            }
            i = i2;
        }
    }
}
