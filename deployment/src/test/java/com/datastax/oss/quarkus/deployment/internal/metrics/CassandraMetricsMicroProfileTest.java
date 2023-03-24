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
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.builder.Version;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import java.util.Collections;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metered;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricRegistry.Type;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class CassandraMetricsMicroProfileTest {

  @Inject QuarkusCqlSession cqlSession;

  @Inject CassandraClientConfig config;

  @Inject
  @RegistryType(type = Type.VENDOR)
  MetricRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest quarkus =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .setForcedDependencies(
              Collections.singletonList(
                  new AppArtifact("io.quarkus", "quarkus-smallrye-metrics", Version.getVersion())))
          .overrideConfigKey("quarkus.cassandra.metrics.enabled", "true");

  @Test
  public void should_expose_driver_metrics_via_meter_registry() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    registry.getMetrics().entrySet().stream()
        .filter(entry -> filterAllCassandraMetrics(entry.getKey()))
        .forEach(
            entry -> {
              Metric metric = entry.getValue();
              if (metric instanceof Gauge) {
                @SuppressWarnings("unchecked")
                long value = ((Gauge<Number>) metric).getValue().longValue();
                assertThat(value).isGreaterThanOrEqualTo(0L);
              } else if (metric instanceof Metered) {
                assertThat(((Metered) metric).getCount()).isGreaterThanOrEqualTo(0);
              } else if (metric instanceof Counter) {
                assertThat(((Counter) metric).getCount()).isGreaterThanOrEqualTo(0);
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
                registry.getMetrics().keySet().stream()
                    .filter(this::filterAllCassandraMetrics)
                    .count())
        .isEqualTo(numberOfRootMetrics);
  }

  private boolean filterAllCassandraMetrics(MetricID id) {
    return id.getName().startsWith(config.cassandraClientMetricsConfig.prefix);
  }
}
