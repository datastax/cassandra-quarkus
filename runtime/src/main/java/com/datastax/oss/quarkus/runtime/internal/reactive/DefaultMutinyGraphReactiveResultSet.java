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
import com.datastax.oss.quarkus.runtime.api.reactive.MultiPublisher;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyGraphReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.AbstractMulti;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.reactivestreams.Subscriber;

public class DefaultMutinyGraphReactiveResultSet extends AbstractMulti<ReactiveGraphNode>
    implements MutinyGraphReactiveResultSet {

  private final MultiPublisher<ReactiveGraphNode> multi;
  private final MultiPublisher<ExecutionInfo> executionInfos;

  public DefaultMutinyGraphReactiveResultSet(ReactiveGraphResultSet reactiveGraphResultSet) {
    multi = MutinyWrappers.toMulti(reactiveGraphResultSet);
    @SuppressWarnings("unchecked")
    MultiPublisher<ExecutionInfo> executionInfos =
        (MultiPublisher<ExecutionInfo>)
            MutinyWrappers.toMulti(reactiveGraphResultSet.getExecutionInfos());
    this.executionInfos = executionInfos;
  }

  @NonNull
  @Override
  public MultiPublisher<ExecutionInfo> getExecutionInfos() {
    return executionInfos;
  }

  @Override
  public void subscribe(MultiSubscriber<? super ReactiveGraphNode> subscriber) {
    multi.subscribe(Infrastructure.onMultiSubscription(multi, subscriber));
  }

  @Override
  public void subscribe(Subscriber<? super ReactiveGraphNode> subscriber) {
    subscribe(AdaptersToFlow.subscriber(subscriber));
  }
}
