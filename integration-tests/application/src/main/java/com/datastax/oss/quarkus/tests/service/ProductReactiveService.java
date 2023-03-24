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
package com.datastax.oss.quarkus.tests.service;

import com.datastax.oss.quarkus.tests.dao.ProductReactiveDao;
import com.datastax.oss.quarkus.tests.entity.Product;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class ProductReactiveService {

  @Inject Uni<ProductReactiveDao> daoUni;

  public Uni<Void> create(Product product) {
    return daoUni.flatMap(dao -> dao.create(product));
  }

  public Uni<Void> update(Product product) {
    return daoUni.flatMap(dao -> dao.update(product));
  }

  public Uni<Void> delete(UUID productId) {
    return daoUni.flatMap(dao -> dao.delete(productId));
  }

  public Uni<Product> findById(UUID productId) {
    return daoUni.flatMap(dao -> dao.findById(productId));
  }

  public Multi<Product> findAll() {
    return daoUni.toMulti().flatMap(ProductReactiveDao::findAll);
  }
}
