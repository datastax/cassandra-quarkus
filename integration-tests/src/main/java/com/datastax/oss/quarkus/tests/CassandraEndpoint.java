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
package com.datastax.oss.quarkus.tests;

import com.datastax.oss.quarkus.tests.dao.Product;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/cassandra")
public class CassandraEndpoint {

  @Inject ProductDaoService dao;

  @Inject ProductDaoReactiveService reactiveDao;

  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/product/{description}")
  public UUID saveProduct(@PathParam("description") String desc) {
    UUID id = UUID.randomUUID();
    dao.getDao().create(new Product(id, desc));
    return id;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/product/{id}")
  public Product getProduct(@PathParam("id") UUID id) {
    return dao.getDao().findById(id);
  }

  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/product-reactive/{description}")
  public Uni<UUID> saveProductReactive(@PathParam("description") String desc) {
    UUID id = UUID.randomUUID();
    return reactiveDao.getDao().create(new Product(id, desc)).then(i -> Uni.createFrom().item(id));
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/product-reactive/{id}")
  public Multi<Product> getProductReactive(@PathParam("id") UUID id) {
    return reactiveDao.getDao().findById(id);
  }
}
