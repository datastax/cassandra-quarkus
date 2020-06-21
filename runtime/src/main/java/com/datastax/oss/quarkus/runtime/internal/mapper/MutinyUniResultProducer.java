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
package com.datastax.oss.quarkus.runtime.internal.mapper;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.driver.api.mapper.result.MapperResultProducer;
import com.datastax.oss.quarkus.runtime.internal.reactive.Wrappers;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.smallrye.mutiny.Uni;

public class MutinyUniResultProducer implements MapperResultProducer {

  @Override
  public boolean canProduce(@NonNull GenericType<?> resultType) {
    return Uni.class.equals(resultType.getRawType());
  }

  @Override
  public Object execute(
      @NonNull Statement<?> statement,
      @NonNull MapperContext context,
      @Nullable EntityHelper<?> entityHelper) {
    ReactiveResultSet source = context.getSession().executeReactive(statement);
    if (entityHelper == null) {
      // If no entity helper is present, accepted return types are: Uni<Void> or Uni<Row>
      return Wrappers.toUni(source);
    } else {
      // If an entity helper is present, accepted return type is Uni<EntityT>;
      // note that Uni does not comply with the Publisher specs and will emit a null item
      // when the query returns no rows; we need to catch that here.
      return Wrappers.toUni(source).map(row -> row == null ? null : entityHelper.get(row));
    }
  }

  @Nullable
  @Override
  public Object wrapError(@NonNull Exception e) {
    return Wrappers.failedUni(e);
  }
}
