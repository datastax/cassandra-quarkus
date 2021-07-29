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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoUpdateMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

public class QuarkusDaoUpdateMethodGeneratorTest extends DaoMethodGeneratorTest {

  private static final AnnotationSpec UPDATE_ANNOTATION =
      AnnotationSpec.builder(Update.class).build();

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
        MethodSpec.methodBuilder("update")
            .addAnnotation(Update.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Uni.class, Void.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("update")
            .addAnnotation(
                AnnotationSpec.builder(Update.class).addMember("ifExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Uni.class, Boolean.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("update")
            .addAnnotation(Update.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(MutinyReactiveResultSet.class)
            .build()
      },
      {
        MethodSpec.methodBuilder("update")
            .addAnnotation(
                AnnotationSpec.builder(Update.class).addMember("ifExists", "true").build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ENTITY_CLASS_NAME, "product")
            .returns(ParameterizedTypeName.get(Multi.class, ReactiveRow.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("update")
            .addAnnotation(
                AnnotationSpec.builder(Update.class).addMember("ifExists", "true").build())
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
        "Update methods must take the entity to update as the first parameter",
        MethodSpec.methodBuilder("update")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(UPDATE_ANNOTATION)
            .build(),
      },
      {
        "Update methods must take the entity to update as the first parameter",
        MethodSpec.methodBuilder("update")
            .addAnnotation(UPDATE_ANNOTATION)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(String.class, "a").build())
            .build(),
      },
      {
        "Invalid return type: Update methods must return one of [VOID, FUTURE_OF_VOID, "
            + "RESULT_SET, BOUND_STATEMENT, FUTURE_OF_ASYNC_RESULT_SET, BOOLEAN, "
            + "FUTURE_OF_BOOLEAN, REACTIVE_RESULT_SET, "
            + "UNI_OF_VOID, UNI_OF_BOOLEAN, MUTINY_REACTIVE_RESULT_SET, "
            + "MULTI_OF_ROW, MULTI_OF_REACTIVE_ROW]",
        MethodSpec.methodBuilder("update")
            .addAnnotation(UPDATE_ANNOTATION)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(TypeName.INT)
            .build(),
      },
      {
        "Invalid annotation parameters: Update cannot have both ifExists and customIfClause",
        MethodSpec.methodBuilder("update")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(Update.class)
                    .addMember("ifExists", "true")
                    .addMember("customIfClause", "$S", "1 = 1")
                    .build())
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(TypeName.VOID)
            .build(),
      },
    };
  }

  @Test
  public void should_warn_when_non_bind_marker_has_cql_name() {
    should_succeed_with_expected_warning(
        "Parameter entity does not refer "
            + "to a bind marker, @CqlName annotation will be ignored",
        MethodSpec.methodBuilder("update")
            .addAnnotation(
                AnnotationSpec.builder(Update.class)
                    .addMember("customIfClause", "$S", "description LIKE :searchString")
                    .build())
            .addParameter(
                ParameterSpec.builder(ENTITY_CLASS_NAME, "entity")
                    .addAnnotation(
                        AnnotationSpec.builder(CqlName.class)
                            .addMember("value", "$S", "irrelevant")
                            .build())
                    .build())
            .addParameter(
                ParameterSpec.builder(String.class, "searchString")
                    .addAnnotation(
                        AnnotationSpec.builder(CqlName.class)
                            .addMember("value", "$S", "irrelevant")
                            .build())
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build());
  }

  @ParameterizedTest
  @MethodSource
  void should_process_timestamp(String timestamp, CodeBlock expected) {
    // given
    ProcessorContext processorContext = mock(ProcessorContext.class);
    TestDaoUpdateMethodGenerator daoUpdateMethodGenerator =
        new TestDaoUpdateMethodGenerator(null, null, null, null, processorContext);
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();

    // when
    daoUpdateMethodGenerator.maybeAddTimestamp(timestamp, builder);

    // then
    assertThat(builder.build().code).isEqualTo(expected);
  }

  @SuppressWarnings("unused")
  static Object[][] should_process_timestamp() {
    return new Object[][] {
      {"1", CodeBlock.of(".usingTimestamp(1)")},
      {
        ":ts", CodeBlock.of(".usingTimestamp($T.bindMarker($S))", QueryBuilder.class, "ts"),
      },
      {"1", CodeBlock.of(".usingTimestamp(1)")},
      {
        ":TS", CodeBlock.of(".usingTimestamp($T.bindMarker($S))", QueryBuilder.class, "TS"),
      },
    };
  }

  @ParameterizedTest
  @MethodSource
  void should_process_ttl(String ttl, CodeBlock expected) {
    // given
    ProcessorContext processorContext = Mockito.mock(ProcessorContext.class);
    TestDaoUpdateMethodGenerator daoUpdateMethodGenerator =
        new TestDaoUpdateMethodGenerator(null, null, null, null, processorContext);
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();

    // when
    daoUpdateMethodGenerator.maybeAddTtl(ttl, builder);

    // then
    assertThat(builder.build().code).isEqualTo(expected);
  }

  @SuppressWarnings("unused")
  static Object[][] should_process_ttl() {
    return new Object[][] {
      {"1", CodeBlock.of(".usingTtl(1)")},
      {
        ":ttl", CodeBlock.of(".usingTtl($T.bindMarker($S))", QueryBuilder.class, "ttl"),
      },
      {"1", CodeBlock.of(".usingTtl(1)")},
      {
        ":TTL", CodeBlock.of(".usingTtl($T.bindMarker($S))", QueryBuilder.class, "TTL"),
      },
    };
  }

  private static class TestDaoUpdateMethodGenerator extends DaoUpdateMethodGenerator {

    public TestDaoUpdateMethodGenerator(
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters,
        TypeElement processedType,
        DaoImplementationSharedCode enclosingClass,
        ProcessorContext context) {
      super(methodElement, typeParameters, processedType, enclosingClass, context);
    }

    @Override
    public void maybeAddTimestamp(String timestamp, Builder methodBuilder) {
      super.maybeAddTimestamp(timestamp, methodBuilder);
    }

    @Override
    public void maybeAddTtl(String ttl, Builder methodBuilder) {
      super.maybeAddTtl(ttl, methodBuilder);
    }
  }
}
