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
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.DaoProfile;
import com.datastax.oss.driver.api.mapper.annotations.DaoTable;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.util.Capitalizer;
import com.datastax.oss.driver.internal.mapper.processor.util.NameIndex;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusGeneratedNames;
import com.datastax.oss.quarkus.internal.mapper.processor.mapper.QuarkusDaoFactoryMethodType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class QuarkusDaoProducerMethodGenerator implements MethodGenerator {

  private final ExecutableElement methodElement;
  private final ProcessorContext context;
  private final String methodName;

  private TypeName daoInterfaceName;
  private ParameterizedTypeName daoUniTypeName;
  private ParameterizedTypeName daoStageTypeName;
  private QuarkusDaoFactoryMethodType methodType;

  public QuarkusDaoProducerMethodGenerator(
      ExecutableElement methodElement, ProcessorContext context, NameIndex daoProducerMethodNames) {
    this.methodElement = methodElement;
    this.context = context;
    methodName =
        daoProducerMethodNames.uniqueField(
            "produce" + Capitalizer.capitalize(methodElement.getSimpleName().toString()));
  }

  public Optional<MethodSpec> generate() {
    if (!validateReturnType()) {
      return Optional.empty();
    }
    if (!validateArguments()) {
      return Optional.empty();
    }
    daoStageTypeName =
        ParameterizedTypeName.get(QuarkusGeneratedNames.COMPLETION_STAGE, daoInterfaceName);
    daoUniTypeName = ParameterizedTypeName.get(QuarkusGeneratedNames.UNI, daoInterfaceName);
    if (methodType == QuarkusDaoFactoryMethodType.ASYNC) {
      return Optional.of(generateAsyncMethod());
    } else if (methodType == QuarkusDaoFactoryMethodType.SYNC) {
      return Optional.of(generateSyncMethod());
    } else {
      return Optional.of(generateReactiveMethod());
    }
  }

  private boolean validateReturnType() {
    TypeMirror returnTypeMirror = methodElement.getReturnType();
    if (returnTypeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredReturnType = (DeclaredType) returnTypeMirror;
      if (declaredReturnType.getTypeArguments().isEmpty()) {
        Element returnTypeElement = declaredReturnType.asElement();
        if (returnTypeElement.getAnnotation(Dao.class) != null) {
          methodType = QuarkusDaoFactoryMethodType.SYNC;
          daoInterfaceName = ClassName.get(declaredReturnType);
          return true;
        }
      } else if (context.getClassUtils().isFuture(declaredReturnType)) {
        TypeMirror typeArgument = declaredReturnType.getTypeArguments().get(0);
        if (typeArgument.getKind() == TypeKind.DECLARED) {
          Element typeArgumentElement = ((DeclaredType) typeArgument).asElement();
          if (typeArgumentElement.getAnnotation(Dao.class) != null) {
            methodType = QuarkusDaoFactoryMethodType.ASYNC;
            daoInterfaceName = ClassName.get(typeArgument);
            return true;
          }
        }
      } else if (context.getClassUtils().isSame(declaredReturnType.asElement(), Uni.class)) {
        TypeMirror typeArgument = declaredReturnType.getTypeArguments().get(0);
        if (typeArgument.getKind() == TypeKind.DECLARED) {
          Element typeArgumentElement = ((DeclaredType) typeArgument).asElement();
          if (typeArgumentElement.getAnnotation(Dao.class) != null) {
            methodType = QuarkusDaoFactoryMethodType.REACTIVE;
            daoInterfaceName = ClassName.get(typeArgument);
            return true;
          }
        }
      }
    }
    // wrong return type: already reported
    return false;
  }

  private boolean validateArguments() {
    for (VariableElement parameterElement : methodElement.getParameters()) {
      if (parameterElement.getAnnotation(DaoKeyspace.class) != null) {
        context
            .getMessager()
            .warn(
                methodElement,
                "@%s annotation is not supported for automatic bean production; no injectable bean will be automatically produced for this method's returned value",
                DaoKeyspace.class.getSimpleName());
        return false;
      } else if (parameterElement.getAnnotation(DaoTable.class) != null) {
        context
            .getMessager()
            .warn(
                methodElement,
                "@%s annotation is not supported for automatic bean production; no injectable bean will be automatically produced for this method's returned value",
                DaoTable.class.getSimpleName());
        return false;
      } else if (parameterElement.getAnnotation(DaoProfile.class) != null) {
        context
            .getMessager()
            .warn(
                methodElement,
                "@%s annotation is not supported for automatic bean production; no injectable bean will be automatically produced for this method's returned value",
                DaoProfile.class.getSimpleName());
        return false;
      } else {
        // invalid annotation: already reported
        return false;
      }
    }
    return true;
  }

  private MethodSpec generateAsyncMethod() {
    return MethodSpec.methodBuilder(methodName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(QuarkusGeneratedNames.PRODUCES)
        .addAnnotation(QuarkusGeneratedNames.APPLICATION_SCOPED)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT_BEAN)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT)
        .addAnnotation(QuarkusGeneratedNames.GENERATED_DAO_BEAN)
        .returns(daoStageTypeName)
        .addStatement(
            "return mapperStage.thenCompose(mapper -> mapper.$L())", methodElement.getSimpleName())
        .build();
  }

  private MethodSpec generateSyncMethod() {
    return MethodSpec.methodBuilder(methodName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(QuarkusGeneratedNames.PRODUCES)
        .addAnnotation(QuarkusGeneratedNames.APPLICATION_SCOPED)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT_BEAN)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT)
        .addAnnotation(QuarkusGeneratedNames.GENERATED_DAO_BEAN)
        .addException(ExecutionException.class)
        .addException(InterruptedException.class)
        .returns(daoInterfaceName)
        .addStatement(
            "return mapperStage.toCompletableFuture().get().$L()", methodElement.getSimpleName())
        .build();
  }

  private MethodSpec generateReactiveMethod() {
    return MethodSpec.methodBuilder(methodName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(QuarkusGeneratedNames.PRODUCES)
        .addAnnotation(QuarkusGeneratedNames.APPLICATION_SCOPED)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT_BEAN)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT)
        .addAnnotation(QuarkusGeneratedNames.GENERATED_DAO_BEAN)
        .returns(daoUniTypeName)
        .addStatement(
            "return $T.createFrom().completionStage(mapperStage).flatMap(mapper -> mapper.$L())",
            QuarkusGeneratedNames.UNI,
            methodElement.getSimpleName())
        .build();
  }
}
