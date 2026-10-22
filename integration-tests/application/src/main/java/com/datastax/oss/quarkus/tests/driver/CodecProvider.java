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
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.quarkus.runtime.api.config.CassandraCodecProvider;
import com.datastax.oss.quarkus.tests.entity.Birthdate;
import java.time.LocalDate;

// This will be used to give the user control over hidden codecs
public class CodecProvider {

  @CassandraCodecProvider
  public static TypeCodec<?>[] customCodecs() {
    return new TypeCodec[] {new BirthdateCodec()};
  }

  private static class BirthdateCodec extends MappingCodec<LocalDate, Birthdate> {
    public BirthdateCodec() {
      super(TypeCodecs.DATE, GenericType.of(Birthdate.class));
    }

    @Override
    protected Birthdate innerToOuter(LocalDate value) {
      if (value == null) {
        return null;
      }
      return new Birthdate(value);
    }

    @Override
    protected LocalDate outerToInner(Birthdate value) {
      if (value == null) {
        return null;
      }
      return value.date();
    }
  }
}
