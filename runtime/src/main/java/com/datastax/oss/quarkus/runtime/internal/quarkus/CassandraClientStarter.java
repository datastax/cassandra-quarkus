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
package com.datastax.oss.quarkus.runtime.internal.quarkus;

import com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.runtime.StartupEvent;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that triggers eager initialization of {@link QuarkusCqlSession}.
 *
 * @see <a href="https://quarkus.io/guides/cdi-reference#eager-instantiation-of-beans">Eager
 *     Instantiation of Beans</a>
 */
@Dependent
public class CassandraClientStarter {

  private static final Logger LOG = LoggerFactory.getLogger(CassandraClientStarter.class);

  @SuppressWarnings("unused")
  public void startup(
      @Observes StartupEvent event,
      CompletionStage<QuarkusCqlSession> sessionProxy,
      CassandraClientConfig config)
      throws ExecutionException, InterruptedException {
    if (config.cassandraClientInitConfig.eagerSessionInit) {
      LOG.debug("Triggering eager initialization of Quarkus session at startup");
      sessionProxy.toCompletableFuture().get();
    } else {
      LOG.debug("Not triggering eager initialization of Quarkus session at startup");
    }
  }
}
