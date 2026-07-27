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
 * A method that hands out {@link TypeCodec} instances to register on the session produced by this
 * extension, that is, a {@code public static} no-arg method returning {@code TypeCodec<?>[]},
 * declared in a public class.
 *
 * <p>The extension itself produces one item for each such method found in the application's Jandex
 * index; other extensions may produce additional items.
 *
 * <p>This is the way to register codecs that {@link CassandraTypeCodecBuildItem} cannot handle:
 * codec classes that are not public (typically code-generated ones, kept package-private behind a
 * factory method), and codecs that cannot be built from a no-arg constructor, such as those
 * returned by {@code ExtraTypeCodecs.enumNamesOf(...)}.
 */
public final class CassandraTypeCodecProviderBuildItem extends MultiBuildItem {

  private final String className;
  private final String methodName;

  public CassandraTypeCodecProviderBuildItem(String className, String methodName) {
    this.className = Objects.requireNonNull(className, "className cannot be null");
    this.methodName = Objects.requireNonNull(methodName, "methodName cannot be null");
  }

  /** Returns the fully-qualified name of the class declaring the method. */
  public String getClassName() {
    return className;
  }

  /** Returns the name of the method. */
  public String getMethodName() {
    return methodName;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CassandraTypeCodecProviderBuildItem that)) {
      return false;
    }
    return className.equals(that.className) && methodName.equals(that.methodName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(className, methodName);
  }

  @Override
  public String toString() {
    return className + '.' + methodName + "()";
  }
}
