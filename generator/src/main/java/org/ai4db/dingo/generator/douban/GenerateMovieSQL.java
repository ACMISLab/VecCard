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
public class GenerateMovieSQL {
    public static void main(String[] args) {
        System.out.println(GeneratePredicate(1000));

    }



    public static List<String> GeneratePredicate(int allExtractCount) {
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

        String prefix = "db_movies.";

        return deduplicatedPredicates.stream()
                .map(predicate -> prefix + predicate)
                .collect(Collectors.toList());

    }



    public static List<String> GenerateSixPredicate(int extractCount){
        JSONObject movie = JSONFileReader.getJSONObejct("data/sample/db_movie.json");
        List<String> score = GeneratorUtils.getColumnValue(movie.getJSONArray("score"), 0);
        List<String> vote = GeneratorUtils.getColumnValue(movie.getJSONArray("vote"), 0);
        List<String> language = GeneratorUtils.getColumnValue(movie.getJSONArray("language"), 0);
        List<String> mins = GeneratorUtils.getColumnValue(movie.getJSONArray("mins"), 0);
        List<String> region = GeneratorUtils.getColumnValue(movie.getJSONArray("region"), 0);
        List<String> year = GeneratorUtils.getColumnValue(movie.getJSONArray("year"), 0);

        List<String> scorePredicates = GeneratorUtils.singlePredicateForNumericType("score", score, getMinValueDouble(score), getMaxValueDouble(score));
        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> languagePredicates = GeneratorUtils.singlePredicateForStringType("language", language);
        List<String> minsPredicates = GeneratorUtils.singlePredicateForNumericType("mins", mins, getMinValueDouble(mins), getMaxValueDouble(mins));
        List<String> regionPredicates = GeneratorUtils.singlePredicateForStringType("region", region);
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(languagePredicates);
        totalSinglePredicate.add(regionPredicates);
        totalSinglePredicate.add(scorePredicates);
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(minsPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < extractCount; i++) {
            List<String> list1 = languagePredicates;
            List<String> list2 = regionPredicates;
            List<String> list3 = scorePredicates;
            List<Integer> rondomIndexs = GeneratorUtils.randomChooseNNum(3, 3, 5);

            List<String> list4 = totalSinglePredicate.get(rondomIndexs.get(0));
            List<String> list5 = totalSinglePredicate.get(rondomIndexs.get(1));
            List<String> list6 = totalSinglePredicate.get(rondomIndexs.get(2));

            String p1 = list1.get(random.nextInt(list1.size()));
            String p2 = list2.get(random.nextInt(list2.size()));
            String p3 = list3.get(random.nextInt(list3.size()));
            String p4 = list4.get(random.nextInt(list4.size()));
            String p5 = list5.get(random.nextInt(list5.size()));
            String p6 = list6.get(random.nextInt(list6.size()));

            /**
             *  format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
             *  format1: 11(^) AND (1 AND 1) OR  (1 AND 1)
             *  format2: 11(^) AND (1 AND 1 AND 1) OR 1 )
             *  format3: 1(^) AND  ((1 AND 1) OR  (1 AND 1 AND 1))
             */

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
        JSONObject movie = JSONFileReader.getJSONObejct("data/sample/db_movie.json");
        List<String> score = GeneratorUtils.getColumnValue(movie.getJSONArray("score"), 0);
        List<String> vote = GeneratorUtils.getColumnValue(movie.getJSONArray("vote"), 0);
        List<String> language = GeneratorUtils.getColumnValue(movie.getJSONArray("language"), 0);
        List<String> mins = GeneratorUtils.getColumnValue(movie.getJSONArray("mins"), 0);
        List<String> region = GeneratorUtils.getColumnValue(movie.getJSONArray("region"), 0);
        List<String> year = GeneratorUtils.getColumnValue(movie.getJSONArray("year"), 0);

        List<String> scorePredicates = GeneratorUtils.singlePredicateForNumericType("score", score, getMinValueDouble(score), getMaxValueDouble(score));
        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> languagePredicates = GeneratorUtils.singlePredicateForStringType("language", language);
        List<String> minsPredicates = GeneratorUtils.singlePredicateForNumericType("mins", mins, getMinValueDouble(mins), getMaxValueDouble(mins));
        List<String> regionPredicates = GeneratorUtils.singlePredicateForStringType("region", region);
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(languagePredicates);
        totalSinglePredicate.add(regionPredicates);
        totalSinglePredicate.add(scorePredicates);
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(minsPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<String> list1 = languagePredicates;
            List<String> list2 = regionPredicates;
            List<String> list3 = scorePredicates;

            List<Integer> rondomIndexs = GeneratorUtils.randomChooseNNum(2, 3, 5);
            List<String> list4 = totalSinglePredicate.get(rondomIndexs.get(0));
            List<String> list5 = totalSinglePredicate.get(rondomIndexs.get(1));

            String p1 = list1.get(random.nextInt(list1.size()));
            String p2 = list2.get(random.nextInt(list2.size()));
            String p3 = list3.get(random.nextInt(list3.size()));
            String p4 = list4.get(random.nextInt(list4.size()));
            String p5 = list5.get(random.nextInt(list5.size()));

            /**
             *   format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
             *   format1: (1 AND 1 ) AND ( (1 AND 1) OR 1)
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
        JSONObject movie = JSONFileReader.getJSONObejct("data/sample/db_movie.json");
        List<String> score = GeneratorUtils.getColumnValue(movie.getJSONArray("score"), 0);
        List<String> vote = GeneratorUtils.getColumnValue(movie.getJSONArray("vote"), 0);
        List<String> language = GeneratorUtils.getColumnValue(movie.getJSONArray("language"), 0);
        List<String> mins = GeneratorUtils.getColumnValue(movie.getJSONArray("mins"), 0);
        List<String> region = GeneratorUtils.getColumnValue(movie.getJSONArray("region"), 0);
        List<String> year = GeneratorUtils.getColumnValue(movie.getJSONArray("year"), 0);

        List<String> scorePredicates = GeneratorUtils.singlePredicateForNumericType("score", score, getMinValueDouble(score), getMaxValueDouble(score));
        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> languagePredicates = GeneratorUtils.singlePredicateForStringType("language", language);
        List<String> minsPredicates = GeneratorUtils.singlePredicateForNumericType("mins", mins, getMinValueDouble(mins), getMaxValueDouble(mins));
        List<String> regionPredicates = GeneratorUtils.singlePredicateForStringType("region", region);
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(scorePredicates);
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(languagePredicates);
        totalSinglePredicate.add(minsPredicates);
        totalSinglePredicate.add(regionPredicates);
        totalSinglePredicate.add(yearPredicates);
        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndexs_1 = GeneratorUtils.randomChooseNNum(2,0,2);

            List<String> list1 = totalSinglePredicate.get(randomIndexs_1.get(0));
            List<String> list2 = totalSinglePredicate.get(randomIndexs_1.get(1));

            List<Integer> randomIndexs_2 = GeneratorUtils.randomChooseNNum(2,3,5 );

            List<String> list3 = totalSinglePredicate.get(randomIndexs_2.get(0));
            List<String> list4 = totalSinglePredicate.get(randomIndexs_2.get(1));


            String p1 = list1.get(random.nextInt(list1.size()));
            String p2 = list2.get(random.nextInt(list2.size()));
            String p3 = list3.get(random.nextInt(list3.size()));
            String p4 = list4.get(random.nextInt(list4.size()));


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
        JSONObject movie = JSONFileReader.getJSONObejct("data/sample/db_movie.json");
        List<String> score = GeneratorUtils.getColumnValue(movie.getJSONArray("score"), 0);
        List<String> vote = GeneratorUtils.getColumnValue(movie.getJSONArray("vote"), 0);
        List<String> language = GeneratorUtils.getColumnValue(movie.getJSONArray("language"), 0);
        List<String> mins = GeneratorUtils.getColumnValue(movie.getJSONArray("mins"), 0);
        List<String> region = GeneratorUtils.getColumnValue(movie.getJSONArray("region"), 0);
        List<String> year = GeneratorUtils.getColumnValue(movie.getJSONArray("year"), 0);

        List<String> scorePredicates = GeneratorUtils.singlePredicateForNumericType("score", score, getMinValueDouble(score), getMaxValueDouble(score));
        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> languagePredicates = GeneratorUtils.singlePredicateForStringType("language", language);
        List<String> minsPredicates = GeneratorUtils.singlePredicateForNumericType("mins", mins, getMinValueDouble(mins), getMaxValueDouble(mins));
        List<String> regionPredicates = GeneratorUtils.singlePredicateForStringType("region", region);
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(scorePredicates);
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(languagePredicates);
        totalSinglePredicate.add(minsPredicates);
        totalSinglePredicate.add(regionPredicates);
        totalSinglePredicate.add(yearPredicates);
        int size  = totalSinglePredicate.size();

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            // 随机抽取两个List
            Set<Integer> uniqueIndexSet = new HashSet<>();
            while (uniqueIndexSet.size() < 3) {
                uniqueIndexSet.add(random.nextInt(size));
            }

            List<Integer> indexList = new ArrayList<>(uniqueIndexSet);
            List<String> list1 = totalSinglePredicate.get(indexList.get(0));
            List<String> list2 = totalSinglePredicate.get(indexList.get(1));
            List<String> list3 = totalSinglePredicate.get(indexList.get(2));

            int index1 = random.nextInt(list1.size());
            int index2 = random.nextInt(list2.size());
            int index3 = random.nextInt(list3.size());

            String p1 = list1.get(index1);
            String p2 = list2.get(index2);
            String p3 = list3.get(index3);

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
        JSONObject movie = JSONFileReader.getJSONObejct("data/sample/db_movie.json");
        List<String> score = GeneratorUtils.getColumnValue(movie.getJSONArray("score"), 0);
        List<String> vote = GeneratorUtils.getColumnValue(movie.getJSONArray("vote"), 0);
        List<String> language = GeneratorUtils.getColumnValue(movie.getJSONArray("language"), 0);
        List<String> mins = GeneratorUtils.getColumnValue(movie.getJSONArray("mins"), 0);
        List<String> region = GeneratorUtils.getColumnValue(movie.getJSONArray("region"), 0);
        List<String> year = GeneratorUtils.getColumnValue(movie.getJSONArray("year"), 0);

        List<String> scorePredicates = GeneratorUtils.singlePredicateForNumericType("score", score, getMinValueDouble(score), getMaxValueDouble(score));
        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> languagePredicates = GeneratorUtils.singlePredicateForStringType("language", language);
        List<String> minsPredicates = GeneratorUtils.singlePredicateForNumericType("mins", mins, getMinValueDouble(mins), getMaxValueDouble(mins));
        List<String> regionPredicates = GeneratorUtils.singlePredicateForStringType("region", region);
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(scorePredicates);
        totalSinglePredicate.add(votePredicates);
        totalSinglePredicate.add(languagePredicates);
        totalSinglePredicate.add(minsPredicates);
        totalSinglePredicate.add(regionPredicates);
        totalSinglePredicate.add(yearPredicates);
        int size  = totalSinglePredicate.size();

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            // 随机抽取两个List
            int index1 = random.nextInt(size);
            int index2;
            do {
                index2 = random.nextInt(size);
            } while (index2 == index1);

            List<String> list1 = totalSinglePredicate.get(index1);
            List<String> list2 = totalSinglePredicate.get(index2);

            int size1 = list1.size();
            int size2 = list2.size();

            int index3 = random.nextInt(size1);
            int index4 = random.nextInt(size2);

            String p1 = list1.get(index3);
            String p2 = list2.get(index4);

            finallyPredicate.add(
                    Expression.ConcatTwoPredicate(p1, p2)
            );

        }
        return finallyPredicate;


    }


    public static List<String> GenerateOnePredicate(int extractCount){
        JSONObject movie = JSONFileReader.getJSONObejct("data/sample/db_movie.json");
        List<String> score = GeneratorUtils.getColumnValue(movie.getJSONArray("score"), 0);
        List<String> vote = GeneratorUtils.getColumnValue(movie.getJSONArray("vote"), 0);
        List<String> language = GeneratorUtils.getColumnValue(movie.getJSONArray("language"), 0);
        List<String> mins = GeneratorUtils.getColumnValue(movie.getJSONArray("mins"), 0);
        List<String> region = GeneratorUtils.getColumnValue(movie.getJSONArray("region"), 0);
        List<String> year = GeneratorUtils.getColumnValue(movie.getJSONArray("year"), 0);

        List<String> scorePredicates = GeneratorUtils.singlePredicateForNumericType("score", score, getMinValueDouble(score), getMaxValueDouble(score));
        List<String> votePredicates = GeneratorUtils.singlePredicateForNumericType("vote", vote, getMinValue(vote), getMaxValue(vote));
        List<String> languagePredicates = GeneratorUtils.singlePredicateForStringType("language", language);
        List<String> minsPredicates = GeneratorUtils.singlePredicateForNumericType("mins", mins, getMinValueDouble(mins), getMaxValueDouble(mins));
        List<String> regionPredicates = GeneratorUtils.singlePredicateForStringType("region", region);
        List<String> yearPredicates = GeneratorUtils.singlePredicateForNumericType("year", year, getMinValue(year), getMaxValue(year));

        List<String> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.addAll(scorePredicates);
        totalSinglePredicate.addAll(votePredicates);
        totalSinglePredicate.addAll(languagePredicates);
        totalSinglePredicate.addAll(minsPredicates);
        totalSinglePredicate.addAll(regionPredicates);
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
