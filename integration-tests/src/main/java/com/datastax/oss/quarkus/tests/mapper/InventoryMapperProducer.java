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
package com.datastax.oss.quarkus.tests.mapper;

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.tests.dao.CustomerDao;
import com.datastax.oss.quarkus.tests.dao.ProductDao;
import com.datastax.oss.quarkus.tests.dao.ProductReactiveDao;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class InventoryMapperProducer {

  private final CompletionStage<QuarkusCqlSession> sessionCompletionStage;
  private final CompletionStage<InventoryMapper> mapperCompletionStage;

  @Inject
  public InventoryMapperProducer(CompletionStage<QuarkusCqlSession> sessionCompletionStage) {
    this.sessionCompletionStage = sessionCompletionStage;
    mapperCompletionStage =
        sessionCompletionStage.thenApply(session -> new InventoryMapperBuilder(session).build());
  }

  @Produces
  @ApplicationScoped
  CompletionStage<ProductDao> produceProductDao() {
    return sessionCompletionStage
        .thenApply(
            s ->
                s.getKeyspace()
                    .orElseThrow(
                        () -> new IllegalStateException("Session is not bound to a keyspace")))
        .thenCompose(
            keyspace -> mapperCompletionStage.thenCompose(mapper -> mapper.productDao(keyspace)));
  }

  @Produces
  @ApplicationScoped
  CompletionStage<ProductReactiveDao> produceProductReactiveDao() {
    return sessionCompletionStage
        .thenApply(
            s ->
                s.getKeyspace()
                    .orElseThrow(
                        () -> new IllegalStateException("Session is not bound to a keyspace")))
        .thenCompose(
            keyspace ->
                mapperCompletionStage.thenCompose(mapper -> mapper.productReactiveDao(keyspace)));
  }

  @Produces
  @ApplicationScoped
  CompletionStage<CustomerDao> produceCustomerDao() {
    return sessionCompletionStage
        .thenApply(
            s ->
                s.getKeyspace()
                    .orElseThrow(
                        () -> new IllegalStateException("Session is not bound to a keyspace")))
        .thenCompose(
            keyspace -> mapperCompletionStage.thenCompose(mapper -> mapper.customerDao(keyspace)));
  }
}
