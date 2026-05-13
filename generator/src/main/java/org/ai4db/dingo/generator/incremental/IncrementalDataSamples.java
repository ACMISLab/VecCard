package org.ai4db.dingo.generator.incremental;

import org.ai4db.utils.CSVFileUtils;

import java.util.ArrayList;
import java.util.List;

public class IncrementalDataSamples {
    public static void main(String[] args) {
        int generateCount =  30000;
        double yelpUserScaleFactor = 0.5;
        double mcxUserScaleFactor = 0.5;
        List<String> predicates = new ArrayList<>();
        predicates.addAll(GenerateYelpUserSQL.GeneratePredicate((int) (yelpUserScaleFactor*generateCount)));
        predicates.addAll(GenerateMcxUserSQL.GeneratePredicate((int) (mcxUserScaleFactor*generateCount)));

        CSVFileUtils.writeListToCSVWithSerial(predicates, "data/predicate/predicates_incremental.csv");
        System.out.println("Generated " + predicates.size() + " predicates.");
    }
}
