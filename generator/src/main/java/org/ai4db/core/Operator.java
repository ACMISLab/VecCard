package org.ai4db.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-12 19:42
 */
public class Operator {

    public static String Equal = "=";
    public static String NotEqual = "!=";
    public static String LessThan = "<";
    public static String LessThanOrEqual = "<=";
    public static String GreaterThan = ">";
    public static String GreaterThanOrEqual = ">=";

    public static  String[] AllOperators = {"<", "<=","=", "!=", ">", ">="};

    public static  String[] StringOperators = {"=", "!="};

    public static  String[] CompareOperators = {"<", "<=", ">", ">=","="};

    public static   String[] CompareNoEqualOperators = {"<", "<=", ">", ">="};

    public static  String[] InequalityOperators = {"<", ">"};


    public static String[] ForMinValueOperators = {"=", "!=", ">", ">="};

    public static String[] ForMaxValueOperators = {"=", "!=", "<", "<="};

    public static String[] ForMinValueNoEqualOperators = {">", ">="};

    public static String[] ForMaxValueNoEqualOperators = {"<", "<="};




    public static String randomOperator(){
        return AllOperators[(int)(Math.random()*AllOperators.length)];
    }


    public static String randomNonNumericOperator(){
        return StringOperators[(int)(Math.random()*StringOperators.length)];
    }

    public static String randomCompareOperator(){
        return CompareOperators[(int)(Math.random()*CompareOperators.length)];
    }

    public static String randomInequalityOperator(){
        return InequalityOperators[(int)(Math.random()*InequalityOperators.length)];
    }

    public static String randomForMinValueOperator(){
        return ForMinValueOperators[(int)(Math.random()*ForMinValueOperators.length)];
    }

    public static String randomForMaxValueOperator(){
       return ForMaxValueOperators[(int)(Math.random()*ForMaxValueOperators.length)];
    }


    public static String randomForStringOperator(){
        return StringOperators[(int)(Math.random()*StringOperators.length)];
    }






}
