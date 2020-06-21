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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.dao.Product;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class ProductResourceIT {

  @Test
  public void should_save_and_get_product() {

    String productId =
        when()
            .post("/cassandra/product/test1")
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

    String productId1 =
        when()
            .post("/cassandra/product-reactive/reactive1")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .asString();
    Product product1 = new Product(UUID.fromString(productId1), "reactive1");

    String productId2 =
        when()
            .post("/cassandra/product-reactive/reactive2")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .asString();
    Product product2 = new Product(UUID.fromString(productId2), "reactive2");

    Product actual1 =
        when()
            .get("/cassandra/product-reactive/" + productId1)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .as(Product.class);
    assertThat(actual1).isEqualTo(product1);

    Product actual2 =
        when()
            .get("/cassandra/product-reactive/" + productId2)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .as(Product.class);
    assertThat(actual2).isEqualTo(product2);

    Product[] products =
        when()
            .get("/cassandra/product-reactive")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body(notNullValue())
            .extract()
            .body()
            .as(Product[].class);
    assertThat(products).contains(product1, product2);
  }
}
