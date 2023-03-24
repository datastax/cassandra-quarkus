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

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import mutiny.zero.flow.adapters.AdaptersToReactiveStreams;
import org.junit.jupiter.api.Test;

class DefaultMultiPublisherTest {

  @Test
  void should_subscribe_with_flow_subscriber() {
    // given
    Multi<Integer> inner = Multi.createFrom().range(1, 5);
    DefaultMultiPublisher<Integer> publisher = new DefaultMultiPublisher<>(inner);

    // when
    AssertSubscriber<Integer> subscriber =
        publisher.subscribe().withSubscriber(AssertSubscriber.create(10));

    // then
    subscriber.assertCompleted().assertItems(1, 2, 3, 4);
  }

  @Test
  void should_subscribe_with_reactive_streams_subscriber() {
    // given
    Multi<Integer> inner = Multi.createFrom().range(1, 5);
    DefaultMultiPublisher<Integer> publisher = new DefaultMultiPublisher<>(inner);

    // when
    AssertSubscriber<Integer> subscriber = AssertSubscriber.create(10);
    publisher.subscribe(AdaptersToReactiveStreams.subscriber(subscriber));

    // then
    subscriber.assertCompleted().assertItems(1, 2, 3, 4);
  }
}
