package org.ai4db.core;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-01 15:19
 */
public class Conjunction {

    public static String AND = "and";

    public static String OR = "or";

    public static String RandomConjunction(){
        String[] conjunctions = {AND, OR};
        return conjunctions[(int)(Math.random()*conjunctions.length)];
    }


}
