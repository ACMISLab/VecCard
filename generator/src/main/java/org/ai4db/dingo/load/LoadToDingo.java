package org.ai4db.dingo.load;

import org.ai4db.utils.DingoConnectionUtils;
import org.ai4db.utils.DingoPropertiesUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-07-15 19:47
 */
public class LoadToDingo {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        if (args.length==0){
            System.out.println("No parameters passed");
            System.exit(0);
        }
        String path = args[0];
        String targetTable = args[1];
        if (targetTable.equals("business")){
            loadToBusiness(path);
        }else if (targetTable.equals("tip")){
            loadToTip(path);
        } else if (targetTable.equals("review")) {
            loadToReview(path);
        }else if (targetTable.equals("problem")){
            loadToProblem(path);
        } else {
            System.out.println("No such table");
            System.exit(0);
        }
    }

    public static void loadToBusiness(String path) throws IOException, SQLException, ClassNotFoundException {
        Properties properties = DingoPropertiesUtils.getProperties(path);
        Connection connection = DingoConnectionUtils.getConnection(path);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(3600);
        String filePath = properties.getProperty("load.data.business.path");

        StringBuilder sql = null;
        String subSql = null;
        CSVRecord lineData = null;


        int startIndex = Integer.parseInt(properties.getProperty("load.data.business.startColumnIndex"));
        int endIndex = Integer.parseInt(properties.getProperty("load.data.business.endColumnIndex"));

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
        int batchSize = Integer.parseInt(properties.getProperty("load.data.business.batchSize"));

        int totalSize = records.size();
        int totalBatches = (totalSize / batchSize) + (totalSize % batchSize > 0 ? 1 : 0);
        for (int i = 0; i < totalBatches; i++){
            sql = new StringBuilder("INSERT INTO business values");
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            int index_id = startIndex+start;
            List<CSVRecord> currentBatch = records.subList(start, end);
            for (int k = 0; k < currentBatch.size(); k++){
                lineData = currentBatch.get(k);
                subSql = " ('"+
                        lineData.get(0).replace("'","")+"','"+
                        lineData.get(1).replace("'","")+"','"+
                        lineData.get(3).replace("'","")+"','"+
                        lineData.get(4)+"',"+
                        lineData.get(6)+","+
                        lineData.get(7)+","+
                        lineData.get(8)+","+
                        lineData.get(9)+","+
                        lineData.get(10)+",'"+
                        lineData.get(11).replace("'","")+"',array"+
                        lineData.get(12)+","+
                        (index_id+k+1)+")";
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
                e.printStackTrace();
                System.out.println("\033[31m" + "ERROR ======  "+(startIndex+start)+ " to "+(startIndex+end));
                break;
            }
        }
        statement.close();
        connection.close();
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
            sql = new StringBuilder("INSERT INTO problem values");
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            int index_id = startIndex+start;
            List<CSVRecord> currentBatch = records.subList(start, end);
            for (int k = 0; k < currentBatch.size(); k++){
                lineData = currentBatch.get(k);
                subSql = " ("+
                        lineData.get(0)+",'"+
                        lineData.get(1)+"',"+
                        lineData.get(3)+","+
                        lineData.get(4)+",'"+
                        lineData.get(2).replace("'","")+"',array"+
                        lineData.get(6)+")";
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


    public static void loadToTip(String path) throws IOException, SQLException, ClassNotFoundException {
        Properties properties = DingoPropertiesUtils.getProperties(path);
        Connection connection = DingoConnectionUtils.getConnection(path);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(3600);
        String filePath = properties.getProperty("load.data.tip.path");

        StringBuilder sql = null;
        String subSql = null;
        CSVRecord lineData = null;

        int count = Integer.parseInt(properties.getProperty("load.data.tip.count"));

        int startIndex = Integer.parseInt(properties.getProperty("load.data.tip.startColumnIndex"));
        int endIndex = Integer.parseInt(properties.getProperty("load.data.tip.endColumnIndex"));

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

        int batchSize = Integer.parseInt(properties.getProperty("load.data.tip.batchSize"));

        int totalSize = records.size();
        int totalBatches = (totalSize / batchSize) + (totalSize % batchSize > 0 ? 1 : 0);
        for (int i = 0; i < totalBatches; i++){
            sql = new StringBuilder("INSERT INTO tip values");
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            int index_id = startIndex+start;
            List<CSVRecord> currentBatch = records.subList(start, end);
            for (int k = 0; k < currentBatch.size(); k++){
                lineData = currentBatch.get(k);
                subSql = " ('"+
                        lineData.get(0)+"','"+
                        lineData.get(1)+"',"+
                        lineData.get(2)+","+
                        lineData.get(3)+","+
                        lineData.get(4)+",'"+
                        lineData.get(5).replace("'","")+"',array"+
                        lineData.get(6)+","+
                        (count+index_id+k+1)+")";
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



    public static void loadToReview(String path) throws IOException, SQLException, ClassNotFoundException {
        Properties properties = DingoPropertiesUtils.getProperties(path);
        Connection connection = DingoConnectionUtils.getConnection(path);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(3600);
        String filePath = properties.getProperty("load.data.review.path");

        StringBuilder sql = null;
        String subSql = null;
        CSVRecord lineData = null;


        int startIndex = Integer.parseInt(properties.getProperty("load.data.review.startColumnIndex"));
        int endIndex = Integer.parseInt(properties.getProperty("load.data.review.endColumnIndex"));

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

        int count = Integer.parseInt(properties.getProperty("load.data.review.count"));

        int batchSize = Integer.parseInt(properties.getProperty("load.data.review.batchSize"));

        int totalSize = records.size();
        int totalBatches = (totalSize / batchSize) + (totalSize % batchSize > 0 ? 1 : 0);

        for (int i = 0; i < totalBatches; i++){
            sql = new StringBuilder("INSERT INTO review values");
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            int index_id = startIndex+start;
            List<CSVRecord> currentBatch = records.subList(start, end);
            for (int k = 0; k < currentBatch.size(); k++){
                lineData = currentBatch.get(k);
                subSql = " ('"+
                        lineData.get(0)+"',"+
                        lineData.get(1)+","+
                        lineData.get(2)+","+
                        lineData.get(3)+","+
                        lineData.get(4)+","+
                        lineData.get(5)+","+
                        lineData.get(6)+","+
                        lineData.get(7)+","+
                        lineData.get(8)+",'"+
                        lineData.get(9).replace("'","")+"',array"+
                        lineData.get(10)+","+
                        (count+index_id+k+1)+")";
                if (k==currentBatch.size()-1){
                    sql.append(subSql);
                }else {
                    sql.append(subSql).append(",");
                }
            }
            try {
                System.out.println("\033[31m" + "==================>InsertData:\t"+ (startIndex+start)+ " to "+(startIndex+end));
                statement.execute(String.valueOf(sql));
            } catch (Exception e) {
                System.out.println("ERROR ======  "+(startIndex+start)+ " to "+(startIndex+end));
            }
        }
        statement.close();
        connection.close();
    }



}
