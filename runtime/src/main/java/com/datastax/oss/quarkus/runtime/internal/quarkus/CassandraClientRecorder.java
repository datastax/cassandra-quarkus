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
import io.netty.channel.EventLoopGroup;
import io.quarkus.arc.Arc;
import io.quarkus.netty.MainEventLoopGroup;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.metrics.MetricRegistries;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Recorder
public class CassandraClientRecorder {
  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientRecorder.class);
  private static final Type COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE =
      new TypeLiteral<CompletionStage<QuarkusCqlSession>>() {}.getType();

  public void configureRuntimeProperties(CassandraClientConfig config) {
    CassandraClientProducer producer = getProducerInstance();
    producer.setCassandraClientConfig(config);
  }

  public RuntimeValue<CompletionStage<QuarkusCqlSession>> buildClient(ShutdownContext shutdown) {
    LOG.trace("Requesting production of session stage bean");
    @SuppressWarnings("unchecked")
    CompletionStage<QuarkusCqlSession> sessionStage =
        (CompletionStage<QuarkusCqlSession>)
            Arc.container().instance(COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE).get();
    LOG.trace("Session stage bean produced: {}", sessionStage);
    shutdown.addShutdownTask(
        () -> {
          // invoke methods on the session stage bean only if it was produced;
          // trying to access a non-produced bean here would
          // trigger its production, and thus the initialization of the underlying session.
          QuarkusCqlSessionStageBeanState sessionState =
              Arc.container().instance(QuarkusCqlSessionStageBeanState.class).get();
          LOG.trace(
              "Executing shutdown hook, session stage bean produced = {}",
              sessionState.isProduced());
          if (sessionState.isProduced()) {
            CompletableFuture<QuarkusCqlSession> sessionFuture = sessionStage.toCompletableFuture();
            LOG.trace(
                "Session future done = {}, cancelled = {}",
                sessionFuture.isDone(),
                sessionFuture.isCancelled());
            try {
              QuarkusCqlSession session = sessionFuture.getNow(null);
              LOG.trace("Session object = {}", session);
              if (session != null) {
                LOG.info("Closing Quarkus session.");
                session.close();
              } else {
                LOG.info("Cancelling Quarkus session initialization.");
                sessionFuture.cancel(true);
              }
            } catch (RuntimeException e) {
              // no need to log this again, it was logged already
              LOG.trace("Quarkus session could not be closed normally.", e);
            }
          }
        });
    return new RuntimeValue<>(sessionStage);
  }

  public void configureMetrics(
      List<String> enabledNodeMetrics, List<String> enabledSessionMetrics) {
    CassandraClientProducer producer = getProducerInstance();
    MetricRegistry metricRegistry = MetricRegistries.get(MetricRegistry.Type.VENDOR);
    producer.setMetricRegistry(metricRegistry);
    producer.setEnabledNodeMetrics(enabledNodeMetrics);
    producer.setEnabledSessionMetrics(enabledSessionMetrics);
  }

  public void configureCompression(String protocolCompression) {
    CassandraClientProducer producer = getProducerInstance();
    producer.setProtocolCompression(protocolCompression);
  }

  public void setInjectedNettyEventLoop(boolean useQuarkusNettyEventLoop) {
    CassandraClientProducer producer = getProducerInstance();

    if (useQuarkusNettyEventLoop) {
      EventLoopGroup mainEventLoop =
          Arc.container()
              .instance(EventLoopGroup.class, new AnnotationLiteral<MainEventLoopGroup>() {})
              .get();

      producer.setMainEventLoop(mainEventLoop);
    }
  }

  private CassandraClientProducer getProducerInstance() {
    return Arc.container().instance(CassandraClientProducer.class).get();
  }
}
