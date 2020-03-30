package com.datastax.oss.quarkus.dao.nameconverters;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface TestMapper {
    @DaoFactory
    NameConverterEntityDao nameConverterEntityDao(@DaoKeyspace CqlIdentifier keyspace);
}