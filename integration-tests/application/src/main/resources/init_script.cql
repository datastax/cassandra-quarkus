CREATE KEYSPACE IF NOT EXISTS ks1
    WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS ks1.customer
(
    customer_id   uuid PRIMARY KEY,
    customer_name text
);

CREATE TABLE IF NOT EXISTS ks1.product
(
    id   uuid PRIMARY KEY,
    name text
);

CREATE TABLE IF NOT EXISTS ks1.votes
(
    product_id uuid PRIMARY KEY,
    up_votes   counter,
    down_votes counter
);
