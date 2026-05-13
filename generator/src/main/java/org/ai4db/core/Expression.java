package org.ai4db.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-12 19:52
 */
public class Expression {

    public static String ConcatPredicate(String predicate1, String conjunction, String predicate2){
        return "("+predicate1 + " " + conjunction + " " + predicate2+")";
    }

    public static String ConcatAllORConjunctionPredicate(String... predicates){
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < predicates.length; i++) {
            sb.append(predicates[i]);
            if (i < predicates.length - 1) {
                sb.append(" or ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String ConcatAllANDConjunctionPredicate(String... predicates){
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < predicates.length; i++) {
            sb.append(predicates[i]);
            if (i < predicates.length - 1) {
                sb.append(" and ");
            }
        }
        sb.append(")");
        return sb.toString();
    }




    public static String ConcatTwoPredicate(String Predicate1, String Predicate2){
        return Predicate1 + " " + Conjunction.RandomConjunction() + " " + Predicate2;
    }

    public static String ConcatThreePredicate(String Predicate1, String Predicate2, String Predicate3){
        return Predicate1 + " " + Conjunction.RandomConjunction() + " " + Predicate2 + " " + Conjunction.RandomConjunction() + " " + Predicate3;
    }

    public static void queryRewriteThreePredicate(String expression){
        List<String> operators = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        // 正则表达式用于匹配括号内的条件表达式
        Pattern predicatePattern = Pattern.compile("\\((.*?)\\)");
        Matcher predicateMatcher = predicatePattern.matcher(expression);

        // 正则表达式用于匹配操作符
        Pattern operatorPattern = Pattern.compile("\\)\\s*(and|or)\\s*\\(");
        Matcher operatorMatcher = operatorPattern.matcher(expression);

        StringBuffer sb = new StringBuffer();
        while (predicateMatcher.find()) {
            predicates.add(predicateMatcher.group(1)); // 添加条件表达式
        }

        while (operatorMatcher.find()) {
            operators.add(operatorMatcher.group(1)); // 添加操作符
        }

        for (int i = 0; i < operators.size(); i++) {
            String operator = operators.get(i);
            if (operator.equals("and")) {
                String predicate1 = predicates.get(i);
                String predicate2 = predicates.get(i + 1);



                System.out.println("Predicate1: " + predicate1);
                System.out.println("Predicate2: " + predicate2);
            }
        }


        System.out.println("Operators: " + operators);
        System.out.println("Predicates: " + predicates);
    }


    public static void main(String[] args) {
        String sql ="(city='Lutz' or state='TN') and (city='Redington Shores' or state!='FL') or (city!='Hazelwood' or state='LA');";
        queryRewriteThreePredicate(sql);
    }
}
