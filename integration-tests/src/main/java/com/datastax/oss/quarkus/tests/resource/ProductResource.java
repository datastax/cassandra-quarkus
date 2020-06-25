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
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/product")
public class ProductResource {

  @Inject ProductService service;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createProduct(Product product) {
    service.create(product);
    return Response.created(URI.create("/product/" + product.getId())).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateProduct(@PathParam("id") UUID id, Product product) {
    product.setId(id);
    service.update(product);
    return Response.ok().build();
  }

  @DELETE
  @Path("/{id}")
  public Response deleteProduct(@PathParam("id") UUID id) {
    service.delete(id);
    return Response.ok().build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProduct(@PathParam("id") UUID id) {
    Product product = service.findById(id);
    if (product == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      return Response.ok(product).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Product> getAllProducts() {
    return service.findAll();
  }
}
