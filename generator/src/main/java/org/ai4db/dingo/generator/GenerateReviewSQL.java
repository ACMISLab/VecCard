package org.ai4db.dingo.generator;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.ai4db.core.Conjunction;
import org.ai4db.core.Expression;
import org.ai4db.core.Predicate;
import org.ai4db.core.SQL;
import org.ai4db.utils.CSVFileUtils;
import org.ai4db.utils.JSONFileReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-13 16:16
 */
public class GenerateReviewSQL {
    public static void main(String[] args) {

    }

    public static List<String> GeneratePredicate(int allExtractCount){
        List<String> predicates = new ArrayList<>();
        int extractCount = allExtractCount/8;
//        predicates.addAll(GenerateTwentyPredicate(extractCount));
//        predicates.addAll(GenerateElevenPredicate(extractCount));
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

        String prefix = "review.";

        return deduplicatedPredicates.stream()
                .map(predicate -> prefix + predicate)
                .collect(Collectors.toList());

    }



    public static List<String> GenerateTwentyPredicate(int extractCount) {
        /**
         * format1: 1111111111(^) AND 1 OR 1
         * format2: 111111111(^) AND 11(v) OR 1
         * format3: 11111111(^) AND 111(V) OR 1
         * format4: 1111111(^) AND 1111(V) OR 1
         * format5: 111111(^) AND 11111(V) OR 1
         * format6: 11111(^) AND 111111(V) OR 1
         * format7: 1111(^) AND 1111111(V) OR 1
         * format8: 111(^) AND 11111111(V) OR 1
         * format9: 11(^) AND 111111111(V) OR 1
         * format10: 1(^) AND 1111111111(V) OR 1
         */
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(8,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
            String p7 = totalSinglePredicate.get(randomIndex.get(6)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(6)).size()));
            String p8 = totalSinglePredicate.get(randomIndex.get(7)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(7)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            stringPredicate.add(p4);
            stringPredicate.add(p5);
            stringPredicate.add(p6);
            stringPredicate.add(p7);
            stringPredicate.add(p8);

            String p9,p10,p11,p12;
            do {
                List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(4, 0, 7);
                p9 = totalSinglePredicate.get(randomIndexs.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(0)).size()));
                p10 = totalSinglePredicate.get(randomIndexs.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(1)).size()));
                p11 = totalSinglePredicate.get(randomIndexs.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(2)).size()));
                p12 = totalSinglePredicate.get(randomIndexs.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(3)).size()));
            }while (stringPredicate.contains(p9) && stringPredicate.contains(p10) && stringPredicate.contains(p11) && stringPredicate.contains(p12));

            if (i % 10 == 0){
                // format1: 1111111111(^) AND 1 OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7, p8,p9,p10),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p11, p12)
                        )
                );
            }else if (i % 10 == 1){
                // format2: 111111111(^) AND 11(v) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7, p8,p9),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p10, p11, p12)
                        )
                );
            }else if (i % 10 == 2){
                // format3: 11111111(^) AND 111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7, p8),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p9, p10, p11, p12)
                        )
                );
            }else if (i % 10 == 3){
                // format4: 1111111(^) AND 1111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p8, p9, p10, p11, p12)
                        )
                );
            }else if (i % 10 == 4){
                // format5: 111111(^) AND 11111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p7, p8, p9, p10, p11, p12)
                        )
                );
            }else if (i % 10 == 5){
                // format6: 11111(^) AND 111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p6, p7, p8, p9, p10, p11, p12)
                        )
                );
            }else if (i % 10 == 6){
                // format7: 1111(^) AND 1111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p5, p6, p7, p8, p9, p10, p11, p12)
                        )
                );
            }else if (i % 10 == 7){
                // format8: 111(^) AND 11111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p4, p5, p6, p7, p8, p9, p10, p11, p12)
                        )
                );
            }else if (i % 10 == 8){
                // format9: 11(^) AND 111111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)
                        )
                );
            }else {
                // format10: 1(^) AND 1111111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)
                        )
                );
            }

        }
        return finallyPredicate;
    }
    public static List<String> GenerateElevenPredicate(int extractCount) {
        /**
         * format1: 111111111(^) AND 1 OR 1
         * format2: 11111111(^) AND 11(v) OR 1
         * format3: 1111111(^) AND 111(V) OR 1
         * format4: 111111(^) AND 1111(V) OR 1
         * format5: 11111(^) AND 11111(V) OR 1
         * format6: 1111(^) AND 111111(V) OR 1
         * format7: 111(^) AND 1111111(V) OR 1
         * format8: 11(^) AND 11111111(V) OR 1
         * format9: 1(^) AND 111111111(V) OR 1
         */
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(8,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
            String p7 = totalSinglePredicate.get(randomIndex.get(6)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(6)).size()));
            String p8 = totalSinglePredicate.get(randomIndex.get(7)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(7)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            stringPredicate.add(p4);
            stringPredicate.add(p5);
            stringPredicate.add(p6);
            stringPredicate.add(p7);
            stringPredicate.add(p8);

            String p9,p10,p11;
            do {
                List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(3, 0, 7);
                p9 = totalSinglePredicate.get(randomIndexs.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(0)).size()));
                p10 = totalSinglePredicate.get(randomIndexs.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(1)).size()));
                p11 = totalSinglePredicate.get(randomIndexs.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(2)).size()));
            }while (stringPredicate.contains(p9) && stringPredicate.contains(p10) && stringPredicate.contains(p11));
            if (i % 9 == 0){
                // format1: 111111111(^) AND 1 OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7, p8,p9),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p10, p11)
                        )
                );
            }else if (i % 9 == 1){
                // format2: 11111111(^) AND 11(v) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7, p8),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p9, p10, p11)
                        )
                );
            }else if (i % 9 == 2){
                // format3: 1111111(^) AND 111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6, p7),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p8, p9, p10, p11)
                        )
                );
            }else if (i % 9 == 3){
                // format4: 111111(^) AND 1111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5, p6),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p7, p8, p9, p10, p11)
                        )
                );
            }else if (i % 9 == 4){
                // format5: 11111(^) AND 11111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4, p5),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p6, p7, p8, p9, p10, p11)
                        )
                );
            }else if (i % 9 == 5){
                // format6: 1111(^) AND 111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3, p4),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p5, p6, p7, p8, p9, p10, p11)
                        )
                );
            }else if (i % 9 == 6){
                // format7: 111(^) AND 1111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatAllANDConjunctionPredicate(p1, p2, p3),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p4, p5, p6, p7, p8, p9, p10, p11)
                        )
                );
            }else if (i % 9 == 7){
                // format8: 11(^) AND 11111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                Expression.ConcatPredicate(p1, Conjunction.AND, p2),
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p3, p4, p5, p6, p7, p8, p9, p10, p11)
                        )
                );
            }else {
                // format9: 1(^) AND 111111111(V) OR 1
                finallyPredicate.add(
                        Expression.ConcatPredicate(
                                p1,
                                Conjunction.AND,
                                Expression.ConcatAllORConjunctionPredicate(p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)
                        )
                );
            }
        }
        return finallyPredicate;
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
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(8,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
            String p7 = totalSinglePredicate.get(randomIndex.get(6)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(6)).size()));
            String p8 = totalSinglePredicate.get(randomIndex.get(7)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(7)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            stringPredicate.add(p4);
            stringPredicate.add(p5);
            stringPredicate.add(p6);
            stringPredicate.add(p7);
            stringPredicate.add(p8);

            String p9,p10;

            do {
                List<Integer> randomIndexs = GeneratorUtils.randomChooseNNum(2, 0, 7);
                p9 = totalSinglePredicate.get(randomIndexs.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(0)).size()));
                p10 = totalSinglePredicate.get(randomIndexs.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndexs.get(1)).size()));
            }while (stringPredicate.contains(p9) && stringPredicate.contains(p10));

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
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(8,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
            String p7 = totalSinglePredicate.get(randomIndex.get(6)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(6)).size()));
            String p8 = totalSinglePredicate.get(randomIndex.get(7)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(7)).size()));

            List<String> stringPredicate = new ArrayList<>();
            stringPredicate.add(p1);
            stringPredicate.add(p2);
            stringPredicate.add(p3);
            stringPredicate.add(p4);
            stringPredicate.add(p5);
            stringPredicate.add(p6);
            stringPredicate.add(p7);
            stringPredicate.add(p8);
            String p9;
            do{
                List<Integer> randomIndex2 = GeneratorUtils.randomChooseNNum(1,0,7);
                p9 = totalSinglePredicate.get(randomIndex2.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex2.get(0)).size()));
            }while (stringPredicate.contains(p9));

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
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(8,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
            String p7 = totalSinglePredicate.get(randomIndex.get(6)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(6)).size()));
            String p8 = totalSinglePredicate.get(randomIndex.get(7)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(7)).size()));

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
         * format2: 1(^) AND ((1 and 1 and 1) or (1 and 1 and 1)
         */
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(7,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
            String p7 = totalSinglePredicate.get(randomIndex.get(6)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(6)).size()));

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
         *  format1: 11(^) AND (1 AND 1) OR  (1 AND 1)
         *  format2: 11(^) AND (1 AND 1 AND 1) OR 1 )
         *  format3: 1(^) AND  ((1 AND 1) OR  (1 AND 1 AND 1))
         */
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(6,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            String p3 = totalSinglePredicate.get(randomIndex.get(2)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(2)).size()));
            String p4 = totalSinglePredicate.get(randomIndex.get(3)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(3)).size()));
            String p5 = totalSinglePredicate.get(randomIndex.get(4)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(4)).size()));
            String p6 = totalSinglePredicate.get(randomIndex.get(5)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(5)).size()));
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
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(5,0,7);
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

    public static List<String> GenerateFourPredicate(int extractCount) {
        /**
         * format :  ___,___AND ((__ AND __ AND __) OR (___AND___AND___ ))
         *  format1: 11(^) AND 1 OR 1
         *  format2: 1 AND (  (1 AND 1)  OR 1 )
         */
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){

            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(4,0,7);
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


    public static List<String> GenerateThreePredicate(int extractCount) {
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++){
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(3,0,7);
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


    public static List<String> GenerateTwoPredicate(int extractCount) {
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");


        List<List<String>> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.add(readsPredicates);
        totalSinglePredicate.add(starsPredicates);
        totalSinglePredicate.add(usefulPredicates);
        totalSinglePredicate.add(funnyPredicates);
        totalSinglePredicate.add(coolPredicates);
        totalSinglePredicate.add(likesPredicates);
        totalSinglePredicate.add(dislikesPredicates);
        totalSinglePredicate.add(viewsPredicates);

        List<String> finallyPredicate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < extractCount; i++) {
            List<Integer> randomIndex = GeneratorUtils.randomChooseNNum(2,0,7);
            String p1 = totalSinglePredicate.get(randomIndex.get(0)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(0)).size()));
            String p2 = totalSinglePredicate.get(randomIndex.get(1)).get(random.nextInt(totalSinglePredicate.get(randomIndex.get(1)).size()));
            finallyPredicate.add(
                    Expression.ConcatTwoPredicate(p1, p2)
            );
        }
        return finallyPredicate;
    }

    public static List<String> GenerateOnePredicate(int extractCount) {
        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        List<String> starts = GeneratorUtils.getColumnValue(review.getJSONArray("stars"), 0);
        List<String> useful = GeneratorUtils.getColumnValue(review.getJSONArray("useful"), 0);
        List<String> funny = GeneratorUtils.getColumnValue(review.getJSONArray("funny"), 0);
        List<String> cool = GeneratorUtils.getColumnValue(review.getJSONArray("cool"), 0);
        List<String> likes = GeneratorUtils.getColumnValue(review.getJSONArray("likes"), 0);
        List<String> dislikes = GeneratorUtils.getColumnValue(review.getJSONArray("dislikes"), 0);
        List<String> views = GeneratorUtils.getColumnValue(review.getJSONArray("views"), 0);

        List<String> starsPredicates = GeneratorUtils.singlePredicateForNumericType("stars", starts,1.0,5.0);
        List<String> usefulPredicates = GeneratorUtils.singlePredicateForNumericType("useful", useful,0.0,58.0);
        List<String> funnyPredicates = GeneratorUtils.singlePredicateForNumericType("funny", funny,0.0,34.0);
        List<String> coolPredicates = GeneratorUtils.singlePredicateForNumericType("cool", cool,0.0,62.0);
        List<String> likesPredicates = GeneratorUtils.singlePredicateForNumericType("likes", likes,0,150);
        List<String> dislikesPredicates = GeneratorUtils.singlePredicateForNumericType("dislikes", dislikes,0,48);
        List<String> viewsPredicates = GeneratorUtils.singlePredicateForNumericType("views", views,0,20);
        List<String> readsPredicates = new ArrayList<>();
        readsPredicates.add("read = TRUE");
        readsPredicates.add("read = FALSE");
        
        List<String> totalSinglePredicate = new ArrayList<>();
        totalSinglePredicate.addAll(starsPredicates);
        totalSinglePredicate.addAll(usefulPredicates);
        totalSinglePredicate.addAll(funnyPredicates);
        totalSinglePredicate.addAll(coolPredicates);
        totalSinglePredicate.addAll(likesPredicates);
        totalSinglePredicate.addAll(dislikesPredicates);
        totalSinglePredicate.addAll(viewsPredicates);
        totalSinglePredicate.addAll(readsPredicates);
        // 创建一个副本并打乱它
        List<String> shuffledList = new ArrayList<>(totalSinglePredicate);
        Collections.shuffle(shuffledList);
        extractCount = Math.min(extractCount, shuffledList.size());
        return new ArrayList<>(shuffledList.subList(0, extractCount));
    }
}
