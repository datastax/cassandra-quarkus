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

import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiBroadcast;
import io.smallrye.mutiny.groups.MultiCollect;
import io.smallrye.mutiny.groups.MultiConvert;
import io.smallrye.mutiny.groups.MultiGroup;
import io.smallrye.mutiny.groups.MultiOnCompletion;
import io.smallrye.mutiny.groups.MultiOnEvent;
import io.smallrye.mutiny.groups.MultiOnFailure;
import io.smallrye.mutiny.groups.MultiOnItem;
import io.smallrye.mutiny.groups.MultiOverflow;
import io.smallrye.mutiny.groups.MultiSubscribe;
import io.smallrye.mutiny.groups.MultiTransform;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import org.reactivestreams.Subscriber;

public class DefaultMutinyMappedReactiveResultSet<EntityT>
    implements MutinyMappedReactiveResultSet<EntityT> {
  private final Multi<EntityT> multi;
  private final Multi<ExecutionInfo> executionInfos;
  private final Multi<ColumnDefinitions> columnDefinitions;
  private final Multi<Boolean> wasApplied;

  public DefaultMutinyMappedReactiveResultSet(MappedReactiveResultSet<EntityT> resultSet) {
    multi = Wrappers.toMulti(resultSet);
    @SuppressWarnings("unchecked")
    Multi<ColumnDefinitions> columnDefinitions =
        (Multi<ColumnDefinitions>) Wrappers.toMulti(resultSet.getColumnDefinitions());
    this.columnDefinitions = columnDefinitions;
    @SuppressWarnings("unchecked")
    Multi<ExecutionInfo> executionInfos =
        (Multi<ExecutionInfo>) Wrappers.toMulti(resultSet.getExecutionInfos());
    this.executionInfos = executionInfos;
    wasApplied = Wrappers.toMulti(resultSet.wasApplied());
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
  public MultiSubscribe<EntityT> subscribe() {
    return multi.subscribe();
  }

  @Override
  public MultiOnItem<EntityT> onItem() {
    return multi.onItem();
  }

  @Override
  public <O> O then(Function<Multi<EntityT>, O> stage) {
    return multi.then(stage);
  }

  @Override
  public Uni<EntityT> toUni() {
    return multi.toUni();
  }

  @Override
  public MultiOnFailure<EntityT> onFailure() {
    return multi.onFailure();
  }

  @Override
  public MultiOnFailure<EntityT> onFailure(Predicate<? super Throwable> predicate) {
    return multi.onFailure(predicate);
  }

  @Override
  public MultiOnFailure<EntityT> onFailure(Class<? extends Throwable> aClass) {
    return multi.onFailure(aClass);
  }

  @Override
  public MultiOnEvent<EntityT> on() {
    return multi.on();
  }

  @Override
  public Multi<EntityT> cache() {
    return multi.cache();
  }

  @Override
  public MultiCollect<EntityT> collectItems() {
    return multi.collectItems();
  }

  @Override
  public MultiGroup<EntityT> groupItems() {
    return multi.groupItems();
  }

  @Override
  public Multi<EntityT> emitOn(Executor executor) {
    return multi.emitOn(executor);
  }

  @Override
  public Multi<EntityT> runSubscriptionOn(Executor executor) {
    return multi.runSubscriptionOn(executor);
  }

  @Override
  public MultiOnCompletion<EntityT> onCompletion() {
    return multi.onCompletion();
  }

  @Override
  public MultiTransform<EntityT> transform() {
    return multi.transform();
  }

  @Override
  public MultiOverflow<EntityT> onOverflow() {
    return multi.onOverflow();
  }

  @Override
  public MultiBroadcast<EntityT> broadcast() {
    return multi.broadcast();
  }

  @Override
  public MultiConvert<EntityT> convert() {
    return multi.convert();
  }

  @Override
  public void subscribe(Subscriber<? super EntityT> subscriber) {
    multi.subscribe(subscriber);
  }
}
