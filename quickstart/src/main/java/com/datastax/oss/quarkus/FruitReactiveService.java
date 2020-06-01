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
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FruitReactiveService {

  private final FruitDaoReactive fruitDao;

  @Inject
  public FruitReactiveService(FruitMapper fruitMapper, FruitServiceConfig fruitServiceConfig) {
    fruitDao = fruitMapper.fruitDaoReactive(CqlIdentifier.fromCql(fruitServiceConfig.keyspace));
  }

  public Uni<Void> add(Fruit fruit) {
    return fruitDao.update(fruit);
  }

  public Multi<Fruit> get(String id) {
    return fruitDao.findById(id);
  }
}
