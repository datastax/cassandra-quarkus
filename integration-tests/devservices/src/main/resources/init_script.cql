CREATE KEYSPACE IF NOT EXISTS ks1
    WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS ks1.product
(
    id   uuid PRIMARY KEY,
    name text
);

INSERT INTO ks1.product (id, name) VALUES (00000000-0000-0000-0000-000000000001, 'product1');