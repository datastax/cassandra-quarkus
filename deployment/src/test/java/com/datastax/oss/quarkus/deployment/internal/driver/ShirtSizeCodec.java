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

import com.datastax.oss.driver.internal.core.type.codec.extras.enums.EnumNameCodec;

/**
 * A codec that index scanning cannot find, although it is public, concrete and has a public no-arg
 * constructor: its parent chain leaves the index at {@link EnumNameCodec}, a driver class that
 * ships no Jandex index and that the extension does not scan for subtypes. Jandex keys subtypes by
 * direct supertype name, so the walk down from {@code MappingCodec} stops before this class.
 *
 * <p>Its only way in is {@code quarkus.cassandra.codecs}.
 */
public class ShirtSizeCodec extends EnumNameCodec<ShirtSize> {

  public ShirtSizeCodec() {
    super(ShirtSize.class);
  }
}
