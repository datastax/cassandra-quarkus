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

import com.datastax.oss.driver.api.mapper.annotations.QueryProvider;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.quarkus.internal.mapper.processor.QuarkusMapperProcessor;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class QuarkusDaoQueryProviderMethodGeneratorTest extends DaoMethodGeneratorTest {

  @NonNull
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @ParameterizedTest
  @MethodSource
  @Override
  public void should_fail_with_expected_error(String expectedError, MethodSpec method) {
    super.should_fail_with_expected_error(expectedError, method);
  }

  @SuppressWarnings("unused")
  static Object[][] should_fail_with_expected_error() {
    return new Object[][] {
      {
        "Invalid annotation configuration: the elements in QueryProvider.entityHelpers "
            + "must be Entity-annotated classes (offending element: java.lang.String)",
        MethodSpec.methodBuilder("select")
            .addAnnotation(
                AnnotationSpec.builder(QueryProvider.class)
                    // We don't go until instantiation, any class will do
                    .addMember("providerClass", "$T.class", String.class)
                    .addMember(
                        "entityHelpers", "{ $T.class, $T.class }", ENTITY_CLASS_NAME, String.class)
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build(),
      },
    };
  }
}
