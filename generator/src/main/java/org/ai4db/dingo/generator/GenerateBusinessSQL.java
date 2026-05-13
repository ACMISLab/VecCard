package org.ai4db.dingo.generator;


import com.alibaba.fastjson2.JSONObject;
import org.ai4db.core.Conjunction;
import org.ai4db.core.Expression;
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
public class GenerateBusinessSQL {
    public static void main(String[] args) {
        List<String> test = GenerateOnePredicate(10);
        System.out.println(test);
        System.out.println(test.size());
    }

    public static List<String> GeneratePredicate(int allExtractCount) {
        List<String> predicates = new ArrayList<>();
        int extractCount = allExtractCount / 8;

//        predicates.addAll(GenerateTenPredicate(extractCount));
//        predicates.addAll(GenerateNinePredicate(extractCount));
        predicates.addAll(GenerateEightPredicate(extractCount));
        predicates.addAll(GenerateSevenPredicate(extractCount));
        predicates.addAll(GenerateSixPredicate(extractCount));
        predicates.addAll(GenerateFivePredicate(extractCount));
        predicates.addAll(GenerateFourPredicate(extractCount));
        predicates.addAll(GenerateThreePredicate(extractCount));
        predicates.addAll(GenerateTwoPredicate(extractCount));
        predicates.addAll(GenerateOnePredicate(extractCount));

        Set<String> uniquePredicates = new HashSet<>(predicates);

        List<String> deduplicatedPredicates = new ArrayList<>(uniquePredicates);

        String prefix = "business.";

        return deduplicatedPredicates.stream()
                .map(predicate -> prefix + predicate)
                .collect(Collectors.toList());
    }

    public static List<String> GenerateTenPredicate(int extractCount) {
        /**
         * format1: 11111111(^) AND 1 OR 1
         * format2: 1111111(^) AND 11(v) OR 1
         * format3: 111111(^) AND 111(V) OR 1
         * format4: 11111(^) AND 1111(V) OR 1
         * format5: 1111(^) AND 11111(V) OR 1
         * format6: 111(^) AND 111111(V) OR 1
         * format7: 11(^) AND 1111111(V) OR 1
         * format8: 1(^) AND 11111111(V) OR 1
         */
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < extractCount; i++) {
            String p1 = citySinglePredicate.get(random.nextInt(citySinglePredicate.size()));
            String p2 = stateSinglePredicate.get(random.nextInt(stateSinglePredicate.size()));
            String p3 = popularSinglePredicate.get(random.nextInt(popularSinglePredicate.size()));
            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            String p4,p5,p6;
            do {
                List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(3, 0, 2);
                List<String> list1 = totalSinglePredicate.get(randomIndexs.get(0));
                List<String> list2 = totalSinglePredicate.get(randomIndexs.get(1));
                List<String> list3 = totalSinglePredicate.get(randomIndexs.get(2));
                p4 = list1.get(random.nextInt(list1.size()));
                p5 = list2.get(random.nextInt(list2.size()));
                p6 = list3.get(random.nextInt(list3.size()));
            }while (stringPredicate.contains(p4) && stringPredicate.contains(p5) && stringPredicate.contains(p6));

            String p7 = starsSinglePredicate.get(random.nextInt(starsSinglePredicate.size()));
            String p8 = reviewSinglePredicate.get(random.nextInt(reviewSinglePredicate.size()));
            String p9 = favoritesSinglePredicate.get(random.nextInt(favoritesSinglePredicate.size()));
            String p10 = avgspendSinglePredicate.get(random.nextInt(avgspendSinglePredicate.size()));

            if (i % 8 == 0){
                // format1: 11111111(^) AND 1 OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7, p8),
                                Conjunction.AND,
                                Expression.ConcatPredicate(p9, Conjunction.OR, p10)
                        )
                );
            }else if (i % 8 == 1){
                // format2: 1111111(^) AND 11(v) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p8, p9, p10)
                        )
                );
            }else if (i % 8 == 2){
                // format3: 111111(^) AND 111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p7, p8, p9, p10)
                        )
                );
            }else if (i % 8 == 3){
                // format4: 11111(^) AND 1111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p6, p7, p8, p9, p10)
                        )
                );
            }else if (i % 8 == 4){
                // format5: 1111(^) AND 11111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p5, p6, p7, p8, p9, p10)
                        )
                );
            }else if (i % 8 == 5){
                // format6: 111(^) AND 111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p4, p5, p6, p7, p8, p9, p10)
                        )
                );
            }else if (i % 8 == 6){
                // format7: 11(^) AND 1111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p3, p4, p5, p6, p7, p8, p9, p10)
                        )
                );
            }else {
                // format8: 1(^) AND 11111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p2, p3, p4, p5, p6, p7, p8, p9, p10)
                        )
                );
            }


        }
        return finallyPredicate;

    }

    public static List<String> GenerateNinePredicate(int extractCount) {
        /**
         *  format1: 1111111(^) AND 1 OR 1
         *  format2: 111111(^) AND 11(v) OR 1
         *  format3: 11111(^) AND 111(V) OR 1
         *  format4: 1111(^) AND 1111(V) OR 1
         *  format5: 111(^) AND 11111(V) OR 1
         *  format6: 11(^) AND 111111(V) OR 1
         *  format7: 1(^) AND 1111111(V) OR 1
         */
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);

        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);


        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < extractCount; i++){
            String p1 = citySinglePredicate.get(random.nextInt(citySinglePredicate.size()));
            String p2 = stateSinglePredicate.get(random.nextInt(stateSinglePredicate.size()));
            String p3 = popularSinglePredicate.get(random.nextInt(popularSinglePredicate.size()));
            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            String p4,p5;
            do {
                List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(2, 0, 2);
                List<String> list1 = totalSinglePredicate.get(randomIndexs.get(0));
                List<String> list2 = totalSinglePredicate.get(randomIndexs.get(1));
                p4 = list1.get(random.nextInt(list1.size()));
                p5 = list2.get(random.nextInt(list2.size()));
            }while (stringPredicate.contains(p4) && stringPredicate.contains(p5));

            String p6 = starsSinglePredicate.get(random.nextInt(starsSinglePredicate.size()));
            String p7 = reviewSinglePredicate.get(random.nextInt(reviewSinglePredicate.size()));
            String p8 = favoritesSinglePredicate.get(random.nextInt(favoritesSinglePredicate.size()));
            String p9 = avgspendSinglePredicate.get(random.nextInt(avgspendSinglePredicate.size()));

            if (i % 7 == 0){
                // format1: 1111111(^) AND 1 OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7),
                                Conjunction.AND,
                                Expression.ConcatPredicate(p8, Conjunction.OR, p9)
                        )
                );
            }else if (i % 7 == 1){
                // format2: 111111(^) AND 11(v) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p7, p8, p9)
                        )
                );
            }else if (i % 7 == 2){
                // format3: 11111(^) AND 111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p6, p7, p8, p9)
                        )
                );
            }else if (i % 7 == 3){
                // format4: 1111(^) AND 1111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p5, p6, p7, p8, p9)
                        )
                );
            }else if (i % 7 == 4){
                // format5: 111(^) AND 11111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p5, p6, p7, p8, p9)
                        )
                );
            }else if (i % 7 == 5){
                // format6: 11(^) AND 111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p4, p5, p6, p7, p8, p9)
                        )
                );
            }else {
                // format7: 1(^) AND 1111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p2, p3, p4, p5, p6, p7, p8, p9)
                        )
                );
            }
        }

        return finallyPredicate;
    }

    public static List<String> GenerateEightPredicate(int extractCount) {
        /**
         * format : format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         * format1: 11(^) AND ( (1 and 1 and 1 ) or (1 and 1 and 1) )
         */
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < extractCount; i++){
            String p1 = citySinglePredicate.get(random.nextInt(citySinglePredicate.size()));
            String p2 = stateSinglePredicate.get(random.nextInt(stateSinglePredicate.size()));
            String p3 = popularSinglePredicate.get(random.nextInt(popularSinglePredicate.size()));
            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(1, 1, 6);
            List<String> list1 = totalSinglePredicate.get(randomIndexs.get(0));
            String p4;
            do {
                p4 = list1.get(random.nextInt(list1.size()));
            }while (stringPredicate.contains(p4));

            String p5 = starsSinglePredicate.get(random.nextInt(starsSinglePredicate.size()));
            String p6 = reviewSinglePredicate.get(random.nextInt(reviewSinglePredicate.size()));
            String p7 = favoritesSinglePredicate.get(random.nextInt(favoritesSinglePredicate.size()));
            String p8 = avgspendSinglePredicate.get(random.nextInt(avgspendSinglePredicate.size()));

            finallyPredicate.add(
                    Expression.ConcatPredicate(
                            Expression.ConcatAllANDConjunctionPredicate(p1, p2),
                            Conjunction.AND,
                            Expression.ConcatPredicate(
                                    Expression.ConcatAllANDConjunctionPredicate(p3, p4, p5),
                                    Conjunction.OR,
                                    Expression.ConcatAllANDConjunctionPredicate(p6, p7, p8)
                            )
                    )
            );
        }

        return finallyPredicate;

    }

    public static List<String> GenerateSevenPredicate(int extractCount) {
        /**
         * format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         * format1: 11(^) AND ((1 and 1 and 1) or (1 and 1))
         * format2: 1(^) AND ((1 and 1 and 1) or (1 and 1 and 1))
         */

        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();
        /**
         * format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         * format1: 11(^) AND ((1 and 1 and 1) or (1 and 1))
         * format2: 1(^) AND ((1 and 1 and 1) or (1 and 1 and 1))
         */

        for (int i = 0; i < extractCount; i++) {
            String p1 = citySinglePredicate.get(random.nextInt(citySinglePredicate.size()));
            String p2 = stateSinglePredicate.get(random.nextInt(stateSinglePredicate.size()));
            String p3 = popularSinglePredicate.get(random.nextInt(popularSinglePredicate.size()));
            String p4 = starsSinglePredicate.get(random.nextInt(starsSinglePredicate.size()));
            String p5 = reviewSinglePredicate.get(random.nextInt(reviewSinglePredicate.size()));
            String p6 = favoritesSinglePredicate.get(random.nextInt(favoritesSinglePredicate.size()));
            String p7 = avgspendSinglePredicate.get(random.nextInt(avgspendSinglePredicate.size()));
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

    public static List<String> GenerateSixPredicate(int extractCount) {
        /**
         *  format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         *  format1: 11(^) AND ((1 AND 1) OR  (1 AND 1))
         *  format2: 11(^) AND ((1 AND 1 AND 1) OR 1 ))
         *  format3: 1(^) AND  ((1 AND 1) OR  (1 AND 1 AND 1))
         */
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < extractCount; i++) {
            List<String> list1 = citySinglePredicate;
            List<String> list2 = stateSinglePredicate;
            List<String> list3 = popularSinglePredicate;
            List<Integer> rondomIndexs = GeneratorUtils.randomChooseNNum(3, 3, 6);

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

    public static List<String> GenerateFivePredicate(int extractCount) {
        /**
         *   format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         *   format1: 11(^) AND ( (1 AND 1) OR 1)
         *   format2: 1 AND ((1 AND 1) OR (1 AND 1))
         *   format3: 1 AND ((1 AND 1 AND 1) OR 1)
         */
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);

        List<String> finallyPredicate = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<String> list1 = citySinglePredicate;
            List<String> list2 = stateSinglePredicate;
            List<String> list3 = popularSinglePredicate;

            List<Integer> rondomIndexs = GeneratorUtils.randomChooseNNum(2, 3, 6);
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

    public static List<String> GenerateFourPredicate(int extractCount) {
        /**
         * format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         *  format1: 11(^) AND 1 OR 1
         *  format2: 1 AND (  (1 AND 1)  OR 1 )
         */
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);

        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");

        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
        // 非数值型放前面
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);
        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndexs_1 = GeneratorUtils.randomChooseNNum(2,0,2);

            List<String> list1 = totalSinglePredicate.get(randomIndexs_1.get(0));
            List<String> list2 = totalSinglePredicate.get(randomIndexs_1.get(1));

            List<Integer> randomIndexs_2 = GeneratorUtils.randomChooseNNum(2,3,6 );

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

    public static List<String> GenerateThreePredicate(int extractCount) {

        /**
         * format :  ( ___ AND ___ ) OR ( ___ AND ___ )  AND  ( ___ AND ___ ) OR ( ___ AND ___ )
         *
         */

        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
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


    public static List<String> GenerateTwoPredicate(int extractCount) {
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(citySinglePredicate);
        totalSinglePredicate.add(stateSinglePredicate);
        totalSinglePredicate.add(starsSinglePredicate);
        totalSinglePredicate.add(reviewSinglePredicate);
        totalSinglePredicate.add(favoritesSinglePredicate);
        totalSinglePredicate.add(avgspendSinglePredicate);
        totalSinglePredicate.add(popularSinglePredicate);
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
        int cityCountMinValue = 86;
        int reviewCountMinValue =36 ;
        int favoritesCountMinValue = 580;
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        List<String> city = GeneratorUtils.getColumnValue(business.getJSONArray("city"), cityCountMinValue);
        List<String> state = GeneratorUtils.getColumnValue(business.getJSONArray("state"), 0);
        List<String> stars = GeneratorUtils.getColumnValue(business.getJSONArray("stars"), 0);
        List<String> review = GeneratorUtils.getColumnValue(business.getJSONArray("review"), reviewCountMinValue);
        List<String> favorites = GeneratorUtils.getColumnValue(business.getJSONArray("favorites"), favoritesCountMinValue);
        List<String> avgspend = GeneratorUtils.getColumnValue(business.getJSONArray("avgspend"), 0);


        List<String> citySinglePredicate = GeneratorUtils.singlePredicateForStringType("city", city);
        List<String> stateSinglePredicate = GeneratorUtils.singlePredicateForStringType("state", state);
        List<String> starsSinglePredicate = GeneratorUtils.singlePredicateForNumericType("stars", stars, 1.0, 5.0);
        List<String> reviewSinglePredicate = GeneratorUtils.singlePredicateForNumericType("review", review, 5, 252);
        List<String> favoritesSinglePredicate = GeneratorUtils.singlePredicateForNumericType("favorites", favorites, 0, 220);
        List<String> avgspendSinglePredicate = GeneratorUtils.singlePredicateForNumericType("avgspend", avgspend, 10, 160);
        List<String> popularSinglePredicate = new ArrayList<>();
        popularSinglePredicate.add("popular = TRUE");
        popularSinglePredicate.add("popular = FALSE");


        List<String> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.addAll(citySinglePredicate);
        totalSinglePredicate.addAll(stateSinglePredicate);
        totalSinglePredicate.addAll(starsSinglePredicate);
        totalSinglePredicate.addAll(reviewSinglePredicate);
        totalSinglePredicate.addAll(favoritesSinglePredicate);
        totalSinglePredicate.addAll(avgspendSinglePredicate);
        totalSinglePredicate.addAll(popularSinglePredicate);

        // 创建一个副本并打乱它
        List<String> shuffledList = new ArrayList<>(totalSinglePredicate);
        Collections.shuffle(shuffledList);
        extractCount = Math.min(extractCount, shuffledList.size());
        return new ArrayList<>(shuffledList.subList(0, extractCount));
    }
}
