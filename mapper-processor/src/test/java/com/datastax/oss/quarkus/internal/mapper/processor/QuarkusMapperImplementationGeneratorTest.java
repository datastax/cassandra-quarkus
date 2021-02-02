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

import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.mapper.MapperMethodGeneratorTest;
import com.squareup.javapoet.MethodSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;

class QuarkusMapperImplementationGeneratorTest extends MapperMethodGeneratorTest {

  @NonNull
  @Override
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @Test
  void should_fail_if_method_is_not_annotated() {
    this.should_fail_with_expected_error(
        "Unrecognized method signature: no implementation will be generated",
        MethodSpec.methodBuilder("productDao")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(DAO_CLASS_NAME)
            .build());
  }

  @Test
  void should_ignore_static_methods() {
    this.should_succeed_without_warnings(
        MethodSpec.methodBuilder("doNothing")
            .addModifiers(new Modifier[] {Modifier.PUBLIC, Modifier.STATIC})
            .build());
  }

  @Test
  void should_ignore_default_methods() {
    this.should_succeed_without_warnings(
        MethodSpec.methodBuilder("doNothing")
            .addModifiers(new Modifier[] {Modifier.PUBLIC, Modifier.DEFAULT})
            .build());
  }
}
