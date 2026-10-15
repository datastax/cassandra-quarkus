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
package com.datastax.oss.quarkus.deployment.internal.driver;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

/** A codec that is a plain class, not a CDI bean, and can be instantiated by the extension. */
public class PriceCodec extends MappingCodec<Integer, Price> {

  public PriceCodec() {
    super(TypeCodecs.INT, GenericType.of(Price.class));
  }

  @Override
  protected Price innerToOuter(Integer value) {
    return value == null ? null : new Price(value);
  }

  @Override
  protected Integer outerToInner(Price value) {
    return value == null ? null : value.cents();
  }
}
