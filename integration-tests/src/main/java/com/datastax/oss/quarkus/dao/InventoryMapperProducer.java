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
package com.datastax.oss.quarkus.dao;

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class InventoryMapperProducer {
  private final QuarkusCqlSession cqlSession;

  @Inject
  public InventoryMapperProducer(QuarkusCqlSession cqlSession) {
    this.cqlSession = cqlSession;
  }

  @Produces
  @ApplicationScoped
  InventoryMapper produceFruitMapper() {
    cqlSession.execute(
        "CREATE KEYSPACE IF NOT EXISTS k1 WITH replication "
            + "= {'class':'SimpleStrategy', 'replication_factor':1};");

    cqlSession.execute(
        "CREATE TABLE IF NOT EXISTS k1.product(id uuid PRIMARY KEY, description text)");

    cqlSession.execute(
        "CREATE TABLE IF NOT EXISTS k1.test_NameConverterEntity(test_entityId int primary key)");

    return new InventoryMapperBuilder(cqlSession).build();
  }
}
