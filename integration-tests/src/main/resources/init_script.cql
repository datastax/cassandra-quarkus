CREATE KEYSPACE IF NOT EXISTS k1 WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
CREATE TABLE IF NOT EXISTS k1.test_NameConverterEntity(test_entityId int primary key);
CREATE TABLE IF NOT EXISTS k1.product(id uuid PRIMARY KEY, description text);