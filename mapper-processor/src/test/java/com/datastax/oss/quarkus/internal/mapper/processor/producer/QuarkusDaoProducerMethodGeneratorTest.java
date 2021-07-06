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
package com.datastax.oss.quarkus.internal.mapper.processor.producer;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.DaoProfile;
import com.datastax.oss.driver.api.mapper.annotations.DaoTable;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessorTest;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.datastax.oss.quarkus.runtime.api.mapper.QuarkusMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Uni;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class QuarkusDaoProducerMethodGeneratorTest extends MapperProcessorTest {

  private static final ClassName DAO_CLASS_NAME = ClassName.get("test", "ProductDao");

  private static final TypeSpec DAO_SPEC =
      TypeSpec.interfaceBuilder(DAO_CLASS_NAME)
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(Dao.class)
          .build();

  @NonNull
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @ParameterizedTest
  @MethodSource
  void should_succeed_with_expected_warning(String expectedWarning, MethodSpec method) {
    should_succeed_with_expected_warning(
        expectedWarning,
        "test",
        DAO_SPEC,
        TypeSpec.interfaceBuilder(ClassName.get("test", "InventoryMapper"))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Mapper.class)
            .addMethod(method)
            .build());
  }

  @SuppressWarnings("unused")
  static Object[][] should_succeed_with_expected_warning() {
    return new Object[][] {
      {
        "@DaoKeyspace annotation is not supported for automatic bean production; no injectable bean will be automatically produced for this method's returned value",
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(DAO_CLASS_NAME)
            .addParameter(
                ParameterSpec.builder(String.class, "ks").addAnnotation(DaoKeyspace.class).build())
            .build()
      },
      {
        "@DaoTable annotation is not supported for automatic bean production; no injectable bean will be automatically produced for this method's returned value",
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(DAO_CLASS_NAME)
            .addParameter(
                ParameterSpec.builder(String.class, "table").addAnnotation(DaoTable.class).build())
            .build(),
      },
      {
        "@DaoProfile annotation is not supported for automatic bean production; no injectable bean will be automatically produced for this method's returned value",
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(DAO_CLASS_NAME)
            .addParameter(
                ParameterSpec.builder(String.class, "profile")
                    .addAnnotation(DaoProfile.class)
                    .build())
            .build(),
      },
    };
  }

  @ParameterizedTest
  @MethodSource
  void should_succeed_without_warnings(MethodSpec method) {
    should_succeed_without_warnings(
        "test",
        DAO_SPEC,
        TypeSpec.interfaceBuilder(ClassName.get("test", "InventoryMapper"))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Mapper.class)
            .addMethod(method)
            .build());
  }

  @SuppressWarnings("unused")
  static Object[][] should_succeed_without_warnings() {
    return new Object[][] {
      {
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(DAO_CLASS_NAME)
            .build()
      },
      {
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(
                ParameterizedTypeName.get(ClassName.get(CompletionStage.class), DAO_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(
                ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), DAO_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("productDao")
            .addAnnotation(DaoFactory.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ParameterizedTypeName.get(ClassName.get(Uni.class), DAO_CLASS_NAME))
            .build()
      },
    };
  }

  @Test
  void should_skip_producer_class_generation() {
    should_succeed_without_warnings(
        "test",
        DAO_SPEC,
        TypeSpec.interfaceBuilder(ClassName.get("test", "InventoryMapper"))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Mapper.class)
            .addAnnotation(
                AnnotationSpec.builder(QuarkusMapper.class)
                    .addMember("generateProducers", "false")
                    .build())
            .addMethod(
                MethodSpec.methodBuilder("productDao")
                    .addAnnotation(DaoFactory.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(DAO_CLASS_NAME)
                    .addParameter(
                        ParameterSpec.builder(String.class, "ks")
                            .addAnnotation(DaoKeyspace.class)
                            .build())
                    .build())
            .build());
  }
}
