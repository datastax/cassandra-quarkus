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
package com.datastax.oss.quarkus.tests.tests.init;

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@Dependent
public class DseGraphStarter {

  @Inject QuarkusCqlSession session;

  @SuppressWarnings("unused")
  public void onStartup(@Observes StartupEvent event) {
    session.execute(
        ScriptGraphStatement.newInstance("system.graph('food_qs').ifNotExists().create()"));
    session.execute(
        ScriptGraphStatement.newInstance(
                "schema.vertexLabel('person').\n"
                    + "  ifNotExists().\n"
                    + "  partitionBy('person_id', Uuid).\n"
                    + "  property('name', Text).\n"
                    + "  property('gender', Text).\n"
                    + "  property('nickname', setOf(Text)).\n"
                    + "  property('cal_goal', Int).\n"
                    + "  property('macro_goal', listOf(Int)).\n"
                    + "  property('country', listOf(tupleOf(Text, Date, Date))).\n"
                    + "  property('badge', mapOf(Text, Date)).\n"
                    + "  create()")
            .setGraphName("food_qs"));
    session.execute(
        ScriptGraphStatement.newInstance(
                "g.V().drop().iterate()\n"
                    + "g.addV('person').\n"
                    + "   property('person_id', 'e7cd5752-bc0d-4157-a80f-7523add8dbcd' as UUID).\n"
                    + "   property('name', 'Julia CHILD').\n"
                    + "   property('gender','F').\n"
                    + "   property('nickname', ['Jay', 'Julia'] as Set).\n"
                    + "   property('country', [['USA', '1912-08-12' as LocalDate, '1944-01-01' as LocalDate] as Tuple])")
            .setGraphName("food_qs"));
  }
}
