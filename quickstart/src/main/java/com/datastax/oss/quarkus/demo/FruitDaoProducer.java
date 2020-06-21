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
package com.datastax.oss.quarkus.demo;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class FruitDaoProducer {

  private final FruitDao fruitDao;
  private final FruitDaoReactive fruitDaoReactive;

  @Inject
  public FruitDaoProducer(QuarkusCqlSession cqlSession, FruitServiceConfig fruitServiceConfig) {
    FruitMapper mapper = new FruitMapperBuilder(cqlSession).build();
    CqlIdentifier keyspace = CqlIdentifier.fromCql(fruitServiceConfig.getKeyspace());
    fruitDao = mapper.fruitDao(keyspace);
    fruitDaoReactive = mapper.fruitDaoReactive(keyspace);
  }

  @Produces
  @ApplicationScoped
  FruitDao produceFruitDao() {
    return fruitDao;
  }

  @Produces
  @ApplicationScoped
  FruitDaoReactive produceFruitDaoReactive() {
    return fruitDaoReactive;
  }
}
