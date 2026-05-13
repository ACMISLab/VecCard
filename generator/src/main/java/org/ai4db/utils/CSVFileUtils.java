package org.ai4db.utils;

import org.ai4db.core.SQL;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-01 14:04
 */
public class CSVFileUtils {

    public static List<CSVRecord> readerCSV(String filePath) throws FileNotFoundException {
        List<CSVRecord> records =new ArrayList<>();
        String resolvedPath = PathUtils.resolvePath(filePath);
        if (!Files.exists(Paths.get(resolvedPath))) {
            return records;
        }
        try (CSVParser parser = new CSVParser(new FileReader(resolvedPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                records.add(record);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    public static CSVRecord readerRandomCSVRecord(String filePath, int randomIndex){
        CSVRecord randomRecord = null;
        Random random = new Random();
        String resolvedPath = PathUtils.resolvePath(filePath);
        if (!Files.exists(Paths.get(resolvedPath))) {
            return null;
        }
        try (CSVParser parser = new CSVParser(new FileReader(resolvedPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            int currentIndex = 0;
            for (CSVRecord record : parser) {
                if (currentIndex == randomIndex) {
                    randomRecord = record;
                    break;
                }
                currentIndex++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return randomRecord;
    }

    public static int getCSVRecordSize(String filePath) throws FileNotFoundException {
        int size = 0;
        String resolvedPath = PathUtils.resolvePath(filePath);
        if (!Files.exists(Paths.get(resolvedPath))) {
            return 0;
        }
        try (CSVParser parser = new CSVParser(new FileReader(resolvedPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                size++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return size;
    }


    public static void writeListToCSVWithSerial(List<String> data, String fileName){
        String resolvedPath = PathUtils.resolvePath(fileName);
        createParentDirectory(resolvedPath);
        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(resolvedPath), CSVFormat.DEFAULT)) {
            for (int i = 0; i < data.size(); i++) {
                // 直接写入CSV文件，不需要创建额外的列表
                csvPrinter.printRecord(new String[]{String.valueOf(i + 1), data.get(i)});
            }
            System.out.println("CSV file was created successfully."+PathUtils.resolvePath(fileName));
        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    public static void writeListToCSV(List<String> data, String fileName) {
        // 设置CSV文件的格式
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");

        String resolvedPath = PathUtils.resolvePath(fileName);
        createParentDirectory(resolvedPath);
        try (FileWriter fileWriter = new FileWriter(resolvedPath);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, format)) {

            // 将List中的每个元素作为单独的记录写入CSV文件
            for (String record : data) {
                // 假设每个record都是一个完整的CSV行，用逗号分隔的值
                csvPrinter.printRecord(record);
            }

            System.out.println("CSV file was created successfully."+PathUtils.resolvePath(fileName));
        } catch (IOException e) {
            System.err.println("Error writing the CSV file: " + e.getMessage());
        }
    }

    private static void createParentDirectory(String fileName) throws IOException {
        Path parent = Paths.get(fileName).toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

}
