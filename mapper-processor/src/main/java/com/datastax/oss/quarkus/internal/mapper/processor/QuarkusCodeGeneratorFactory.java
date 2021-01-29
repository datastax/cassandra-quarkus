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
import com.datastax.oss.driver.internal.mapper.processor.CodeGenerator;
import com.datastax.oss.driver.internal.mapper.processor.DefaultCodeGeneratorFactory;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.mapper.MapperImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.util.NameIndex;
import java.util.Optional;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class QuarkusCodeGeneratorFactory extends DefaultCodeGeneratorFactory {

  private final NameIndex daoProducerMethodNames = new NameIndex();

  public QuarkusCodeGeneratorFactory(QuarkusProcessorContext quarkusProcessorContext) {
    super(quarkusProcessorContext);
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
}
