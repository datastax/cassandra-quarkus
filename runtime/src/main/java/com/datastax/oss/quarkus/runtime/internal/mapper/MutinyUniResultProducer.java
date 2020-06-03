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
    return resultType.getRawType().equals(Uni.class);
  }

  @Override
  public <EntityT> Object execute(
      @NonNull Statement<?> statement,
      @NonNull MapperContext context,
      @Nullable EntityHelper<EntityT> entityHelper) {
    return Wrappers.toUni(context.getSession().executeAsync(statement));
  }

  @Override
  public Object wrapError(@NonNull Throwable error) {
    return Wrappers.failedUni(error);
  }
}
