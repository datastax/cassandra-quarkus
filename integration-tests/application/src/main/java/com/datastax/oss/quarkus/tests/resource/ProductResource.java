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
import com.datastax.oss.quarkus.tests.service.ProductService;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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

  @Inject ProductService service;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createProduct(Product product) {
    return service
        .create(product)
        .thenApply((ignore) -> Response.created(URI.create("/product/" + product.getId())).build());
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateProduct(@PathParam("id") UUID id, Product product) {
    product.setId(id);
    return service.update(product).thenApply((ignore) -> Response.ok().build());
  }

  @DELETE
  @Path("/{id}")
  public CompletionStage<Response> deleteProduct(@PathParam("id") UUID id) {
    return service.delete(id).thenApply((ignore) -> Response.ok().build());
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getProduct(@PathParam("id") UUID id) {
    return service
        .findById(id)
        .thenApply(
            product -> {
              if (product == null) {
                return Response.status(Status.NOT_FOUND).build();
              } else {
                return Response.ok(product).build();
              }
            });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<Product> getAllProducts() {
    return service.findAll();
  }
}
