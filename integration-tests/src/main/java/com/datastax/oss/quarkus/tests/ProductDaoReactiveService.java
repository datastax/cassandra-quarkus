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

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.quarkus.tests.dao.InventoryMapper;
import com.datastax.oss.quarkus.tests.dao.ProductDaoReactive;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProductDaoReactiveService {

  private final ProductDaoReactive dao;

  @Inject
  public ProductDaoReactiveService(InventoryMapper inventoryMapper) {
    dao = inventoryMapper.productDaoReactive(CqlIdentifier.fromCql("k1"));
  }

  ProductDaoReactive getDao() {
    return dao;
  }
}
