package com.google.zxing.datamatrix.encoder;

import android.support.v4.view.InputDeviceCompat;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;

final class Base256Encoder implements Encoder {
    public int getEncodingMode() {
        return 5;
    }

    Base256Encoder() {
    }

    public void encode(EncoderContext encoderContext) {
        int lookAheadTest;
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append(0);
        while (encoderContext.hasMoreCharacters()) {
            stringBuilder.append(encoderContext.getCurrentChar());
            encoderContext.pos++;
            lookAheadTest = HighLevelEncoder.lookAheadTest(encoderContext.getMessage(), encoderContext.pos, getEncodingMode());
            if (lookAheadTest != getEncodingMode()) {
                encoderContext.signalEncoderChange(lookAheadTest);
                break;
            }
        }
        lookAheadTest = stringBuilder.length() - 1;
        int codewordCount = (encoderContext.getCodewordCount() + lookAheadTest) + 1;
        encoderContext.updateSymbolInfo(codewordCount);
        Object obj = encoderContext.getSymbolInfo().getDataCapacity() - codewordCount > 0 ? 1 : null;
        if (encoderContext.hasMoreCharacters() || obj != null) {
            if (lookAheadTest <= 249) {
                stringBuilder.setCharAt(0, (char) lookAheadTest);
            } else if (lookAheadTest <= 1555) {
                stringBuilder.setCharAt(0, (char) ((lookAheadTest / Callback.DEFAULT_SWIPE_ANIMATION_DURATION) + 249));
                stringBuilder.insert(1, (char) (lookAheadTest % Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
            } else {
                stringBuilder = new StringBuilder("Message length not in valid ranges: ");
                stringBuilder.append(lookAheadTest);
                throw new IllegalStateException(stringBuilder.toString());
            }
        }
        lookAheadTest = stringBuilder.length();
        while (i < lookAheadTest) {
            encoderContext.writeCodeword(randomize255State(stringBuilder.charAt(i), encoderContext.getCodewordCount() + 1));
            i++;
        }
    }

    private static char randomize255State(char c, int i) {
        int i2 = c + (((i * 149) % 255) + 1);
        return i2 <= 255 ? (char) i2 : (char) (i2 + InputDeviceCompat.SOURCE_ANY);
    }
}
