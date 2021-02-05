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

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Recorder
public class CassandraClientRecorder {

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientRecorder.class);

  public static final TypeLiteral<CompletionStage<QuarkusCqlSession>> SESSION_STAGE =
      new TypeLiteral<CompletionStage<QuarkusCqlSession>>() {};

  public RuntimeValue<CompletionStage<QuarkusCqlSession>> buildClient(ShutdownContext shutdown) {
    LOG.debug("CassandraClientRecorder.buildClient");
    CompletionStage<QuarkusCqlSession> sessionStage = Arc.container().instance(SESSION_STAGE).get();
    shutdown.addShutdownTask(
        () -> {
          // invoke methods on the session stage bean only if it was produced;
          // trying to access a non-produced bean here would
          // trigger its production, and thus the initialization of the underlying session.
          CassandraClientProducer cassandraClientProducer =
              Arc.container().instance(CassandraClientProducer.class).get();
          if (cassandraClientProducer != null) {
            LOG.debug(
                "Executing shutdown hook, session stage bean produced = {}",
                cassandraClientProducer.isProduced());
            if (cassandraClientProducer.isProduced()) {
              CompletableFuture<QuarkusCqlSession> sessionFuture =
                  sessionStage.toCompletableFuture();
              LOG.debug(
                  "Session future done = {}, cancelled = {}",
                  sessionFuture.isDone(),
                  sessionFuture.isCancelled());
              try {
                QuarkusCqlSession session = sessionFuture.getNow(null);
                LOG.debug("Session object = {}", session);
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
          }
        });
    return new RuntimeValue<>(sessionStage);
  }

  private abstract static class RegistryTypeQualifier extends AnnotationLiteral<RegistryType>
      implements RegistryType {}

  public void configureMicrometerMetrics() {
    LOG.info("Enabling Cassandra metrics using Micrometer");
    MeterRegistry meterRegistry = Arc.container().instance(MeterRegistry.class).get();
    CassandraClientProducer producer = getProducerInstance();
    producer.setMetricRegistry(meterRegistry);
    producer.setMetricsFactoryClassName(
        "com.datastax.oss.driver.internal.metrics.micrometer.MicrometerMetricsFactory");
  }

  public void configureMicroProfileMetrics() {
    LOG.info("Enabling Cassandra metrics using MicroProfile");
    MetricRegistry metricRegistry =
        Arc.container()
            .instance(
                MetricRegistry.class,
                new RegistryTypeQualifier() {
                  @Override
                  public MetricRegistry.Type type() {
                    return MetricRegistry.Type.VENDOR;
                  }
                })
            .get();
    CassandraClientProducer producer = getProducerInstance();
    producer.setMetricRegistry(metricRegistry);
    producer.setMetricsFactoryClassName(
        "com.datastax.oss.driver.internal.metrics.microprofile.MicroProfileMetricsFactory");
  }

  public void configureCompression(String protocolCompression) {
    LOG.debug("Configuring protocol compression {}", protocolCompression);
    CassandraClientProducer producer = getProducerInstance();
    producer.setProtocolCompression(protocolCompression);
  }

  private CassandraClientProducer getProducerInstance() {
    return Arc.container().instance(CassandraClientProducer.class).get();
  }
}
