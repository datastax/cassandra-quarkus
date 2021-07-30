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

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.dao.ProductReactiveDao;
import com.datastax.oss.quarkus.tests.entity.Product;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link ProductReactiveDao} and all supported Mutiny return types in various
 * DAO methods.
 */
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ProductReactiveDaoIT {

  @Inject QuarkusCqlSession session;

  @Inject ProductReactiveDao dao;

  Product product = new Product(UUID.randomUUID(), "name");

  @BeforeEach
  void truncateTables() {
    session.execute("TRUNCATE product");
    session.execute("TRUNCATE votes");
  }

  @Test
  void createUniVoid() {
    // when
    Uni<Void> uni = dao.create(product);
    // then
    UniAssertSubscriber<Void> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void createUniBooleanSuccess() {
    // when
    Uni<Boolean> uni = dao.createUniBoolean(product);
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(true);
  }

  @Test
  void createUniBooleanFailure() {
    // given
    Product previous = new Product(product.getId(), "previous");
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", previous.getId(), previous.getName());
    // when
    Uni<Boolean> uni = dao.createUniBoolean(product);
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(false);
  }

  @Test
  void createUniProductSuccess() {
    // when
    Uni<Product> uni = dao.createUniProduct(product);
    // then
    UniAssertSubscriber<Product> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void createUniProductFailure() {
    // given
    Product previous = new Product(product.getId(), "previous");
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", previous.getId(), previous.getName());
    // when
    Uni<Product> uni = dao.createUniProduct(product);
    // then
    UniAssertSubscriber<Product> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(previous);
  }

  @Test
  void createResultSetSuccess() {
    // when
    MutinyReactiveResultSet rs = dao.createResultSet(product);
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void createResultSetFailure() {
    // given
    Product previous = new Product(product.getId(), "previous");
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", previous.getId(), previous.getName());
    // when
    MutinyReactiveResultSet rs = dao.createResultSet(product);
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(3);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getColumnDefinitions().get("id")).isNotNull();
              assertThat(row.getColumnDefinitions().get("name")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
              assertThat(row.getUuid("id")).isEqualTo(previous.getId());
              assertThat(row.getString("name")).isEqualTo(previous.getName());
            });
  }

  @Test
  void createMultiReactiveRowSuccess() {
    // when
    Multi<? extends Row> rs = dao.createMultiReactiveRow(product);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void createMultiReactiveRowFailure() {
    // given
    Product previous = new Product(product.getId(), "previous");
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", previous.getId(), previous.getName());
    // when
    Multi<? extends Row> rs = dao.createMultiReactiveRow(product);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(3);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getColumnDefinitions().get("id")).isNotNull();
              assertThat(row.getColumnDefinitions().get("name")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
              assertThat(row.getUuid("id")).isEqualTo(previous.getId());
              assertThat(row.getString("name")).isEqualTo(previous.getName());
            });
  }

  @Test
  void createMultiRowSuccess() {
    // when
    Multi<Row> rs = dao.createMultiRow(product);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void createMultiRowFailure() {
    // given
    Product previous = new Product(product.getId(), "previous");
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", previous.getId(), previous.getName());
    // when
    Multi<Row> rs = dao.createMultiRow(product);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(3);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getColumnDefinitions().get("id")).isNotNull();
              assertThat(row.getColumnDefinitions().get("name")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
              assertThat(row.getUuid("id")).isEqualTo(previous.getId());
              assertThat(row.getString("name")).isEqualTo(previous.getName());
            });
  }

  @Test
  void updateUniVoid() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Void> uni = dao.update(updated);
    // then
    UniAssertSubscriber<Void> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void updateUniBooleanSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Boolean> uni = dao.updateUniBoolean(updated);
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(true);
  }

  @Test
  void updateUniBooleanFailure() {
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Boolean> uni = dao.updateUniBoolean(updated);
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(false);
  }

  @Test
  void updateResultSetSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    MutinyReactiveResultSet rs = dao.updateResultSet(updated);
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void updateResultSetFailure() {
    // when
    Product updated = new Product(product.getId(), "updated");
    MutinyReactiveResultSet rs = dao.updateResultSet(updated);
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
            });
  }

  @Test
  void updateMultiReactiveRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Multi<? extends Row> rs = dao.updateMultiReactiveRow(updated);
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void updateMultiReactiveRowFailure() {
    // when
    Product updated = new Product(product.getId(), "updated");
    Multi<? extends Row> rs = dao.updateMultiReactiveRow(updated);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
            });
  }

  @Test
  void updateMultiRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Multi<Row> rs = dao.updateMultiRow(updated);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void updateMultiRowFailure() {
    // when
    Product updated = new Product(product.getId(), "updated");
    Multi<Row> rs = dao.updateMultiRow(updated);
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
            });
  }

  @Test
  void deleteUniVoid() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Void> uni = dao.delete(product.getId());
    // then
    UniAssertSubscriber<Void> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void deleteUniBooleanSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Boolean> uni = dao.deleteUniBoolean(product);
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(true);
  }

  @Test
  void deleteUniBooleanFailure() {
    // when
    Uni<Boolean> uni = dao.deleteUniBoolean(product);
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(false);
  }

  @Test
  void deleteResultSetSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    MutinyReactiveResultSet rs = dao.deleteResultSet(product.getId());
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void deleteResultSetFailure() {
    // when
    MutinyReactiveResultSet rs = dao.deleteResultSet(product.getId());
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
            });
  }

  @Test
  void deleteMultiReactiveRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<? extends Row> rs = dao.deleteMultiReactiveRow(product.getId());
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void deleteMultiReactiveRowFailure() {
    // when
    Multi<? extends Row> rs = dao.deleteMultiReactiveRow(product.getId());
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
            });
  }

  @Test
  void deleteMultiRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<Row> rs = dao.deleteMultiRow(product.getId());
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean(0)).isTrue();
            });
  }

  @Test
  void deleteMultiRowFailure() {
    // when
    Multi<Row> rs = dao.deleteMultiRow(product.getId());
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(1);
              assertThat(row.getColumnDefinitions().get("\"[applied]\"")).isNotNull();
              assertThat(row.getBoolean("\"[applied]\"")).isFalse();
            });
  }

  @Test
  void findByIdSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Product> rs = dao.findById(product.getId());
    // then
    UniAssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(product);
  }

  @Test
  void findByIdFailure() {
    // when
    Uni<Product> rs = dao.findById(product.getId());
    // then
    UniAssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void findAll() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<Product> rs = dao.findAll();
    // then
    AssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion().assertItems(product);
  }

  @Test
  void selectMappedResultSet() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    MutinyMappedReactiveResultSet<Product> rs = dao.selectMappedResultSet();
    // then
    AssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion().assertItems(product);
  }

  @Test
  void querySelectMultiProduct() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<Product> rs = dao.querySelectMultiProduct();
    // then
    AssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion().assertItems(product);
  }

  @Test
  void querySelectUniProduct() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Product> rs = dao.querySelectUniProduct(product.getId());
    // then
    UniAssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(product);
  }

  @Test
  void querySelectResultSet() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    MutinyReactiveResultSet rs = dao.querySelectResultSet();
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void querySelectMappedResultSet() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    MutinyMappedReactiveResultSet<Product> rs = dao.querySelectMappedResultSet();
    // then
    AssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion().assertItems(product);
  }

  @Test
  void querySelectMultiRow() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<Row> rs = dao.querySelectMultiRow();
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void querySelectMultiReactiveRow() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<? extends Row> rs = dao.querySelectMultiReactiveRow();
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void querySelectUniRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Row> rs = dao.querySelectUniRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem())
        .isNotNull()
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void querySelectUniRowFailure() {
    // when
    Uni<Row> rs = dao.querySelectUniRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem()).isNull();
  }

  @Test
  void querySelectUniReactiveRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<? extends Row> rs = dao.querySelectUniReactiveRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem())
        .isNotNull()
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void querySelectUniReactiveRowFailure() {
    // when
    Uni<? extends Row> rs = dao.querySelectUniReactiveRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem()).isNull();
  }

  @Test
  void queryUpdateUniVoid() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Void> uni = dao.queryUpdateUniVoid(updated.getId(), updated.getName());
    // then
    UniAssertSubscriber<Void> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void queryUpdateUniBooleanSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Boolean> uni = dao.queryUpdateUniBoolean(updated.getId(), updated.getName());
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(true);
  }

  @Test
  void queryUpdateUniBooleanFailure() {
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Boolean> uni = dao.queryUpdateUniBoolean(updated.getId(), updated.getName());
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(false);
  }

  @Test
  void querySelectCount() {
    // given
    session.execute("INSERT INTO product (id, name) VALUES (?, ?)", UUID.randomUUID(), "name1");
    session.execute("INSERT INTO product (id, name) VALUES (?, ?)", UUID.randomUUID(), "name2");
    // when
    Uni<Long> uni = dao.querySelectCount();
    // then
    UniAssertSubscriber<Long> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(2L);
  }

  @Test
  void queryProviderSelectMultiProduct() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<Product> rs = dao.queryProviderSelectMultiProduct();
    // then
    AssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion().assertItems(product);
  }

  @Test
  void queryProviderSelectUniProduct() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Product> rs = dao.queryProviderSelectUniProduct(product.getId());
    // then
    UniAssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(product);
  }

  @Test
  void queryProviderSelectResultSet() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    MutinyReactiveResultSet rs = dao.queryProviderSelectResultSet();
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void queryProviderSelectMappedResultSet() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    MutinyMappedReactiveResultSet<Product> rs = dao.queryProviderSelectMappedResultSet();
    // then
    AssertSubscriber<Product> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion().assertItems(product);
  }

  @Test
  void queryProviderSelectMultiRow() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<Row> rs = dao.queryProviderSelectMultiRow();
    // then
    AssertSubscriber<? extends Row> subscriber =
        rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void queryProviderSelectMultiReactiveRow() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Multi<? extends Row> rs = dao.queryProviderSelectMultiReactiveRow();
    // then
    AssertSubscriber<Row> subscriber = rs.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<Row> rows = subscriber.getItems();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0))
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void queryProviderSelectUniRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<Row> rs = dao.queryProviderSelectUniRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem())
        .isNotNull()
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void queryProviderSelectUniRowFailure() {
    // when
    Uni<Row> rs = dao.queryProviderSelectUniRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem()).isNull();
  }

  @Test
  void queryProviderSelectUniReactiveRowSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Uni<? extends Row> rs = dao.queryProviderSelectUniReactiveRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem())
        .isNotNull()
        .satisfies(
            row -> {
              assertThat(row.getColumnDefinitions()).hasSize(2);
              assertThat(row.getUuid("id")).isEqualTo(product.getId());
              assertThat(row.getString("name")).isEqualTo(product.getName());
            });
  }

  @Test
  void queryProviderSelectUniReactiveRowFailure() {
    // when
    Uni<? extends Row> rs = dao.queryProviderSelectUniReactiveRow(product.getId());
    // then
    UniAssertSubscriber<Row> subscriber =
        rs.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted();
    assertThat(subscriber.getItem()).isNull();
  }

  @Test
  void queryProviderUpdateUniVoid() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Void> uni = dao.queryProviderUpdateUniVoid(updated.getId(), updated.getName());
    // then
    UniAssertSubscriber<Void> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
  }

  @Test
  void queryProviderUpdateUniBooleanSuccess() {
    // given
    session.execute(
        "INSERT INTO product (id, name) VALUES (?, ?)", product.getId(), product.getName());
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Boolean> uni = dao.queryProviderUpdateUniBoolean(updated.getId(), updated.getName());
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(true);
  }

  @Test
  void queryProviderUpdateUniBooleanFailure() {
    // when
    Product updated = new Product(product.getId(), "updated");
    Uni<Boolean> uni = dao.queryProviderUpdateUniBoolean(updated.getId(), updated.getName());
    // then
    UniAssertSubscriber<Boolean> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(false);
  }

  @Test
  void queryProviderSelectCount() {
    // given
    session.execute("INSERT INTO product (id, name) VALUES (?, ?)", UUID.randomUUID(), "name1");
    session.execute("INSERT INTO product (id, name) VALUES (?, ?)", UUID.randomUUID(), "name2");
    // when
    Uni<Long> uni = dao.queryProviderSelectCount();
    // then
    UniAssertSubscriber<Long> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(2L);
  }

  @Test
  void incrementUpVotes() {
    // when
    Uni<Void> uni = dao.incrementUpVotes(product.getId(), 10);
    // then
    UniAssertSubscriber<Void> subscriber =
        uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitItem().assertCompleted().assertItem(null);
    long counter = getCounterValue("up_votes");
    assertThat(counter).isEqualTo(10);
  }

  @Test
  void incrementDownVotes() {
    // when
    MutinyReactiveResultSet uni = dao.incrementDownVotes(product.getId(), 10);
    // then
    AssertSubscriber<ReactiveRow> subscriber =
        uni.subscribe().withSubscriber(AssertSubscriber.create(1));
    subscriber.awaitCompletion();
    List<? extends Row> rows = subscriber.getItems();
    assertThat(rows).isEmpty();
    long counter = getCounterValue("down_votes");
    assertThat(counter).isEqualTo(10);
  }

  private long getCounterValue(String counterColumn) {
    Row row =
        session
            .execute(
                "SELECT " + counterColumn + " FROM votes WHERE product_id = " + product.getId())
            .one();
    assertThat(row).isNotNull();
    return row.getLong(0);
  }
}
