package org.ai4db.dingo;

import org.ai4db.core.Operator;

/**
 * @author wusf233@uniplore.io
 * @Description
 * @create 2024-08-14 10:15
 */
public class OperatorTest {
    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            System.out.println(Operator.randomForMinValueOperator());
        }
    }
}
