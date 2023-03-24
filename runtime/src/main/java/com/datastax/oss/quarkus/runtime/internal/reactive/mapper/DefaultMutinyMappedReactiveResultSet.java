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
package com.datastax.oss.quarkus.runtime.internal.reactive.mapper;

import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.api.reactive.MultiPublisher;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.MutinyWrappers;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.AbstractMulti;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.reactivestreams.Subscriber;

public class DefaultMutinyMappedReactiveResultSet<EntityT> extends AbstractMulti<EntityT>
    implements MutinyMappedReactiveResultSet<EntityT> {
  private final MultiPublisher<EntityT> multi;
  private final MultiPublisher<ExecutionInfo> executionInfos;
  private final MultiPublisher<ColumnDefinitions> columnDefinitions;
  private final MultiPublisher<Boolean> wasApplied;

  public DefaultMutinyMappedReactiveResultSet(MappedReactiveResultSet<EntityT> resultSet) {
    multi = MutinyWrappers.toMulti(resultSet);
    @SuppressWarnings("unchecked")
    MultiPublisher<ColumnDefinitions> columnDefinitions =
        (MultiPublisher<ColumnDefinitions>)
            MutinyWrappers.toMulti(resultSet.getColumnDefinitions());
    this.columnDefinitions = columnDefinitions;
    @SuppressWarnings("unchecked")
    MultiPublisher<ExecutionInfo> executionInfos =
        (MultiPublisher<ExecutionInfo>) MutinyWrappers.toMulti(resultSet.getExecutionInfos());
    this.executionInfos = executionInfos;
    wasApplied = MutinyWrappers.toMulti(resultSet.wasApplied());
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
  public void subscribe(MultiSubscriber<? super EntityT> subscriber) {
    multi.subscribe(Infrastructure.onMultiSubscription(multi, subscriber));
  }

  @Override
  public void subscribe(Subscriber<? super EntityT> subscriber) {
    subscribe(AdaptersToFlow.subscriber(subscriber));
  }
}
