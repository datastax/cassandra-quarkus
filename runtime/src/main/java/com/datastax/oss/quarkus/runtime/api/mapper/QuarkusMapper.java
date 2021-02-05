/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.quarkus.runtime.api.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Companion annotation to the driver's {@link com.datastax.oss.driver.api.mapper.annotations.Mapper
 * Mapper} annotation. It allows to customize Quarkus-specific features when generating code for a
 * Mapper-annotated interface.
 *
 * <p>Currently only one attribute is supported: {@link #generateProducers()}. When the annotation
 * is absent, or when it is present and the attribute is true (the default), bean producers will be
 * generated for the annotated Mapper interface itself, and for all of the DAO factory methods
 * declared in it, except those taking arguments. This makes it possible to inject such beans
 * automatically. When the attribute is false, the mapper annotation processor will skip the
 * generation of bean producers completely, and injection of such beans has to be done manually.
 *
 * <p>Example:
 *
 * <pre>
 * &#64;Mapper
 * &#64;QuarkusMapper(generateProducers=false)
 * public interface InventoryMapper {
 *   &#64;DaoFactory
 *   ProductDao productDao();
 * }
 * </pre>
 *
 * The mapper annotation processor would still generate an implementation and a builder for the
 * above Mapper interface, but no bean producer would be generated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuarkusMapper {

  /**
   * Whether to generate producers. When the annotation is absent, or when it is present and the
   * attribute is true (the default), CDI bean producers will be generated for the annotated Mapper
   * interface itself, and for all of the DAO factory methods declared in it, except those taking
   * arguments. This makes it possible to inject such beans automatically. If you want to turn off
   * this feature, set this attribute to false.
   */
  boolean generateProducers() default true;
}
