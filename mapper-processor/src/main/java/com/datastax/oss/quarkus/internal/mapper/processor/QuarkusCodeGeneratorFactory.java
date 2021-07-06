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
package com.datastax.oss.quarkus.internal.mapper.processor;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Increment;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.QueryProvider;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.internal.mapper.processor.CodeGenerator;
import com.datastax.oss.driver.internal.mapper.processor.DefaultCodeGeneratorFactory;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnTypeParser;
import com.datastax.oss.driver.internal.mapper.processor.mapper.MapperImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.util.NameIndex;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoDeleteMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoIncrementMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoInsertMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoQueryMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoQueryProviderMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoReturnTypeParser;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoSelectMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.dao.QuarkusDaoUpdateMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.mapper.QuarkusMapperDaoFactoryMethodGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.mapper.QuarkusMapperGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.producer.QuarkusDaoProducerGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.producer.QuarkusDaoProducerMethodGenerator;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public class QuarkusCodeGeneratorFactory extends DefaultCodeGeneratorFactory {

  private final NameIndex daoProducerMethodNames = new NameIndex();

  private final QuarkusDaoReturnTypeParser daoReturnTypeParser;

  public QuarkusCodeGeneratorFactory(QuarkusProcessorContext quarkusProcessorContext) {
    super(quarkusProcessorContext);
    this.daoReturnTypeParser = new QuarkusDaoReturnTypeParser(context);
  }

  public QuarkusDaoProducerGenerator newDaoProducer(TypeElement interfaceElement) {
    return new QuarkusDaoProducerGenerator(interfaceElement, context, daoProducerMethodNames);
  }

  public Optional<MethodGenerator> newDaoProducerMethod(ExecutableElement methodElement) {
    if (methodElement.getAnnotation(DaoFactory.class) == null) {
      return Optional.empty();
    } else {
      return Optional.of(
          new QuarkusDaoProducerMethodGenerator(methodElement, context, daoProducerMethodNames));
    }
  }

  @Override
  public CodeGenerator newMapper(TypeElement interfaceElement) {
    return new QuarkusMapperGenerator(interfaceElement, context);
  }

  @Override
  public Optional<MethodGenerator> newMapperImplementationMethod(
      ExecutableElement methodElement,
      TypeElement processedType,
      MapperImplementationSharedCode enclosingClass) {
    if (methodElement.getAnnotation(DaoFactory.class) != null) {
      return Optional.of(
          new QuarkusMapperDaoFactoryMethodGenerator(methodElement, enclosingClass, context));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<MethodGenerator> newDaoImplementationMethod(
      ExecutableElement methodElement,
      Map<Name, TypeElement> typeParameters,
      TypeElement processedType,
      DaoImplementationSharedCode enclosingClass) {
    if (methodElement.getAnnotation(Insert.class) != null) {
      return Optional.of(
          new QuarkusDaoInsertMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else if (methodElement.getAnnotation(Select.class) != null) {
      return Optional.of(
          new QuarkusDaoSelectMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else if (methodElement.getAnnotation(Delete.class) != null) {
      return Optional.of(
          new QuarkusDaoDeleteMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else if (methodElement.getAnnotation(Query.class) != null) {
      return Optional.of(
          new QuarkusDaoQueryMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else if (methodElement.getAnnotation(Update.class) != null) {
      return Optional.of(
          new QuarkusDaoUpdateMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else if (methodElement.getAnnotation(QueryProvider.class) != null) {
      return Optional.of(
          new QuarkusDaoQueryProviderMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else if (methodElement.getAnnotation(Increment.class) != null) {
      return Optional.of(
          new QuarkusDaoIncrementMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    } else {
      return super.newDaoImplementationMethod(
          methodElement, typeParameters, processedType, enclosingClass);
    }
  }

  @Override
  public DaoReturnTypeParser getDaoReturnTypeParser() {
    return daoReturnTypeParser;
  }
}
