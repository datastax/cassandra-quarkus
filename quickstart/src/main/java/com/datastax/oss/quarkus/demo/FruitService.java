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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/** A service that manages {@link Fruit} objects, leveraging the {@link FruitDao} DAO. */
@ApplicationScoped
public class FruitService {

  @Inject FruitDao dao;

  public void save(Fruit fruit) {
    dao.update(fruit);
  }

  public List<Fruit> getAll() {
    return dao.findAll().all();
  }
}
