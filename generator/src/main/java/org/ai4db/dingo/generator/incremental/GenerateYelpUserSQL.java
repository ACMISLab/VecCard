package org.ai4db.dingo.generator.incremental;


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
public class GenerateYelpUserSQL {
    public static void main(String[] args) {

        System.out.println(GeneratePredicate(10000).size());


    }

    public static List<String> GeneratePredicate(int allExtractCount){
        List<String> predicates = new ArrayList<>();
        int extractCount = allExtractCount/7;
        predicates.addAll(GenerateSevenPredicate(extractCount));
        predicates.addAll(GenerateSixPredicate(extractCount));
        predicates.addAll(GenerateFivePredicate(extractCount));
        predicates.addAll(GenerateFourPredicate(extractCount));
        predicates.addAll(GenerateThreePredicate(extractCount));
        predicates.addAll(GenerateTwoPredicate(extractCount));
        predicates.addAll(GenerateOnePredicate(extractCount));

        Set<String> uniquePredicates = new HashSet<>(predicates);

        List<String> deduplicatedPredicates = new ArrayList<>(uniquePredicates);

        String prefix = "yelp_user.";

        return deduplicatedPredicates.stream()
                .map(predicate -> prefix + predicate)
                .collect(Collectors.toList());

    }


    public static List<String> GenerateSevenPredicate(int extractCount){
        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(reviewPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(fansPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(5,0,4);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            stringPredicate.add(p4);
            stringPredicate.add(p5);
            String p6,p7;
            do{
                List<Integer> randomIndexs2 = GeneratorUtils.randomChooseNNum(2,0,4 );
                p6 = totalSinglePredicate.get(randomIndexs2.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(0)).size()));
                p7 = totalSinglePredicate.get(randomIndexs2.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(1)).size()));
            }while (stringPredicate.contains(p6) && stringPredicate.contains(p7));

            if (i % 2 == 0){
                // format1: 11(^) AND ((1 and 1 and 1) or (1 and 1))
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p3, p4, p5),
                                        Conjunction.OR,
                                        Expression.ConcatAllANDConjunctionPredicate(p6, p7)
                                )
                        )
                );
            }else {
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatPredicate(
                                        Expression.ConcatAllANDConjunctionPredicate(p2, p3, p4),
                                        Conjunction.OR,
                                        Expression.ConcatAllANDConjunctionPredicate(p5, p6, p7)
                                )
                        )
                );
            }
        }
        return finallyPredicate;
    }






    public static List<String> GenerateSixPredicate(int extractCount){
        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(reviewPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(fansPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(5,0,4);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            stringPredicate.add(p4);
            stringPredicate.add(p5);
            String p6;
            do{
                List<Integer> randomIndexs2 = GeneratorUtils.randomChooseNNum(1,0,4 );
                p6 = totalSinglePredicate.get(randomIndexs2.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs2.get(0)).size()));
            }while (stringPredicate.contains(p6));

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
        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(reviewPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(fansPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(5,0,4);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));

            /**
             *   format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
             *   format1: 11(^) AND ( (1 AND 1) OR 1)
             *   format2: 1 AND ((1 AND 1) OR (1 AND 1))
             *   format3: 1 AND ((1 AND 1 AND 1) OR 1)
             */

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
        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(reviewPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(fansPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){

            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(4,0,4);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));

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
        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(reviewPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(fansPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(3,0,4);
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
        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(reviewPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(fansPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(2,0,4);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            finallyPredicate.add(
                    Expression.ConcatTwoPredicate(p1, p2)
            );
        }
        return finallyPredicate;
    }



    public static List<String> GenerateOnePredicate(int extractCount) {

        JSONObject user = JSONFileReader.getJSONObejct("data/sample/yelp-user.json");
        List<String> review	  = GeneratorUtils.getColumnValue(user.getJSONArray("review"), 0);
        List<String> useful	  = GeneratorUtils.getColumnValue(user.getJSONArray("useful"), 0);
        List<String> funny	  = GeneratorUtils.getColumnValue(user.getJSONArray("funny"), 0);
        List<String> cool	  = GeneratorUtils.getColumnValue(user.getJSONArray("cool"), 0);
        List<String> fans	  = GeneratorUtils.getColumnValue(user.getJSONArray("fans"), 0);
        List<String> reviewPredicates = GeneratorUtils.singlePredicateForNumericType("review", review,getMinValue(review),getMaxValue(review));
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,getMinValue(useful),getMaxValue(useful));
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,getMinValue(funny),getMaxValue(funny));
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,getMinValue(cool),getMaxValue(cool));
        List<String> fansPredicates = GeneratorUtils.singlePredicateForNumericType("fans", fans,getMinValue(fans),getMaxValue(fans));

        List<String> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.addAll(reviewPredicates);
        totalSinglePredicate.addAll(usefulPredicates);
        totalSinglePredicate.addAll(funnyPredicates);
        totalSinglePredicate.addAll(coolPredicates);
        totalSinglePredicate.addAll(fansPredicates);
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

}
