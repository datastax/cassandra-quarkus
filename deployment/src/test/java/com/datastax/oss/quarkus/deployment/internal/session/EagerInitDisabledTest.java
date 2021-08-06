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
package com.datastax.oss.quarkus.deployment.internal.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.quarkus.deployment.internal.CassandraClientBuildItem;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientProducer;
import com.datastax.oss.quarkus.runtime.internal.quarkus.CassandraClientRecorder;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.arc.Arc;
import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.test.QuarkusUnitTest;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EagerInitDisabledTest {

  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .overrideConfigKey("quarkus.cassandra.init.eager-init", "false")
          .addBuildChainCustomizer(buildCustomizer());

  @Test
  @Order(1)
  public void should_have_quarkus_cql_session_in_the_di_container_with_state_not_produced() {
    assertThat(Arc.container().instance(QuarkusCqlSession.class).get()).isNotNull();
    assertThat(Arc.container().instance(CassandraClientProducer.class).get().isProduced())
        .isFalse();
  }

  @Test
  @Order(2)
  public void
      should_have_completion_stage_of_quarkus_cql_session_in_the_di_container_with_state_not_produced() {
    // We need to avoid calling assertThat(CompletionStage<RESULT> actual) otherwise AssertJ
    // will call toCompletableFuture() underneath and ruin our test
    assertThat((Object) Arc.container().instance(CassandraClientRecorder.SESSION_STAGE).get())
        .isNotNull();
    assertThat(Arc.container().instance(CassandraClientProducer.class).get().isProduced())
        .isFalse();
  }

  @Test
  @Order(3)
  public void should_mark_quarkus_cql_session_produced_when_accessed_for_the_first_time() {
    Arc.container().instance(QuarkusCqlSession.class).get().getName();
    assertThat(Arc.container().instance(CassandraClientProducer.class).get().isProduced()).isTrue();
  }

  @Test
  @Order(3)
  public void
      should_mark_completion_stage_of_quarkus_cql_session_produced_when_accessed_for_the_first_time()
          throws ExecutionException, InterruptedException {
    CompletionStage<QuarkusCqlSession> completionStage =
        Arc.container().instance(CassandraClientRecorder.SESSION_STAGE).get();
    completionStage.toCompletableFuture().get();
    assertThat(Arc.container().instance(CassandraClientProducer.class).get().isProduced()).isTrue();
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
