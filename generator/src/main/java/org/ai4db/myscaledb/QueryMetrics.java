package org.ai4db.myscaledb;

import org.ai4db.utils.PathUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QueryMetrics {
    static final String CSV_PATH = "data/predicate/mcx/cardinality_mcx_test.csv";
    static final String CSV_PATH_1 = "data/dataset/problem.csv";
    static final String url = System.getenv().getOrDefault("VECCARD_CLICKHOUSE_URL", "jdbc:clickhouse://127.0.0.1:8123/yelp");
    static final int predict = 6;
    static final int limit = 10;
    static final Integer row = 200;

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
//        VectorSearch();
        FilteredVectorSearch();


    }

    public static void VectorSearch() throws SQLException, IOException{
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = sdf.format(date);
        System.out.println("Start Date: " + formattedDate);
        Connection conn = DriverManager.getConnection(url,System.getenv().getOrDefault("VECCARD_CLICKHOUSE_USER", "default"),System.getenv().getOrDefault("VECCARD_CLICKHOUSE_PASSWORD", ""));
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(3600);
        List<String> vectors  = new ArrayList<>();
        String vectorCsvPath = PathUtils.resolvePath(CSV_PATH_1);
        if (!Files.exists(Paths.get(vectorCsvPath))) {
            statement.close();
            conn.close();
            return;
        }
        try (CSVParser parser = new CSVParser(new FileReader(vectorCsvPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            int currentIndex = 0;
            for (CSVRecord record : parser) {
                vectors.add(record.get(6));
                currentIndex++;
                if (currentIndex >= row){
                    break;
                }
            }
        }

        List<Long> times = new ArrayList<>();
        int i = 1;
        for (String vector : vectors){
            System.out.println("正在处理第"+i+"行");
            i++;
            String query1 = "select lang,score,type,";
            String query2 = "distance('ef_s=50')(feature, "+vector+") as dist ";
            String query3 = "FROM problem ORDER BY dist LIMIT "+limit+";";
            String query = query1+query2+query3;
            System.out.println("Query:\t" +query);
            long startTime = System.currentTimeMillis();
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (columnCount>0){
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                times.add(duration);
            }
            resultSet.close();
        }
        System.out.println(times);
        System.out.println("Size:\t"+times.size());
        int avg_latency = calculateAverageInSeconds(times);
        System.out.println("Avg Latency:"+avg_latency);
        double qps = QPS(avg_latency, row);
        System.out.println("QPS:\t"+qps);
        statement.close();
        conn.close();

    }


    public static void FilteredVectorSearch() throws SQLException, IOException, ClassNotFoundException {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = sdf.format(date);
        System.out.println("Start Date: " + formattedDate);
        Connection conn = DriverManager.getConnection(url,System.getenv().getOrDefault("VECCARD_CLICKHOUSE_USER", "default"),System.getenv().getOrDefault("VECCARD_CLICKHOUSE_PASSWORD", ""));
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(3600);
        List<String> expr  = new ArrayList<>();
        String predicateCsvPath = PathUtils.resolvePath(CSV_PATH);
        String vectorCsvPath = PathUtils.resolvePath(CSV_PATH_1);
        if (!Files.exists(Paths.get(predicateCsvPath)) || !Files.exists(Paths.get(vectorCsvPath))) {
            statement.close();
            conn.close();
            return;
        }
        try (CSVParser parser = new CSVParser(new FileReader(predicateCsvPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                String lineData = record.get(1);
                String[] parts = lineData.split("\\.", 2);
                if (parts[0].equals("problem")){
                    int andCount = countOccurrences(lineData, " and ");
                    int orCount = countOccurrences(lineData, " or ");
                    int count = andCount+orCount;
                    if (count == predict-1 && expr.size()<row){
                        expr.add(lineData.replace("language","lang"));
                    }
                    if (expr.size() >= row){
                        break;
                    }
                }
            }
        }



        List<String> vectors  = new ArrayList<>();
        try (CSVParser parser = new CSVParser(new FileReader(vectorCsvPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            int currentIndex = 0;
            for (CSVRecord record : parser) {
                vectors.add(record.get(6));
                currentIndex++;
                if (currentIndex >= row){
                    break;
                }
            }
        }
        List<Long> times = new ArrayList<>();

        for (int i = 0; i < expr.size(); i++) {
            System.out.println("正在处理第"+(i+1)+"行");
            String[] parts = expr.get(i).split("\\.", 2);
            String tableName = parts[0];
            String whereCause = parts[1];

            String query1 = "select lang,score,type,";
            String query2 = "distance('ef_s=50')(feature, "+vectors.get(i)+") as dist ";
            String query3 = "FROM "+tableName;
            String query4 = " WHERE "+whereCause;
            String query5 = " ORDER BY dist LIMIT "+limit+";";
            String query = query1+query2+query3+query4+query5;
            System.out.println("Query:\t" +query);
            long startTime = System.currentTimeMillis();
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (columnCount>0){
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                times.add(duration);
            }
            resultSet.close();
        }

        System.out.println(times);
        System.out.println("Size:\t"+times.size());
        int avg_latency = calculateAverageInSeconds(times);
        System.out.println("Avg Latency:"+avg_latency);
        double qps = QPS(avg_latency, row);
        System.out.println("QPS:\t"+qps);
        statement.close();
        conn.close();
    }


    public static int calculateAverageInSeconds(List<Long> times) {
        if (times == null || times.isEmpty()) {
            throw new IllegalArgumentException("列表不能为空");
        }

        long sum = 0;
        for (Long time : times) {
            sum += time;
        }

        // 计算平均值，并将毫秒转换为秒
        double averageInMilliseconds = (double) sum / times.size();
        // 取整并转换为秒
        int averageInSeconds = (int) (averageInMilliseconds);
        return averageInSeconds;
    }

    public static int countOccurrences(String str, String sub) {
        int count = 0;
        int fromIndex = 0;

        while ((fromIndex = str.indexOf(sub, fromIndex)) != -1) {
            count++;
            fromIndex += sub.length();
        }
        return count;
    }

    public static double QPS(int avg_latency, int row){
        return row*1000/avg_latency;
    }

}
