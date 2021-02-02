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

import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.mapper.DaoBeanProducer;
import com.datastax.oss.quarkus.runtime.internal.mapper.MapperBeanProducer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import io.smallrye.mutiny.Uni;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

public class QuarkusGeneratedNames {

  public static final ClassName COMPLETION_STAGE = ClassName.get(CompletionStage.class);

  public static final ParameterizedTypeName SESSION_FUTURE =
      ParameterizedTypeName.get(COMPLETION_STAGE, ClassName.get(QuarkusCqlSession.class));

  public static final ClassName INJECT = ClassName.get(Inject.class);

  public static final ClassName PRODUCES = ClassName.get(Produces.class);

  public static final ClassName APPLICATION_SCOPED = ClassName.get(ApplicationScoped.class);

  public static final ClassName UNI = ClassName.get(Uni.class);

  public static final ClassName DEFAULT = ClassName.get(Default.class);

  public static final ClassName MAPPER_BEAN_PRODUCER = ClassName.get(MapperBeanProducer.class);

  public static final ClassName DAO_BEAN_PRODUCER = ClassName.get(DaoBeanProducer.class);

  public static ClassName daoProducer(TypeElement mapperInterface) {
    String custom = mapperInterface.getAnnotation(Mapper.class).builderName();
    if (custom.isEmpty()) {
      return peerClass(mapperInterface, "Producer");
    } else {
      int i = custom.lastIndexOf('.');
      return ClassName.get(custom.substring(0, i), custom.substring(i + 1));
    }
  }

  // Content below is copied from GeneratedNames

  // Generates a non-nested peer class. If the base class is nested, the names of all enclosing
  // classes are prepended to ensure uniqueness. For example:
  // com.datastax.Foo.Bar.Baz => com.datastax.Foo_Bar_BazImpl
  private static ClassName peerClass(ClassName base, String suffix) {
    ClassName topLevel = base;
    StringBuilder prefix = new StringBuilder();
    while (topLevel.enclosingClassName() != null) {
      topLevel = topLevel.enclosingClassName();
      prefix.insert(0, '_').insert(0, topLevel.simpleName());
    }
    return topLevel.peerClass(prefix.toString() + base.simpleName() + suffix);
  }

  private static ClassName peerClass(
      TypeElement element, @SuppressWarnings("SameParameterValue") String suffix) {
    return peerClass(ClassName.get(element), suffix);
  }
}
