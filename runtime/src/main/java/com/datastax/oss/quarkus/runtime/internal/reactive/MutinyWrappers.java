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

import com.datastax.oss.quarkus.runtime.api.reactive.MultiPublisher;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import java.util.concurrent.Executor;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.reactivestreams.Publisher;

public class MutinyWrappers {

  public static <T> MultiPublisher<T> toMulti(Publisher<T> source) {
    Multi<T> multi = Multi.createFrom().publisher(AdaptersToFlow.publisher(source));
    Context context = Vertx.currentContext();
    if (context != null) {
      multi = multi.emitOn(new VertxContextExecutor(context));
    }
    return new DefaultMultiPublisher<>(multi);
  }

  public static <T> Uni<T> toUni(Publisher<T> source) {
    Uni<T> uni = Uni.createFrom().publisher(AdaptersToFlow.publisher(source));
    Context context = Vertx.currentContext();
    if (context != null) {
      uni = uni.emitOn(new VertxContextExecutor(context));
    }
    return uni;
  }

  private static class VertxContextExecutor implements Executor {

    private final Context context;

    public VertxContextExecutor(Context context) {
      this.context = context;
    }

    @Override
    public void execute(@NonNull Runnable command) {
      context.runOnContext(x -> command.run());
    }
  }
}
