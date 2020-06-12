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
package com.datastax.oss.quarkus.deployment.internal.metrics;

import static com.datastax.oss.quarkus.runtime.internal.metrics.MicroProfileMetricsUpdater.CASSANDRA_METRICS_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.quarkus.deployment.internal.tests.CassandraTestResource;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
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

@QuarkusTestResource(CassandraTestResource.class)
public class CassandraMetricsTest {

  @Inject QuarkusCqlSession cqlSession;

  @Inject
  @RegistryType(type = MetricRegistry.Type.VENDOR)
  MetricRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest config =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .withConfigurationResource("application-metrics.properties");

  @Test
  @SuppressWarnings("unchecked")
  public void should_expose_driver_metrics_via_meter_registry() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    registry.getMetrics().entrySet().stream()
        .filter(v -> filterAllCassandraMetrics(v.getKey()))
        .forEach(
            m -> {
              Metric metric = m.getValue();
              if (metric instanceof Gauge) {
                assertThat(((Gauge<Number>) metric).getValue().longValue())
                    .isGreaterThanOrEqualTo(0L);
              } else if (metric instanceof Metered) {
                assertThat(((Metered) metric).getCount()).isGreaterThanOrEqualTo(0);
              } else if (metric instanceof Counter) {
                assertThat(((Counter) metric).getCount()).isGreaterThanOrEqualTo(0);
              } else {
                throw new IllegalArgumentException("unsupported metric type");
              }
            });
  }

  @Test
  public void should_expose_correct_number_of_metrics() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    int numberOfRootMetrics =
        37; // number of metrics from java-driver in application-metrics.properties
    assertThat(
            (int)
                registry.getMetrics().keySet().stream()
                    .filter(this::filterAllCassandraMetrics)
                    .count())
        .isEqualTo(numberOfRootMetrics);
  }

  private boolean filterAllCassandraMetrics(MetricID v) {
    return v.getName().startsWith(CASSANDRA_METRICS_PREFIX);
  }
}
