package com.twowheeler.auth_service.Annotation;

import java.lang.annotation.*;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DynamoTable {

    String tableName();
}
