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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.builder.Version;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import java.util.Arrays;
import javax.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class CassandraMetricsMicrometerTest {

  @Inject QuarkusCqlSession cqlSession;

  @Inject CassandraClientConfig config;

  @Inject MeterRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest quarkus =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .setForcedDependencies(
              Arrays.asList(
                  new AppArtifact("io.quarkus", "quarkus-micrometer", Version.getVersion()),
                  new AppArtifact("io.quarkus", "quarkus-resteasy", Version.getVersion())))
          .overrideConfigKey("quarkus.cassandra.metrics.enabled", "true")
          // test a different prefix
          .overrideConfigKey("quarkus.cassandra.metrics.prefix", "custom.prefix");

  @Test
  public void should_expose_driver_metrics_via_meter_registry() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    registry.getMeters().stream()
        .filter(metric -> filterAllCassandraMetrics(metric.getId()))
        .forEach(
            metric -> {
              if (metric instanceof Gauge) {
                assertThat(((Gauge) metric).value()).isGreaterThanOrEqualTo(0L);
              } else if (metric instanceof Counter) {
                assertThat(((Counter) metric).count()).isGreaterThanOrEqualTo(0);
              } else if (metric instanceof Timer) {
                assertThat(((Timer) metric).count()).isGreaterThanOrEqualTo(0);
              } else {
                fail("unsupported metric type");
              }
            });
  }

  @Test
  public void should_expose_correct_number_of_metrics() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    int numberOfRootMetrics = 5 + 26; // 5 session metrics + 26 node metrics are enabled by default
    assertThat(
            (int)
                registry.getMeters().stream()
                    .filter(metric -> filterAllCassandraMetrics(metric.getId()))
                    .count())
        .isEqualTo(numberOfRootMetrics);
  }

  private boolean filterAllCassandraMetrics(Id id) {
    assertThat(config.cassandraClientMetricsConfig.prefix).isEqualTo("custom.prefix");
    return id.getName().startsWith(config.cassandraClientMetricsConfig.prefix);
  }
}
