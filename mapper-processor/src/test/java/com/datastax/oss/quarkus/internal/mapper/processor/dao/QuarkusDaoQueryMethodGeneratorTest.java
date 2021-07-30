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
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusGeneratedNames;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class QuarkusDaoQueryMethodGeneratorTest extends DaoMethodGeneratorTest {

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
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Uni.class, Void.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Uni.class, Boolean.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Uni.class, Long.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Uni.class, Row.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Uni.class, ReactiveRow.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(QuarkusGeneratedNames.UNI, ENTITY_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(MutinyReactiveResultSet.class)
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(MutinyMappedReactiveResultSet.class), ENTITY_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(ClassName.get(Multi.class), ENTITY_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Multi.class, Row.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("query")
            .addAnnotation(
                AnnotationSpec.builder(Query.class).addMember("value", "$S", "irrelevant").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(ParameterizedTypeName.get(Multi.class, ReactiveRow.class))
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
        "Invalid return type: Query methods must return one of [VOID, BOOLEAN, LONG, ROW, "
            + "ENTITY, OPTIONAL_ENTITY, RESULT_SET, BOUND_STATEMENT, PAGING_ITERABLE, FUTURE_OF_VOID, "
            + "FUTURE_OF_BOOLEAN, FUTURE_OF_LONG, FUTURE_OF_ROW, FUTURE_OF_ENTITY, "
            + "FUTURE_OF_OPTIONAL_ENTITY, FUTURE_OF_ASYNC_RESULT_SET, "
            + "FUTURE_OF_ASYNC_PAGING_ITERABLE, REACTIVE_RESULT_SET, MAPPED_REACTIVE_RESULT_SET, "
            + "STREAM, FUTURE_OF_STREAM, "
            + "MUTINY_REACTIVE_RESULT_SET, MUTINY_MAPPED_REACTIVE_RESULT_SET, MULTI_OF_ROW, "
            + "MULTI_OF_REACTIVE_ROW, MULTI_OF_ENTITY, UNI_OF_ROW, UNI_OF_REACTIVE_ROW, UNI_OF_ENTITY, "
            + "UNI_OF_VOID, UNI_OF_BOOLEAN, UNI_OF_LONG]",
        MethodSpec.methodBuilder("select")
            .addAnnotation(
                AnnotationSpec.builder(Query.class)
                    .addMember("value", "$S", "SELECT * FROM whatever")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(UUID.class)
            .build(),
      },
    };
  }
}
