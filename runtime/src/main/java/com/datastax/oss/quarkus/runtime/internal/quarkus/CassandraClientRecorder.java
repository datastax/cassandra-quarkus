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

import com.datastax.oss.driver.api.core.AsyncAutoCloseable;
import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.metrics.MetricsConfig;
import com.datastax.oss.quarkus.runtime.internal.metrics.NoopMetricRegistry;
import io.netty.channel.EventLoopGroup;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.netty.MainEventLoopGroup;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.metrics.MetricRegistries;
import java.lang.reflect.Type;
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

  public RuntimeValue<CompletionStage<QuarkusCqlSession>> buildClient(
      ShutdownContext shutdown, BeanContainer beanContainer) {
    QuarkusCqlSessionState quarkusCqlSessionState =
        beanContainer.instance(QuarkusCqlSessionState.class);
    @SuppressWarnings("unchecked")
    CompletionStage<QuarkusCqlSession> cqlSession =
        (CompletionStage<QuarkusCqlSession>)
            Arc.container().instance(COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE).get();
    shutdown.addShutdownTask(
        () -> {
          // invoke close() on session only, if it was initialized.
          // If the close() will be called on the non-initialized QuarkusCqlSession, it would
          // trigger the connection and close it immediately
          if (quarkusCqlSessionState.isInitialized()) {
            LOG.info("Closing the QuarkusCqlSession.");
            cqlSession.thenAccept(AsyncAutoCloseable::close);
          }
        });
    return new RuntimeValue<>(cqlSession);
  }

  public void configureMetrics(MetricsConfig metricsConfig) {
    CassandraClientProducer producer = getProducerInstance();
    producer.setMetricsConfig(metricsConfig);
  }

  public void setInjectedMetricRegistry() {
    CassandraClientProducer producer = getProducerInstance();
    MetricRegistry metricRegistry = MetricRegistries.get(MetricRegistry.Type.VENDOR);
    producer.setMetricRegistry(metricRegistry);
  }

  public void setNoopMetricRegistry() {
    CassandraClientProducer producer = getProducerInstance();
    producer.setMetricRegistry(new NoopMetricRegistry());
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
