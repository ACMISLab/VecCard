package org.ai4db.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DingoConnectionUtils {
    public static Connection getConnection(String path) throws ClassNotFoundException, SQLException, IOException {
        Properties prop = DingoPropertiesUtils.getProperties(path);
        Class.forName(prop.getProperty("jdbc.driver"));
        Connection connection = DriverManager.getConnection(
                prop.getProperty("jdbc.url"),
                prop.getProperty("jdbc.username"),
                prop.getProperty("jdbc.password"));
        return connection;
    }
}
