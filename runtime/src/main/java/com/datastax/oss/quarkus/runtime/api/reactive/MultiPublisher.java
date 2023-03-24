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

import io.smallrye.mutiny.Multi;
import org.reactivestreams.Publisher;

/**
 * An object that implements both {@link Multi} and (Reactive Streams) {@link Publisher}.
 *
 * <p>This interface has been introduced to allow continued use of {@link Multi} in reactive result
 * sets, even after Mutiny has moved away from the Reactive Streams API, while the driver still
 * depends on it.
 */
@SuppressWarnings("ReactiveStreamsPublisherImplementation")
public interface MultiPublisher<ElementT> extends Multi<ElementT>, Publisher<ElementT> {}
