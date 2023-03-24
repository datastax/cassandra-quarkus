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
package com.datastax.oss.quarkus.deployment.internal.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.datastax.oss.quarkus.runtime.internal.health.CassandraAsyncHealthCheck;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CassandraHealthCheckBuildItemDisabledTest {

  @Inject @Readiness Provider<CassandraAsyncHealthCheck> healthCheckProvider;

  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .overrideConfigKey("quarkus.cassandra.health.enabled", "false");

  @Test
  public void should_not_have_health_check_in_the_container() {
    assertThat(healthCheckProvider).isNotNull();
    assertThatThrownBy(healthCheckProvider::get)
        .isInstanceOf(UnsatisfiedResolutionException.class)
        .hasMessageContaining("No bean found for required type");
  }
}
