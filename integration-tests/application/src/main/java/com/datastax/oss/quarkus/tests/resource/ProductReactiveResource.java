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
package com.datastax.oss.quarkus.tests.resource;

import com.datastax.oss.quarkus.tests.entity.Product;
import com.datastax.oss.quarkus.tests.service.ProductReactiveService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@ApplicationScoped
public class ProductReactiveResource {

  @Inject ProductReactiveService service;

  @Route(
      path = "/rx/product",
      methods = HttpMethod.POST,
      consumes = MediaType.APPLICATION_JSON,
      order = 1)
  public void createProduct(RoutingExchange ex) {
    Product product = Json.decodeValue(ex.context().getBody(), Product.class);
    service
        .create(product)
        .subscribe()
        .with(v -> ex.response().setStatusCode(Status.CREATED.getStatusCode()).end());
  }

  @Route(
      path = "/rx/product/:id",
      methods = HttpMethod.PUT,
      consumes = MediaType.APPLICATION_JSON,
      order = 1)
  public void updateProduct(RoutingExchange ex) {
    UUID id = ex.getParam("id").map(UUID::fromString).orElseThrow(IllegalStateException::new);
    Product product = Json.decodeValue(ex.context().getBody(), Product.class);
    product.setId(id);
    service
        .update(product)
        .subscribe()
        .with(v -> ex.response().setStatusCode(Status.OK.getStatusCode()).end());
  }

  @Route(path = "/rx/product/:id", methods = HttpMethod.DELETE, order = 1)
  public void deleteProduct(RoutingExchange ex) {
    UUID id = ex.getParam("id").map(UUID::fromString).orElseThrow(IllegalStateException::new);
    service
        .delete(id)
        .subscribe()
        .with(v -> ex.response().setStatusCode(Status.OK.getStatusCode()).end());
  }

  @Route(
      path = "/rx/product/:id",
      methods = HttpMethod.GET,
      produces = MediaType.APPLICATION_JSON,
      order = 1)
  public void findProduct(RoutingExchange ex) {
    UUID id = ex.getParam("id").map(UUID::fromString).orElseThrow(IllegalStateException::new);
    service
        .findById(id)
        .subscribe()
        .with(
            product -> {
              if (product == null) {
                ex.response().setStatusCode(Status.NOT_FOUND.getStatusCode()).end();
              } else {
                ex.response()
                    .setStatusCode(Status.OK.getStatusCode())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodeToBuffer(product));
              }
            });
  }

  @Route(
      path = "/rx/product",
      methods = HttpMethod.GET,
      produces = MediaType.APPLICATION_JSON,
      order = 1)
  public void findAllProducts(RoutingExchange ex) {
    service
        .findAll()
        .collect()
        .asList()
        .subscribe()
        .with(
            list ->
                ex.response()
                    .setStatusCode(Status.OK.getStatusCode())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodeToBuffer(list)));
  }
}
