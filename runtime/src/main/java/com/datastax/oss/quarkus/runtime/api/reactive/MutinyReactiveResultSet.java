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
package com.datastax.oss.quarkus.runtime.api.reactive;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.smallrye.mutiny.Multi;

/**
 * An adapter that adapts the driver's {@link ReactiveResultSet} to Mutiny's {@link Multi}. All
 * reactive execution methods in {@link QuarkusCqlSession}, like {@link
 * QuarkusCqlSession#executeReactive(String)}, return instances of this type.
 *
 * <p>If you are using the object mapper, you might prefer {@link
 * com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet
 * MutinyMappedReactiveResultSet} instead, since it provides support for high-level entity types
 * instead of the low-level database rows exposed in this interface. A {@code
 * MutinyMappedReactiveResultSet} can be used in Dao methods wherever a {@link
 * com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet MappedReactiveResultSet} is
 * supported.
 *
 * @see ReactiveResultSet
 * @see QuarkusCqlSession#executeReactive(String)
 * @see QuarkusCqlSession#executeReactive(Statement)
 * @see com.datastax.oss.quarkus.runtime.api.reactive.mapper.MutinyMappedReactiveResultSet
 *     MutinyMappedReactiveResultSet
 * @see com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet MappedReactiveResultSet
 */
public interface MutinyReactiveResultSet
    extends Multi<ReactiveRow>, ReactiveResultSet, MutinyReactiveQueryMetadata {}
