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
package com.datastax.oss.quarkus.runtime.internal.health;

import com.datastax.oss.driver.api.core.AsyncPagingIterable;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.arc.Arc;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.util.TypeLiteral;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class CassandraAsyncHealthCheck implements AsyncHealthCheck {
  /** Name of the health-check. */
  private static final String HEALTH_CHECK_NAME = "DataStax Apache Cassandra Driver health check";

  private static final Type COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE =
      new TypeLiteral<CompletionStage<QuarkusCqlSession>>() {}.getType();
  static final String HEALTH_CHECK_QUERY =
      "SELECT data_center, release_version, cluster_name, cql_version FROM system.local";

  private CompletionStage<QuarkusCqlSession> cqlSessionCompletionStage;

  @SuppressWarnings("unchecked")
  public CompletionStage<QuarkusCqlSession> beanProvider() {
    return (CompletionStage<QuarkusCqlSession>)
        Arc.container().instance(COMPLETION_STAGE_OF_QUARKUS_CQL_SESSION_TYPE).get();
  }

  @PostConstruct
  protected void init() {
    this.cqlSessionCompletionStage = beanProvider();
  }

  @Override
  public Uni<HealthCheckResponse> call() {
    return Uni.createFrom()
        .completionStage(
            cqlSessionCompletionStage.thenCompose(
                cqlSession ->
                    cqlSession
                        .executeAsync(HEALTH_CHECK_QUERY)
                        .thenApply(AsyncPagingIterable::one)
                        .thenApply(
                            result -> {
                              HealthCheckResponseBuilder builder =
                                  HealthCheckResponse.named(HEALTH_CHECK_NAME).up();
                              if (result == null) {
                                return builder
                                    .down()
                                    .withData("reason", "system.local returned null")
                                    .build();
                              }
                              for (Map.Entry<String, String> entry :
                                  extractInfoFromResult(result).entrySet()) {
                                builder.withData(entry.getKey(), entry.getValue());
                              }
                              return builder
                                  .withData(
                                      "numberOfNodes", cqlSession.getMetadata().getNodes().size())
                                  .up()
                                  .build();
                            })
                        .exceptionally(
                            ex -> {
                              HealthCheckResponseBuilder builder =
                                  HealthCheckResponse.named(HEALTH_CHECK_NAME).up();
                              return builder.down().withData("reason", ex.getMessage()).build();
                            })));
  }

  private Map<String, String> extractInfoFromResult(Row result) {
    HashMap<String, String> details = new HashMap<>();
    details.put("datacenter", result.getString("data_center"));
    details.put("releaseVersion", result.getString("release_version"));
    details.put("clusterName", result.getString("cluster_name"));
    details.put("cqlVersion", result.getString("cql_version"));
    return details;
  }
}
