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
import com.datastax.oss.quarkus.tests.entity.Address;
import edu.umd.cs.findbugs.annotations.Nullable;

// This codec compiles but is total bogus. It will be excluded via config-option
public class ExcludedByConfigCodec extends MappingCodec<Integer, Address> {

  public ExcludedByConfigCodec() {
    super(TypeCodecs.INT, GenericType.of(Address.class));
  }

  @Nullable
  @Override
  protected Address innerToOuter(@Nullable Integer value) {
    if (value == null) {
      return null;
    }
    return new Address("fubar", "zip", "city");
  }

  @Nullable
  @Override
  protected Integer outerToInner(@Nullable Address value) {
    if (value == null) {
      return null;
    }
    return 1;
  }
}
