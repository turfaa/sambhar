package com.bumptech.glide.load.engine.prefill;

import android.graphics.Bitmap.Config;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.bumptech.glide.util.Preconditions;

public final class PreFillType {
    @VisibleForTesting
    static final Config DEFAULT_CONFIG = Config.RGB_565;
    private final Config config;
    private final int height;
    private final int weight;
    private final int width;

    public static class Builder {
        private Config config;
        private final int height;
        private int weight;
        private final int width;

        public Builder(int i) {
            this(i, i);
        }

        public Builder(int i, int i2) {
            this.weight = 1;
            if (i <= 0) {
                throw new IllegalArgumentException("Width must be > 0");
            } else if (i2 > 0) {
                this.width = i;
                this.height = i2;
            } else {
                throw new IllegalArgumentException("Height must be > 0");
            }
        }

        public Builder setConfig(@Nullable Config config) {
            this.config = config;
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public Config getConfig() {
            return this.config;
        }

        public Builder setWeight(int i) {
            if (i > 0) {
                this.weight = i;
                return this;
            }
            throw new IllegalArgumentException("Weight must be > 0");
        }

        /* Access modifiers changed, original: 0000 */
        public PreFillType build() {
            return new PreFillType(this.width, this.height, this.config, this.weight);
        }
    }

    PreFillType(int i, int i2, Config config, int i3) {
        this.config = (Config) Preconditions.checkNotNull(config, "Config must not be null");
        this.width = i;
        this.height = i2;
        this.weight = i3;
    }

    /* Access modifiers changed, original: 0000 */
    public int getWidth() {
        return this.width;
    }

    /* Access modifiers changed, original: 0000 */
    public int getHeight() {
        return this.height;
    }

    /* Access modifiers changed, original: 0000 */
    public Config getConfig() {
        return this.config;
    }

    /* Access modifiers changed, original: 0000 */
    public int getWeight() {
        return this.weight;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof PreFillType)) {
            return false;
        }
        PreFillType preFillType = (PreFillType) obj;
        if (this.height == preFillType.height && this.width == preFillType.width && this.weight == preFillType.weight && this.config == preFillType.config) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (((((this.width * 31) + this.height) * 31) + this.config.hashCode()) * 31) + this.weight;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PreFillSize{width=");
        stringBuilder.append(this.width);
        stringBuilder.append(", height=");
        stringBuilder.append(this.height);
        stringBuilder.append(", config=");
        stringBuilder.append(this.config);
        stringBuilder.append(", weight=");
        stringBuilder.append(this.weight);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
