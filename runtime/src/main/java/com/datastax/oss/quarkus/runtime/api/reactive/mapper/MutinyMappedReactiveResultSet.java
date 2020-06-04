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
package com.datastax.oss.quarkus.runtime.api.reactive.mapper;

import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import io.smallrye.mutiny.Multi;

/**
 * A wrapper interface for {@code MappedReactiveResultSet<EntityT>} returned by the java driver
 * object mapper. It provides the translation from {@code Publisher<EntityT>} to {@code
 * Multi<EntityT}
 *
 * <p>You can leverage this class in the {@link Dao}:
 *
 * <pre>
 * &#64;Dao
 * public interface FruitDaoReactive {
 *   &#64;Select
 *   MutinyMappedReactiveResultSet<Fruit> findByIdAsync(String id);
 * }
 * </pre>
 *
 * @see MappedReactiveResultSet
 */
public interface MutinyMappedReactiveResultSet<EntityT>
    extends MappedReactiveResultSet<EntityT>, Multi<EntityT> {}
