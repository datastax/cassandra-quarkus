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
package com.datastax.oss.quarkus.tests.tests.resource;

import com.datastax.dse.driver.api.core.data.geometry.Geometry;
import com.datastax.dse.driver.api.core.data.geometry.Point;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/geo")
public class DseGeoTypesResource {

  @Inject QuarkusCqlSession session;

  @GET
  @Path("/points")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getAll() {
    return StreamSupport.stream(session.execute("SELECT pt FROM points").spliterator(), false)
        .map(row -> row.get(0, Point.class))
        .filter(Objects::nonNull)
        .map(Geometry::asWellKnownText)
        .collect(Collectors.toList());
  }
}
