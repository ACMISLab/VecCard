package org.ai4db.dingo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SelectVectorAndScalarNodeIndexTest {
    public static String  driver = "io.dingodb.driver.client.DingoDriverClient";
    public static String  url = System.getenv().getOrDefault("DINGODB_JDBC_URL", "jdbc:dingo:thin:url=127.0.0.1:8765/dingo");
    public static String user = System.getenv().getOrDefault("DINGODB_USER", "root");
    public static String password = System.getenv().getOrDefault("DINGODB_PASSWORD", "");
    public static Connection connection;
    public static String tableName = "demo2";


////    @BeforeEach
//    void setUp() throws ClassNotFoundException, SQLException {
//        Class.forName(driver);
//        connection = DriverManager.getConnection(url,user,password);
//    }

    @Test
    void intiTable() throws SQLException {
//        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE %s(\n" +
                "    id int,\n" +
                "    name varchar(32),\n" +
                "    feature float array not null,\n" +
                "    feature_id bigint not null,\n" +
                "    index feature_index vector(feature_id, feature) partition by hash partitions=5 parameters(type=hnsw, metricType=L2, dimension=64, efConstruction=40, nlinks=32)\n" +
                ");";
        sql = String.format(sql, tableName);

        System.out.println(sql);

//        statement.execute(sql);
//        statement.close();
    }

    @Test
    void insertData() throws SQLException {
//        Statement statement = connection.createStatement();
        String sql = "INSERT INTO "+tableName+"(id,name,feature, feature_id) VALUES ";
        int num = 20;
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

            subSql = "("+k+",'"+ UUID.randomUUID().toString().replace("-","") +"',array" + floatArrayAsString + ", " + k + ")";
            if (k < num) {
                subSql += ",";
            }
            sql += subSql;
        }

        System.out.println("============>");
        System.out.println(sql+";");
//        try {
//            System.out.println("============>插入数据");
//            statement.execute(sql);
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//        statement.close();
    }

    @Test
    void selectData() throws SQLException {
//        Statement statement = connection.createStatement();
        String baseSql = "select name,feature_id,feature_index$distance from vector("+tableName+", feature, array%s, 10, map[efSearch, 40])order by feature_index$distance limit 10;";

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
        System.out.println(sql);
//        try (ResultSet resultSet = statement.executeQuery(sql)){
//            while (resultSet.next()) {
//                System.out.println(resultSet.getString(1)+"\t"+resultSet.getString(2)+"\t"+resultSet.getString(3)+"\t");
//            }
//        }
//
//        statement.execute(sql);
//        statement.close();
    }


    @Test
    void selectProblemTableData() throws SQLException {
        Statement statement = connection.createStatement();
        String path  = "/root/data/mooccubex/problem/vector_"+1+".csv";
        CsvReader reader = CsvUtil.getReader();
        List<CsvRow> rows = reader.read(FileUtil.file(path)).getRows();
        rows.remove(0);
        Random random = new Random();
        int randomIndex = random.nextInt(rows.size());
        CsvRow randomElement = rows.get(randomIndex);
        String vector = randomElement.get(7);
        System.out.println("============>"+randomElement.get(3));
        System.out.println(vector);

        String baseSql = "select id,score,content,content_vector_index$distance\n"+
                "from vector(problem,content_vector,array%s,10, map[efSearch, 12])order by content_vector_index$distance limit 10";

        String sql = String.format(baseSql, vector);
        System.out.println("==================>");
        System.out.println(sql);
        try (ResultSet resultSet = statement.executeQuery(sql)){
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1)+"\t"+resultSet.getString(2)+"\t"+resultSet.getString(3)+"\t"+resultSet.getString(4));
            }
        }
        statement.close();
    }


    @Test
    void selectProblemTableDataNoDistance() throws SQLException {
        Statement statement = connection.createStatement();
        String path  = "/root/data/mooccubex/problem/vector_"+1+".csv";
        CsvReader reader = CsvUtil.getReader();
        List<CsvRow> rows = reader.read(FileUtil.file(path)).getRows();
        rows.remove(0);
        Random random = new Random();
        int randomIndex = random.nextInt(rows.size());
        CsvRow randomElement = rows.get(randomIndex);
        String vector = randomElement.get(7);
        System.out.println("============>"+randomElement.get(3));
        System.out.println(vector);

        String baseSql = "select id,score,content\n"+
                "from vector(problem,content_vector,array%s,10, map[efSearch, 12]) limit 10";

        String sql = String.format(baseSql, vector);
        System.out.println("==================>");
        System.out.println(sql);
        try (ResultSet resultSet = statement.executeQuery(sql)){
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1)+"\t"+resultSet.getString(2)+"\t"+resultSet.getString(3)+"\t");
            }
        }
        statement.close();
    }




//    @AfterAll
//    static void close() throws SQLException {
//        connection.close();
//    }


}
