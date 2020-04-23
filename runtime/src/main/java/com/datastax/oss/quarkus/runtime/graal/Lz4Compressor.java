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
package com.datastax.oss.quarkus.runtime.graal;

import com.datastax.oss.driver.api.core.context.DriverContext;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.buffer.ByteBuf;
import java.util.function.BooleanSupplier;

@TargetClass(
    className = "com.datastax.oss.driver.internal.core.protocol.Lz4Compressor",
    onlyWith = Lz4MissingSelector.class)
final class Lz4Compressor {

  @Substitute
  public Lz4Compressor(DriverContext context) {
    // no-op
  }

  @Substitute
  protected ByteBuf compressHeap(ByteBuf input) {
    throw new UnsupportedOperationException(
        "Lz4 compression is not supported when the org.lz4:lz4-java dependency is not present.");
  }

  @Substitute
  protected ByteBuf decompressDirect(ByteBuf input) {
    throw new UnsupportedOperationException(
        "Lz4 compression is not supported when the org.lz4:lz4-java dependency is not present.");
  }

  @Substitute
  protected ByteBuf decompressHeap(ByteBuf input) {
    throw new UnsupportedOperationException(
        "Lz4 compression is not supported when the org.lz4:lz4-java dependency is not present.");
  }

  @Substitute
  protected ByteBuf compressDirect(ByteBuf input) {
    throw new UnsupportedOperationException(
        "Lz4 compression is not supported when the org.lz4:lz4-java dependency is not present.");
  }
}

final class Lz4MissingSelector implements BooleanSupplier {

  @Override
  public boolean getAsBoolean() {
    try {
      Class.forName("net.jpountz.lz4.LZ4Compressor");
      return false;
    } catch (ClassNotFoundException e) {
      return true;
    }
  }
}
