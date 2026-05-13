package org.ai4db.dingo.explain;

import org.ai4db.utils.PathUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cardinality {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String connectUrl = System.getenv().getOrDefault("VECCARD_MYSQL_URL", "jdbc:mysql://127.0.0.1:3307/yelp");
    static final String explain = "explain plan for ";
    static final String CSV_PATH = "data/predicate/mcx/cardinality_mcx_test.csv";

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(connectUrl, System.getenv().getOrDefault("VECCARD_MYSQL_USER", "root"), System.getenv().getOrDefault("VECCARD_MYSQL_PASSWORD", ""));
        Statement statement = connection.createStatement();

        List<CSVRecord> records =new ArrayList<>();
        String csvPath = PathUtils.resolvePath(CSV_PATH);
        if (!Files.exists(Paths.get(csvPath))) {
            statement.close();
            connection.close();
            return;
        }
        try (CSVParser parser = new CSVParser(new FileReader(csvPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                records.add(record);
            }
        }
        for (CSVRecord csvRecord :records){
            String expr = csvRecord.get(1);
            String[] parts = expr.split("\\.", 2);
            String tableName = parts[0];
            String whereCause = parts[1];
            whereCause = whereCause.replace("language","lang");
            String query = explain + "select count(*) from " + tableName + " where " + whereCause+";";
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String s = resultSet.getObject(columnName).toString();
                    String[] lines = s.split("\r\n|\r|\n");
                    for (String line : lines) {
                        if (line.startsWith("DingoRoot")){
                            Pattern pattern = Pattern.compile("rowcount = ([\\d\\.]+)");
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                String rowcountValue = matcher.group(1);
                                System.out.println("Rowcount value: " + rowcountValue);
                            } else {
                                System.out.println("No match found.");
                            }
                        }
                        if (line.startsWith("IMPLEMENTATION PLAN")){
                            break;
                        }
                    }
                }
            }
            resultSet.close();
        }
        statement.close();
        connection.close();
    }
}
