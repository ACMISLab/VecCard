package org.ai4db.myscaledb;

import org.ai4db.utils.DingoConnectionUtils;
import org.ai4db.utils.DingoPropertiesUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Load {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        if (args.length==0){
            System.out.println("No parameters passed");
            System.exit(0);
        }
        String path = args[0];
        loadToProblem(path);
    }

    public static void loadToProblem(String path) throws SQLException, IOException, ClassNotFoundException {
        Properties properties = DingoPropertiesUtils.getProperties(path);
        Connection connection = DingoConnectionUtils.getConnection(path);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(3600);
        String filePath = properties.getProperty("load.data.problem.path");

        StringBuilder sql = null;
        String subSql = null;
        CSVRecord lineData = null;

        int startIndex = Integer.parseInt(properties.getProperty("load.data.problem.startColumnIndex"));
        int endIndex = Integer.parseInt(properties.getProperty("load.data.problem.endColumnIndex"));

        List<CSVRecord> records =new ArrayList<>();
        try (CSVParser parser = new CSVParser(new FileReader(filePath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            int currentIndex = 0;
            for (CSVRecord record : parser) {
                if (currentIndex >= startIndex && currentIndex < endIndex) {
                    records.add(record);
                }
                currentIndex++;
                if (currentIndex >= endIndex) {
                    break;
                }
            }
        }
        int batchSize = Integer.parseInt(properties.getProperty("load.data.problem.batchSize"));

        int totalSize = records.size();
        int totalBatches = (totalSize / batchSize) + (totalSize % batchSize > 0 ? 1 : 0);
        for (int i = 0; i < totalBatches; i++){
            sql = new StringBuilder("INSERT INTO problem (*) values");
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            List<CSVRecord> currentBatch = records.subList(start, end);
            for (int k = 0; k < currentBatch.size(); k++){
                lineData = currentBatch.get(k);
                subSql = " ("+
                        lineData.get(0)+",'"+
                        lineData.get(1)+"',"+
                        lineData.get(3)+","+
                        lineData.get(4)+",'"+
                        lineData.get(2).replace("'","")+"','"+
                        lineData.get(6)+"')";
                if (k==currentBatch.size()-1){
                    sql.append(subSql);
                }else {
                    sql.append(subSql).append(",");
                }
            }
            try {
                System.out.println("==================>InsertData:\t"+ (startIndex+start)+ " to "+(startIndex+end));
                statement.execute(String.valueOf(sql));
            } catch (Exception e) {
                System.out.println("\033[31m" + "ERROR ======  "+(startIndex+start)+ " to "+(startIndex+end));
            }
        }
        statement.close();
        connection.close();
    }

}
