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

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A service that manages {@link Fruit} objects using reactive-style programming. This service
 * leverages the {@link ReactiveFruitDao} DAO.
 */
@ApplicationScoped
public class ReactiveFruitService {

  @Inject ReactiveFruitDao fruitDao;

  public Uni<Void> add(Fruit fruit) {
    return fruitDao.update(fruit);
  }

  public Multi<Fruit> getAll() {
    return fruitDao.findAll();
  }
}
