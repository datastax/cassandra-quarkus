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

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;

/**
 * A DAO for retrieving {@link Fruit} instances.
 *
 * @see <a
 *     href="https://docs.datastax.com/en/developer/java-driver/latest/manual/mapper/daos/">Defining
 *     DAOs with the DataStax Java driver object mapper</a>
 */
@Dao
public interface FruitDao {

  /**
   * Creates or updates the given {@link Fruit} in the database.
   *
   * @param fruit The {@link Fruit} to create or update. Cannot be null.
   */
  @Update
  void update(Fruit fruit);

  /**
   * Finds all the fruits.
   *
   * @return An {@linkplain PagingIterable iterable} containing all the results found in the
   *     database.
   */
  @Select
  PagingIterable<Fruit> findAll();
}
