package org.ai4db.dingo.generator.douban;


import com.alibaba.fastjson2.JSONObject;
import org.ai4db.core.Conjunction;
import org.ai4db.core.Expression;
import org.ai4db.dingo.generator.GeneratorUtils;
import org.ai4db.utils.JSONFileReader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-01 14:03
 *
 *  AND > OR
 *  pre node input max 3 and min 1
 *  format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
 * predicate max = 8
 */
public class GenerateCommentSQL {
    public static void main(String[] args) {


    }

    public static List<String> GeneratePredicate(int allExtractCount){
        List<String> predicates = new ArrayList<>();
        int extractCount = allExtractCount / 6;
        predicates.addAll(GenerateSixPredicate(extractCount));
        predicates.addAll(GenerateFivePredicate(extractCount));
        predicates.addAll(GenerateFourPredicate(extractCount));
        predicates.addAll(GenerateThreePredicate(extractCount));
        predicates.addAll(GenerateTwoPredicate(extractCount));
        predicates.addAll(GenerateOnePredicate(extractCount));
        Set<String> uniquePredicates = new HashSet<>(predicates);

        List<String> deduplicatedPredicates = new ArrayList<>(uniquePredicates);

        String prefix = "db_comments.";

        return deduplicatedPredicates.stream()
                .map(predicate -> prefix + predicate)
                .collect(Collectors.toList());
    }


    public static List<String> GenerateSixPredicate(int extractCount){
        JSONObject comment = JSONFileReader.getJSONObejct("data/sample/db_comment.json");
        List<String> vote  =  GeneratorUtils.getColumnValue(comment.getJSONArray("vote"), 0);
        List<String> rating  =  GeneratorUtils.getColumnValue(comment.getJSONArray("rating"), 0);
        List<String> year  =  GeneratorUtils.getColumnValue(comment.getJSONArray("year"), 0);

        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> ratingPredicates = GeneratorUtils.singlePredicateForNumericType("rating", rating, getMinValueDouble(rating), getMaxValueDouble(rating));
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));
        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(ratingPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(3,0,2 );
            String p1 = totalSinglePredicate.get(randomIndexs.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndexs.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndexs.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(2)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            String p4,p5,p6;
            do{
                List<Integer> randomIndexs2 = GeneratorUtils.randomChooseNNum(3,0,2 );
                p4 = totalSinglePredicate.get(randomIndexs2.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(0)).size()));
                p5 = totalSinglePredicate.get(randomIndexs2.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(1)).size()));
                p6 = totalSinglePredicate.get(randomIndexs2.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(2)).size()));


            }while (stringPredicate.contains(p4) && stringPredicate.contains(p5) && stringPredicate.contains(p6));

            if (i % 3 == 0){
                // 11(^) AND (1 AND 1) OR  (1 AND 1)
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p3, p4),
                                        Conjunction.OR,
                                        Expression.ConcatAllANDConjunctionPredicate(p5, p6)
                                )
                        )
                );
            }else if (i % 3 == 1){
                // format2: 11(^) AND (1 AND 1 AND 1) OR 1 )
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p3, p4, p5),
                                        Conjunction.OR,
                                        p6
                                )
                        )
                );

            }else{
                // 1(^) AND  ((1 AND 1) OR  (1 AND 1 AND 1))
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p2, p3),
                                        Conjunction.OR,
                                        Expression.ConcatAllANDConjunctionPredicate(p4, p5, p6)
                                )
                        )
                );
            }
        }
        return finallyPredicate;


    }


    public static List<String> GenerateFivePredicate(int extractCount){
        JSONObject comment = JSONFileReader.getJSONObejct("data/sample/db_comment.json");
        List<String> vote  =  GeneratorUtils.getColumnValue(comment.getJSONArray("vote"), 0);
        List<String> rating  =  GeneratorUtils.getColumnValue(comment.getJSONArray("rating"), 0);
        List<String> year  =  GeneratorUtils.getColumnValue(comment.getJSONArray("year"), 0);

        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> ratingPredicates = GeneratorUtils.singlePredicateForNumericType("rating", rating, getMinValueDouble(rating), getMaxValueDouble(rating));
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));
        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(ratingPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(3,0,2 );
            String p1 = totalSinglePredicate.get(randomIndexs.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndexs.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndexs.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(2)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);

            String p4,p5;
            do{
                List<Integer> randomIndexs2 = GeneratorUtils.randomChooseNNum(2,0,2 );
                p4 = totalSinglePredicate.get(randomIndexs2.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(0)).size()));
                p5 = totalSinglePredicate.get(randomIndexs2.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(1)).size()));
            }while (stringPredicate.contains(p4) && stringPredicate.contains(p5));

            if (i % 3 == 0){
                // format1: 11(^) AND ( (1 AND 1) OR 1)
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2),
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p3, p4),
                                        Conjunction.OR,
                                        p5
                                )
                        )
                );
            }else if (i % 3 == 1){
                // 1 AND ((1 AND 1) OR (1 AND 1))
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p2, p3),
                                        Conjunction.OR,
                                        Expression.ConcatAllANDConjunctionPredicate(p4, p5)
                                )
                        )
                );
            }else {
                // format3: 1 AND ((1 AND 1 AND 1) OR 1)
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p2, p3, p4),
                                        Conjunction.OR,
                                        p5
                                )
                        )
                );
            }
        }
        return finallyPredicate;


    }


    public static List<String> GenerateFourPredicate(int extractCount){
        JSONObject comment = JSONFileReader.getJSONObejct("data/sample/db_comment.json");
        List<String> vote  =  GeneratorUtils.getColumnValue(comment.getJSONArray("vote"), 0);
        List<String> rating  =  GeneratorUtils.getColumnValue(comment.getJSONArray("rating"), 0);
        List<String> year  =  GeneratorUtils.getColumnValue(comment.getJSONArray("year"), 0);

        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> ratingPredicates = GeneratorUtils.singlePredicateForNumericType("rating", rating, getMinValueDouble(rating), getMaxValueDouble(rating));
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));
        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(ratingPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(3,0,2 );
            String p1 = totalSinglePredicate.get(randomIndexs.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndexs.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndexs.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(2)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);

            String p4;
            do {
                int index = GeneratorUtils.randomChooseNNum(1,0,2 ).get(0);
                p4 = totalSinglePredicate.get(index).get(random.nextInt(totalSinglePredicate.get(index).size()));
            }while (stringPredicate.contains(p4));

            if (i % 2 ==0){
                // format1: 2(^) 1 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatPredicate(p3, Conjunction.OR, p4)
                        )
                );
            }else {
                // format2: 1 AND (  (1 AND 1)  OR 1 )

                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatPredicate(p2, Conjunction.AND, p3),
                                        Conjunction.OR,
                                        p4
                                )
                        )
                );

            }
        }
        return finallyPredicate;
    }


    public static List<String> GenerateThreePredicate(int extractCount){

        JSONObject comment = JSONFileReader.getJSONObejct("data/sample/db_comment.json");
        List<String> vote  =  GeneratorUtils.getColumnValue(comment.getJSONArray("vote"), 0);
        List<String> rating  =  GeneratorUtils.getColumnValue(comment.getJSONArray("rating"), 0);
        List<String> year  =  GeneratorUtils.getColumnValue(comment.getJSONArray("year"), 0);

        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> ratingPredicates = GeneratorUtils.singlePredicateForNumericType("rating", rating, getMinValueDouble(rating), getMaxValueDouble(rating));
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));
        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(ratingPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(3,0,2);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));

            finallyPredicate.add(
                    Expression.ConcatPredicate(
                            p1,
                            Conjunction.AND,
                            Expression.ConcatPredicate(p2, Conjunction.OR, p3)
                    )
            );
        }
        return finallyPredicate;


    }


    public static List<String> GenerateTwoPredicate(int extractCount){
        JSONObject comment = JSONFileReader.getJSONObejct("data/sample/db_comment.json");
        List<String> vote  =  GeneratorUtils.getColumnValue(comment.getJSONArray("vote"), 0);
        List<String> rating  =  GeneratorUtils.getColumnValue(comment.getJSONArray("rating"), 0);
        List<String> year  =  GeneratorUtils.getColumnValue(comment.getJSONArray("year"), 0);

        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> ratingPredicates = GeneratorUtils.singlePredicateForNumericType("rating", rating, getMinValueDouble(rating), getMaxValueDouble(rating));
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));
        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(ratingPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(2,0,2);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            finallyPredicate.add(
                    Expression.ConcatTwoPredicate(p1, p2)
            );
        }
        return finallyPredicate;

    }


    public static List<String> GenerateOnePredicate(int extractCount) {
        JSONObject comment = JSONFileReader.getJSONObejct("data/sample/db_comment.json");
        List<String> vote  =  GeneratorUtils.getColumnValue(comment.getJSONArray("vote"), 0);
        List<String> rating  =  GeneratorUtils.getColumnValue(comment.getJSONArray("rating"), 0);
        List<String> year  =  GeneratorUtils.getColumnValue(comment.getJSONArray("year"), 0);

        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> ratingPredicates = GeneratorUtils.singlePredicateForNumericType("rating", rating, getMinValueDouble(rating), getMaxValueDouble(rating));
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<String> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.addAll(votePredicates);
        totalSinglePredicate.addAll(ratingPredicates);
        totalSinglePredicate.addAll(yearPredicates);

        // 创建一个副本并打乱它
        List<String> shuffledList = new ArrayList<>(totalSinglePredicate);
        Collections.shuffle(shuffledList);
        extractCount = Math.min(extractCount, shuffledList.size());
        return new ArrayList<>(shuffledList.subList(0, extractCount));
    }


    public static int getMaxValue(List<String> values) {
        int maxValue = Integer.parseInt(values.get(0));
        for (String value : values) {
            if (Integer.parseInt(value) > maxValue) {
                maxValue = Integer.parseInt(value);
            }
        }
        return maxValue;
    }

    public static int getMinValue(List<String> values) {
        int minValue = Integer.parseInt(values.get(0));
        for (String value : values) {
            if (Integer.parseInt(value) < minValue) {
                minValue = Integer.parseInt(value);
            }
        }
        return minValue;
    }

    public static double getMaxValueDouble(List<String> values) {
        double maxValue = Double.parseDouble(values.get(0));
        for (String value : values) {
            if (Double.parseDouble(value) > maxValue) {
                maxValue = Double.parseDouble(value);
            }
        }
        return maxValue;
    }

    public static double getMinValueDouble(List<String> values) {
        double minValue = Double.parseDouble(values.get(0));
        for (String value : values) {
            if (Double.parseDouble(value) < minValue) {
                minValue = Double.parseDouble(value);
            }
        }
        return minValue;
    }

}
