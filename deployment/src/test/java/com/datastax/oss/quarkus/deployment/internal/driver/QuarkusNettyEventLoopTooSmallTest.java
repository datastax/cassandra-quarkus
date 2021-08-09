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
package com.datastax.oss.quarkus.deployment.internal.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.driver.internal.core.context.NettyOptions;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.context.QuarkusDriverContext;
import com.datastax.oss.quarkus.runtime.internal.driver.QuarkusNettyOptions;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.QuarkusUnitTest;
import io.vertx.core.impl.VertxThread;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class QuarkusNettyEventLoopTooSmallTest {

  @Inject QuarkusCqlSession cqlSession;

  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .overrideConfigKey("quarkus.cassandra.init.use-quarkus-event-loop", "true")
          .overrideConfigKey("quarkus.vertx.event-loops-pool-size", "2")
          .setLogRecordPredicate(
              log -> log.getMessage().contains("Main event loop pool size is too small"))
          .assertLogRecords(
              logs -> {
                assertThat(logs).hasSize(1);
                assertThat(logs.get(0).getParameters()).hasSize(1);
                assertThat(logs.get(0).getParameters()[0]).isEqualTo(2);
              });

  @Test
  public void should_use_quarkus_netty_event_loop()
      throws ExecutionException, InterruptedException {
    // when
    NettyOptions nettyOptions = ((QuarkusDriverContext) cqlSession.getContext()).getNettyOptions();

    // then
    assertThat(nettyOptions).isInstanceOf(QuarkusNettyOptions.class);

    // when
    AtomicReference<Class<? extends Thread>> threadClass = new AtomicReference<>();
    CompletionStage<Void> asyncRequest =
        cqlSession
            .executeAsync("SELECT release_version FROM system.local")
            .thenAccept(rs -> threadClass.set(Thread.currentThread().getClass()));
    asyncRequest.toCompletableFuture().get();

    // then
    assertThat(threadClass.get().getName()).isEqualTo(VertxThread.class.getName());
  }
}
