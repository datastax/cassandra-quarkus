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
package com.datastax.oss.quarkus.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.codec.CodecNotFoundException;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.dao.PersonDao;
import com.datastax.oss.quarkus.tests.driver.BornCodec;
import com.datastax.oss.quarkus.tests.driver.excluded.Wildcard;
import com.datastax.oss.quarkus.tests.entity.Address;
import com.datastax.oss.quarkus.tests.entity.Birthdate;
import com.datastax.oss.quarkus.tests.entity.Born;
import com.datastax.oss.quarkus.tests.entity.Person;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class CustomCodecIT {

  @Inject QuarkusCqlSession session;

  @Inject PersonDao dao;

  @BeforeEach
  void truncateTable() {
    session.execute("TRUNCATE person");
  }

  @DisplayName("Born codec should be registered via discovery")
  @Test
  void should_register_discovered_codec_on_session() {
    assertThat(session.getContext().getCodecRegistry().codecFor(GenericType.of(Born.class)))
        .isInstanceOf(BornCodec.class);
  }

  @DisplayName("Birthdate codec should be registered via user-supplied provider-method")
  @Test
  void should_register_provider_codec_on_session() {
    // as the Birthday codec is private, we can only check that something is there
    assertThat(session.getContext().getCodecRegistry().codecFor(GenericType.of(Birthdate.class)))
        .isNotNull();
  }

  @DisplayName("Address codec should not be registered, its class name is excluded by config")
  @Test
  void should_not_register_excluded_codec_on_session() {
    assertThatExceptionOfType(CodecNotFoundException.class)
        .isThrownBy(
            () -> session.getContext().getCodecRegistry().codecFor(GenericType.of(Address.class)));
  }

  @DisplayName("Wildcard codec should not be registered, its package is excluded by config")
  @Test
  void should_not_register_wildcard_excluded_codec_on_session() {
    assertThatExceptionOfType(CodecNotFoundException.class)
        .isThrownBy(
            () -> session.getContext().getCodecRegistry().codecFor(GenericType.of(Wildcard.class)));
  }

  @DisplayName("DB-roundtrip properly handles custom types")
  @Test
  void should_round_trip_custom_type_through_mapper() {
    Person expected = new Person(UUID.randomUUID(), "Ada", Birthdate.of(31, 12, 1990));
    dao.create(expected);
    assertThat(dao.findById(expected.getId())).isEqualTo(expected);
  }

  @DisplayName("DB-roundtrip properly handles null custom types")
  @Test
  void should_round_trip_null_custom_type() {
    Person expected = new Person(UUID.randomUUID(), "unknown", null);
    dao.create(expected);
    assertThat(dao.findById(expected.getId())).isEqualTo(expected);
  }

  @DisplayName("DB-roundtrip properly writes inner value for custom codec types")
  @Test
  void should_store_custom_type_as_codec_inner_type() {
    Person person = new Person(UUID.randomUUID(), "Grace", Birthdate.of(31, 12, 1985));
    dao.create(person);
    // BornCodec maps Born onto a CQL int; read the raw column to prove the mapping really happened
    Row row =
        session.execute("SELECT born, birthdate FROM person WHERE id = ?", person.getId()).one();
    assertThat(row).isNotNull();
    assertThat(row.getInt("born")).isEqualTo(1985);
    assertThat(row.getLocalDate("birthdate")).isEqualTo(LocalDate.of(1985, 12, 31));
  }

  @DisplayName("Query-parameter can handle Born type")
  @Test
  void should_use_born_codec_for_query_parameter() {
    Person older = new Person(UUID.randomUUID(), "older", Birthdate.of(31, 12, 1970));
    Person younger = new Person(UUID.randomUUID(), "younger", Birthdate.of(31, 12, 2000));
    dao.create(older);
    dao.create(younger);
    assertThat(dao.findBornBefore(Born.inYear(1980)).all()).containsExactly(older);
  }

  @DisplayName("Query-parameter can handle Birthdate type")
  @Test
  void should_use_birthdate_codec_for_query_parameter() {
    Person older = new Person(UUID.randomUUID(), "older", Birthdate.of(31, 12, 1970));
    Person younger = new Person(UUID.randomUUID(), "younger", Birthdate.of(31, 12, 2000));
    dao.create(older);
    dao.create(younger);
    assertThat(dao.findBirthdateBefore(Birthdate.of(1, 1, 1971)).all()).containsExactly(older);
  }
}
