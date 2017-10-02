/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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
package com.github.cafdataprocessing.corepolicy.common;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Marker annotation that can be used to define a non-static
 * method as a "setter" or "getter" for a logical property
 * (depending on its signature),
 * or non-static object field to be used (serialized, deserialized) as
 * a logical property.
 *<p>
 * Default value ("") indicates that the field name is used
 * as the property name without any modifications, but it
 * can be specified to non-empty value to specify different
 * name. Property name refers to name used externally, as
 * the field name in JSON objects.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface FilterName
{
    /**
     * Special value that indicates that handlers should use the default
     * name (derived from method or field name) for property
     */
    public final static String USE_DEFAULT_NAME = "";

    /**
     * Defines the name of the logical property when used as a paging Filter
     * i.e. Matches the JSON object field name in use for the property.
     * If value is empty String (which is the default), will try to use
     * name of the field that is annotated.
     */
    String value() default USE_DEFAULT_NAME;

}
