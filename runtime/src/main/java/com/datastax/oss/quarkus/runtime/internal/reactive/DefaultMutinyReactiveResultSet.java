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

public class DefaultMutinyReactiveResultSet
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
  public MultiSubscribe<ReactiveRow> subscribe() {
    return multi.subscribe();
  }

  @Override
  public MultiOnItem<ReactiveRow> onItem() {
    return multi.onItem();
  }

  @Override
  public <O> O then(Function<Multi<ReactiveRow>, O> stage) {
    return multi.then(stage);
  }

  @Override
  public Uni<ReactiveRow> toUni() {
    return multi.toUni();
  }

  @Override
  public MultiOnFailure<ReactiveRow> onFailure() {
    return multi.onFailure();
  }

  @Override
  public MultiOnFailure<ReactiveRow> onFailure(Predicate<? super Throwable> predicate) {
    return multi.onFailure(predicate);
  }

  @Override
  public MultiOnFailure<ReactiveRow> onFailure(Class<? extends Throwable> aClass) {
    return multi.onFailure(aClass);
  }

  @Override
  public MultiOnEvent<ReactiveRow> on() {
    return multi.on();
  }

  @Override
  public Multi<ReactiveRow> cache() {
    return multi.cache();
  }

  @Override
  public MultiCollect<ReactiveRow> collectItems() {
    return multi.collectItems();
  }

  @Override
  public MultiGroup<ReactiveRow> groupItems() {
    return multi.groupItems();
  }

  @Override
  public Multi<ReactiveRow> emitOn(Executor executor) {
    return multi.emitOn(executor);
  }

  @Override
  public Multi<ReactiveRow> runSubscriptionOn(Executor executor) {
    return multi.runSubscriptionOn(executor);
  }

  @Override
  public MultiOnCompletion<ReactiveRow> onCompletion() {
    return multi.onCompletion();
  }

  @Override
  public MultiTransform<ReactiveRow> transform() {
    return multi.transform();
  }

  @Override
  public MultiOverflow<ReactiveRow> onOverflow() {
    return multi.onOverflow();
  }

  @Override
  public MultiBroadcast<ReactiveRow> broadcast() {
    return multi.broadcast();
  }

  @Override
  public MultiConvert<ReactiveRow> convert() {
    return multi.convert();
  }

  @Override
  public void subscribe(Subscriber<? super ReactiveRow> subscriber) {
    multi.subscribe(subscriber);
  }
}
