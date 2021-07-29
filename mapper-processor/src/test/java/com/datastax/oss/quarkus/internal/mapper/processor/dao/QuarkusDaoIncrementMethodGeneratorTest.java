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

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.Increment;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class QuarkusDaoIncrementMethodGeneratorTest extends DaoMethodGeneratorTest {

  @NonNull
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @ParameterizedTest
  @MethodSource
  void should_succeed_without_error(MethodSpec method) {
    super.should_succeed_without_warnings(
        "test",
        VOTES_SPEC,
        TypeSpec.interfaceBuilder(ClassName.get("test", "VotesDao"))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Dao.class)
            .addMethod(method)
            .build());
  }

  @SuppressWarnings("unused")
  static Object[][] should_succeed_without_error() {
    return new Object[][] {
      {
        MethodSpec.methodBuilder("increment")
            .addAnnotation(
                AnnotationSpec.builder(Increment.class)
                    .addMember("entityClass", "$T.class", VOTES_CLASS_NAME)
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .addParameter(Long.TYPE, "up")
            .returns(ParameterizedTypeName.get(Uni.class, Void.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("increment")
            .addAnnotation(
                AnnotationSpec.builder(Increment.class)
                    .addMember("entityClass", "$T.class", VOTES_CLASS_NAME)
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .addParameter(Long.class, "down")
            .returns(MutinyReactiveResultSet.class)
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
        "Missing entity class: Increment methods must always have an 'entityClass' argument",
        MethodSpec.methodBuilder("increment")
            .addAnnotation(Increment.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build(),
        VOTES_SPEC
      },
      {
        "Invalid return type: Increment methods must return one of [VOID, FUTURE_OF_VOID, "
            + "REACTIVE_RESULT_SET, UNI_OF_VOID, MUTINY_REACTIVE_RESULT_SET]",
        MethodSpec.methodBuilder("increment")
            .addAnnotation(
                AnnotationSpec.builder(Increment.class)
                    .addMember("entityClass", "$T.class", VOTES_CLASS_NAME)
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(UUID.class, "id")
            .returns(TypeName.BOOLEAN)
            .build(),
        VOTES_SPEC
      },
    };
  }

  private static final ClassName VOTES_CLASS_NAME = ClassName.get("test", "Votes");

  private static final TypeSpec VOTES_SPEC =
      TypeSpec.classBuilder(VOTES_CLASS_NAME)
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(Entity.class)
          .addField(UUID.class, "id", Modifier.PRIVATE)
          .addField(Long.TYPE, "up", Modifier.PRIVATE)
          .addField(Long.TYPE, "down", Modifier.PRIVATE)
          .addMethod(
              MethodSpec.methodBuilder("setId")
                  .addParameter(UUID.class, "id")
                  .addModifiers(Modifier.PUBLIC)
                  .addStatement("this.id = id")
                  .build())
          .addMethod(
              MethodSpec.methodBuilder("getId")
                  .addAnnotation(PartitionKey.class)
                  .returns(UUID.class)
                  .addModifiers(Modifier.PUBLIC)
                  .addStatement("return id")
                  .build())
          .addMethod(
              MethodSpec.methodBuilder("setUp")
                  .addParameter(Long.TYPE, "up")
                  .addModifiers(Modifier.PUBLIC)
                  .addStatement("this.up = up")
                  .build())
          .addMethod(
              MethodSpec.methodBuilder("getUp")
                  .returns(Long.TYPE)
                  .addModifiers(Modifier.PUBLIC)
                  .addStatement("return up")
                  .build())
          .addMethod(
              MethodSpec.methodBuilder("setDown")
                  .addParameter(Long.TYPE, "down")
                  .addModifiers(Modifier.PUBLIC)
                  .addStatement("this.down = down")
                  .build())
          .addMethod(
              MethodSpec.methodBuilder("getDown")
                  .returns(Long.TYPE)
                  .addModifiers(Modifier.PUBLIC)
                  .addStatement("return down")
                  .build())
          .build();
}
