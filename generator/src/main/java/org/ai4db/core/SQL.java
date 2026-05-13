package org.ai4db.core;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-12 19:52
 */
public class SQL {
    public static String generateCardinalitySQL(String table,String expression){
        return "select count(*) from " + table + " where " + expression+";";
    }


}
