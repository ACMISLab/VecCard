package org.ai4db.dingo.generator;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.ai4db.utils.JSONFileReader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-15 21:26
 */
public class ComputeCriticalValue {
    public static void main(String[] args) {
        JSONObject business = JSONFileReader.getJSONObejct("data/sample/business.json");
        JSONObject business_field_length = new JSONObject();
        business_field_length.put("city", business.getJSONArray("city").size());
        business_field_length.put("state", business.getJSONArray("state").size());
        business_field_length.put("stars", business.getJSONArray("stars").size());
        business_field_length.put("review", business.getJSONArray("review").size());
        business_field_length.put("favorites", business.getJSONArray("favorites").size());
        business_field_length.put("avgspend", business.getJSONArray("avgspend").size());
        business_field_length.put("popular", business.getJSONArray("popular").size());

        System.out.println(business_field_length);


        JSONObject review = JSONFileReader.getJSONObejct("data/sample/review.json");
        JSONObject review_field_length = new JSONObject();
        review_field_length.put("stars", review.getJSONArray("stars").size());
        review_field_length.put("useful", review.getJSONArray("useful").size());
        review_field_length.put("funny", review.getJSONArray("funny").size());
        review_field_length.put("cool", review.getJSONArray("cool").size());
        review_field_length.put("likes", review.getJSONArray("likes").size());
        review_field_length.put("dislikes", review.getJSONArray("dislikes").size());
        review_field_length.put("views", review.getJSONArray("views").size());
        review_field_length.put("reads", review.getJSONArray("reads").size());

        System.out.println(review_field_length);

        JSONObject tip = JSONFileReader.getJSONObejct("data/sample/tip.json");
        JSONObject tip_field_length = new JSONObject();
        tip_field_length.put("compliment", tip.getJSONArray("compliment").size());
        tip_field_length.put("num", tip.getJSONArray("num").size());
        tip_field_length.put("amount", tip.getJSONArray("amount").size());

        System.out.println(tip_field_length);


        JSONObject problem = JSONFileReader.getJSONObejct("data/sample/problem.json");
        JSONObject problem_field_length = new JSONObject();
        problem_field_length.put("language", problem.getJSONArray("language").size());
        problem_field_length.put("score", problem.getJSONArray("score").size());
        problem_field_length.put("type", problem.getJSONArray("type").size());
        problem_field_length.put("typetext", problem.getJSONArray("typetext").size());

        System.out.println(problem_field_length);

        System.out.println("========================================>");
        int valVectorMax = 0;
        JSONArray city = business.getJSONArray("city");
        sortAndFilter(city,86);

        JSONArray businessReview = business.getJSONArray("review");
        sortAndFilter(businessReview,36);

        JSONArray favorites = business.getJSONArray("favorites");
        sortAndFilter(favorites,580);


    }

    private static void sortAndFilter(JSONArray review,int value) {
        List<JSONObject> reviewList = review.toJavaList(JSONObject.class);
        List<JSONObject> reviewSortedAndFilteredList = reviewList.stream()
                .filter(obj -> obj.getInteger("num") >= value)
                .sorted((o1, o2) -> o2.getInteger("num").compareTo(o1.getInteger("num")))
                .collect(Collectors.toList());
        System.out.println(reviewSortedAndFilteredList.size());
    }
}
