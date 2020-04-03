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
package com.datastax.oss.quarkus;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.dao.InventoryMapperBuilder;
import com.datastax.oss.quarkus.dao.ProductDao;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProductDaoService {

  private final ProductDao dao;

  @Inject
  public ProductDaoService(CqlSession session) {

    session.execute(
        "CREATE KEYSPACE IF NOT EXISTS k1 WITH replication "
            + "= {'class':'SimpleStrategy', 'replication_factor':1};");

    session.execute("CREATE TABLE IF NOT EXISTS k1.product(id uuid PRIMARY KEY, description text)");
    dao = new InventoryMapperBuilder(session).build().productDao(CqlIdentifier.fromCql("k1"));
  }

  ProductDao getDao() {
    return dao;
  }
}
