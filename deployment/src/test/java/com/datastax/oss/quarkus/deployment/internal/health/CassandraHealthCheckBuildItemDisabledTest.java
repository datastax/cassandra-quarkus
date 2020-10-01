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

import com.datastax.oss.quarkus.runtime.internal.health.CassandraAsyncHealthCheck;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.arc.Arc;
import io.quarkus.test.QuarkusUnitTest;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CassandraHealthCheckBuildItemDisabledTest {
  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestResource.class))
          .withConfigurationResource("application-health-disabled.properties");

  @Test
  public void should_not_have_health_check_in_the_container() {
    Set<Bean<?>> beans = Arc.container().beanManager().getBeans(CassandraAsyncHealthCheck.class);
    assertThat(beans.size()).isZero();
  }
}
