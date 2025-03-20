package com.qendolin.betterclouds.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface DisableMixin {
    boolean value() default false;
}