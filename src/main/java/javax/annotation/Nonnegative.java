package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@TypeQualifier(applicableTo = Number.class)
public @interface Nonnegative {

    public static class Checker implements TypeQualifierValidator<Nonnegative> {
        public When forConstantValue(Nonnegative nonnegative, Object obj) {
            if (!(obj instanceof Number)) {
                return When.NEVER;
            }
            Number number = (Number) obj;
            Object obj2 = null;
            if ((number instanceof Long) ? number.longValue() >= 0 : (number instanceof Double) ? number.doubleValue() >= 0.0d : (number instanceof Float) ? number.floatValue() >= 0.0f : number.intValue() >= 0) {
                obj2 = 1;
            }
            if (obj2 != null) {
                return When.NEVER;
            }
            return When.ALWAYS;
        }
    }

    When when() default When.ALWAYS;
}
