package org.ai4db.dingo.init;

import org.ai4db.utils.DingoConnectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-07-15 16:23
 */
public class InitTable {
    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        if (args.length==0){
            System.out.println("No parameters passed");
            System.exit(0);
        }

        String path = args[0];
        Connection connection = DingoConnectionUtils.getConnection(path);
        Statement statement = connection.createStatement();

        String business_sql = "CREATE TABLE business(\n" +
                "        business_id VARCHAR NOT NULL,\n" +
                "        name VARCHAR  NOT NULL,\n" +
                "        address VARCHAR ,\n" +
                "        city VARCHAR,\n" +
                "        state VARCHAR,\n" +
                "        postal_code VARCHAR,\n" +
                "        stars FLOAT,\n" +
                "        review_count  INT,\n" +
                "        categories VARCHAR NOT NULL,\n" +
                "        feature FLOAT ARRAY NOT NULL,\n" +
                "        id BIGINT  NOT NULL,\n" +
                "        PRIMARY KEY (id,business_id),\n" +
                "        index city_index (city) with (name),\n" +
                "        index state_index (state) with (name),\n" +
                "        index postal_code_index (postal_code) with (address),\n" +
                "        index start_index (stars) partition by range values (2.0),(3.0),(4.0),(5.0),\n" +
                "        index review_count_index (review_count) partition by range values (20000),(40000),(60000),\n" +
                "        index feature_index vector(id, feature) partition by hash partitions=4 parameters(type=hnsw, metricType=L2, dimension=768, efConstruction=40, nlinks=32)        \n" +
                ")";
        statement.execute(business_sql);

        System.out.println("============================> business table created");



        String review_sql = "CREATE TABLE review(\n" +
                "        review_id VARCHAR NOT NULL,\n" +
                "        user_id VARCHAR NOT NULL,\n" +
                "        business_id VARCHAR NOT NULL,\n" +
                "        stars FLOAT,\n" +
                "        useful INT,\n" +
                "        funny INT,\n" +
                "        cool  INT,\n" +
                "        review_text  VARCHAR NOT NULL,\n" +
                "        review_date TIMESTAMP,\n" +
                "        feature FLOAT ARRAY NOT NULL,\n" +
                "        id BIGINT  NOT NULL,\n" +
                "        PRIMARY KEY (id,review_id),\n" +
                "        index start_index (stars) partition by range values (2.0),(3.0),(4.0),(5.0),\n" +
                "        index useful_index (useful) partition by range values (250),(750),(1250),\n" +
                "        index funny_index (funny) partition by range values (200),(500),(800),\n" +
                "        index cool_index (cool) partition by range values (200),(500),(800),\n" +
                "        index review_date_index (review_date) with (user_id),\n" +
                "        index feature_index vector(id, feature) partition by hash partitions=4 parameters(type=hnsw, metricType=L2, dimension=768, efConstruction=40, nlinks=32)\n" +
                ")";


        statement.execute(review_sql);
        System.out.println("============================> review table created");


        String tip_sql ="CREATE TABLE tip(\n" +
                "        user_id VARCHAR NOT NULL,\n" +
                "        business_id VARCHAR NOT NULL,\n" +
                "        review_text  VARCHAR NOT NULL,\n" +
                "        review_date TIMESTAMP,\n" +
                "        compliment_count INT,\n" +
                "        feature FLOAT ARRAY NOT NULL,\n" +
                "        id BIGINT  NOT NULL,\n" +
                "        PRIMARY KEY (id,user_id),\n" +
                "        index compliment_count_index (compliment_count) partition by range values (2),(4),(6),\n" +
                "        index review_date_index (review_date) with (business_id),\n" +
                "        index feature_index vector(id, feature) partition by hash partitions=4 parameters(type=hnsw, metricType=L2, dimension=768, efConstruction=40, nlinks=32)\n" +
                ")";
        statement.execute(tip_sql);
        System.out.println("============================> tip table created");
        statement.close();
        connection.close();
    }
}
