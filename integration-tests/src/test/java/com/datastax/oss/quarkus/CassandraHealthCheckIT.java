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

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class CassandraHealthCheckIT {
  @Test
  public void healthCheckShouldReportStatusUp() {
    // when
    Response response = RestAssured.with().get("/health/ready");

    // then
    assertThat(response.statusCode()).isEqualTo(Status.OK.getStatusCode());

    Map<String, Object> body = response.as(new TypeRef<Map<String, Object>>() {});
    assertThat(body.get("status")).isEqualTo("UP");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> checks = (List<Map<String, Object>>) body.get("checks");
    assertThat(checks.size()).isOne();
    assertThat(checks.get(0).get("name"))
        .isEqualTo("DataStax Apache Cassandra Driver health check");
  }
}
