package com.velor.storage.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseMapping {
	String name() default "";

	String[] langs() default {};

	boolean primaryKey() default false;

	String foreignKey() default "";
}
