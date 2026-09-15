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

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import com.datastax.oss.quarkus.tests.dao.PersonDao;
import com.datastax.oss.quarkus.tests.driver.BornCodec;
import com.datastax.oss.quarkus.tests.entity.Born;
import com.datastax.oss.quarkus.tests.entity.Person;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

  @Test
  void should_register_discovered_codec_on_session() {
    assertThat(session.getContext().getCodecRegistry().codecFor(GenericType.of(Born.class)))
        .isInstanceOf(BornCodec.class);
  }

  @Test
  void should_round_trip_custom_type_through_mapper() {
    Person expected = new Person(UUID.randomUUID(), "Ada", Born.inYear(1990));
    dao.create(expected);
    assertThat(dao.findById(expected.getId())).isEqualTo(expected);
  }

  @Test
  void should_round_trip_null_custom_type() {
    Person expected = new Person(UUID.randomUUID(), "unknown", null);
    dao.create(expected);
    assertThat(dao.findById(expected.getId())).isEqualTo(expected);
  }

  @Test
  void should_store_custom_type_as_codec_inner_type() {
    Person person = new Person(UUID.randomUUID(), "Grace", Born.inYear(1985));
    dao.create(person);
    // BornCodec maps Born onto a CQL int; read the raw column to prove the mapping really happened
    Row row = session.execute("SELECT born FROM person WHERE id = ?", person.getId()).one();
    assertThat(row).isNotNull();
    assertThat(row.getInt("born")).isEqualTo(1985);
  }

  @Test
  void should_use_custom_codec_for_query_parameter() {
    Person older = new Person(UUID.randomUUID(), "older", Born.inYear(1970));
    Person younger = new Person(UUID.randomUUID(), "younger", Born.inYear(2000));
    dao.create(older);
    dao.create(younger);
    assertThat(dao.findBornBefore(Born.inYear(1980)).all()).containsExactly(older);
  }
}
