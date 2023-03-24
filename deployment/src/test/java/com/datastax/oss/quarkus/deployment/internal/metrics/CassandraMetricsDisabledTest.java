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

import com.datastax.oss.driver.internal.core.context.InternalDriverContext;
import com.datastax.oss.driver.internal.core.metrics.DefaultMetricsFactory;
import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.builder.Version;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import java.util.Arrays;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class CassandraMetricsDisabledTest {

  @Inject QuarkusCqlSession cqlSession;

  @Inject CassandraClientConfig config;

  @Inject MeterRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest quarkus =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))

          // Micrometer is present...
          .setForcedDependencies(
              Arrays.asList(
                  new AppArtifact("io.quarkus", "quarkus-micrometer", Version.getVersion()),
                  new AppArtifact("io.quarkus", "quarkus-resteasy", Version.getVersion())))
          // but Cassandra metrics are disabled
          .overrideConfigKey("quarkus.cassandra.metrics.enabled", "false");

  @Test
  public void should_not_enable_metrics_when_metrics_disabled_by_configuration() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    assertThat(((InternalDriverContext) cqlSession.getContext()).getMetricsFactory())
        .isInstanceOf(DefaultMetricsFactory.class);
    assertThat(((InternalDriverContext) cqlSession.getContext()).getMetricRegistry()).isNull();
    assertThat(cqlSession.getMetrics()).isEmpty();

    // then
    assertThat(
            registry.getMeters().stream()
                .filter(metric -> filterAllCassandraMetrics(metric.getId()))
                .count())
        .isZero();
  }

  private boolean filterAllCassandraMetrics(Id id) {
    return id.getName().startsWith(config.cassandraClientMetricsConfig.prefix);
  }
}
