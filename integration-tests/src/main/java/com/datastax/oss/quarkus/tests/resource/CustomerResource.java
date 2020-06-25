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

import com.datastax.oss.quarkus.tests.entity.Customer;
import com.datastax.oss.quarkus.tests.service.CustomerService;
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

@Path("/customer")
public class CustomerResource {

  @Inject CustomerService service;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createCustomer(Customer customer) {
    service.create(customer);
    return Response.created(URI.create("/customer/" + customer.getId())).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateCustomer(@PathParam("id") UUID id, Customer customer) {
    customer.setId(id);
    service.update(customer);
    return Response.ok().build();
  }

  @DELETE
  @Path("/{id}")
  public Response deleteCustomer(@PathParam("id") UUID id) {
    service.delete(id);
    return Response.ok().build();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCustomer(@PathParam("id") UUID id) {
    Customer customer = service.findById(id);
    if (customer == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      return Response.ok(customer).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Customer> getAllCustomers() {
    return service.findAll();
  }
}
