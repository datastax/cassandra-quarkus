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

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.Row;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.assertj.core.util.Lists;

public class MockAsyncResultSet implements AsyncResultSet {

  public static CompletableFuture<AsyncResultSet> createResults(int numPages, int elementsPerPage) {
    CompletableFuture<AsyncResultSet> previous = null;
    for (int i = 0; i < numPages; i++) {
      List<Row> rows = new ArrayList<>();
      for (int j = 0; j < elementsPerPage; j++) {
        Row row = mock(Row.class);
        when(row.getInt(0)).thenReturn(i);
        rows.add(row);
      }
      CompletableFuture<AsyncResultSet> future = new CompletableFuture<>();
      future.complete(new MockAsyncResultSet(rows, previous));
      previous = future;
    }
    return previous;
  }

  private final List<Row> rows;
  private final Iterator<Row> iterator;
  private final CompletionStage<AsyncResultSet> nextPage;
  private final ExecutionInfo executionInfo = mock(ExecutionInfo.class);
  private final ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);

  private int remaining;

  public MockAsyncResultSet(List<Row> rows, CompletionStage<AsyncResultSet> nextPage) {
    this.rows = rows;
    iterator = rows.iterator();
    remaining = rows.size();
    this.nextPage = nextPage;
  }

  @Override
  public Row one() {
    Row next = iterator.next();
    remaining--;
    return next;
  }

  @Override
  public int remaining() {
    return remaining;
  }

  @NonNull
  @Override
  public List<Row> currentPage() {
    return Lists.newArrayList(rows);
  }

  @Override
  public boolean hasMorePages() {
    return nextPage != null;
  }

  @NonNull
  @Override
  public CompletionStage<AsyncResultSet> fetchNextPage() throws IllegalStateException {
    return nextPage;
  }

  @NonNull
  @Override
  public ColumnDefinitions getColumnDefinitions() {
    return columnDefinitions;
  }

  @NonNull
  @Override
  public ExecutionInfo getExecutionInfo() {
    return executionInfo;
  }

  @Override
  public boolean wasApplied() {
    return true;
  }
}
