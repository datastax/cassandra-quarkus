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
package com.datastax.oss.quarkus.deployment.internal;

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.runtime.RuntimeValue;
import java.util.concurrent.CompletionStage;

public final class CassandraClientBuildItem extends SimpleBuildItem {
  private final RuntimeValue<CompletionStage<QuarkusCqlSession>> cqlSession;

  public CassandraClientBuildItem(RuntimeValue<CompletionStage<QuarkusCqlSession>> cqlSession) {
    this.cqlSession = cqlSession;
  }

  @SuppressWarnings("unused")
  public RuntimeValue<CompletionStage<QuarkusCqlSession>> getCqlSession() {
    return cqlSession;
  }
}
