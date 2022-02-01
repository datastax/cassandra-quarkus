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
import static org.hamcrest.Matchers.endsWith;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.entity.Address;
import com.datastax.oss.quarkus.tests.entity.Customer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class CustomerResourceIT {

  private static final Address ADDRESS1 = new Address("Rue Montorgueil", "75002", "Paris");
  private static final Address ADDRESS2 = new Address("Rue Montmartre", "75002", "Paris");

  @Test
  public void should_create_customer() {
    Customer expected = new Customer(UUID.randomUUID(), "name", ADDRESS1);
    assertCreate(expected);
  }

  @Test
  public void should_update_customer() {
    Customer expected = new Customer(UUID.randomUUID(), "name", ADDRESS1);
    assertCreate(expected);
    expected.setName("updated name");
    expected.setAddress(ADDRESS2);
    assertUpdate(expected);
  }

  @Test
  public void should_delete_customer() {
    Customer expected = new Customer(UUID.randomUUID(), "name", ADDRESS1);
    assertCreate(expected);
    assertDelete(expected);
  }

  @Test
  public void should_find_customers() {
    Customer expected1 = new Customer(UUID.randomUUID(), "name1", ADDRESS1);
    Customer expected2 = new Customer(UUID.randomUUID(), "name2", ADDRESS2);
    Customer expected3 = new Customer(UUID.randomUUID(), "name3", null);
    assertCreate(expected1);
    assertCreate(expected2);
    assertCreate(expected3);
    Customer[] actual =
        when()
            .get("/customer")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .body()
            .as(Customer[].class);
    assertThat(actual).contains(expected1, expected2, expected3);
  }

  private void assertCreate(Customer customer) {
    given()
        .body(customer)
        .contentType(ContentType.JSON)
        .when()
        .post("/customer")
        .then()
        .statusCode(Status.CREATED.getStatusCode())
        .header("location", endsWith("/customer/" + customer.getId()));
    assertFind(customer);
  }

  private void assertUpdate(Customer customer) {
    given()
        .body(customer)
        .contentType(ContentType.JSON)
        .when()
        .put("/customer/{id}", customer.getId())
        .then()
        .statusCode(Status.OK.getStatusCode());
    assertFind(customer);
  }

  private void assertDelete(Customer expected) {
    when().delete("/customer/{id}", expected.getId()).then().statusCode(Status.OK.getStatusCode());
    when()
        .get("/customer/{id}", expected.getId())
        .then()
        .statusCode(Status.NOT_FOUND.getStatusCode());
  }

  private void assertFind(Customer expected) {
    Customer actual =
        when()
            .get("/customer/{id}", expected.getId())
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .body()
            .as(Customer.class);
    assertThat(actual).isEqualTo(expected);
  }
}
