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
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.mapper.DaoBeanProducer;
import com.datastax.oss.quarkus.runtime.internal.mapper.MapperBeanProducer;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
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

  @Inject Instance<CompletionStage<QuarkusCqlSession>> sessionStages;

  @Inject @MapperBeanProducer Instance<Object> mappers;

  @Inject @DaoBeanProducer Instance<Object> daos;

  @SuppressWarnings("unused")
  public void startup(@Observes StartupEvent event)
      throws ExecutionException, InterruptedException {
    if (config.cassandraClientInitConfig.eagerSessionInit) {
      LOG.info("Eagerly initializing Quarkus Cassandra client");
      initializeSessions();
      initializeGeneratedBeans(mappers, "mapper");
      initializeGeneratedBeans(daos, "DAO");
    } else {
      LOG.debug(
          "Eager initialization of Quarkus Cassandra client at startup is disabled by configuration");
    }
  }

  private void initializeSessions() throws InterruptedException, ExecutionException {
    Duration timeout = config.cassandraClientInitConfig.eagerSessionInitTimeout;
    for (CompletionStage<QuarkusCqlSession> sessionStage : sessionStages) {
      try {
        sessionStage.toCompletableFuture().get(timeout.toNanos(), TimeUnit.NANOSECONDS);
      } catch (TimeoutException e) {
        LOG.warn(
            "Eager initialization of a Quarkus Cassandra session bean did not complete within {}; "
                + "resuming application startup with uninitialized session",
            timeout);
      }
    }
  }

  private void initializeGeneratedBeans(Instance<Object> beans, String beanName)
      throws InterruptedException, ExecutionException {
    Duration timeout = config.cassandraClientInitConfig.eagerSessionInitTimeout;
    for (Object bean : beans) {
      try {
        if (bean instanceof CompletionStage) {
          ((CompletionStage<?>) bean)
              .toCompletableFuture()
              .get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } else if (bean instanceof Uni) {
          ((Uni<?>) bean).await().atMost(timeout);
        }
      } catch (TimeoutException | io.smallrye.mutiny.TimeoutException e) {
        LOG.warn(
            "Eager initialization of a {} bean did not complete within {}; "
                + "resuming application startup with uninitialized bean",
            beanName,
            timeout);
      }
    }
  }
}
