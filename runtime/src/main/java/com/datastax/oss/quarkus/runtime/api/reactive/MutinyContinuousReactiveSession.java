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

import com.datastax.dse.driver.api.core.cql.continuous.ContinuousSession;
import com.datastax.dse.driver.api.core.cql.continuous.reactive.ContinuousReactiveSession;
import com.datastax.dse.driver.internal.core.cql.continuous.reactive.ContinuousCqlRequestReactiveProcessor;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import java.util.Objects;

/**
 * A specialized session type that supports the Reactive Mutiny API for continuous paging queries.
 *
 * <p>Note: continuous paging is only available for DataStax Enterprise (DSE) 5.1 and higher.
 */
public interface MutinyContinuousReactiveSession extends ContinuousReactiveSession {

  /**
   * Returns a {@link Multi} that, once subscribed to, executes the given query continuously and
   * emits all the results.
   *
   * <p>See {@link ContinuousSession} for more explanations about continuous paging.
   *
   * <p>This feature is only available with Datastax Enterprise. Executing continuous queries
   * against an Apache Cassandra&reg; cluster will result in a runtime error.
   *
   * @param query the query to execute.
   * @return The {@link Multi} that will publish the returned results.
   */
  @NonNull
  @Override
  default MutinyContinuousReactiveResultSet executeContinuouslyReactive(@NonNull String query) {
    return executeContinuouslyReactive(SimpleStatement.newInstance(query));
  }

  /**
   * Returns a {@link Multi} that, once subscribed to, executes the given query continuously and
   * emits all the results.
   *
   * <p>See {@link ContinuousSession} for more explanations about continuous paging.
   *
   * <p>This feature is only available with Datastax Enterprise. Executing continuous queries
   * against an Apache Cassandra&reg; cluster will result in a runtime error.
   *
   * @param statement the statement to execute.
   * @return The {@link Multi} that will publish the returned results.
   */
  @NonNull
  @Override
  default MutinyContinuousReactiveResultSet executeContinuouslyReactive(
      @NonNull Statement<?> statement) {
    return new DefaultMutinyReactiveResultSet(
        Objects.requireNonNull(
            execute(
                statement, ContinuousCqlRequestReactiveProcessor.CONTINUOUS_REACTIVE_RESULT_SET)));
  }
}
