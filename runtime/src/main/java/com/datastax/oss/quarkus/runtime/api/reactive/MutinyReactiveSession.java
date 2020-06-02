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

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveSession;
import com.datastax.dse.driver.internal.core.cql.reactive.CqlRequestReactiveProcessor;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import java.util.Objects;

public interface MutinyReactiveSession extends ReactiveSession {

  /**
   * Returns a {@link Multi} that, once subscribed to, executes the given query and emits all the
   * results.
   *
   * @param query the query to execute.
   * @return The {@link Multi} that will publish the returned results.
   */
  @NonNull
  @Override
  default MutinyReactiveResultSet executeReactive(@NonNull String query) {
    return executeReactive(SimpleStatement.newInstance(query));
  }

  /**
   * Returns a {@link Multi} that, once subscribed to, executes the given query and emits all the
   * results.
   *
   * @param statement the statement to execute.
   * @return The {@link Multi} that will publish the returned results.
   */
  @NonNull
  @Override
  default MutinyReactiveResultSet executeReactive(@NonNull Statement<?> statement) {
    return new DefaultMutinyReactiveResultSet(
        Objects.requireNonNull(
            execute(statement, CqlRequestReactiveProcessor.REACTIVE_RESULT_SET)));
  }
}
