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

import com.datastax.oss.driver.internal.mapper.processor.GeneratedNames;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.SingleFileCodeGenerator;
import com.datastax.oss.driver.internal.mapper.processor.util.Capitalizer;
import com.datastax.oss.driver.internal.mapper.processor.util.NameIndex;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusCodeGeneratorFactory;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusGeneratedNames;
import com.datastax.oss.quarkus.internal.mapper.processor.mapper.QuarkusMapperGenerator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import jakarta.annotation.Generated;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class QuarkusDaoProducerGenerator extends SingleFileCodeGenerator {

  private final TypeElement interfaceElement;
  private final ClassName producerName;
  private final ClassName builderName;
  private final String asyncMethodName;
  private final String syncMethodName;
  private final String reactiveMethodName;

  public QuarkusDaoProducerGenerator(
      TypeElement interfaceElement, ProcessorContext context, NameIndex daoProducerMethodNames) {
    super(context);
    this.interfaceElement = interfaceElement;
    this.producerName = QuarkusGeneratedNames.daoProducer(interfaceElement);
    this.builderName = GeneratedNames.mapperBuilder(interfaceElement);
    asyncMethodName =
        daoProducerMethodNames.uniqueField(
            "produce"
                + Capitalizer.capitalize(interfaceElement.getSimpleName().toString())
                + "Async");
    syncMethodName =
        daoProducerMethodNames.uniqueField(
            "produce"
                + Capitalizer.capitalize(interfaceElement.getSimpleName().toString())
                + "Sync");
    reactiveMethodName =
        daoProducerMethodNames.uniqueField(
            "produce"
                + Capitalizer.capitalize(interfaceElement.getSimpleName().toString())
                + "Reactive");
  }

  @Override
  protected ClassName getPrincipalTypeName() {
    return producerName;
  }

  @Override
  protected JavaFile.Builder getContents() {

    TypeSpec.Builder classContents =
        TypeSpec.classBuilder(producerName)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc(
                "A DAO bean producer for DAO factory methods declared in {@link $T}.",
                interfaceElement)
            .addJavadoc(JAVADOC_PARAGRAPH_SEPARATOR)
            .addJavadoc(JAVADOC_GENERATED_WARNING)
            .addAnnotation(
                AnnotationSpec.builder(Generated.class)
                    .addMember("value", "\"$L\"", QuarkusMapperGenerator.class.getName())
                    .addMember("date", "\"$L\"", Instant.now().toString())
                    .build())
            .addAnnotation(
                AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "\"all\"")
                    .build())
            .addField(
                FieldSpec.builder(
                        QuarkusGeneratedNames.SESSION_FUTURE,
                        "sessionStage",
                        Modifier.PRIVATE,
                        Modifier.FINAL)
                    .build())
            .addField(
                FieldSpec.builder(
                        ParameterizedTypeName.get(
                            ClassName.get(CompletionStage.class), ClassName.get(interfaceElement)),
                        "mapperStage",
                        Modifier.PRIVATE,
                        Modifier.FINAL)
                    .build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(QuarkusGeneratedNames.INJECT)
                    .addParameter(QuarkusGeneratedNames.SESSION_FUTURE, "sessionStage")
                    .addStatement("this.sessionStage = sessionStage")
                    .addStatement(
                        "mapperStage = sessionStage.thenApply(session -> new $T(session).build())",
                        builderName)
                    .build());

    classContents.addMethod(generateMapperProducerAsyncMethod());
    classContents.addMethod(generateMapperProducerSyncMethod());
    classContents.addMethod(generateMapperProducerReactiveMethod());

    for (Element child : interfaceElement.getEnclosedElements()) {
      if (child.getKind() == ElementKind.METHOD) {
        ExecutableElement methodElement = (ExecutableElement) child;
        Set<Modifier> modifiers = methodElement.getModifiers();
        if (!modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.DEFAULT)) {
          Optional<MethodGenerator> maybeGenerator =
              ((QuarkusCodeGeneratorFactory) context.getCodeGeneratorFactory())
                  .newDaoProducerMethod(methodElement);
          if (maybeGenerator.isPresent()) {
            maybeGenerator.flatMap(MethodGenerator::generate).ifPresent(classContents::addMethod);
          }
        }
      }
    }
    return JavaFile.builder(producerName.packageName(), classContents.build());
  }

  private MethodSpec generateMapperProducerAsyncMethod() {
    return MethodSpec.methodBuilder(asyncMethodName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(QuarkusGeneratedNames.PRODUCES)
        .addAnnotation(QuarkusGeneratedNames.APPLICATION_SCOPED)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT_BEAN)
        .addAnnotation(QuarkusGeneratedNames.GENERATED_MAPPER_BEAN)
        .returns(
            ParameterizedTypeName.get(
                QuarkusGeneratedNames.COMPLETION_STAGE, ClassName.get(interfaceElement)))
        .addStatement("return mapperStage")
        .build();
  }

  private MethodSpec generateMapperProducerSyncMethod() {
    return MethodSpec.methodBuilder(syncMethodName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(QuarkusGeneratedNames.PRODUCES)
        .addAnnotation(QuarkusGeneratedNames.APPLICATION_SCOPED)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT_BEAN)
        .addAnnotation(QuarkusGeneratedNames.GENERATED_MAPPER_BEAN)
        .addException(ExecutionException.class)
        .addException(InterruptedException.class)
        .returns(ClassName.get(interfaceElement))
        .addStatement("return mapperStage.toCompletableFuture().get()")
        .build();
  }

  private MethodSpec generateMapperProducerReactiveMethod() {
    return MethodSpec.methodBuilder(reactiveMethodName)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(QuarkusGeneratedNames.PRODUCES)
        .addAnnotation(QuarkusGeneratedNames.APPLICATION_SCOPED)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT)
        .addAnnotation(QuarkusGeneratedNames.DEFAULT_BEAN)
        .addAnnotation(QuarkusGeneratedNames.GENERATED_MAPPER_BEAN)
        .returns(
            ParameterizedTypeName.get(QuarkusGeneratedNames.UNI, ClassName.get(interfaceElement)))
        .addStatement(
            "return $T.createFrom().completionStage($L())",
            QuarkusGeneratedNames.UNI,
            asyncMethodName)
        .build();
  }
}
