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
package com.datastax.oss.quarkus.tests.mapper;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.tests.dao.CustomerDao;
import com.datastax.oss.quarkus.tests.dao.ProductDao;
import com.datastax.oss.quarkus.tests.dao.ProductReactiveDao;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class InventoryMapperProducer {

  private final InventoryMapper mapper;

  @Inject
  public InventoryMapperProducer(QuarkusCqlSession cqlSession) {
    mapper = new InventoryMapperBuilder(cqlSession).build();
  }

  @Produces
  @ApplicationScoped
  ProductDao produceProductDao() {
    return mapper.productDao(CqlIdentifier.fromCql("k1"));
  }

  @Produces
  @ApplicationScoped
  ProductReactiveDao produceProductReactiveDao() {
    return mapper.productReactiveDao(CqlIdentifier.fromCql("k1"));
  }

  @Produces
  @ApplicationScoped
  CustomerDao produceCustomerDao() {
    return mapper.customerDao(CqlIdentifier.fromCql("k1"));
  }
}
