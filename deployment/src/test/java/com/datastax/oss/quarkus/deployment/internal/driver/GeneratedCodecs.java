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
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

/**
 * Mimics a code generator that keeps its codecs behind a single factory method, the way annotation
 * processors usually emit them.
 */
public final class GeneratedCodecs {

  private GeneratedCodecs() {}

  /** The only entry point a generator needs to expose. */
  public static TypeCodec<?>[] all() {
    return new TypeCodec[] {new TemperatureCodec(), new PressureCodec()};
  }

  public record Temperature(int celsius) {}

  public record Pressure(int millibars) {}

  public record Humidity(int percent) {}

  /** Public, so index scanning finds it too: the extension must not register it twice. */
  public static class TemperatureCodec extends MappingCodec<Integer, Temperature> {

    public TemperatureCodec() {
      super(TypeCodecs.INT, GenericType.of(Temperature.class));
    }

    @Override
    protected Temperature innerToOuter(Integer value) {
      return value == null ? null : new Temperature(value);
    }

    @Override
    protected Integer outerToInner(Temperature value) {
      return value == null ? null : value.celsius();
    }
  }

  /** Public, but deliberately not returned by {@link #all()}: index scanning must still find it. */
  public static class HumidityCodec extends MappingCodec<Integer, Humidity> {

    public HumidityCodec() {
      super(TypeCodecs.INT, GenericType.of(Humidity.class));
    }

    @Override
    protected Humidity innerToOuter(Integer value) {
      return value == null ? null : new Humidity(value);
    }

    @Override
    protected Integer outerToInner(Humidity value) {
      return value == null ? null : value.percent();
    }
  }

  /** Package-private, so it is only reachable through {@link #all()}. */
  static class PressureCodec extends MappingCodec<Integer, Pressure> {

    public PressureCodec() {
      super(TypeCodecs.INT, GenericType.of(Pressure.class));
    }

    @Override
    protected Pressure innerToOuter(Integer value) {
      return value == null ? null : new Pressure(value);
    }

    @Override
    protected Integer outerToInner(Pressure value) {
      return value == null ? null : value.millibars();
    }
  }
}
