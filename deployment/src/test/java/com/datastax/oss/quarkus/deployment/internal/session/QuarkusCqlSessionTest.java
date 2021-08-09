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
package com.datastax.oss.quarkus.deployment.internal.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.dse.driver.api.core.config.DseDriverOption;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.groups.MultiSubscribe;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class QuarkusCqlSessionTest {

  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .overrideConfigKey("quarkus.cassandra.keyspace", "test_keyspace")
          .overrideConfigKey("quarkus.cassandra.auth.username", "alice")
          .overrideConfigKey("quarkus.cassandra.auth.password", "fakePasswordForTests")
          .overrideConfigKey("quarkus.cassandra.init.eager-init", "false")
          .overrideConfigKey("quarkus.cassandra.init.reconnect-on-init", "false")
          .overrideConfigKey("quarkus.cassandra.init.resolve-contact-points", "true")
          .overrideConfigKey("quarkus.cassandra.request.timeout", "PT10S")
          .overrideConfigKey("quarkus.cassandra.request.consistency-level", "ONE")
          .overrideConfigKey("quarkus.cassandra.request.serial-consistency-level", "LOCAL_SERIAL")
          .overrideConfigKey("quarkus.cassandra.request.page-size", "1000")
          .overrideConfigKey("quarkus.cassandra.request.default-idempotence", "true")
          .overrideConfigKey("quarkus.cassandra.graph.name", "test_graph")
          .overrideConfigKey("quarkus.cassandra.graph.request.timeout", "PT60S")
          .overrideConfigKey("quarkus.cassandra.graph.read-consistency-level", "QUORUM")
          .overrideConfigKey("quarkus.cassandra.graph.write-consistency-level", "QUORUM")
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClasses(CassandraTestResource.class)
                      .addAsResource("application.json")
                      .addAsResource("application.conf"));

  @Inject QuarkusCqlSession session;

  @Test
  public void should_execute_query_using_injected_cql_session() {
    assertThat(session.execute("SELECT * FROM system.local")).isNotEmpty();
  }

  @Test
  public void application_conf_settings_should_have_priority_over_reference_conf_from_driver() {
    DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();
    assertThat(profile.getDuration(DefaultDriverOption.CONFIG_RELOAD_INTERVAL))
        .isEqualTo(Duration.ofMinutes(2));
  }

  @Test
  public void application_json_settings_should_have_priority_over_reference_conf_from_driver() {
    DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();
    assertThat(profile.getBoolean(DefaultDriverOption.LOAD_BALANCING_POLICY_SLOW_AVOIDANCE))
        .isFalse();
  }

  @Test
  public void should_configure_connection_settings() {
    DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();
    assertThat(profile.getStringList(DefaultDriverOption.CONTACT_POINTS)).hasSize(1);
    assertThat(profile.getString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER))
        .isEqualTo("datacenter1");
    assertThat(profile.getString(DefaultDriverOption.SESSION_KEYSPACE)).isEqualTo("test_keyspace");
  }

  @Test
  public void should_configure_init_settings() {
    DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();
    assertThat(profile.getBoolean(DefaultDriverOption.RECONNECT_ON_INIT)).isFalse();
    assertThat(profile.getBoolean(DefaultDriverOption.RESOLVE_CONTACT_POINTS)).isTrue();
  }

  @Test
  public void should_configure_request_settings() {
    DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();
    assertThat(profile.getDuration(DefaultDriverOption.REQUEST_TIMEOUT))
        .isEqualTo(Duration.ofSeconds(10));
    assertThat(profile.getString(DefaultDriverOption.REQUEST_CONSISTENCY)).isEqualTo("ONE");
    assertThat(profile.getString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY))
        .isEqualTo("LOCAL_SERIAL");
    assertThat(profile.getInt(DefaultDriverOption.REQUEST_PAGE_SIZE)).isEqualTo(1000);
    assertThat(profile.getBoolean(DefaultDriverOption.REQUEST_DEFAULT_IDEMPOTENCE)).isTrue();
  }

  @Test
  public void should_configure_graph_settings() {
    DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();
    assertThat(profile.getString(DseDriverOption.GRAPH_NAME)).isEqualTo("test_graph");
    assertThat(profile.getString(DseDriverOption.GRAPH_READ_CONSISTENCY_LEVEL)).isEqualTo("QUORUM");
    assertThat(profile.getString(DseDriverOption.GRAPH_WRITE_CONSISTENCY_LEVEL))
        .isEqualTo("QUORUM");
    assertThat(profile.getDuration(DseDriverOption.GRAPH_TIMEOUT))
        .isEqualTo(Duration.ofSeconds(60));
  }

  @Test
  public void should_execute_reactive_query_string() {
    // when
    MutinyReactiveResultSet reactiveRowMulti =
        session.executeReactive("select * from system.local");

    // then
    validateReactiveRowNotEmpty(reactiveRowMulti);
    validateExecutionInfoNotEmpty(reactiveRowMulti);
    validateWasApplied(reactiveRowMulti);
    validateColumnDefinitions(reactiveRowMulti);
  }

  @Test
  public void should_execute_reactive_query_statement() {
    // when
    MutinyReactiveResultSet reactiveRowMulti =
        session.executeReactive(SimpleStatement.newInstance("select * from system.local"));

    // then
    validateReactiveRowNotEmpty(reactiveRowMulti);
    validateExecutionInfoNotEmpty(reactiveRowMulti);
    validateWasApplied(reactiveRowMulti);
    validateColumnDefinitions(reactiveRowMulti);
  }

  private void validateReactiveRowNotEmpty(Multi<ReactiveRow> reactiveRowMulti) {
    MultiSubscribe<ReactiveRow> result = reactiveRowMulti.subscribe();
    List<ReactiveRow> collect = result.asIterable().stream().collect(Collectors.toList());
    assertThat(collect).isNotEmpty();
  }

  private void validateExecutionInfoNotEmpty(MutinyReactiveResultSet reactiveRowMulti) {
    MultiSubscribe<ExecutionInfo> result = reactiveRowMulti.getExecutionInfos().subscribe();
    List<ExecutionInfo> collect = result.asIterable().stream().collect(Collectors.toList());
    assertThat(collect.get(0).getResponseSizeInBytes()).isGreaterThan(0);
  }

  private void validateWasApplied(MutinyReactiveResultSet reactiveRowMulti) {
    MultiSubscribe<Boolean> result = reactiveRowMulti.wasApplied().subscribe();
    List<Boolean> collect = result.asIterable().stream().collect(Collectors.toList());
    assertThat(collect.get(0)).isTrue();
  }

  private void validateColumnDefinitions(MutinyReactiveResultSet reactiveRowMulti) {
    MultiSubscribe<ColumnDefinitions> result = reactiveRowMulti.getColumnDefinitions().subscribe();
    List<ColumnDefinitions> collect = result.asIterable().stream().collect(Collectors.toList());
    assertThat(collect.get(0)).isNotEmpty();
  }
}
