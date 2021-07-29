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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.dse.driver.api.core.graph.AsyncGraphResultSet;
import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.dse.driver.internal.core.graph.GraphExecutionInfoConverter;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.assertj.core.util.Lists;

public class MockAsyncGraphResultSet implements AsyncGraphResultSet {

  public static CompletableFuture<AsyncGraphResultSet> createGraphResults(
      int numPages, int elementsPerPage) {
    CompletableFuture<AsyncGraphResultSet> previous = null;
    for (int i = 0; i < numPages; i++) {
      List<GraphNode> nodes = new ArrayList<>();
      for (int j = 0; j < elementsPerPage; j++) {
        GraphNode node = mock(GraphNode.class);
        when(node.asInt()).thenReturn(i);
        nodes.add(node);
      }
      CompletableFuture<AsyncGraphResultSet> future = new CompletableFuture<>();
      future.complete(new MockAsyncGraphResultSet(nodes, previous));
      previous = future;
    }
    return previous;
  }

  private final List<GraphNode> rows;
  private final Iterator<GraphNode> iterator;
  private final CompletionStage<AsyncGraphResultSet> nextPage;
  private final ExecutionInfo executionInfo = mock(ExecutionInfo.class);

  private int remaining;

  public MockAsyncGraphResultSet(
      List<GraphNode> rows, CompletionStage<AsyncGraphResultSet> nextPage) {
    this.rows = rows;
    iterator = rows.iterator();
    remaining = rows.size();
    this.nextPage = nextPage;
  }

  @Override
  public GraphNode one() {
    GraphNode next = iterator.next();
    remaining--;
    return next;
  }

  @NonNull
  @Override
  public ExecutionInfo getRequestExecutionInfo() {
    return executionInfo;
  }

  @Deprecated
  @NonNull
  @Override
  public com.datastax.dse.driver.api.core.graph.GraphExecutionInfo getExecutionInfo() {
    return GraphExecutionInfoConverter.convert(executionInfo);
  }

  @Override
  public int remaining() {
    return remaining;
  }

  @NonNull
  @Override
  public List<GraphNode> currentPage() {
    return Lists.newArrayList(rows);
  }

  @Override
  public boolean hasMorePages() {
    return nextPage != null;
  }

  @NonNull
  @Override
  public CompletionStage<AsyncGraphResultSet> fetchNextPage() throws IllegalStateException {
    return nextPage;
  }

  @Override
  public void cancel() {}
}
