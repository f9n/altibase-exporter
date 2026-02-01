package com.f9n.altibase.exporter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a scrape method; value = metric key(s) for ALTIBASE_DISABLED_METRICS. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScrapeMetric {

    String[] value();

    boolean skipWhenAllDisabled() default true;

    boolean catchSchemaError() default false;
}
