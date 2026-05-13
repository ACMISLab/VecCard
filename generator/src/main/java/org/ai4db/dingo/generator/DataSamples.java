package org.ai4db.dingo.generator;

import org.ai4db.utils.CSVFileUtils;

import java.security.cert.CertStore;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-13 21:19
 */
public class DataSamples {
    public static void main(String[] args) {
        int generateCount =  150000;
        double businessScaleFactor = 0.35;
        double reviewScaleFactor = 0.35;
        double tipScaleFactor = 0.15;
        double problemScaleFactor = 0.15;
        List<String> predicates = new ArrayList<>();
        predicates.addAll(GenerateBusinessSQL.GeneratePredicate((int) (businessScaleFactor*generateCount)));
        predicates.addAll(GenerateProblemSQL.GeneratePredicate((int) (problemScaleFactor*generateCount)));
        predicates.addAll(GenerateReviewSQL.GeneratePredicate((int) (reviewScaleFactor*generateCount)));
        predicates.addAll(GenerateTipSQL.GeneratePredicate((int) (tipScaleFactor*generateCount)));

        CSVFileUtils.writeListToCSVWithSerial(predicates, "data/predicate/predicates_new_1.csv");
    }
}
