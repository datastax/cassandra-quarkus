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

  @Inject MeterRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest config =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .setForcedDependencies(
              Arrays.asList(
                  new AppArtifact("io.quarkus", "quarkus-micrometer", Version.getVersion()),
                  new AppArtifact("io.quarkus", "quarkus-resteasy", Version.getVersion())))
          .withConfigurationResource("application-metrics.properties");

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
                registry.getMeters().stream()
                    .filter(metric -> filterAllCassandraMetrics(metric.getId()))
                    .count())
        .isEqualTo(numberOfRootMetrics);
  }

  private boolean filterAllCassandraMetrics(Id id) {
    return id.getName().startsWith(CassandraClientConfig.CONFIG_NAME);
  }
}
