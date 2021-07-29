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
package com.datastax.oss.quarkus.tests.dao;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Increment;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.QueryProvider;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.tests.entity.Product;
import com.datastax.oss.quarkus.tests.entity.Votes;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.UUID;

/**
 * This DAO is intended to test mapper code generation for the Quarkus extension. It contains
 * methods sporting all supported Mutiny return types declared in QuarkusDaoReturnTypeKind, in
 * various DAO methods whose generation is handled by the Quarkus mapper processor.
 */
@Dao
public interface ProductReactiveDao {

  // insert

  @Insert
  Uni<Void> create(Product product);

  @Insert(ifNotExists = true)
  Uni<Boolean> createUniBoolean(Product product);

  @Insert(ifNotExists = true)
  Uni<Product> createUniProduct(Product product);

  @Insert(ifNotExists = true)
  MutinyReactiveResultSet createResultSet(Product product);

  @Insert(ifNotExists = true)
  Multi<ReactiveRow> createMultiReactiveRow(Product product);

  @Insert(ifNotExists = true)
  Multi<Row> createMultiRow(Product product);

  // update

  @Update
  Uni<Void> update(Product product);

  @Update(ifExists = true)
  Uni<Boolean> updateUniBoolean(Product product);

  @Update(ifExists = true)
  MutinyReactiveResultSet updateResultSet(Product product);

  @Update(ifExists = true)
  Multi<ReactiveRow> updateMultiReactiveRow(Product product);

  @Update(ifExists = true)
  Multi<Row> updateMultiRow(Product product);

  // delete

  @Delete(entityClass = Product.class)
  Uni<Void> delete(UUID productId);

  @Delete(ifExists = true)
  Uni<Boolean> deleteUniBoolean(Product product);

  @Delete(entityClass = Product.class, ifExists = true)
  MutinyReactiveResultSet deleteResultSet(UUID productId);

  @Delete(entityClass = Product.class, ifExists = true)
  Multi<ReactiveRow> deleteMultiReactiveRow(UUID productId);

  @Delete(entityClass = Product.class, ifExists = true)
  Multi<Row> deleteMultiRow(UUID productId);

  // select

  @Select
  Uni<Product> findById(UUID productId);

  @Select
  Multi<Product> findAll();

  @Select
  MutinyMappedReactiveResultSet<Product> selectMappedResultSet();

  // query

  @Query("SELECT * FROM product")
  Multi<Product> querySelectMultiProduct();

  @Query("SELECT * FROM product WHERE id = :id")
  Uni<Product> querySelectUniProduct(UUID id);

  @Query("SELECT * FROM product")
  MutinyReactiveResultSet querySelectResultSet();

  @Query("SELECT * FROM product")
  MutinyMappedReactiveResultSet<Product> querySelectMappedResultSet();

  @Query("SELECT * FROM product")
  Multi<Row> querySelectMultiRow();

  @Query("SELECT * FROM product")
  Multi<ReactiveRow> querySelectMultiReactiveRow();

  @Query("SELECT * FROM product WHERE id = :id")
  Uni<Row> querySelectUniRow(UUID id);

  @Query("SELECT * FROM product WHERE id = :id")
  Uni<ReactiveRow> querySelectUniReactiveRow(UUID id);

  @Query("UPDATE product set name = :name WHERE id = :id")
  Uni<Void> queryUpdateUniVoid(UUID id, String name);

  @Query("UPDATE product set name = :name WHERE id = :id IF EXISTS")
  Uni<Boolean> queryUpdateUniBoolean(UUID id, String name);

  @Query("SELECT COUNT(*) FROM product")
  Uni<Long> querySelectCount();

  // query provider

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Multi<Product> queryProviderSelectMultiProduct();

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Uni<Product> queryProviderSelectUniProduct(UUID id);

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  MutinyReactiveResultSet queryProviderSelectResultSet();

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  MutinyMappedReactiveResultSet<Product> queryProviderSelectMappedResultSet();

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Multi<Row> queryProviderSelectMultiRow();

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Multi<ReactiveRow> queryProviderSelectMultiReactiveRow();

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Uni<Row> queryProviderSelectUniRow(UUID id);

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Uni<ReactiveRow> queryProviderSelectUniReactiveRow(UUID id);

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Uni<Void> queryProviderUpdateUniVoid(UUID id, String name);

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Uni<Boolean> queryProviderUpdateUniBoolean(UUID id, String name);

  @QueryProvider(providerClass = ProductReactiveQueries.class, entityHelpers = Product.class)
  Uni<Long> queryProviderSelectCount();

  // Increment

  @Increment(entityClass = Votes.class)
  Uni<Void> incrementUpVotes(UUID productId, long upVotes);

  @Increment(entityClass = Votes.class)
  MutinyReactiveResultSet incrementDownVotes(UUID productId, long downVotes);
}
