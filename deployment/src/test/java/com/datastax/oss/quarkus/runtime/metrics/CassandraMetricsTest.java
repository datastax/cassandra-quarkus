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
package com.datastax.oss.quarkus.runtime.metrics;

import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.BYTES_RECEIVED;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.BYTES_SENT;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CONNECTED_NODES;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CQL_CLIENT_TIMEOUTS;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CQL_PREPARED_CACHE_SIZE;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CQL_REQUESTS;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.THROTTLING_DELAY;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.THROTTLING_ERRORS;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.THROTTLING_QUEUE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.CassandraTestBase;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metered;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestBase.class)
public class CassandraMetricsTest {

  @Inject CqlSession cqlSession;

  @Inject
  @RegistryType(type = MetricRegistry.Type.VENDOR)
  MetricRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest config =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestBase.class))
          .withConfigurationResource("application-metrics.properties");

  @Test
  void testMetricsInitialization() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    assertThat(getGaugeValue(cqlSession.getName(), CONNECTED_NODES.getPath())).isEqualTo(1L);
    assertThat(getGaugeValue(cqlSession.getName(), THROTTLING_QUEUE_SIZE.getPath())).isEqualTo(0L);
    assertThat(getMeteredValue(cqlSession.getName(), BYTES_RECEIVED.getPath()).getCount())
        .isGreaterThan(0L);
    assertThat(getMeteredValue(cqlSession.getName(), BYTES_SENT.getPath()).getCount())
        .isGreaterThan(0L);
    assertThat(getMeteredValue(cqlSession.getName(), CQL_REQUESTS.getPath()).getCount())
        .isEqualTo(1L);
    assertThat(getMeteredValue(cqlSession.getName(), THROTTLING_DELAY.getPath()).getCount())
        .isEqualTo(0L);
    assertThat(getCounterValue(cqlSession.getName(), CQL_CLIENT_TIMEOUTS.getPath())).isEqualTo(0L);
    assertThat(getCounterValue(cqlSession.getName(), THROTTLING_ERRORS.getPath())).isEqualTo(0L);
    assertThat(getGaugeValue(cqlSession.getName(), CQL_PREPARED_CACHE_SIZE.getPath()))
        .isEqualTo(0L);
  }

  @SuppressWarnings("unchecked")
  private Number getGaugeValue(String sessionPrefix, String metricName) {
    MetricID metricID = new MetricID(buildMetricName(sessionPrefix, metricName));
    Metric metric = registry.getMetrics().get(metricID);
    return ((Gauge<Number>) metric).getValue().longValue();
  }

  private Metered getMeteredValue(String sessionPrefix, String metricName) {
    MetricID metricID = new MetricID(buildMetricName(sessionPrefix, metricName));
    Metric metric = registry.getMetrics().get(metricID);
    return ((Metered) metric);
  }

  private long getCounterValue(String sessionPrefix, String metricName) {
    MetricID metricID = new MetricID(buildMetricName(sessionPrefix, metricName));
    Metric metric = registry.getMetrics().get(metricID);
    return ((Counter) metric).getCount();
  }

  private String buildMetricName(String sessionPrefix, String metricName) {
    return String.format("%s.%s", sessionPrefix, metricName);
  }
}
