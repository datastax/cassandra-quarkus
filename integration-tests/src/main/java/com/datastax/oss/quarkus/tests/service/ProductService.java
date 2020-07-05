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

import com.datastax.oss.quarkus.tests.dao.ProductDao;
import com.datastax.oss.quarkus.tests.entity.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProductService {

  @Inject ProductDao dao;

  public void create(Product product) {
    dao.create(product);
  }

  public void update(Product product) {
    dao.update(product);
  }

  public void delete(UUID productId) {
    dao.delete(productId);
  }

  public Product findById(UUID productId) {
    return dao.findById(productId);
  }

  public List<Product> findAll() {
    List<Product> products = new ArrayList<>();
    for (Product product : dao.findAll()) {
      products.add(product);
    }
    return products;
  }
}
