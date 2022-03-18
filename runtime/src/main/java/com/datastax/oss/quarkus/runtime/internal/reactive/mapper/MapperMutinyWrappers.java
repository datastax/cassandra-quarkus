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
package com.datastax.oss.quarkus.runtime.internal.reactive.mapper;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.mapper.MapperException;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.quarkus.runtime.internal.reactive.MutinyWrappers;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Methods in this class are only referenced by generated mapper code, see
 * cassandra-quarkus-mapper-processor module.
 */
public class MapperMutinyWrappers {

  private static final CqlIdentifier APPLIED = CqlIdentifier.fromInternal("[applied]");

  private static final MapperException NO_ROW =
      new MapperException("Expected the COUNT query to return exactly one row");

  private static final MapperException INVALID_ROW =
      new MapperException(
          "Expected the COUNT query to return a column with CQL type BIGINT in first position");

  @SuppressWarnings("unused")
  public static <EntityT> Uni<EntityT> toEntityUni(
      ReactiveResultSet source, EntityHelper<EntityT> entityHelper) {
    return MutinyWrappers.toUni(source).map(row -> extractEntity(row, entityHelper));
  }

  @SuppressWarnings("unused")
  public static Uni<Void> toVoidUni(ReactiveResultSet source) {
    return MutinyWrappers.toUni(source).map(ignored -> null);
  }

  @SuppressWarnings("unused")
  public static Uni<Boolean> toWasAppliedUni(ReactiveResultSet source) {
    return MutinyWrappers.toUni(source)
        // The main publisher must complete in order for wasApplied to complete
        .flatMap(ignored -> MutinyWrappers.toUni(source.wasApplied()));
  }

  @SuppressWarnings("unused")
  public static Uni<Long> toCountUni(ReactiveResultSet source) {
    return MutinyWrappers.toUni(source).flatMap(MapperMutinyWrappers::extractCount);
  }

  @SuppressWarnings("unused")
  public static Uni<Row> toRowUni(ReactiveResultSet source) {
    return MutinyWrappers.toUni(source).onItem().castTo(Row.class);
  }

  @SuppressWarnings("unused")
  public static Multi<Row> toRowMulti(ReactiveResultSet source) {
    return MutinyWrappers.toMulti(source).onItem().castTo(Row.class);
  }

  @SuppressWarnings("unused")
  public static <T> Uni<T> failedUni(Throwable error) {
    return Uni.createFrom().failure(error);
  }

  @SuppressWarnings("unused")
  public static <T> Multi<T> failedMulti(Throwable error) {
    return Multi.createFrom().failure(error);
  }

  /** Copy of {@code DaoBase#asEntity(Row, EntityHelper)}. */
  private static <EntityT> EntityT extractEntity(Row row, EntityHelper<EntityT> entityHelper) {
    if (row == null) {
      return null;
    }
    // Special case for INSERT IF NOT EXISTS. If the row did not exist, the
    // query returns only [applied], we want to return null to indicate there
    // was no previous entity
    ColumnDefinitions cols = row.getColumnDefinitions();
    if (cols.size() == 1 && cols.get(0).getName().equals(APPLIED)) {
      return null;
    }
    return entityHelper.get(row, false);
  }

  /** Copy of {@code DaoBase#extractCount(Row)}. */
  private static Uni<Long> extractCount(Row row) {
    if (row == null) {
      return Uni.createFrom().failure(NO_ROW);
    }
    ColumnDefinitions columns = row.getColumnDefinitions();
    if (columns.size() == 0 || !columns.get(0).getType().equals(DataTypes.BIGINT)) {
      return Uni.createFrom().failure(INVALID_ROW);
    }
    return Uni.createFrom().item(row.getLong(0));
  }
}
