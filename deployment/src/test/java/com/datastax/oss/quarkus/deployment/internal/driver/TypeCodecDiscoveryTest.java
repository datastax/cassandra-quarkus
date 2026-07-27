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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.datastax.oss.driver.api.core.type.codec.CodecNotFoundException;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestResource.class)
public class TypeCodecDiscoveryTest {

  @RegisterExtension
  static final QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClasses(CassandraTestResource.class)
                      // codecs written by hand, one registrable and one not
                      .addClasses(Price.class, PriceCodec.class, Weight.class, WeightCodec.class)
                      // codecs handed out by a factory method, the way generators emit them
                      .addClasses(
                          GeneratedCodecs.class,
                          GeneratedCodecs.Temperature.class,
                          GeneratedCodecs.Pressure.class,
                          GeneratedCodecs.Humidity.class,
                          GeneratedCodecs.TemperatureCodec.class,
                          GeneratedCodecs.PressureCodec.class,
                          GeneratedCodecs.HumidityCodec.class));

  @Inject QuarkusCqlSession session;

  @Test
  public void should_register_type_codec_found_in_application() {
    CodecRegistry codecRegistry = session.getContext().getCodecRegistry();
    TypeCodec<Price> codec = codecRegistry.codecFor(GenericType.of(Price.class));
    assertThat(codec).isInstanceOf(PriceCodec.class);
  }

  @Test
  public void should_register_type_codecs_returned_by_provider_method() {
    CodecRegistry codecRegistry = session.getContext().getCodecRegistry();
    // public nested codec: found by index scanning too, but registered only through all()
    assertThat(codecRegistry.codecFor(GenericType.of(GeneratedCodecs.Temperature.class)))
        .isInstanceOf(GeneratedCodecs.TemperatureCodec.class);
    // package-private nested codec: all() is the only way to get an instance
    assertThat(codecRegistry.codecFor(GenericType.of(GeneratedCodecs.Pressure.class)))
        .isInstanceOf(GeneratedCodecs.PressureCodec.class);
  }

  @Test
  public void should_register_public_type_codec_that_no_provider_method_returns() {
    CodecRegistry codecRegistry = session.getContext().getCodecRegistry();
    assertThat(codecRegistry.codecFor(GenericType.of(GeneratedCodecs.Humidity.class)))
        .isInstanceOf(GeneratedCodecs.HumidityCodec.class);
  }

  @Test
  public void should_ignore_type_codec_without_no_arg_constructor() {
    CodecRegistry codecRegistry = session.getContext().getCodecRegistry();
    assertThatThrownBy(() -> codecRegistry.codecFor(GenericType.of(Weight.class)))
        .isInstanceOf(CodecNotFoundException.class);
  }
}
