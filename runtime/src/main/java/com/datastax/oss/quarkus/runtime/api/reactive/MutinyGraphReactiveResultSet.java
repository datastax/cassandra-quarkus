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
package com.datastax.oss.quarkus.runtime.api.reactive;

import com.datastax.dse.driver.api.core.graph.GraphStatement;
import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphNode;
import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphResultSet;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;

/**
 * A marker interface for {@code Multi<ReactiveGraphNode>} results returned by {@link
 * QuarkusCqlSession}, when querying a Graph database.
 *
 * <p>Note: Graph databases are only available for DataStax Enterprise (DSE).
 *
 * @see QuarkusCqlSession#executeReactive(GraphStatement)
 * @see ReactiveGraphResultSet
 * @see <a href="https://www.datastax.com/products/datastax-graph">DataStax Enteprise Graph</a>
 */
public interface MutinyGraphReactiveResultSet
    extends Multi<ReactiveGraphNode>, ReactiveGraphResultSet {

  /**
   * Returns {@linkplain ExecutionInfo information about the execution} of all requests that have
   * been performed so far to assemble this result set.
   *
   * <p>If the query is not paged, this {@link Multi} will emit exactly one item as soon as the
   * response arrives, then complete. If the query is paged, it will emit multiple items, one per
   * page; then it will complete when the last page arrives. If the query execution fails, then this
   * multi will fail with the same error.
   *
   * <p>By default, the multi returned by this method does not support multiple subscriptions.
   */
  @NonNull
  @Override
  MultiPublisher<ExecutionInfo> getExecutionInfos();
}
