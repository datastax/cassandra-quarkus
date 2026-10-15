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
package com.datastax.oss.quarkus.tests.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.quarkus.tests.entity.Birthdate;
import com.datastax.oss.quarkus.tests.entity.Born;
import com.datastax.oss.quarkus.tests.entity.Person;
import java.util.UUID;

@Dao
public interface PersonDao {

  @Insert
  void create(Person person);

  @Select
  Person findById(UUID id);

  /** Exercises the codec on a bind marker rather than on an entity column. */
  @Query("SELECT * FROM ${qualifiedTableId} WHERE born <= :born ALLOW FILTERING")
  PagingIterable<Person> findBornBefore(Born born);

  /** Exercises the codec on a bind marker rather than on an entity column. */
  @Query("SELECT * FROM ${qualifiedTableId} WHERE birthdate <= :birthdate ALLOW FILTERING")
  PagingIterable<Person> findBirthdateBefore(Birthdate birthdate);
}
