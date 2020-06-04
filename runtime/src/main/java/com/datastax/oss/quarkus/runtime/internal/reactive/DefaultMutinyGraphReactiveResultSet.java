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

import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphNode;
import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphResultSet;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyGraphReactiveResultSet;
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

public class DefaultMutinyGraphReactiveResultSet implements MutinyGraphReactiveResultSet {

  private final Multi<ReactiveGraphNode> multi;
  private final Multi<ExecutionInfo> executionInfos;

  public DefaultMutinyGraphReactiveResultSet(ReactiveGraphResultSet reactiveGraphResultSet) {
    multi = Wrappers.toMulti(reactiveGraphResultSet);
    @SuppressWarnings("unchecked")
    Multi<ExecutionInfo> executionInfos =
        (Multi<ExecutionInfo>) Wrappers.toMulti(reactiveGraphResultSet.getExecutionInfos());
    this.executionInfos = executionInfos;
  }

  @NonNull
  @Override
  public Multi<ExecutionInfo> getExecutionInfos() {
    return executionInfos;
  }

  @Override
  public MultiSubscribe<ReactiveGraphNode> subscribe() {
    return multi.subscribe();
  }

  @Override
  public MultiOnItem<ReactiveGraphNode> onItem() {
    return multi.onItem();
  }

  @Override
  public <O> O then(Function<Multi<ReactiveGraphNode>, O> stage) {
    return multi.then(stage);
  }

  @Override
  public Uni<ReactiveGraphNode> toUni() {
    return multi.toUni();
  }

  @Override
  public MultiOnFailure<ReactiveGraphNode> onFailure() {
    return multi.onFailure();
  }

  @Override
  public MultiOnFailure<ReactiveGraphNode> onFailure(Predicate<? super Throwable> predicate) {
    return multi.onFailure(predicate);
  }

  @Override
  public MultiOnFailure<ReactiveGraphNode> onFailure(Class<? extends Throwable> aClass) {
    return multi.onFailure(aClass);
  }

  @Override
  public MultiOnEvent<ReactiveGraphNode> on() {
    return multi.on();
  }

  @Override
  public Multi<ReactiveGraphNode> cache() {
    return multi.cache();
  }

  @Override
  public MultiCollect<ReactiveGraphNode> collectItems() {
    return multi.collectItems();
  }

  @Override
  public MultiGroup<ReactiveGraphNode> groupItems() {
    return multi.groupItems();
  }

  @Override
  public Multi<ReactiveGraphNode> emitOn(Executor executor) {
    return multi.emitOn(executor);
  }

  @Override
  public Multi<ReactiveGraphNode> runSubscriptionOn(Executor executor) {
    return multi.runSubscriptionOn(executor);
  }

  @Override
  public MultiOnCompletion<ReactiveGraphNode> onCompletion() {
    return multi.onCompletion();
  }

  @Override
  public MultiTransform<ReactiveGraphNode> transform() {
    return multi.transform();
  }

  @Override
  public MultiOverflow<ReactiveGraphNode> onOverflow() {
    return multi.onOverflow();
  }

  @Override
  public MultiBroadcast<ReactiveGraphNode> broadcast() {
    return multi.broadcast();
  }

  @Override
  public MultiConvert<ReactiveGraphNode> convert() {
    return multi.convert();
  }

  @Override
  public void subscribe(Subscriber<? super ReactiveGraphNode> subscriber) {
    multi.subscribe(subscriber);
  }
}
