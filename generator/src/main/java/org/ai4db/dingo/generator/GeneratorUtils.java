package org.ai4db.dingo.generator;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.ai4db.core.Operator;
import org.ai4db.core.Predicate;
import org.ai4db.core.ValuesType;

import java.lang.reflect.Type;
import java.util.*;

public class GeneratorUtils {

    public static List<String> getColumnValue(JSONArray columnData, int minValue){
        List<String> columnValue = new ArrayList<>();
        for (int i = 0; i < columnData.size(); i++) {
            JSONObject col  = columnData.getJSONObject(i);
            if(col.getInteger("num") >= minValue){
                columnValue.add(col.getString("name"));
            }
        }
        return columnValue;
    }


    public static List<String> singlePredicate(String cloName,List<String> colValues, String type){
        List<String> predicates = new ArrayList<>();
        if (type.equals(ValuesType.STRING)) {
            predicates = singlePredicateForStringType(cloName, colValues);
        }
        return predicates;
    }


    public static List<String> singlePredicateForStringType(String cloName,List<String> colValues){
        List<String> predicates = new ArrayList<>();
        for (String colValue : colValues) {
            predicates.add(Predicate.ConcatForStringType(cloName, Operator.randomForStringOperator(), colValue));
        }
        return predicates;

    }



    public static List<String> singlePredicateForNumericType(String cloName,List<String> colValues, int minValue,int maxValue){
        List<String> predicates = new ArrayList<>();
        for (String colValue : colValues) {
            if (Integer.parseInt(colValue) == minValue) {
                predicates.add(
                        Predicate.ConcatColAndVal(cloName, Operator.randomForMinValueOperator(), Integer.parseInt(colValue))
                );
            }else if (Integer.parseInt(colValue) == maxValue){
                predicates.add(
                        Predicate.ConcatColAndVal(cloName, Operator.randomForMaxValueOperator(), Integer.parseInt(colValue))
                );

            }else {
                predicates.add(
                        Predicate.ConcatColAndVal(cloName, Operator.randomOperator(), Integer.parseInt(colValue))
                );
            }
        }

        return predicates;
    }


    public static List<String> singlePredicateForNumericType(String cloName,List<String> colValues, double minValue,double maxValue){
        List<String> predicates = new ArrayList<>();
        for (String colValue : colValues) {
            if (Double.parseDouble(colValue) == minValue) {
                predicates.add(
                        Predicate.ConcatColAndVal(cloName, Operator.randomForMinValueOperator(), Double.parseDouble(colValue))
                );
            }else if (Double.parseDouble(colValue) == maxValue){
                predicates.add(
                        Predicate.ConcatColAndVal(cloName, Operator.randomForMaxValueOperator(), Double.parseDouble(colValue))
                );

            }else {
                predicates.add(
                        Predicate.ConcatColAndVal(cloName, Operator.randomOperator(), Double.parseDouble(colValue))
                );
            }
        }

        return predicates;
    }




    public static List<Integer> randomChooseNNum(int n, int min, int max){
        Random random = new Random();
        List<Integer> result;
        Set<Integer> uniqueNumberSet = new HashSet<>();

        // 确保max大于等于min，且n不超过max和min之间的整数数量
        if (max < min || n > (max - min + 1)) {
            throw new IllegalArgumentException("参数不合法");
        }

        while (uniqueNumberSet.size() < n) {
            // 生成一个介于min（包含）和max（包含）之间的随机整数
            int randomNumber = random.nextInt(max - min + 1) + min;
            uniqueNumberSet.add(randomNumber);
        }

        result = new ArrayList<>(uniqueNumberSet);
        return result;
    }






}
