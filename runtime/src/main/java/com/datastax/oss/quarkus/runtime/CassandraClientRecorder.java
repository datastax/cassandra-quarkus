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
package com.datastax.oss.quarkus.runtime;

import com.datastax.oss.quarkus.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.metrics.MetricsConfig;
import com.datastax.oss.quarkus.runtime.metrics.NoopMetricRegistry;
import io.netty.channel.EventLoopGroup;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.netty.MainEventLoopGroup;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.metrics.MetricRegistries;
import javax.enterprise.util.AnnotationLiteral;
import org.eclipse.microprofile.metrics.MetricRegistry;

@Recorder
public class CassandraClientRecorder {

  public void configureRuntimeProperties(CassandraClientConfig config) {
    CassandraClientProducer producer = getProducerInstance();
    producer.setCassandraClientConfig(config);
  }

  public RuntimeValue<QuarkusCqlSession> buildClient(
      ShutdownContext shutdown, BeanContainer beanContainer) {
    QuarkusCqlSession cqlSession = beanContainer.instance(QuarkusCqlSession.class);
    shutdown.addShutdownTask(cqlSession::close);
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
    producer.setUseQuarkusNettyEventLoop(useQuarkusNettyEventLoop);
  }

  private CassandraClientProducer getProducerInstance() {
    return Arc.container().instance(CassandraClientProducer.class).get();
  }
}
