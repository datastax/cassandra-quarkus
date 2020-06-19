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
package com.datastax.oss.quarkus.deployment.internal;

import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_PAGE_SIZE;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.quarkus.deployment.internal.tests.CassandraTestResource;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class CassandraClientConfigTest {

  @RegisterExtension
  static final QuarkusUnitTest config =
      new QuarkusUnitTest()
          .withConfigurationResource("application-cassandra-client.properties")
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClasses(CassandraTestResource.class)
                      .addAsResource("application.json")
                      .addAsResource("application.conf"));

  @Inject QuarkusCqlSession cqlSession;

  @Test
  public void should_execute_query_using_injected_cql_session() {
    assertThat(cqlSession.execute("SELECT * FROM system.local")).isNotEmpty();
  }

  @Test
  public void should_load_settings_from_application_properties() {
    DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

    assertThat(profile.getString(LOAD_BALANCING_LOCAL_DATACENTER)).isEqualTo("datacenter1");
  }

  @Test
  public void application_conf_settings_should_have_priority_over_reference_conf_from_driver() {
    DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

    assertThat(profile.getDuration(REQUEST_TIMEOUT)).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
  }

  @Test
  public void application_json_settings_should_have_priority_over_reference_conf_from_driver() {
    DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

    assertThat(profile.getInt(REQUEST_PAGE_SIZE)).isEqualTo(1000);
  }
}
