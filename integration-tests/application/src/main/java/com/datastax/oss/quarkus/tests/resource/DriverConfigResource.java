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

import com.datastax.oss.driver.api.core.CqlSession;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/config")
public class DriverConfigResource {

  private static final Response NOT_FOUND = Response.status(Status.NOT_FOUND).build();

  @Inject CqlSession session;

  @GET
  @Path("/{path}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getConfigValue(@PathParam("path") String path) {
    return session.getContext().getConfig().getDefaultProfile().entrySet().stream()
        .filter(entry -> entry.getKey().equals(path))
        .map(Entry::getValue)
        .findFirst()
        .map(v -> Response.ok(v).build())
        .orElse(NOT_FOUND);
  }
}
