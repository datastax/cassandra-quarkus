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
import com.datastax.dse.driver.internal.mapper.reactive.DefaultMappedReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.MutinyWrappers;
import com.datastax.oss.quarkus.runtime.internal.reactive.mapper.DefaultMutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.mapper.MapperMutinyWrappers;
import com.datastax.oss.quarkus.tests.entity.Product;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.UUID;

public class ProductReactiveQueries {

  private final EntityHelper<Product> productHelper;
  private final QuarkusCqlSession session;

  public ProductReactiveQueries(MapperContext context, EntityHelper<Product> productHelper) {
    this.productHelper = productHelper;
    session = (QuarkusCqlSession) context.getSession();
  }

  public Multi<Product> queryProviderSelectMultiProduct() {
    return MutinyWrappers.toMulti(
        new DefaultMappedReactiveResultSet<>(
            session.executeReactive("SELECT * FROM product"), productHelper::get));
  }

  public Uni<Product> queryProviderSelectUniProduct(UUID id) {
    return MapperMutinyWrappers.toEntityUni(
        session.executeReactive(
            SimpleStatement.newInstance("SELECT * FROM product WHERE id = ?", id)),
        productHelper);
  }

  public MutinyReactiveResultSet queryProviderSelectResultSet() {
    return new DefaultMutinyReactiveResultSet(session.executeReactive("SELECT * FROM product"));
  }

  public MutinyMappedReactiveResultSet<Product> queryProviderSelectMappedResultSet() {
    return new DefaultMutinyMappedReactiveResultSet<>(
        new DefaultMappedReactiveResultSet<>(
            session.executeReactive("SELECT * FROM product"), productHelper::get));
  }

  public Multi<Row> queryProviderSelectMultiRow() {
    return MapperMutinyWrappers.toRowMulti(session.executeReactive("SELECT * FROM product"));
  }

  public Multi<ReactiveRow> queryProviderSelectMultiReactiveRow() {
    return queryProviderSelectResultSet();
  }

  public Uni<Row> queryProviderSelectUniRow(UUID id) {
    return MapperMutinyWrappers.toRowUni(
        session.executeReactive(
            SimpleStatement.newInstance("SELECT * FROM product WHERE id = ?", id)));
  }

  public Uni<ReactiveRow> queryProviderSelectUniReactiveRow(UUID id) {
    return MutinyWrappers.toUni(
        session.executeReactive(
            SimpleStatement.newInstance("SELECT * FROM product WHERE id = ?", id)));
  }

  public Uni<Void> queryProviderUpdateUniVoid(UUID id, String name) {
    return MapperMutinyWrappers.toVoidUni(
        session.executeReactive(
            SimpleStatement.newInstance("UPDATE product set name = ? WHERE id = ?", name, id)));
  }

  public Uni<Boolean> queryProviderUpdateUniBoolean(UUID id, String name) {
    return MapperMutinyWrappers.toWasAppliedUni(
        session.executeReactive(
            SimpleStatement.newInstance(
                "UPDATE product set name = ? WHERE id = ? IF EXISTS", name, id)));
  }

  public Uni<Long> queryProviderSelectCount() {
    return MapperMutinyWrappers.toCountUni(
        session.executeReactive(SimpleStatement.newInstance("SELECT COUNT(*) FROM product")));
  }
}
