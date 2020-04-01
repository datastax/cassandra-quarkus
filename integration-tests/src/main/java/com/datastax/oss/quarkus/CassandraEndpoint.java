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
package com.datastax.oss.quarkus;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.datastax.oss.quarkus.dao.Product;

@Path("/cassandra")
public class CassandraEndpoint {

    @Inject
    ProductDaoService dao;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/product/{description}")
    public UUID saveProduct(@PathParam("description") String desc) {
        UUID id = UUID.randomUUID();
        dao.getDao().update(new Product(id, desc));
        return id;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/product/{id}")
    public Product getProduct(@PathParam("id") UUID id) {
        return dao.getDao().findById(id);
    }

}
