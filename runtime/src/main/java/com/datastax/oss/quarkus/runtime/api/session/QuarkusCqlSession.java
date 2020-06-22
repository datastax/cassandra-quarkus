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
package com.datastax.oss.quarkus.runtime.api.session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyContinuousReactiveSession;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyGraphReactiveSession;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveSession;

/**
 * A specialized {@link CqlSession} tailored for Quarkus applications.
 *
 * <p>This interface is the main entry point for Quarkus applications that need to connect to
 * Cassandra databases. Upon application startup, the Cassandra Quarkus extension will configure an
 * application-scoped singleton bean implementing this interface, and then inject it in all
 * application components that require access to the Cassandra database.
 *
 * <p>This interface also implements {@link MutinyReactiveSession}; it exposes reactive query
 * methods such as {@link #executeReactive(String)}, which return Mutiny subtypes that integrate
 * seamlessly with any application using reactive-style programming.
 *
 * @see CqlSession
 * @see MutinyReactiveSession
 * @see com.datastax.oss.quarkus.runtime.api.config.CassandraClientConfig
 */
public interface QuarkusCqlSession
    extends CqlSession,
        MutinyReactiveSession,
        MutinyContinuousReactiveSession,
        MutinyGraphReactiveSession {}
