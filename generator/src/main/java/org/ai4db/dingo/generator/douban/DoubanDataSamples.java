package org.ai4db.dingo.generator.douban;

import org.ai4db.utils.CSVFileUtils;

import java.util.ArrayList;
import java.util.List;

public class DoubanDataSamples {
    public static void main(String[] args) {
        int generateCount =  20000;

        List<String> predicates = new ArrayList<>();
//        predicates.addAll(GenerateCommentSQL.GeneratePredicate(generateCount));
//        CSVFileUtils.writeListToCSVWithSerial(predicates, "data/predicate/douban_comment.csv");

        predicates.addAll(GenerateMovieSQL.GeneratePredicate(generateCount));
        CSVFileUtils.writeListToCSVWithSerial(predicates, "data/predicate/douban_movie.csv");

        System.out.println("Generated " + predicates.size() + " predicates.");

    }
}
