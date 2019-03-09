package dagger.android;

import dagger.internal.GwtIncompatible;
import dagger.releasablereferences.CanReleaseReferences;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.ANNOTATION_TYPE})
@GwtIncompatible
@CanReleaseReferences
public @interface ReleaseReferencesAt {
    int value();
}
