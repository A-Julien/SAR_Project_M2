package jvn.Proxy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Target(ElementType.METHOD)
public @interface JvnAnnotation {
    String type();
}
