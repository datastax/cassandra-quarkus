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

import static io.restassured.RestAssured.when;
import static org.hamcrest.core.StringContains.containsString;

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class CassandraMetricsIT {

  @Inject QuarkusCqlSession cqlSession;

  @Test
  public void should_report_driver_metrics_via_metrics_endpoint() {
    // when
    cqlSession.execute("select *  from system.local");

    // then
    when()
        .get("/metrics")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(containsString("open_connections"))
        .body(containsString("connected_nodes"));
  }
}
