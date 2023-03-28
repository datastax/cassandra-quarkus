package com.datastax.oss.quarkus.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(CheckAmd64Condition.class)
public @interface SkipIfNotAmd64 { }
