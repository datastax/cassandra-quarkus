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
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class MetricsCapabilityNotPresentTest {

  @Inject QuarkusCqlSession cqlSession;

  @RegisterExtension
  static final QuarkusUnitTest config =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))

          // Cassandra metrics are enabled, but no metrics capability is present
          .overrideConfigKey("quarkus.cassandra.metrics.enabled", "true");

  @Test
  public void should_not_enable_metrics_when_metrics_disabled_by_configuration() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    assertThat(((InternalDriverContext) cqlSession.getContext()).getMetricsFactory())
        .isInstanceOf(DefaultMetricsFactory.class);
    assertThat(((InternalDriverContext) cqlSession.getContext()).getMetricRegistry()).isNull();
    assertThat(cqlSession.getMetrics()).isEmpty();
  }
}
