/*
 * Copyright (C) 2013 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.parser.apdu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.devnied.bitlib.BitUtils;

/**
 * Annotation to describe field information
 * 
 * @author MILLAU Julien
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Data {

	/**
	 * Format date
	 */
	String format() default BitUtils.DATE_FORMAT;

	/**
	 * The current date standard
	 */
	int dateStandard() default 0;

	/**
	 * index of data
	 */
	int index();

	/**
	 * Read the string in hexa
	 */
	boolean readHexa() default false;

	/**
	 * Number of bytes
	 */
	int size();

	/**
	 * Tag Name
	 */
	String tag();
}
