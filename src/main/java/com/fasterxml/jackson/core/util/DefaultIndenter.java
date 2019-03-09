package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.NopIndenter;
import java.io.IOException;

public class DefaultIndenter extends NopIndenter {
    private static final int INDENT_LEVELS = 16;
    public static final DefaultIndenter SYSTEM_LINEFEED_INSTANCE = new DefaultIndenter("  ", SYS_LF);
    public static final String SYS_LF;
    private static final long serialVersionUID = 1;
    private final int charsPerLevel;
    private final String eol;
    private final char[] indents;

    public boolean isInline() {
        return false;
    }

    static {
        String property;
        try {
            property = System.getProperty("line.separator");
        } catch (Throwable unused) {
            property = "\n";
        }
        SYS_LF = property;
    }

    public DefaultIndenter() {
        this("  ", SYS_LF);
    }

    public DefaultIndenter(String str, String str2) {
        this.charsPerLevel = str.length();
        this.indents = new char[(str.length() * 16)];
        int i = 0;
        for (int i2 = 0; i2 < 16; i2++) {
            str.getChars(0, str.length(), this.indents, i);
            i += str.length();
        }
        this.eol = str2;
    }

    public DefaultIndenter withLinefeed(String str) {
        if (str.equals(this.eol)) {
            return this;
        }
        return new DefaultIndenter(getIndent(), str);
    }

    public DefaultIndenter withIndent(String str) {
        if (str.equals(getIndent())) {
            return this;
        }
        return new DefaultIndenter(str, this.eol);
    }

    public void writeIndentation(JsonGenerator jsonGenerator, int i) throws IOException {
        jsonGenerator.writeRaw(this.eol);
        if (i > 0) {
            i *= this.charsPerLevel;
            while (i > this.indents.length) {
                jsonGenerator.writeRaw(this.indents, 0, this.indents.length);
                i -= this.indents.length;
            }
            jsonGenerator.writeRaw(this.indents, 0, i);
        }
    }

    public String getEol() {
        return this.eol;
    }

    public String getIndent() {
        return new String(this.indents, 0, this.charsPerLevel);
    }
}
