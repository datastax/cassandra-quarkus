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

import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnTypeKind;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.FailedMutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.MutinyWrappers;
import com.datastax.oss.quarkus.runtime.internal.reactive.mapper.DefaultMutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.mapper.FailedMutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.mapper.MapperMutinyWrappers;
import com.squareup.javapoet.CodeBlock;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public enum QuarkusDaoReturnTypeKind implements DaoReturnTypeKind {
  MUTINY_REACTIVE_RESULT_SET {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return new $T(executeReactive(boundStatement))", DefaultMutinyReactiveResultSet.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_MUTINY_REACTIVE_RESULT_SET);
    }
  },

  MUTINY_MAPPED_REACTIVE_RESULT_SET {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return new $T<>(executeReactiveAndMap(boundStatement, $L))",
          DefaultMutinyMappedReactiveResultSet.class,
          helperFieldName);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_MUTINY_MAPPED_REACTIVE_RESULT_SET);
    }
  },

  MULTI_OF_ROW {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toRowMulti(executeReactive(boundStatement))", MapperMutinyWrappers.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_MULTI);
    }
  },

  MULTI_OF_REACTIVE_ROW {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return new $T(executeReactive(boundStatement))", DefaultMutinyReactiveResultSet.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_MULTI);
    }
  },

  MULTI_OF_ENTITY {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return new $T<>(executeReactiveAndMap(boundStatement, $L))",
          DefaultMutinyMappedReactiveResultSet.class,
          helperFieldName);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_MULTI);
    }
  },

  UNI_OF_ROW {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toRowUni(executeReactive(boundStatement))", MapperMutinyWrappers.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_UNI);
    }
  },

  UNI_OF_REACTIVE_ROW {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toUni(executeReactive(boundStatement))", MutinyWrappers.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_UNI);
    }
  },

  UNI_OF_ENTITY {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toEntityUni(executeReactive(boundStatement), $L)",
          MapperMutinyWrappers.class,
          helperFieldName);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_UNI);
    }
  },

  UNI_OF_VOID {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toVoidUni(executeReactive(boundStatement))", MapperMutinyWrappers.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_UNI);
    }
  },

  UNI_OF_BOOLEAN {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toWasAppliedUni(executeReactive(boundStatement))", MapperMutinyWrappers.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_UNI);
    }
  },

  UNI_OF_LONG {
    @Override
    public void addExecuteStatement(
        CodeBlock.Builder methodBuilder,
        String helperFieldName,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      methodBuilder.addStatement(
          "return $T.toCountUni(executeReactive(boundStatement))", MapperMutinyWrappers.class);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(
        CodeBlock innerBlock,
        ExecutableElement methodElement,
        Map<Name, TypeElement> typeParameters) {
      return wrapWithErrorHandling(innerBlock, FAILED_UNI);
    }
  },
  ;

  @Override
  public String getDescription() {
    return name();
  }

  @Override
  public boolean requiresReactive() {
    return true;
  }

  static CodeBlock wrapWithErrorHandling(CodeBlock innerBlock, CodeBlock catchBlock) {
    return CodeBlock.builder()
        .beginControlFlow("try")
        .add(innerBlock)
        .nextControlFlow("catch ($T e)", Exception.class)
        .add(catchBlock)
        .endControlFlow()
        .build();
  }

  private static final CodeBlock FAILED_MUTINY_REACTIVE_RESULT_SET =
      CodeBlock.builder()
          .addStatement("return new $T(e)", FailedMutinyReactiveResultSet.class)
          .build();
  private static final CodeBlock FAILED_MUTINY_MAPPED_REACTIVE_RESULT_SET =
      CodeBlock.builder()
          .addStatement("return new $T<>(e)", FailedMutinyMappedReactiveResultSet.class)
          .build();
  private static final CodeBlock FAILED_MULTI =
      CodeBlock.builder()
          .addStatement("return $T.failedMulti(e)", MapperMutinyWrappers.class)
          .build();
  private static final CodeBlock FAILED_UNI =
      CodeBlock.builder()
          .addStatement("return $T.failedUni(e)", MapperMutinyWrappers.class)
          .build();
}
