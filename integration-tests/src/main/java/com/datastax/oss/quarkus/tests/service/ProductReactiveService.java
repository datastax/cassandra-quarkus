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
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProductReactiveService {

  private final ProductReactiveDao dao;

  @Inject
  public ProductReactiveService(ProductReactiveDao dao) {
    this.dao = dao;
  }

  public Uni<Void> create(Product product) {
    return dao.create(product);
  }

  public Uni<Void> update(Product product) {
    return dao.update(product);
  }

  public Uni<Void> delete(UUID productId) {
    return dao.delete(productId);
  }

  public Uni<Product> findById(UUID productId) {
    return dao.findById(productId);
  }

  public Multi<Product> findAll() {
    return dao.findAll();
  }
}
