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

import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.mapper.MappedResultProducer;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.quarkus.runtime.internal.reactive.Wrappers;
import io.smallrye.mutiny.Uni;

public class UniMappedResultSetProducer implements MappedResultProducer {
  private static final GenericType<Uni<?>> PRODUCED_TYPE = new GenericType<Uni<?>>() {};

  @Override
  public boolean canProduce(GenericType<?> resultType) {
    return resultType.isSubtypeOf(PRODUCED_TYPE);
  }

  @Override
  public <EntityT> Object execute(
      Statement<?> statement, MapperContext context, EntityHelper<EntityT> entityHelper) {
    return Wrappers.toUni(context.getSession().executeAsync(statement));
  }

  @Override
  public Object wrapError(Throwable error) {
    return Wrappers.failedUni(error);
  }
}
