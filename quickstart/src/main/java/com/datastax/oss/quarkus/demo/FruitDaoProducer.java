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

/**
 * A Quarkus component that creates DAOs and make them available as container beans for injection.
 */
public class FruitDaoProducer {

  private final FruitDao fruitDao;
  private final FruitDaoReactive fruitDaoReactive;

  @Inject
  public FruitDaoProducer(QuarkusCqlSession cqlSession, FruitServiceConfig fruitServiceConfig) {
    FruitMapper mapper = new FruitMapperBuilder(cqlSession).build();
    CqlIdentifier keyspace = CqlIdentifier.fromCql(fruitServiceConfig.keyspace);
    fruitDao = mapper.fruitDao(keyspace);
    fruitDaoReactive = mapper.fruitDaoReactive(keyspace);
  }

  /** @return A {@link FruitDao} singleton instance. */
  @Produces
  @ApplicationScoped
  FruitDao produceFruitDao() {
    return fruitDao;
  }

  /** @return A {@link FruitDaoReactive} singleton instance. */
  @Produces
  @ApplicationScoped
  FruitDaoReactive produceFruitDaoReactive() {
    return fruitDaoReactive;
  }
}
