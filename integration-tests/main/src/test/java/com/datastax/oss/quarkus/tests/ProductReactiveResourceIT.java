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
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.entity.Product;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class ProductReactiveResourceIT {
  @Test
  public void should_create_product() {
    Product expected = new Product(UUID.randomUUID(), "name");
    assertCreate(expected);
  }

  @Test
  public void should_update_product() {
    Product expected = new Product(UUID.randomUUID(), "name");
    assertCreate(expected);
    expected.setName("updated name");
    assertUpdate(expected);
  }

  @Test
  public void should_delete_product() {
    Product expected = new Product(UUID.randomUUID(), "name");
    assertCreate(expected);
    assertDelete(expected);
  }

  @Test
  public void should_find_products() {
    Product expected1 = new Product(UUID.randomUUID(), "name1");
    Product expected2 = new Product(UUID.randomUUID(), "name2");
    Product expected3 = new Product(UUID.randomUUID(), "name3");
    assertCreate(expected1);
    assertCreate(expected2);
    assertCreate(expected3);
    Product[] actual =
        when()
            .get("/rx/product")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .body()
            .as(Product[].class);
    assertThat(actual).contains(expected1, expected2, expected3);
  }

  private void assertCreate(Product product) {
    given()
        .body(product)
        .contentType(ContentType.JSON)
        .when()
        .post("/rx/product")
        .then()
        .statusCode(Status.CREATED.getStatusCode());
    assertFind(product);
  }

  private void assertUpdate(Product product) {
    given()
        .body(product)
        .contentType(ContentType.JSON)
        .when()
        .put("/rx/product/{id}", product.getId())
        .then()
        .statusCode(Status.OK.getStatusCode());
    assertFind(product);
  }

  private void assertDelete(Product expected) {
    when()
        .delete("/rx/product/{id}", expected.getId())
        .then()
        .statusCode(Status.OK.getStatusCode());
    when()
        .get("/rx/product/{id}", expected.getId())
        .then()
        .statusCode(Status.NOT_FOUND.getStatusCode());
  }

  private void assertFind(Product expected) {
    Product actual =
        when()
            .get("/rx/product/{id}", expected.getId())
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .body()
            .as(Product.class);
    assertThat(actual).isEqualTo(expected);
  }
}
