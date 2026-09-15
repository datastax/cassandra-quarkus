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
package com.datastax.oss.quarkus.tests.driver;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.quarkus.tests.entity.Born;
import edu.umd.cs.findbugs.annotations.Nullable;

public class BornCodec extends MappingCodec<Integer, Born> {

  public BornCodec() {
    super(TypeCodecs.INT, GenericType.of(Born.class));
  }

  @Nullable
  @Override
  protected Born innerToOuter(@Nullable Integer value) {
    if (value == null) {
      return null;
    }
    return new Born(value);
  }

  @Nullable
  @Override
  protected Integer outerToInner(@Nullable Born value) {
    if (value == null) {
      return null;
    }
    return value.value();
  }
}
