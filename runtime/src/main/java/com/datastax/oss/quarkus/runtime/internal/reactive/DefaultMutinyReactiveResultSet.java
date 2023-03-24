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
import com.datastax.oss.quarkus.runtime.api.reactive.MultiPublisher;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyContinuousReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.AbstractMulti;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.reactivestreams.Subscriber;

public class DefaultMutinyReactiveResultSet extends AbstractMulti<ReactiveRow>
    implements MutinyReactiveResultSet, MutinyContinuousReactiveResultSet {

  private final MultiPublisher<ReactiveRow> inner;
  private final MultiPublisher<ColumnDefinitions> columnDefinitions;
  private final MultiPublisher<ExecutionInfo> executionInfos;
  private final MultiPublisher<Boolean> wasApplied;

  public DefaultMutinyReactiveResultSet(ReactiveResultSet reactiveResultSet) {
    inner = MutinyWrappers.toMulti(reactiveResultSet);
    @SuppressWarnings("unchecked")
    MultiPublisher<ColumnDefinitions> columnDefinitions =
        (MultiPublisher<ColumnDefinitions>)
            MutinyWrappers.toMulti(reactiveResultSet.getColumnDefinitions());
    this.columnDefinitions = columnDefinitions;
    @SuppressWarnings("unchecked")
    MultiPublisher<ExecutionInfo> executionInfos =
        (MultiPublisher<ExecutionInfo>)
            MutinyWrappers.toMulti(reactiveResultSet.getExecutionInfos());
    this.executionInfos = executionInfos;
    wasApplied = MutinyWrappers.toMulti(reactiveResultSet.wasApplied());
  }

  @NonNull
  @Override
  public MultiPublisher<ColumnDefinitions> getColumnDefinitions() {
    return columnDefinitions;
  }

  @NonNull
  @Override
  public MultiPublisher<ExecutionInfo> getExecutionInfos() {
    return executionInfos;
  }

  @NonNull
  @Override
  public MultiPublisher<Boolean> wasApplied() {
    return wasApplied;
  }

  @Override
  public void subscribe(MultiSubscriber<? super ReactiveRow> subscriber) {
    inner.subscribe(Infrastructure.onMultiSubscription(inner, subscriber));
  }

  @Override
  public void subscribe(Subscriber<? super ReactiveRow> subscriber) {
    subscribe(AdaptersToFlow.subscriber(subscriber));
  }
}
