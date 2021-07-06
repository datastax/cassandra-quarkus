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
package com.datastax.oss.quarkus.internal.mapper.processor.mapper;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.DaoProfile;
import com.datastax.oss.driver.api.mapper.annotations.DaoTable;
import com.datastax.oss.driver.internal.mapper.DaoCacheKey;
import com.datastax.oss.driver.internal.mapper.processor.GeneratedNames;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.mapper.MapperImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.util.generation.GeneratedCodePatterns;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusGeneratedNames;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation note: this class is functionally equivalent to {@link
 * com.datastax.oss.driver.internal.mapper.processor.mapper.MapperDaoFactoryMethodGenerator}, with
 * the sole exception that it adds support for reactive return types in DAO factory methods, in
 * addition to sync and async types.
 */
public class QuarkusMapperDaoFactoryMethodGenerator implements MethodGenerator {

  private final ExecutableElement methodElement;
  private final MapperImplementationSharedCode enclosingClass;
  private final ProcessorContext context;

  private QuarkusDaoFactoryMethodType methodType;
  private TypeName daoFieldName;
  private ClassName daoImplementationName;
  private String keyspaceArgumentName;
  private String tableArgumentName;
  private String profileArgumentName;
  private boolean profileIsClass;

  public QuarkusMapperDaoFactoryMethodGenerator(
      ExecutableElement methodElement,
      MapperImplementationSharedCode enclosingClass,
      ProcessorContext context) {
    this.methodElement = methodElement;
    this.enclosingClass = enclosingClass;
    this.context = context;
  }

  @Override
  public Optional<MethodSpec> generate() {
    if (!validateReturnType()) {
      return Optional.empty();
    }
    if (!validateArguments()) {
      return Optional.empty();
    }
    boolean isCachedByMethodArguments =
        (keyspaceArgumentName != null || tableArgumentName != null || profileArgumentName != null);
    String fieldName = generateFieldDeclaration(isCachedByMethodArguments);
    MethodSpec.Builder methodBuilder = GeneratedCodePatterns.override(methodElement);
    if (isCachedByMethodArguments) {
      generateCacheKeyInstantiationStatement(methodBuilder);
      generateCacheKeyLookupStatement(fieldName, methodBuilder);
    } else {
      generateNonCachedStatement(fieldName, methodBuilder);
    }
    return Optional.of(methodBuilder.build());
  }

  private boolean validateReturnType() {
    TypeMirror returnTypeMirror = methodElement.getReturnType();
    if (returnTypeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredReturnType = (DeclaredType) returnTypeMirror;
      if (declaredReturnType.getTypeArguments().isEmpty()) {
        Element returnTypeElement = declaredReturnType.asElement();
        if (returnTypeElement.getAnnotation(Dao.class) != null) {
          methodType = QuarkusDaoFactoryMethodType.SYNC;
          daoFieldName = ClassName.get(declaredReturnType);
          daoImplementationName =
              GeneratedNames.daoImplementation(((TypeElement) returnTypeElement));
        }
      } else if (context.getClassUtils().isFuture(declaredReturnType)) {
        TypeMirror typeArgument = declaredReturnType.getTypeArguments().get(0);
        if (typeArgument.getKind() == TypeKind.DECLARED) {
          Element typeArgumentElement = ((DeclaredType) typeArgument).asElement();
          if (typeArgumentElement.getAnnotation(Dao.class) != null) {
            methodType = QuarkusDaoFactoryMethodType.ASYNC;
            daoFieldName = ClassName.get(declaredReturnType);
            daoImplementationName =
                GeneratedNames.daoImplementation(((TypeElement) typeArgumentElement));
          }
        }
      } else if (context.getClassUtils().isSame(declaredReturnType.asElement(), Uni.class)) {
        TypeMirror typeArgument = declaredReturnType.getTypeArguments().get(0);
        if (typeArgument.getKind() == TypeKind.DECLARED) {
          Element typeArgumentElement = ((DeclaredType) typeArgument).asElement();
          if (typeArgumentElement.getAnnotation(Dao.class) != null) {
            methodType = QuarkusDaoFactoryMethodType.REACTIVE;
            daoFieldName =
                ParameterizedTypeName.get(
                    QuarkusGeneratedNames.COMPLETION_STAGE, ClassName.get(typeArgument));
            daoImplementationName =
                GeneratedNames.daoImplementation(((TypeElement) typeArgumentElement));
          }
        }
      }
    }
    if (daoImplementationName == null) {
      context
          .getMessager()
          .error(
              methodElement,
              "Invalid return type: %s methods must return a %s-annotated interface, "
                  + "a future thereof, or a Uni thereof",
              DaoFactory.class.getSimpleName(),
              Dao.class.getSimpleName());
      return false;
    }
    return true;
  }

  private boolean validateArguments() {
    for (VariableElement parameterElement : methodElement.getParameters()) {
      if (parameterElement.getAnnotation(DaoKeyspace.class) != null) {
        keyspaceArgumentName =
            validateKeyspaceOrTableParameter(
                parameterElement, keyspaceArgumentName, DaoKeyspace.class, context);
        if (keyspaceArgumentName == null) {
          return false;
        }
      } else if (parameterElement.getAnnotation(DaoTable.class) != null) {
        tableArgumentName =
            validateKeyspaceOrTableParameter(
                parameterElement, tableArgumentName, DaoTable.class, context);
        if (tableArgumentName == null) {
          return false;
        }
      } else if (parameterElement.getAnnotation(DaoProfile.class) != null) {
        profileArgumentName =
            validateExecutionProfile(parameterElement, profileArgumentName, context);
        profileIsClass =
            context.getClassUtils().isSame(parameterElement.asType(), DriverExecutionProfile.class);
        if (profileArgumentName == null) {
          return false;
        }
      } else {
        context
            .getMessager()
            .error(
                methodElement,
                "Invalid parameter annotations: "
                    + "%s method parameters must be annotated with @%s, @%s or @%s",
                DaoFactory.class.getSimpleName(),
                DaoKeyspace.class.getSimpleName(),
                DaoTable.class.getSimpleName(),
                DaoProfile.class.getSimpleName());
        return false;
      }
    }
    return true;
  }

  private String generateFieldDeclaration(boolean isCachedByMethodArguments) {
    String suggestedFieldName = methodElement.getSimpleName() + "Cache";
    return isCachedByMethodArguments
        ? enclosingClass.addDaoMapField(suggestedFieldName, daoFieldName)
        : enclosingClass.addDaoSimpleField(
            suggestedFieldName,
            daoFieldName,
            daoImplementationName,
            methodType != QuarkusDaoFactoryMethodType.SYNC);
  }

  private void generateCacheKeyInstantiationStatement(MethodSpec.Builder methodBuilder) {
    // DaoCacheKey key = new DaoCacheKey(<ks>, <table>, <profileName>, <profile>)
    // where <ks>, <table> is either the name of the parameter or "(CqlIdentifier)null"
    methodBuilder.addCode("$1T key = new $1T(", DaoCacheKey.class);
    if (keyspaceArgumentName == null) {
      methodBuilder.addCode("($T)null", CqlIdentifier.class);
    } else {
      methodBuilder.addCode("$L", keyspaceArgumentName);
    }
    methodBuilder.addCode(", ");
    if (tableArgumentName == null) {
      methodBuilder.addCode("($T)null", CqlIdentifier.class);
    } else {
      methodBuilder.addCode("$L", tableArgumentName);
    }
    methodBuilder.addCode(", ");
    if (profileArgumentName == null) {
      methodBuilder.addCode("null, null);\n");
    } else {
      if (profileIsClass) {
        methodBuilder.addCode("null, $L);\n", profileArgumentName);
      } else {
        methodBuilder.addCode("$L, null);\n", profileArgumentName);
      }
    }
  }

  private void generateCacheKeyLookupStatement(String fieldName, MethodSpec.Builder methodBuilder) {
    if (methodType == QuarkusDaoFactoryMethodType.SYNC) {
      methodBuilder.addStatement(
          "return $L.computeIfAbsent(key, "
              + "k -> $T.init(context.withDaoParameters(k.getKeyspaceId(), k.getTableId(), "
              + "k.getExecutionProfileName(), k.getExecutionProfile())))",
          fieldName,
          daoImplementationName);
    } else if (methodType == QuarkusDaoFactoryMethodType.ASYNC) {
      methodBuilder.addStatement(
          "return $L.computeIfAbsent(key, "
              + "k -> $T.initAsync(context.withDaoParameters(k.getKeyspaceId(), k.getTableId(), "
              + "k.getExecutionProfileName(), k.getExecutionProfile())))",
          fieldName,
          daoImplementationName);
    } else {
      methodBuilder.addStatement(
          "return $L.computeIfAbsent(key, "
              + "k -> return $T.createFrom().completionStage("
              + "$T.initAsync(context.withDaoParameters(k.getKeyspaceId(), k.getTableId(), "
              + "k.getExecutionProfileName(), k.getExecutionProfile()))))",
          fieldName,
          QuarkusGeneratedNames.UNI,
          daoImplementationName);
    }
  }

  private void generateNonCachedStatement(String fieldName, MethodSpec.Builder methodBuilder) {
    if (methodType == QuarkusDaoFactoryMethodType.REACTIVE) {
      methodBuilder.addStatement(
          "return $T.createFrom().completionStage($L.get())", QuarkusGeneratedNames.UNI, fieldName);
    } else {
      methodBuilder.addStatement("return $L.get()", fieldName);
    }
  }

  private String validateKeyspaceOrTableParameter(
      VariableElement candidate, String previous, Class<?> annotation, ProcessorContext context) {
    if (isRepeatedAnnotation(candidate, previous, annotation, context)) {
      return null;
    }
    TypeMirror type = candidate.asType();
    if (!context.getClassUtils().isSame(type, String.class)
        && !context.getClassUtils().isSame(type, CqlIdentifier.class)) {
      context
          .getMessager()
          .error(
              candidate,
              "Invalid parameter type: @%s-annotated parameter of %s methods must be of type %s or %s",
              annotation.getSimpleName(),
              DaoFactory.class.getSimpleName(),
              String.class.getSimpleName(),
              CqlIdentifier.class.getSimpleName());
      return null;
    }
    return candidate.getSimpleName().toString();
  }

  private String validateExecutionProfile(
      VariableElement candidate, String previous, ProcessorContext context) {
    if (isRepeatedAnnotation(candidate, previous, DaoProfile.class, context)) {
      return null;
    }
    TypeMirror type = candidate.asType();
    if (!context.getClassUtils().isSame(type, String.class)
        && !context.getClassUtils().isSame(type, DriverExecutionProfile.class)) {
      context
          .getMessager()
          .error(
              candidate,
              "Invalid parameter type: @%s-annotated parameter of %s methods must be of type %s or %s ",
              DaoProfile.class.getSimpleName(),
              DaoFactory.class.getSimpleName(),
              String.class.getSimpleName(),
              DriverExecutionProfile.class.getSimpleName());
      return null;
    }
    return candidate.getSimpleName().toString();
  }

  private boolean isRepeatedAnnotation(
      VariableElement candidate, String previous, Class<?> annotation, ProcessorContext context) {
    if (previous != null) {
      context
          .getMessager()
          .error(
              candidate,
              "Invalid parameter annotations: "
                  + "only one %s method parameter can be annotated with @%s",
              DaoFactory.class.getSimpleName(),
              annotation.getSimpleName());
      return true;
    }
    return false;
  }
}
