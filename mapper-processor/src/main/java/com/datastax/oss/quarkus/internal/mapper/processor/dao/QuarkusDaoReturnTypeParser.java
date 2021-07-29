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

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnType;
import com.datastax.oss.driver.internal.mapper.processor.dao.DefaultDaoReturnTypeParser;
import com.datastax.oss.driver.internal.mapper.processor.dao.EntityUtils;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class QuarkusDaoReturnTypeParser extends DefaultDaoReturnTypeParser {

  private static final DaoReturnType MUTINY_REACTIVE_RESULT_SET =
      new DaoReturnType(QuarkusDaoReturnTypeKind.MUTINY_REACTIVE_RESULT_SET);

  private static final DaoReturnType MULTI_OF_ROW =
      new DaoReturnType(QuarkusDaoReturnTypeKind.MULTI_OF_ROW);

  private static final DaoReturnType MULTI_OF_REACTIVE_ROW =
      new DaoReturnType(QuarkusDaoReturnTypeKind.MULTI_OF_REACTIVE_ROW);

  private static final DaoReturnType UNI_OF_ROW =
      new DaoReturnType(QuarkusDaoReturnTypeKind.UNI_OF_ROW);

  private static final DaoReturnType UNI_OF_REACTIVE_ROW =
      new DaoReturnType(QuarkusDaoReturnTypeKind.UNI_OF_REACTIVE_ROW);

  private static final DaoReturnType UNI_OF_VOID =
      new DaoReturnType(QuarkusDaoReturnTypeKind.UNI_OF_VOID);

  private static final DaoReturnType UNI_OF_BOOLEAN =
      new DaoReturnType(QuarkusDaoReturnTypeKind.UNI_OF_BOOLEAN);

  private static final DaoReturnType UNI_OF_LONG =
      new DaoReturnType(QuarkusDaoReturnTypeKind.UNI_OF_LONG);

  public QuarkusDaoReturnTypeParser(ProcessorContext context) {
    super(context);
  }

  @NonNull
  @Override
  public DaoReturnType parse(
      @NonNull TypeMirror returnTypeMirror, @NonNull Map<Name, TypeElement> typeParameters) {

    if (returnTypeMirror.getKind() == TypeKind.DECLARED) {

      DeclaredType returnTypeDeclared = (DeclaredType) returnTypeMirror;

      // MutinyReactiveResultSet
      if (context.getClassUtils().isSame(returnTypeDeclared, MutinyReactiveResultSet.class)) {
        return MUTINY_REACTIVE_RESULT_SET;
      }

      // Mutiny containers of various target objects
      if (returnTypeDeclared.getTypeArguments().size() == 1) {

        TypeMirror typeArgumentMirror = returnTypeDeclared.getTypeArguments().get(0);
        if (typeArgumentMirror.getKind() == TypeKind.DECLARED) {

          Element returnTypeElement = returnTypeDeclared.asElement();

          if (context
              .getClassUtils()
              .isSame(returnTypeElement, MutinyMappedReactiveResultSet.class)) {

            // MutinyMappedReactiveResultSet<EntityT>
            TypeElement entityElement =
                EntityUtils.asEntityElement(typeArgumentMirror, typeParameters);
            if (entityElement != null) {
              return new DaoReturnType(
                  QuarkusDaoReturnTypeKind.MUTINY_MAPPED_REACTIVE_RESULT_SET, entityElement);
            }
          }

          if (context.getClassUtils().isSame(returnTypeElement, Multi.class)) {

            // Multi<Row>
            if (context.getClassUtils().isSame(typeArgumentMirror, Row.class)) {
              return MULTI_OF_ROW;
            }
            // Multi<ReactiveRow>
            if (context.getClassUtils().isSame(typeArgumentMirror, ReactiveRow.class)) {
              return MULTI_OF_REACTIVE_ROW;
            }
            // Multi<EntityT>
            TypeElement entityElement =
                EntityUtils.asEntityElement(typeArgumentMirror, typeParameters);
            if (entityElement != null) {
              return new DaoReturnType(QuarkusDaoReturnTypeKind.MULTI_OF_ENTITY, entityElement);
            }
          }

          if (context.getClassUtils().isSame(returnTypeElement, Uni.class)) {

            // Uni<Row>
            if (context.getClassUtils().isSame(typeArgumentMirror, Row.class)) {
              return UNI_OF_ROW;
            }
            // Uni<ReactiveRow>
            if (context.getClassUtils().isSame(typeArgumentMirror, ReactiveRow.class)) {
              return UNI_OF_REACTIVE_ROW;
            }
            // Uni<Void>
            if (context.getClassUtils().isSame(typeArgumentMirror, Void.class)) {
              return UNI_OF_VOID;
            }
            // Uni<Boolean>
            if (context.getClassUtils().isSame(typeArgumentMirror, Boolean.class)) {
              return UNI_OF_BOOLEAN;
            }
            // Uni<Long>
            if (context.getClassUtils().isSame(typeArgumentMirror, Long.class)) {
              return UNI_OF_LONG;
            }
            // Uni<EntityT>
            TypeElement entityElement =
                EntityUtils.asEntityElement(typeArgumentMirror, typeParameters);
            if (entityElement != null) {
              return new DaoReturnType(QuarkusDaoReturnTypeKind.UNI_OF_ENTITY, entityElement);
            }
          }
        }
      }
    }
    return super.parse(returnTypeMirror, typeParameters);
  }
}
