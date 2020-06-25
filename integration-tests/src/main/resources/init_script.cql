CREATE KEYSPACE IF NOT EXISTS k1
    WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS k1.customer
(
    customer_id   uuid PRIMARY KEY,
    customer_name text
);

CREATE TABLE IF NOT EXISTS k1.product
(
    id   uuid PRIMARY KEY,
    name text
);
