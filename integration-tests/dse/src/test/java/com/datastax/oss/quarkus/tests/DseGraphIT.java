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
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DseGraphIT extends DseTestBase {

  @Test
  public void should_execute_graph_query() {
    when()
        .get("/graph/vertices")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(containsString("dseg:/person/e7cd5752-bc0d-4157-a80f-7523add8dbcd"));
  }
}
