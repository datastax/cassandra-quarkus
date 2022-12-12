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

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 * An object mapper defining all the DAOs for this application.
 *
 * <p>Classes annotated with {@link Mapper} are the main entry point for the DataStax Java driver
 * object mapper.
 *
 * <p>An application-scoped {@link FruitMapper} bean will be automatically created and can be
 * injected everywhere in your application.
 *
 * @see <a
 *     href="https://docs.datastax.com/en/developer/java-driver/latest/manual/mapper/mapper/">Using
 *     the Mapper interface</a>
 */
@Mapper
public interface FruitMapper {

  /**
   * Creates a new {@link FruitDao}. It will operate on the same keyspace as the Quarkus session.
   *
   * <p>The application-scoped {@link FruitDao} bean produced by this method can be automatically
   * injected everywhere in your application.
   *
   * @return a new {@link FruitDao}.
   */
  @DaoFactory
  FruitDao fruitDao();

  /**
   * Creates a new {@link ReactiveFruitDao}. It will operate on the same keyspace as the Quarkus
   * session.
   *
   * <p>The application-scoped {@link FruitDao} bean produced by this method can be automatically
   * injected everywhere in your application.
   *
   * @return a new {@link ReactiveFruitDao}.
   */
  @DaoFactory
  ReactiveFruitDao reactiveFruitDao();
}
