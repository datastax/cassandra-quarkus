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

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyContinuousReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.AbstractMulti;
import io.smallrye.mutiny.subscription.MultiSubscriber;

public class DefaultMutinyReactiveResultSet extends AbstractMulti<ReactiveRow>
    implements MutinyReactiveResultSet, MutinyContinuousReactiveResultSet {

  private final Multi<ReactiveRow> multi;
  private final Multi<ColumnDefinitions> columnDefinitions;
  private final Multi<ExecutionInfo> executionInfos;
  private final Multi<Boolean> wasApplied;

  public DefaultMutinyReactiveResultSet(ReactiveResultSet reactiveResultSet) {
    multi = Wrappers.toMulti(reactiveResultSet);
    @SuppressWarnings("unchecked")
    Multi<ColumnDefinitions> columnDefinitions =
        (Multi<ColumnDefinitions>) Wrappers.toMulti(reactiveResultSet.getColumnDefinitions());
    this.columnDefinitions = columnDefinitions;
    @SuppressWarnings("unchecked")
    Multi<ExecutionInfo> executionInfos =
        (Multi<ExecutionInfo>) Wrappers.toMulti(reactiveResultSet.getExecutionInfos());
    this.executionInfos = executionInfos;
    wasApplied = Wrappers.toMulti(reactiveResultSet.wasApplied());
  }

  @NonNull
  @Override
  public Multi<ColumnDefinitions> getColumnDefinitions() {
    return columnDefinitions;
  }

  @NonNull
  @Override
  public Multi<ExecutionInfo> getExecutionInfos() {
    return executionInfos;
  }

  @NonNull
  @Override
  public Multi<Boolean> wasApplied() {
    return wasApplied;
  }

  @Override
  public void subscribe(MultiSubscriber<? super ReactiveRow> subscriber) {
    multi.subscribe(Infrastructure.onMultiSubscription(multi, subscriber));
  }
}
