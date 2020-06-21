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

import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import java.util.concurrent.Executor;
import org.reactivestreams.Publisher;

public class Wrappers {

  public static <T> Multi<T> toMulti(Publisher<T> source) {
    Multi<T> multi = Multi.createFrom().publisher(source);
    Context context = Vertx.currentContext();
    if (context != null) {
      multi = multi.emitOn(new VertexContextExecutor(context));
    }
    return multi;
  }

  public static <T> Uni<T> toUni(Publisher<T> source) {
    Context context = Vertx.currentContext();
    Uni<T> uni = Uni.createFrom().publisher(source);
    if (context != null) {
      uni = uni.emitOn(new VertexContextExecutor(context));
    }
    return uni;
  }

  public static <T> Uni<T> failedUni(Throwable error) {
    return Uni.createFrom().failure(error);
  }

  private static class VertexContextExecutor implements Executor {

    private final Context context;

    public VertexContextExecutor(Context context) {
      this.context = context;
    }

    @Override
    public void execute(@NonNull Runnable command) {
      context.runOnContext(x -> command.run());
    }
  }
}
