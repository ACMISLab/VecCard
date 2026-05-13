package org.ai4db.dingo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Random;

public class SelectVectorTest {

        public static String  driver = "io.dingodb.driver.client.DingoDriverClient";
        public static String  url = System.getenv().getOrDefault("DINGODB_JDBC_URL", "jdbc:dingo:thin:url=127.0.0.1:8765/dingo");
        public static String user = System.getenv().getOrDefault("DINGODB_USER", "root");
        public static String password = System.getenv().getOrDefault("DINGODB_PASSWORD", "");
        public static Connection connection;

        @BeforeEach
        void setUp() throws ClassNotFoundException, SQLException {
                Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv().getOrDefault("VECCARD_RUN_INTEGRATION_TESTS", "false")));
                Class.forName(driver);
                connection = DriverManager.getConnection(url,user,password);
        }

        @Test
        void intiTable() throws SQLException {
                Statement statement = connection.createStatement();
                String sql = "CREATE TABLE demo(\n" +
                        "    feature float array not null,\n" +
                        "    feature_id bigint not null,\n" +
                        "    index feature_index vector(feature_id, feature) partition by hash partitions=5 parameters(type=hnsw, metricType=L2, dimension=64, efConstruction=40, nlinks=32)\n" +
                        ")";

                statement.execute(sql);
                statement.close();
        }

        @Test
        void insertData() throws SQLException {
                Statement statement = connection.createStatement();
                String sql = "INSERT INTO demo(feature, feature_id) VALUES";
                int num = 100;
                Random random = new Random();
                float[] floatArray = new float[64];
                String floatArrayAsString = null;
                String subSql = null;

                for (int k = 1; k <= num; k++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        for (int i = 0; i < floatArray.length; i++) {
                                floatArray[i] = random.nextFloat(); // 生成0到1之间的随机float数
                                sb.append(floatArray[i]);
                                if (i < floatArray.length - 1) {
                                        sb.append(", "); // 在元素之间添加逗号和空格，除了最后一个元素
                                }
                        }
                        sb.append("]");
                       floatArrayAsString = sb.toString();

                       subSql = "(array" + floatArrayAsString + ", " + k + ")";
                       if (k < num) {
                               subSql += ",";
                       }
                       sql += subSql;
                }

                System.out.println("============>");
                System.out.println(sql);
                try {
                        System.out.println("============>插入数据");
                        statement.execute(sql);
                }catch (SQLException e){
                        e.printStackTrace();
                }
                statement.close();
        }

        @Test
        void selectData() throws SQLException {
                Statement statement = connection.createStatement();
                String baseSql = "select feature_id from vector(demo, feature, array%s, 10, map[efSearch, 40]) limit 10;";

                Random random = new Random();
                float[] floatArray = new float[64];
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < floatArray.length; i++) {
                        floatArray[i] = random.nextFloat(); // 生成0到1之间的随机float数
                        sb.append(floatArray[i]);
                        if (i < floatArray.length - 1) {
                                sb.append(", "); // 在元素之间添加逗号和空格，除了最后一个元素
                        }
                }
                sb.append("]");
                String floatArrayAsString = sb.toString();

                String sql = String.format(baseSql, floatArrayAsString);
                System.out.println("============>");
                try (ResultSet resultSet = statement.executeQuery(sql)){
                        while (resultSet.next()) {
                                System.out.println("===========================================");
                        }
                }

                statement.execute(sql);
                statement.close();
        }



        @AfterAll
        static void close() throws SQLException {
               if (connection != null) {
                       connection.close();
               }
        }




}
