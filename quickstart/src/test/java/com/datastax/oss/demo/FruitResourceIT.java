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
package com.datastax.oss.demo;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.datastax.oss.quarkus.demo.FruitDto;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class FruitResourceIT {

  @Test
  public void should_save_and_retrieve_entity() {
    // given
    FruitDto expected = new FruitDto("apple", "this was created via IT test");

    // when creating, then
    given()
        .contentType(ContentType.JSON)
        .body(expected)
        .when()
        .post("/fruits")
        .then()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode())
        .body(notNullValue());

    // when retrieving, then
    FruitDto[] actual =
        when()
            .get("/fruits")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .as(FruitDto[].class);
    assertThat(actual).contains(expected);
  }

  @Test
  public void should_save_and_retrieve_entity_reactive() {
    // given
    FruitDto expected = new FruitDto("banana", "this was created via reactive IT test");

    // when creating, then
    given()
        .contentType(ContentType.JSON)
        .body(expected)
        .when()
        .post("/reactive-fruits")
        .then()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode())
        .body(notNullValue());

    // when retrieving, then
    FruitDto[] actual =
        when()
            .get("/reactive-fruits")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .as(FruitDto[].class);
    assertThat(actual).contains(expected);
  }
}
