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

import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class CassandraMetricsIT extends DseTestBase {

  @Test
  public void should_report_driver_metrics_via_metrics_endpoint() {

    // Trigger some CQL activity via the test REST endpoint. We're not particularly
    // interested in the product that gets created here, only that it was created.
    when().get("/product").then().statusCode(Response.Status.OK.getStatusCode());

    // then
    when()
        .get("/q/metrics")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(containsString("open_connections"))
        .body(containsString("connected_nodes"));
  }
}
