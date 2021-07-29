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

import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.mapper.MapperGenerator;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusCodeGeneratorFactory;
import com.datastax.oss.quarkus.runtime.api.mapper.QuarkusMapper;
import javax.lang.model.element.TypeElement;

public class QuarkusMapperGenerator extends MapperGenerator {

  private final TypeElement interfaceElement;
  private final ProcessorContext context;

  public QuarkusMapperGenerator(TypeElement interfaceElement, ProcessorContext context) {
    super(interfaceElement, context);
    this.interfaceElement = interfaceElement;
    this.context = context;
  }

  @Override
  public void generate() {
    super.generate();
    if (shouldGenerateProducers()) {
      ((QuarkusCodeGeneratorFactory) context.getCodeGeneratorFactory())
          .newDaoProducer(interfaceElement)
          .generate();
    }
  }

  private boolean shouldGenerateProducers() {
    QuarkusMapper quarkusMapper = interfaceElement.getAnnotation(QuarkusMapper.class);
    if (quarkusMapper != null) {
      return quarkusMapper.generateProducers();
    }
    return true;
  }
}
