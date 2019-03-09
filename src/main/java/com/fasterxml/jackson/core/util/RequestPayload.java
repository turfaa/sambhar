package com.fasterxml.jackson.core.util;

import com.bumptech.glide.load.Key;
import java.io.IOException;
import java.io.Serializable;

public class RequestPayload implements Serializable {
    private static final long serialVersionUID = 1;
    protected String _charset;
    protected byte[] _payloadAsBytes;
    protected CharSequence _payloadAsText;

    public RequestPayload(byte[] bArr, String str) {
        if (bArr != null) {
            this._payloadAsBytes = bArr;
            if (str == null || str.isEmpty()) {
                str = Key.STRING_CHARSET_NAME;
            }
            this._charset = str;
            return;
        }
        throw new IllegalArgumentException();
    }

    public RequestPayload(CharSequence charSequence) {
        if (charSequence != null) {
            this._payloadAsText = charSequence;
            return;
        }
        throw new IllegalArgumentException();
    }

    public Object getRawPayload() {
        if (this._payloadAsBytes != null) {
            return this._payloadAsBytes;
        }
        return this._payloadAsText;
    }

    public String toString() {
        if (this._payloadAsBytes == null) {
            return this._payloadAsText.toString();
        }
        try {
            return new String(this._payloadAsBytes, this._charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
