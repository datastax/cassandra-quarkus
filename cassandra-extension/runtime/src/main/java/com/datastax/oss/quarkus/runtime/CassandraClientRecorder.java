package com.datastax.oss.quarkus.runtime;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.config.CassandraClientConfig;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

@Recorder
public class CassandraClientRecorder {

  public BeanContainerListener addCassandraClient(
      Class<? extends AbstractCassandraClientProducer> cqlSessionProducerClass) {
    return beanContainer -> beanContainer.instance(cqlSessionProducerClass);
  }

  public void configureRuntimeProperties(CassandraClientConfig config) {
    AbstractCassandraClientProducer producer =
        Arc.container().instance(AbstractCassandraClientProducer.class).get();
    producer.setCassandraClientConfig(config);
  }

  @SuppressWarnings("rawtypes")
  private AnnotationLiteral defaultName() {
    return Default.Literal.INSTANCE;
  }

  public RuntimeValue<CqlSession> getClient() {
    return new RuntimeValue<>(Arc.container().instance(CqlSession.class, defaultName()).get());
  }
}