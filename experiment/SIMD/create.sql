CREATE TABLE review(
        id BIGINT  NOT NULL,
        feature FLOAT ARRAY NOT NULL,
        PRIMARY KEY (id),
        index feature_index vector(id, feature) partition by hash partitions=10 parameters(type=hnsw, metricType=L2, dimension=768, efConstruction=100, nlinks=32)
)partition by range values (500000),(1000000),(1500000),(2000000),(2500000),(3000000),(3500000),(4000000);


CREATE TABLE review(
        id BIGINT  NOT NULL,
        feature FLOAT ARRAY NOT NULL,
        PRIMARY KEY (id),
        index feature_index vector(id, feature) partition by hash partitions=10 parameters(type=hnsw, metricType=L2, dimension=768, efConstruction=100, nlinks=32)
)partition by range values (20000),(40000),(60000),(80000),(100000);


CREATE TABLE review(
        id BIGINT  NOT NULL,
        feature FLOAT ARRAY NOT NULL,
        PRIMARY KEY (id),
        index feature_index vector(id, feature) partition by hash partitions=10 parameters(type=flat, metricType=L2, dimension=768)
)partition by range values (20000),(40000),(60000),(80000),(100000);