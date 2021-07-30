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
import io.quarkus.arc.Arc;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.util.TypeLiteral;
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
                  LOG.info("Closing Quarkus Cassandra session.");
                  session.close();
                } else {
                  LOG.info("Cancelling Quarkus Cassandra session initialization.");
                  sessionFuture.cancel(true);
                }
              } catch (RuntimeException e) {
                // no need to log this again, it was logged already
                LOG.trace("Quarkus Cassandra session could not be closed normally.", e);
              }
            }
          }
        });
    return new RuntimeValue<>(sessionStage);
  }

  public void configureMicrometerMetrics() {
    LOG.info("Enabling Cassandra metrics using Micrometer.");
    try {
      Class<?> meterRegistryClass = Class.forName("io.micrometer.core.instrument.MeterRegistry");
      Object meterRegistry = Arc.container().instance(meterRegistryClass).get();
      CassandraClientProducer producer = getProducerInstance();
      producer.setMetricRegistry(meterRegistry);
      producer.setMetricsFactoryClassName(
          "com.datastax.oss.driver.internal.metrics.micrometer.MicrometerMetricsFactory");
    } catch (Exception e) {
      LOG.error("Failed to enable Cassandra metrics using Micrometer", e);
    }
  }

  public void configureMicroProfileMetrics() {
    LOG.info("Enabling Cassandra metrics using MicroProfile.");
    try {
      Object metricRegistry = locateMicroProfileVendorMetricRegistry();
      CassandraClientProducer producer = getProducerInstance();
      producer.setMetricRegistry(metricRegistry);
      producer.setMetricsFactoryClassName(
          "com.datastax.oss.driver.internal.metrics.microprofile.MicroProfileMetricsFactory");
    } catch (Exception e) {
      LOG.error("Failed to enable Cassandra metrics using MicroProfile", e);
    }
  }

  private Object locateMicroProfileVendorMetricRegistry()
      throws ClassNotFoundException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    Class<?> metricRegistriesClass = Class.forName("io.smallrye.metrics.MetricRegistries");
    Object metricRegistries = Arc.container().instance(metricRegistriesClass).get();
    return metricRegistriesClass.getMethod("getVendorRegistry").invoke(metricRegistries);
  }

  public void configureCompression(String protocolCompression) {
    LOG.debug("Configuring protocol compression {}", protocolCompression);
    CassandraClientProducer producer = getProducerInstance();
    producer.setProtocolCompression(protocolCompression);
  }

  private CassandraClientProducer getProducerInstance() {
    return Arc.container().instance(CassandraClientProducer.class).get();
  }

  public void addRequestTrackerClass(String clz) {
    CassandraClientProducer producer = getProducerInstance();
    producer.addRequestTrackerClass(clz);
  }

  public void addSchemaChangeListenerClass(String clz) {
    CassandraClientProducer producer = getProducerInstance();
    producer.addSchemaChangeListenerClass(clz);
  }

  public void addNodeStateListenerClass(String clz) {
    CassandraClientProducer producer = getProducerInstance();
    producer.addNodeStateListenerClass(clz);
  }
}
