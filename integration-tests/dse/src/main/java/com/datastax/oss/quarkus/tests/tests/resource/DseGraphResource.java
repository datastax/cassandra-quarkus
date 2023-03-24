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

import com.datastax.dse.driver.api.core.config.DseDriverOption;
import com.datastax.dse.driver.api.core.graph.DseGraph;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

@Path("/graph")
public class DseGraphResource {

  private final GraphTraversalSource g;

  @Inject
  public DseGraphResource(QuarkusCqlSession session) {
    g =
        AnonymousTraversalSource.traversal()
            .withRemote(
                DseGraph.remoteConnectionBuilder(session)
                    .withExecutionProfile(
                        session
                            .getContext()
                            .getConfig()
                            .getDefaultProfile()
                            .withString(DseDriverOption.GRAPH_NAME, "food_qs"))
                    .build());
  }

  @GET
  @Path("/vertices")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getAll() {
    return g.V().toStream().map(v -> v.id().toString()).collect(Collectors.toList());
  }
}
