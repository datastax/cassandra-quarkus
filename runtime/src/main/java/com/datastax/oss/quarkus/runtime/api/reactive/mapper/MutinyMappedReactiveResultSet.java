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
import io.smallrye.mutiny.Multi;

/**
 * An adapter that adapts the driver's {@link MappedReactiveResultSet} to Mutiny's {@link Multi}.
 *
 * <p>This interface can be used in Dao methods wherever a {@link MappedReactiveResultSet} is
 * supported, e.g.:
 *
 * <pre>
 * &#64;Dao
 * public interface MyReactiveDao {
 *   &#64;Select
 *   MutinyMappedReactiveResultSet&lt;Fruit&gt; findAll();
 *   &#64;Select
 *   MutinyMappedReactiveResultSet&lt;Fruit&gt; findById(String id);
 * }
 * </pre>
 *
 * However, if you don't need any of the methods declared in this interface or its parents, like
 * {@link #getExecutionInfos()} for instance, it's often easier to use a {@link Multi} or a {@link
 * io.smallrye.mutiny.Uni Uni} directly – these return types are also supported in the Quarkus
 * extension:
 *
 * <pre>
 * &#64;Dao
 * public interface MyReactiveDao {
 *   &#64;Select
 *   Multi&lt;Fruit&gt; findAll();
 *   &#64;Select
 *   Uni&lt;Fruit&gt; findById();
 * }
 * </pre>
 *
 * @see MappedReactiveResultSet
 */
public interface MutinyMappedReactiveResultSet<EntityT>
    extends MappedReactiveResultSet<EntityT>, Multi<EntityT> {}
