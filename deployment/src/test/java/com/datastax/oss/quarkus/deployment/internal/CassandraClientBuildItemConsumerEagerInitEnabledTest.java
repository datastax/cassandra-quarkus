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
package com.datastax.oss.quarkus.deployment.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.quarkus.QuarkusCqlSessionState;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.arc.Arc;
import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.test.QuarkusUnitTest;
import java.lang.reflect.Type;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import javax.enterprise.util.TypeLiteral;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CassandraClientBuildItemConsumerEagerInitEnabledTest {

  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .withConfigurationResource("application-eager-session-init-enabled.properties")
          .addBuildChainCustomizer(buildCustomizer());

  private static final Type COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE =
      new TypeLiteral<CompletionStage<QuarkusCqlSession>>() {}.getType();

  @Test
  public void should_have_quarkus_cql_session_in_the_di_container_with_state_initialized() {
    assertThat(Arc.container().instance(QuarkusCqlSession.class).get()).isNotNull();
    assertThat(Arc.container().instance(QuarkusCqlSessionState.class).get().isInitialized())
        .isTrue();
  }

  @Test
  public void
      should_have_completion_stage_of_quarkus_cql_session_in_the_di_container_with_state_initialized() {
    assertThat(Arc.container().instance(COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE).get())
        .isNotNull();
    assertThat(Arc.container().instance(QuarkusCqlSessionState.class).get().isInitialized())
        .isTrue();
  }

  protected static Consumer<BuildChainBuilder> buildCustomizer() {
    // This represents the extension.
    return builder ->
        builder
            .addBuildStep(
                context -> {
                  context.consume(CassandraClientBuildItem.class);
                  context.produce(new FeatureBuildItem("dummy"));
                })
            .consumes(CassandraClientBuildItem.class)
            .produces(FeatureBuildItem.class)
            .build();
  }
}
