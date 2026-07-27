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
package com.datastax.oss.quarkus.deployment.api;

import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import io.quarkus.builder.item.MultiBuildItem;
import java.util.Objects;

/**
 * A {@link TypeCodec} implementation to register on the session produced by this extension.
 *
 * <p>The extension itself produces one item for each {@code TypeCodec} implementation found in the
 * application's Jandex index; other extensions may produce additional items to contribute codecs
 * that cannot be discovered that way.
 *
 * <p>Codecs are instantiated when the session is built, therefore the class must be public and must
 * declare a public no-arg constructor.
 */
public final class CassandraTypeCodecBuildItem extends MultiBuildItem {

  private final String codecClassName;

  public CassandraTypeCodecBuildItem(String codecClassName) {
    this.codecClassName = Objects.requireNonNull(codecClassName, "codecClassName cannot be null");
  }

  /** Returns the fully-qualified name of the codec class. */
  public String getCodecClassName() {
    return codecClassName;
  }
}
