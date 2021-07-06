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

import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoDeleteMethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnTypeKind;
import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public class QuarkusDaoDeleteMethodGenerator extends DaoDeleteMethodGenerator {

  public QuarkusDaoDeleteMethodGenerator(
      ExecutableElement methodElement,
      Map<Name, TypeElement> typeParameters,
      TypeElement processedType,
      DaoImplementationSharedCode enclosingClass,
      ProcessorContext context) {
    super(methodElement, typeParameters, processedType, enclosingClass, context);
  }

  @Override
  protected Set<DaoReturnTypeKind> getSupportedReturnTypes() {
    ImmutableSet.Builder<DaoReturnTypeKind> types = ImmutableSet.builder();
    types.addAll(super.getSupportedReturnTypes());
    types.add(QuarkusDaoReturnTypeKind.UNI_OF_VOID);
    types.add(QuarkusDaoReturnTypeKind.UNI_OF_BOOLEAN);
    types.add(QuarkusDaoReturnTypeKind.MUTINY_REACTIVE_RESULT_SET);
    types.add(QuarkusDaoReturnTypeKind.MULTI_OF_ROW);
    types.add(QuarkusDaoReturnTypeKind.MULTI_OF_REACTIVE_ROW);
    return types.build();
  }
}
