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
import com.datastax.dse.driver.internal.core.graph.reactive.ReactiveGraphRequestProcessor;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyGraphReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;

/**
 * The graph reactive session type that supports the Reactive Mutiny API .
 *
 * <p>It provides user-friendly execution methods for executing reactive {@code GraphStatement}
 * queries.
 */
public interface MutinyGraphReactiveSession extends CqlSession, MutinyReactiveSession {

  /**
   * Returns a {@link MutinyGraphReactiveResultSet} that, once subscribed to, executes the given
   * query and emits all the results.
   *
   * <p>See the javadocs of {@link ReactiveGraphResultSet} for important remarks anc caveats
   * regarding the subscription to and consumption of reactive graph result sets.
   *
   * @param statement the statement to execute.
   * @return The {@link MutinyGraphReactiveResultSet} that will publish the returned results.
   * @see ReactiveGraphResultSet
   * @see ReactiveGraphNode
   */
  @NonNull
  @Override
  default MutinyGraphReactiveResultSet executeReactive(@NonNull GraphStatement<?> statement) {
    return new DefaultMutinyGraphReactiveResultSet(
        Objects.requireNonNull(
            execute(statement, ReactiveGraphRequestProcessor.REACTIVE_GRAPH_RESULT_SET)));
  }
}
