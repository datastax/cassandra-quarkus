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

import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.api.reactive.MultiPublisher;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMultiPublisher;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.AbstractMulti;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.reactivestreams.Subscriber;

/**
 * A mapped reactive result set that immediately signals the error passed at instantiation to all
 * its subscribers.
 */
public class FailedMutinyMappedReactiveResultSet<T> extends AbstractMulti<T>
    implements MutinyMappedReactiveResultSet<T> {

  private final Multi<T> inner;

  public FailedMutinyMappedReactiveResultSet(Throwable error) {
    this.inner = Multi.createFrom().failure(error);
  }

  @NonNull
  @Override
  public MultiPublisher<ColumnDefinitions> getColumnDefinitions() {
    return new DefaultMultiPublisher<>(inner.onItem().castTo(ColumnDefinitions.class));
  }

  @NonNull
  @Override
  public MultiPublisher<ExecutionInfo> getExecutionInfos() {
    return new DefaultMultiPublisher<>(inner.onItem().castTo(ExecutionInfo.class));
  }

  @NonNull
  @Override
  public MultiPublisher<Boolean> wasApplied() {
    return new DefaultMultiPublisher<>(inner.onItem().castTo(Boolean.class));
  }

  @Override
  public void subscribe(MultiSubscriber<? super T> subscriber) {
    inner.subscribe(Infrastructure.onMultiSubscription(inner, subscriber));
  }

  @Override
  public void subscribe(Subscriber<? super T> subscriber) {
    subscribe(AdaptersToFlow.subscriber(subscriber));
  }
}
