package org.ai4db.dingo.init;

import org.ai4db.utils.DingoConnectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-07-15 11:26
 */
public class ClearTable {
    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        if (args.length==0){
            System.out.println("No parameters passed");
            System.exit(0);
        }
        String path = args[0];
        Connection connection = DingoConnectionUtils.getConnection(path);
        Statement statement = connection.createStatement();
        String[] tables = {"business",  "review", "tip"};
        for (String table : tables) {
            String sql = "drop table " + table;
            statement.executeUpdate(sql);
            System.out.println("table " + table + " is dropped");
        }
    }
}
