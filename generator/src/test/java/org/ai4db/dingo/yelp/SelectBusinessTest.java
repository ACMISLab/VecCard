package org.ai4db.dingo.yelp;

import org.ai4db.utils.PathUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-07-25 15:14
 */
public class SelectBusinessTest {
    public static String  driver = "io.dingodb.driver.client.DingoDriverClient";
    public static String  url = System.getenv().getOrDefault("DINGODB_JDBC_URL", "jdbc:dingo:thin:url=127.0.0.1:8765/yelp");
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
    void testSelectBusiness() throws SQLException {

        Statement statement = connection.createStatement();
        CSVRecord randomRecord = null;
        Random random = new Random();
        int randomIndex = random.nextInt(500) + 1;
        String csvPath = PathUtils.resolvePath("data/business.csv");
        Assumptions.assumeTrue(Files.exists(Paths.get(csvPath)));
        try (CSVParser parser = new CSVParser(new FileReader(csvPath), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            int currentIndex = 0;
            for (CSVRecord record : parser) {
                if (currentIndex == randomIndex) {
                    randomRecord = record;
                    break;
                }
                currentIndex++;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String vector = randomRecord.get(10);

        System.out.println("vector: " + vector);

        String sql = "select review_text,feature_index$distance from vector(tip,feature,array"
                + vector + ",10,map[efSearch, 40]);";
        System.out.println(sql);

        System.out.println("===========================>");
        ResultSet resultSet = statement.executeQuery(sql);
        List<String> result = new ArrayList<>();
        while (resultSet.next()) {
            String name = resultSet.getString(1);
            String distance = resultSet.getString(2);
            result.add(name + " " + distance);
        }
        System.out.println(result);
        statement.close();
    }


    @AfterAll
    static void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

}
