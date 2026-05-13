import vdb.dingodb as dg


def get_vector_index_parameters(config):
    index = config["vector_index"]
    match index:
        case "hnsw":
            return "(type=" + config["vector_index"] + ",metricType=" + config["metric_type"] + ",dimension=" + str(
                config["dimension"]) + ",efConstruction=" + str(config[index]["efConstruction"]) + ",nlinks=" + str(
                config[index]["links"]) + ")"

        case "ivf_flat":
            return "(type=" + config["vector_index"].replace("_",
                                                             "") + ",metricType=" + config[
                "metric_type"] + ",dimension=" + str(
                config["dimension"]) + ",ncentroids=" + str(config[index]["centroids"]) + ")"

        case "ivf_pq":
            return "(type=" + config["vector_index"].replace("_",
                                                             "") + ",metricType=" + config[
                "metric_type"] + ",dimension=" + str(
                config["dimension"]) + ",ncentroids=" + str(config[index]["centroids"]) + ",nsubvector=" + str(
                config[index]["subvector"]) + ",nbitsPerIdx=" + str(config[index]["nbitsPerIdx"]) + ")"

        case "flat":
            return "(type=" + config["vector_index"] + ",metricType=" + config["metric_type"] + ",dimension=" + str(
                config["dimension"]) + ")"


def create_table(config):
    print("Creating table...")
    db_config = config["database"]
    db = dg.DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                           db_config["db"])
    db.create_connection()
    exist_sql = "DROP TABLE IF EXISTS " + db_config["table"] + ";"
    db.execute(exist_sql)
    sql = "CREATE TABLE " + db_config["table"] + " (\n"
    sql += "    business_id VARCHAR NOT NULL,\n"
    sql += "    name VARCHAR NOT NULL,\n"
    sql += "    city VARCHAR,\n"
    sql += "    state VARCHAR,\n"
    sql += "    stars FLOAT,\n"
    sql += "    review INT,\n"
    sql += "    favorites INT,\n"
    sql += "    avgspend INT,\n"
    sql += "    popular boolean,\n"
    sql += "    categories VARCHAR NOT NULL,\n"
    sql += "    feature FLOAT ARRAY NOT NULL,\n"
    sql += "    id BIGINT  NOT NULL,\n"
    sql += "    PRIMARY KEY (id,business_id),\n"
    sql += "    index city_index (city) with (id),\n"
    sql += "    index state_index (state) with (id),\n"
    sql += ("index start_index (stars) partition by range values (0.25),(0.5),(0.75),(1.0),(1.25),(1.5),(1.75),(2.0),"
            "(2.25),(2.5),(2.75),(3.0),(3.25),(3.5),(3.75),(4.0),(4.25),(4.5),(4.75),(5.0),\n")
    sql += ("index review_index (review) partition by range values (5000),(10000),(15000),(20000),(25000),(30000),"
            "(35000),(40000),(45000),(50000),(55000),(60000),\n")
    sql += ("index favorites_index (favorites) partition by range values (20),(40),(60),(80),(100),(120),(140),(160),"
            "(180),(200),(220),(240),\n")
    sql += ("index avgspend_index (avgspend) partition by range values (20),(40),(60),(80),(100),(120),(140),(160),"
            "(180),(200),\n")
    sql += "    index popular_index (popular) with (id),\n"
    sql += "    index feature_index vector(id, feature) parameters" + get_vector_index_parameters(
        config["create"]) + "\n"
    sql += ")engine=" + db_config["engine"] + ";"
    # sql += ");"
    print(sql)
    db.execute(sql)
    db.close_connection()


def create_table_distributed(config):
    print("Creating distributed table...")
    db_config = config["database"]
    partition_range = ["(" + str(i * 10000) + ")" for i in range(1, db_config["partition"])]
    partition_range_v = ",".join(partition_range)
    db = dg.DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                           db_config["db"])
    db.create_connection()
    exist_sql = "DROP TABLE IF EXISTS " + db_config["table"] + ";"
    db.execute(exist_sql)
    sql = "CREATE TABLE " + db_config["table"] + " (\n"
    sql += "    id BIGINT  NOT NULL,\n"
    sql += "    name VARCHAR NOT NULL,\n"
    sql += "    city VARCHAR,\n"
    sql += "    state VARCHAR,\n"
    sql += "    stars FLOAT,\n"
    sql += "    review INT,\n"
    sql += "    favorites INT,\n"
    sql += "    avgspend INT,\n"
    sql += "    popular boolean,\n"
    sql += "    categories VARCHAR NOT NULL,\n"
    sql += "    feature FLOAT ARRAY NOT NULL,\n"
    sql += "    PRIMARY KEY (id),\n"
    sql += "    index feature_index vector(id, feature) parameters" + get_vector_index_parameters(
        config["create"]) + "\n"
    sql += ")partition by range values " + partition_range_v + ";"
    print(sql)
    db.execute(sql)
    db.close_connection()


def delete_table(config):
    print("Deleting table...")
    db_config = config["database"]
    db = dg.DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                           db_config["db"])
    db.create_connection()
    sql = "DROP TABLE IF EXISTS " + db_config["table"] + ";"
    db.execute(sql)
    db.close_connection()
