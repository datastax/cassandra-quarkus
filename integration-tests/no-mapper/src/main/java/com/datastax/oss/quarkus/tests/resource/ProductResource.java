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

import com.datastax.oss.driver.api.core.AsyncPagingIterable;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.tests.entity.Product;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Path("/product")
public class ProductResource {

  @Inject QuarkusCqlSession session;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createProduct(Product product) {
    return session
        .executeAsync(
            SimpleStatement.newInstance(
                "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName()))
        .thenApply((ignore) -> Response.created(URI.create("/product/" + product.getId())).build());
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getProduct(@PathParam("id") UUID id) {
    return session
        .executeAsync(SimpleStatement.newInstance("SELECT id, name FROM product WHERE id = ?", id))
        .thenApply(AsyncPagingIterable::one)
        .thenApply(
            row -> {
              if (row == null) {
                return Response.status(Status.NOT_FOUND).build();
              } else {
                return Response.ok(new Product(row.getUuid(0), row.getString(1))).build();
              }
            });
  }
}
