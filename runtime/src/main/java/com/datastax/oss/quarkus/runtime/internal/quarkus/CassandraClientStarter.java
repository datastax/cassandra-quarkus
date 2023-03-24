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
package com.datastax.oss.quarkus.runtime.internal.quarkus;

import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.mapper.QuarkusGeneratedDaoBean;
import com.datastax.oss.quarkus.runtime.api.mapper.QuarkusGeneratedMapperBean;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.arc.ClientProxy;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that triggers eager initialization of {@link QuarkusCqlSession} and DAOs.
 *
 * @see <a href="https://quarkus.io/guides/cdi-reference#eager-instantiation-of-beans">Eager
 *     Instantiation of Beans</a>
 */
@Dependent
public class CassandraClientStarter {

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientStarter.class);

  @Inject CassandraClientConfig config;
  @Inject Instance<CompletionStage<QuarkusCqlSession>> sessions;
  @Inject @QuarkusGeneratedMapperBean Instance<Object> mappers;
  @Inject @QuarkusGeneratedDaoBean Instance<Object> daos;

  @SuppressWarnings("unused")
  public void onStartup(@Observes StartupEvent event)
      throws ExecutionException, InterruptedException {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "CassandraClientStarter.startup, eager = {}, sessions = {}, mappers = {}, daos = {}",
          config.cassandraClientInitConfig.eagerInit,
          sessions.stream().count(),
          mappers.stream().count(),
          daos.stream().count());
    }
    if (config.cassandraClientInitConfig.eagerInit) {
      LOG.info("Eagerly initializing Quarkus Cassandra client.");
      Duration timeout = config.cassandraClientInitConfig.eagerInitTimeout;
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<Void> initFuture =
          executor.submit(
              () -> {
                initializeBeans(sessions);
                initializeBeans(mappers);
                initializeBeans(daos);
                return null;
              });
      try {
        initFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        initFuture.cancel(true);
        LOG.warn(
            "Eager initialization of Quarkus Cassandra client did not complete within {}; "
                + "resuming application startup with an uninitialized client.",
            timeout);
      }
      executor.shutdownNow();
    } else {
      LOG.debug(
          "Eager initialization of Quarkus Cassandra client at startup is disabled by configuration.");
    }
  }

  private void initializeBeans(Instance<?> beans) throws InterruptedException, ExecutionException {
    for (Object bean : beans) {
      ClientProxy.unwrap(bean);
      if (bean instanceof CompletionStage) {
        ((CompletionStage<?>) bean).toCompletableFuture().get();
      } else if (bean instanceof Uni) {
        ((Uni<?>) bean).await().indefinitely();
      }
    }
  }
}
