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

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveQueryMetadata;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;

/**
 * Interface implemented by the {@link MutinyReactiveResultSet} and {@link
 * MutinyContinuousReactiveResultSet}.
 *
 * @see ReactiveQueryMetadata
 */
public interface MutinyReactiveQueryMetadata extends ReactiveQueryMetadata {

  /**
   * Returns metadata about the {@linkplain ColumnDefinitions columns} contained in this result set.
   *
   * <p>This {@link Multi} emits exactly one item as soon as the first response arrives, then
   * completes; it can be safely {@linkplain Multi#toUni() transformed} into a {@link
   * io.smallrye.mutiny.Uni Uni}. If the query execution fails <em>within the first request-response
   * cycle</em>, then this multi will fail with the same error; however if the error happens
   * <em>after the first response</em>, then this multi will be already completed and will not
   * acknowledge that error in any way.
   *
   * <p>By default, the multi returned by this method does not support multiple subscriptions.
   */
  @NonNull
  @Override
  MultiPublisher<ColumnDefinitions> getColumnDefinitions();

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

  /**
   * If the query that produced this result was a conditional update, indicates whether it was
   * successfully applied.
   *
   * <p>This {@link Multi} emits exactly one item as soon as the first response arrives, then
   * completes; it can be safely {@linkplain Multi#toUni() transformed} into a {@link
   * io.smallrye.mutiny.Uni Uni}. If the query execution fails <em>within the first request-response
   * cycle</em>, then this multi will fail with the same error; however if the error happens
   * <em>after the first response</em>, then this multi will be already completed and will not
   * acknowledge that error in any way.
   *
   * <p>By default, the multi returned by this method does not support multiple subscriptions.
   *
   * <p>For consistency, this method always returns {@code true} for non-conditional queries
   * (although there is no reason to call the method in that case). This is also the case for
   * conditional DDL statements ({@code CREATE KEYSPACE... IF NOT EXISTS}, {@code CREATE TABLE... IF
   * NOT EXISTS}), for which Cassandra doesn't return an {@code [applied]} column.
   *
   * <p>Note that, for versions of Cassandra strictly lower than 2.1.0-rc2, a server-side bug (<a
   * href="https://issues.apache.org/jira/browse/CASSANDRA-7337">CASSANDRA-7337</a>) causes this
   * method to always return {@code true} for batches containing conditional queries.
   */
  @NonNull
  @Override
  MultiPublisher<Boolean> wasApplied();
}
