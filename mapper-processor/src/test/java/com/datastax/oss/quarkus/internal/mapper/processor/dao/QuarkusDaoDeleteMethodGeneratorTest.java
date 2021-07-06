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

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableList;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.google.testing.compile.Compilation;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class QuarkusDaoDeleteMethodGeneratorTest extends DaoMethodGeneratorTest {

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
        MethodSpec.methodBuilder("delete")
            .addAnnotation(Delete.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Uni.class, Void.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class).addMember("ifExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Uni.class, Boolean.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class).addMember("ifExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(MutinyReactiveResultSet.class)
            .build()
      },
      {
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class).addMember("ifExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Multi.class, ReactiveRow.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class).addMember("ifExists", "true").build())
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
  public void should_fail_with_expected_error(
      String expectedError, MethodSpec method, TypeSpec entitySpec) {
    super.should_fail_with_expected_error(expectedError, method, entitySpec);
  }

  @SuppressWarnings("unused")
  static Object[][] should_fail_with_expected_error() {
    return new Object[][] {
      {
        "Invalid annotation parameters: Delete cannot have both ifExists and customIfClause",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class)
                    .addMember("ifExists", "true")
                    .addMember("customIfClause", "$S", "whatever")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build(),
        ENTITY_SPEC
      },
      {
        "Wrong number of parameters: Delete methods with no custom clause "
            + "must take either an entity instance, or the primary key components",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(Delete.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build(),
        ENTITY_SPEC
      },
      {
        "Missing entity class: Delete methods that do not operate on an entity "
            + "instance must have an 'entityClass' argument",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(Delete.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .build(),
        ENTITY_SPEC
      },
      {
        "Invalid parameter list: Delete methods that do not operate on an entity instance "
            + "and lack a custom where clause must match the primary key components in the exact "
            + "order (expected primary key of Product: [java.util.UUID]). Mismatch at index 0: java.lang"
            + ".Integer should be java.util.UUID",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class)
                    .addMember("entityClass", "$T.class", ENTITY_CLASS_NAME)
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(Integer.class, "id")
            .build(),
        ENTITY_SPEC
      },
      {
        "Invalid parameter list: Delete methods that do not operate on an entity instance "
            + "and lack a custom where clause must at least specify partition key components "
            + "(expected partition key of ProductSale: [java.util.UUID, java.lang.String])",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class)
                    .addMember("entityClass", "$T.class", SALE_ENTITY_CLASS_NAME)
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(Integer.class, "id")
            .build(),
        SALE_ENTITY_SPEC
      },
      {
        "Delete methods must return one of [VOID, FUTURE_OF_VOID, BOOLEAN, FUTURE_OF_BOOLEAN, "
            + "RESULT_SET, BOUND_STATEMENT, FUTURE_OF_ASYNC_RESULT_SET, REACTIVE_RESULT_SET, "
            + "UNI_OF_VOID, UNI_OF_BOOLEAN, MUTINY_REACTIVE_RESULT_SET, "
            + "MULTI_OF_ROW, MULTI_OF_REACTIVE_ROW]",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(Delete.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .returns(MutinyMappedReactiveResultSet.class)
            .build(),
        ENTITY_SPEC
      },
      {
        "Wrong number of parameters: Delete methods can only have additional parameters "
            + "if they specify a custom WHERE or IF clause",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(Delete.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .addParameter(Integer.class, "extra")
            .build(),
        ENTITY_SPEC
      },
      {
        "Delete methods that have a custom where clause must not take an Entity (Product) as a "
            + "parameter",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class)
                    .addMember("customWhereClause", "$S", "hello")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .returns(Integer.class)
            .build(),
        ENTITY_SPEC
      },
    };
  }

  @Test
  void should_warn_when_non_bind_marker_has_cql_name() {
    should_succeed_with_expected_warning(
        "Parameter id does not refer to a bind marker, @CqlName annotation will be ignored",
        MethodSpec.methodBuilder("delete")
            .addAnnotation(
                AnnotationSpec.builder(Delete.class)
                    .addMember("entityClass", ENTITY_CLASS_NAME + ".class")
                    .addMember("customIfClause", "$S", "description = :description")
                    .build())
            .addParameter(
                ParameterSpec.builder(UUID.class, "id")
                    .addAnnotation(
                        AnnotationSpec.builder(CqlName.class)
                            .addMember("value", "$S", "irrelevant")
                            .build())
                    .build())
            .addParameter(String.class, "description")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build());
  }

  @Test
  void should_not_fail_on_unsupported_result_when_custom_results_enabled() {

    MethodSpec methodSpec =
        MethodSpec.methodBuilder("delete")
            .addAnnotation(Delete.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "entity")
            .returns(Integer.class) // not a built-in return type
            .build();
    TypeSpec daoSpec =
        TypeSpec.interfaceBuilder(ClassName.get("test", "ProductDao"))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Dao.class)
            .addMethod(methodSpec)
            .build();

    for (List<String> compilerOptions :
        ImmutableList.of(
            ImmutableList.of("-Acom.datastax.oss.driver.mapper.customResults.enabled=true"),
            // The option defaults to true, so it should also work without explicit options:
            Collections.<String>emptyList())) {
      Compilation compilation =
          compileWithMapperProcessor("test", compilerOptions, ENTITY_SPEC, daoSpec);
      assertThat(compilation).succeededWithoutWarnings();
    }
  }
}
