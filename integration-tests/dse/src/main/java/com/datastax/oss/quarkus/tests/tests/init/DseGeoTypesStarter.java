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

import com.datastax.dse.driver.api.core.data.geometry.Point;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import io.quarkus.runtime.StartupEvent;
import java.time.Duration;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Dependent
public class DseGeoTypesStarter {

  @Inject QuarkusCqlSession session;

  @SuppressWarnings("unused")
  public void onStartup(@Observes StartupEvent event) {
    session.execute(
        SimpleStatement.newInstance(
                "CREATE TABLE IF NOT EXISTS points (pk int PRIMARY KEY, pt 'PointType')")
            .setTimeout(Duration.ofSeconds(10)));
    session.execute(
        SimpleStatement.newInstance(
            "INSERT INTO points (pk, pt) VALUES (?, ?)", 0, Point.fromCoordinates(12, 34)));
  }
}
