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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

/**
 * Integration test to ensure that application.conf and application.json were properly loaded by the
 * driver, especially in native mode.
 */
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class DriverConfigIT {

  @Test
  void should_load_custom_lbp() {
    given()
        .when()
        .get("/config/basic.load-balancing-policy.class")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(equalTo("com.datastax.oss.quarkus.tests.driver.CustomLoadBalancingPolicy"));
  }

  @Test
  void should_load_request_timeout() {
    given()
        .when()
        .get("/config/basic.request.timeout")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(equalTo("10 seconds"));
  }

  @Test
  void should_load_custom_request_trackers() {
    given()
        .when()
        .get("/config/advanced.request-tracker.classes")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(containsString("RequestLogger"), containsString("MyRequestTracker"));
  }

  @Test
  void should_load_custom_schema_change_listener() {
    given()
        .when()
        .get("/config/advanced.schema-change-listener.classes")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(containsString("MySchemaChangeListener"));
  }

  @Test
  void should_load_custom_node_state_listener() {
    given()
        .when()
        .get("/config/advanced.node-state-listener.classes")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(containsString("MyNodeStateListener"));
  }
}
