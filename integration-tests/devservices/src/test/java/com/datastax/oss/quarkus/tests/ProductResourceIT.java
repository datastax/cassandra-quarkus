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

import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.entity.Product;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class ProductResourceIT {

  @Test
  public void should_create_and_retrieve_product() {
    Product expected =
        new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "product1");
    Product actual =
        when()
            .get("/product/{id}", expected.getId())
            .then()
            .contentType(ContentType.JSON)
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .body()
            .as(Product.class);
    assertThat(actual).isEqualTo(expected);
  }
}
