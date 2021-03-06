package dagger.android;

final class AutoAnnotation_ReleaseReferencesAtCreator_createReleaseReferencesAt implements ReleaseReferencesAt {
    private final int value;

    AutoAnnotation_ReleaseReferencesAtCreator_createReleaseReferencesAt(int i) {
        this.value = i;
    }

    public Class<? extends ReleaseReferencesAt> annotationType() {
        return ReleaseReferencesAt.class;
    }

    public int value() {
        return this.value;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("@dagger.android.ReleaseReferencesAt(");
        stringBuilder.append(this.value);
        stringBuilder.append(')');
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ReleaseReferencesAt)) {
            return false;
        }
        if (this.value != ((ReleaseReferencesAt) obj).value()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.value ^ 1335633679;
    }
}
