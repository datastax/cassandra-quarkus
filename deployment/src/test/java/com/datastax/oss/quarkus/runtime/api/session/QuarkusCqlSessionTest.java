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
package com.datastax.oss.quarkus.runtime.api.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.quarkus.CassandraTestBase;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.groups.MultiSubscribe;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestBase.class)
public class QuarkusCqlSessionTest {
  @Inject QuarkusCqlSession quarkusCqlSession;

  @RegisterExtension
  static QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestBase.class))
          .withConfigurationResource("application-cassandra-client.properties");

  @Test
  public void should_execute_reactive_query_string() {
    // when
    MutinyReactiveResultSet reactiveRowMulti =
        quarkusCqlSession.executeReactive("select * from system.local");

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
        quarkusCqlSession.executeReactive(
            SimpleStatement.newInstance("select * from system.local"));

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
