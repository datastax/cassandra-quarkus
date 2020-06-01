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
package com.datastax.oss.quarkus.runtime.internal.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.dse.driver.api.core.graph.AsyncGraphResultSet;
import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.dse.driver.internal.core.cql.reactive.DefaultReactiveResultSet;
import com.datastax.dse.driver.internal.core.graph.reactive.DefaultReactiveGraphResultSet;
import com.datastax.dse.driver.internal.mapper.reactive.DefaultMappedReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import io.reactivex.Flowable;
import io.smallrye.mutiny.Multi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class MutinyReactiveResultSetTest {
  private static final int NUMBER_OF_ELEMENTS = 20;

  @Test
  public void should_validate_mapped_reactive_result_set() {
    // given
    List<Integer> items = new ArrayList<>();

    // when
    MutinyMappedReactiveResultSet<Integer> resultSet =
        new DefaultMutinyMappedReactiveResultSet<>(
            new DefaultMappedReactiveResultSet<>(
                new DefaultReactiveResultSet(MutinyReactiveResultSetTest::createResults),
                row -> row.getInt(0)));

    // then
    resultSet.subscribe().with(items::add);
    assertThat(items.size()).isEqualTo(NUMBER_OF_ELEMENTS);
  }

  @Test
  public void should_validate_failed_mapped_resultSet() {
    // given
    List<Integer> items = new ArrayList<>();

    // when
    MutinyMappedReactiveResultSet<Integer> resultSet =
        new FailedMutinyMappedReactiveResultSet<>(new Throwable("error"));

    // then
    resultSet.subscribe().with(items::add);
    assertThat(items.size()).isEqualTo(0);
  }

  @Test
  public void should_validate_reactive_result_set() {
    // given
    List<Integer> items = new ArrayList<>();

    // when

    Multi<Integer> resultSet =
        new DefaultMutinyReactiveResultSet(
                new DefaultReactiveResultSet(MutinyReactiveResultSetTest::createResults))
            .map(row -> row.getInt(0));

    // then
    resultSet.subscribe().with(items::add);
    assertThat(items.size()).isEqualTo(NUMBER_OF_ELEMENTS);
  }

  @Test
  public void should_validate_graph_reactive_result_set() {
    // given
    List<Integer> items = new ArrayList<>();

    // when
    Multi<Integer> resultSet =
        new DefaultMutinyGraphReactiveResultSet(
                new DefaultReactiveGraphResultSet(MutinyReactiveResultSetTest::createGraphResults))
            .map(GraphNode::asInt);

    // then
    resultSet.subscribe().with(items::add);
    assertThat(items.size()).isEqualTo(NUMBER_OF_ELEMENTS);
  }

  private static CompletableFuture<AsyncResultSet> createResults() {
    CompletableFuture<AsyncResultSet> previous = null;
    // create pages of 5 elements each to exercise pagination
    List<Integer> pages =
        Flowable.range(0, NUMBER_OF_ELEMENTS).buffer(5).map(List::size).toList().blockingGet();
    Collections.reverse(pages);
    for (Integer size : pages) {
      List<Row> rows =
          Flowable.range(0, size)
              .map(
                  i -> {
                    Row row = mock(Row.class);
                    when(row.getInt(0)).thenReturn(i);
                    return row;
                  })
              .toList()
              .blockingGet();
      CompletableFuture<AsyncResultSet> future = new CompletableFuture<>();
      future.complete(new MockAsyncResultSet(rows, previous));
      previous = future;
    }
    return previous;
  }

  private static CompletableFuture<AsyncGraphResultSet> createGraphResults() {
    CompletableFuture<AsyncGraphResultSet> previous = null;
    // create pages of 5 elements each to exercise pagination
    List<Integer> pages =
        Flowable.range(0, NUMBER_OF_ELEMENTS).buffer(5).map(List::size).toList().blockingGet();
    Collections.reverse(pages);
    for (Integer size : pages) {
      List<GraphNode> rows =
          Flowable.range(0, size)
              .map(
                  i -> {
                    GraphNode node = mock(GraphNode.class);
                    when(node.asInt()).thenReturn(i);
                    return node;
                  })
              .toList()
              .blockingGet();
      CompletableFuture<AsyncGraphResultSet> future = new CompletableFuture<>();
      future.complete(new MockAsyncGraphResultSet(rows, previous));
      previous = future;
    }
    return previous;
  }
}
