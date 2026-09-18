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
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@DisplayName("Disabled Codec-Discovery")
@QuarkusTestResource(CassandraTestResource.class)
public class CodecDiscoveryDisabledTest {

  @RegisterExtension
  static final QuarkusUnitTest runner =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClasses(CassandraTestResource.class)
                      // codecs written by hand
                      .addClasses(Price.class, PriceCodec.class)
                      // codecs handed out by a factory method
                      .addClasses(
                          GeneratedCodecs.class,
                          GeneratedCodecs.Temperature.class,
                          GeneratedCodecs.Pressure.class,
                          GeneratedCodecs.TemperatureCodec.class,
                          GeneratedCodecs.PressureCodec.class)
                      // configuration with disabled discovery
                      .addAsResource(
                          new StringAsset("quarkus.cassandra.codecs.discovery.enabled=false"),
                          "application.properties"));

  @Inject QuarkusCqlSession session;

  @DisplayName("should not register PriceCodec as discovery is disabled")
  @Test
  public void should_not_register_codec_via_discovery() {
    CodecRegistry codecRegistry = session.getContext().getCodecRegistry();
    assertThatThrownBy(() -> codecRegistry.codecFor(GenericType.of(Price.class)))
        .isInstanceOf(CodecNotFoundException.class);
  }

  // TODO should disabling also disable the provider-method?
  @DisplayName(
      "should still register TemperatureCodec and PressureCodec via user-supplied provider method")
  @Test
  public void should_register_codecs_returned_by_provider_method() {
    CodecRegistry codecRegistry = session.getContext().getCodecRegistry();
    // not discovered, but annotated method available
    assertThat(codecRegistry.codecFor(GenericType.of(GeneratedCodecs.Temperature.class)))
        .isInstanceOf(GeneratedCodecs.TemperatureCodec.class);
    assertThat(codecRegistry.codecFor(GenericType.of(GeneratedCodecs.Pressure.class)))
        .isInstanceOf(GeneratedCodecs.PressureCodec.class);
  }
}
