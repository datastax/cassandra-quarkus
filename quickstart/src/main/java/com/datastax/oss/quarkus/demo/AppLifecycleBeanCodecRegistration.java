package com.datastax.oss.quarkus.demo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.datastax.oss.driver.internal.core.type.codec.extras.enums.EnumNameCodec;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.config.Priorities;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class AppLifecycleBeanCodecRegistration {

  @Inject CqlSession session;

  void onStart(@Observes @Priority(Priorities.PLATFORM - 10) StartupEvent ev) {
    TypeCodec<Fruit.Type> myEnumCodec = new EnumNameCodec<>(Fruit.Type.class);

    MutableCodecRegistry registry = (MutableCodecRegistry) session.getContext().getCodecRegistry();
    registry.register(myEnumCodec);
  }
}
