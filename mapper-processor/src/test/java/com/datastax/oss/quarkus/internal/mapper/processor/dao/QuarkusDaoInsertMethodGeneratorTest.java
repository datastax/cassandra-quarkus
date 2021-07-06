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
package com.datastax.oss.quarkus.internal.mapper.processor.dao;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusGeneratedNames;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class QuarkusDaoInsertMethodGeneratorTest extends DaoMethodGeneratorTest {

  @NonNull
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @ParameterizedTest
  @MethodSource
  void should_succeed_without_error(MethodSpec method) {
    super.should_succeed_without_warnings(method);
  }

  @SuppressWarnings("unused")
  static Object[][] should_succeed_without_error() {
    return new Object[][] {
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Uni.class, Void.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("ifNotExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Uni.class, Boolean.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("ifNotExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(QuarkusGeneratedNames.UNI, ENTITY_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(MutinyReactiveResultSet.class)
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("ifNotExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Multi.class, ReactiveRow.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("ifNotExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Multi.class, Row.class))
            .build()
      },
    };
  }

  @ParameterizedTest
  @MethodSource
  @Override
  public void should_fail_with_expected_error(String expectedError, MethodSpec method) {
    super.should_fail_with_expected_error(expectedError, method);
  }

  @SuppressWarnings("unused")
  static Object[][] should_fail_with_expected_error() {
    return new Object[][] {
      {
        "Insert methods must take the entity to insert as the first parameter",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build(),
      },
      {
        "Insert methods must take the entity to insert as the first parameter",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(String.class, "a").build())
            .build(),
      },
      {
        "Insert methods must return one of [VOID, FUTURE_OF_VOID, ENTITY, FUTURE_OF_ENTITY, "
            + "OPTIONAL_ENTITY, FUTURE_OF_OPTIONAL_ENTITY, BOOLEAN, FUTURE_OF_BOOLEAN, RESULT_SET, "
            + "BOUND_STATEMENT, FUTURE_OF_ASYNC_RESULT_SET, REACTIVE_RESULT_SET, "
            + "UNI_OF_VOID, UNI_OF_BOOLEAN, UNI_OF_ENTITY, MUTINY_REACTIVE_RESULT_SET, "
            + "MULTI_OF_ROW, MULTI_OF_REACTIVE_ROW]",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(MutinyMappedReactiveResultSet.class)
            .build(),
      },
    };
  }

  @ParameterizedTest
  @MethodSource
  @Override
  public void should_succeed_with_expected_warning(String expectedWarning, MethodSpec method) {
    super.should_succeed_with_expected_warning(expectedWarning, method);
  }

  @SuppressWarnings("unused")
  static Object[][] should_succeed_with_expected_warning() {
    return new Object[][] {
      {
        "Invalid ttl value: "
            + "':foo bar' is not a valid placeholder, the generated query will probably fail",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("ttl", "$S", ":foo bar").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .build(),
      },
      {
        "Invalid ttl value: "
            + "'foo' is not a bind marker name and can't be parsed as a number literal either, "
            + "the generated query will probably fail",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("ttl", "$S", "foo").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .build(),
      },
      {
        "Invalid timestamp value: "
            + "':foo bar' is not a valid placeholder, the generated query will probably fail",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class)
                    .addMember("timestamp", "$S", ":foo bar")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .build(),
      },
      {
        "Invalid timestamp value: "
            + "'foo' is not a bind marker name and can't be parsed as a number literal either, "
            + "the generated query will probably fail",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(
                AnnotationSpec.builder(Insert.class).addMember("timestamp", "$S", "foo").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .build(),
      },
    };
  }
}
