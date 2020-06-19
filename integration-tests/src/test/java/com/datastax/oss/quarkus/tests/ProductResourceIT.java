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
import static org.hamcrest.Matchers.notNullValue;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class ProductResourceIT {

  @Test
  public void should_save_and_get_product() {

    String productId =
        when()
            .post("/cassandra/product/desc1")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .asString();
    when()
        .get("/cassandra/product/" + productId)
        .then()
        .statusCode(Response.Status.OK.getStatusCode())
        .body(notNullValue());
  }

  @Test
  public void should_save_and_get_product_using_custom_name_converter_that_uses_reflection() {

    String productId =
        when()
            .post("/cassandra-name-converter/product/100")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .asString();
    when()
        .get("/cassandra-name-converter/product/" + productId)
        .then()
        .statusCode(Response.Status.OK.getStatusCode())
        .body(notNullValue());
  }

  @Test
  public void should_save_and_get_product_reactive() {

    String productId =
        when()
            .post("/cassandra/product-reactive/desc1")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .asString();
    when()
        .get("/cassandra/product-reactive/" + productId)
        .then()
        .statusCode(Response.Status.OK.getStatusCode())
        .body(notNullValue());
  }
}
