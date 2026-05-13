package org.ai4db.core;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-12 19:47
 */
public class Predicate {


    public static String ConcatForStringType(String col, String op, String val){
        return col + op + "'" + val+"'";
    }

    public static String ConcatColAndVal(String col, String op, String val){
        return col + op + "'" + val+"'";
    }

    public static String ConcatColAndVal(String col, String op, int val){
        return col + op + val;
    }

    public static String ConcatColAndVal(String col, String op, double val){
        return col + op + val;
    }




    public static String[] ConcatForStringOperators(String col, String val){
        String[] result = new String[Operator.StringOperators.length];
        for (int i = 0; i < Operator.StringOperators.length; i++) {
            result[i] = col + Operator.StringOperators[i] + "'" + val+"'";
        }
        return result;
    }




    public static String[] ConcatForAllOperator(String col, int val){
        String[] result = new String[Operator.AllOperators.length];
        for (int i = 0; i < Operator.AllOperators.length; i++) {
            result[i] = col + Operator.AllOperators[i] + val;
        }
        return result;
    }

    public static String[] ConcatForAllOperator(String col, double val){
        String[] result = new String[Operator.AllOperators.length];
        for (int i = 0; i < Operator.AllOperators.length; i++) {
            result[i] = col + Operator.AllOperators[i] + val;
        }
        return result;
    }



    public static String[] ConcatForInequalityOperator (String col, double val){
        String[] result = new String[Operator.InequalityOperators.length];
        for (int i = 0; i < Operator.InequalityOperators.length; i++) {
            result[i] = col + Operator.InequalityOperators[i] + val;
        }
        return result;
    }


    public static String[] ConcatForMinValueOperator (String col, double val){
        String[] result = new String[Operator.ForMinValueOperators.length];
        for (int i = 0; i < Operator.ForMinValueOperators.length; i++) {
            result[i] = col + Operator.ForMinValueOperators[i] + val;
        }
        return result;
    }

    public static String[] ConcatForMaxValueOperator (String col, double val){
        String[] result = new String[Operator.ForMaxValueOperators.length];
        for (int i = 0; i < Operator.ForMaxValueOperators.length; i++) {
            result[i] = col + Operator.ForMaxValueOperators[i] + val;
        }
        return result;
    }


    public static String[] ConcatForMinValueNoEqualOperator (String col, double val){
        String[] result = new String[Operator.ForMinValueNoEqualOperators.length];
        for (int i = 0; i < Operator.ForMinValueNoEqualOperators.length; i++) {
            result[i] = col + Operator.ForMinValueNoEqualOperators[i] + val;
        }
        return result;
    }

    public static String[] ConcatForMinValueNoEqualOperator (String col, int val){
        String[] result = new String[Operator.ForMinValueNoEqualOperators.length];
        for (int i = 0; i < Operator.ForMinValueNoEqualOperators.length; i++) {
            result[i] = col + Operator.ForMinValueNoEqualOperators[i] + val;
        }
        return result;
    }

    public static String[] ConcatForMaxValueNoEqualOperator (String col, double val){
        String[] result = new String[Operator.ForMaxValueNoEqualOperators.length];
        for (int i = 0; i < Operator.ForMaxValueNoEqualOperators.length; i++) {
            result[i] = col + Operator.ForMaxValueNoEqualOperators[i] + val;
        }
        return result;
    }

    public static String[] ConcatForMaxValueNoEqualOperator (String col, int val){
        String[] result = new String[Operator.ForMaxValueNoEqualOperators.length];
        for (int i = 0; i < Operator.ForMaxValueNoEqualOperators.length; i++) {
            result[i] = col + Operator.ForMaxValueNoEqualOperators[i] + val;
        }
        return result;
    }


    public static String[] ConcatForCompareNoEqualOperators (String col, double val){
        String[] result = new String[Operator.CompareNoEqualOperators.length];
        for (int i = 0; i < Operator.CompareNoEqualOperators.length; i++) {
            result[i] = col + Operator.CompareNoEqualOperators[i] + val;
        }
        return result;
    }

    public static String[] ConcatForCompareNoEqualOperators (String col, int val){
        String[] result = new String[Operator.CompareNoEqualOperators.length];
        for (int i = 0; i < Operator.CompareNoEqualOperators.length; i++) {
            result[i] = col + Operator.CompareNoEqualOperators[i] + val;
        }
        return result;
    }


}
