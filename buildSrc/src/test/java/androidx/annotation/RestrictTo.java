package androidx.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Stand-in for the AndroidX annotation (the dumper matches on the descriptor). */
@Retention(RetentionPolicy.CLASS)
public @interface RestrictTo {
    Scope[] value();

    enum Scope { LIBRARY }
}
