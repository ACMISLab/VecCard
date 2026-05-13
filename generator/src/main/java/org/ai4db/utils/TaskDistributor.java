package org.ai4db.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskDistributor {
    public static List<Integer> distributeTasks(int N, int M) {
        // 边界条件处理
        if (N <= 0 || M < N) {
            return new ArrayList<>();
        }

        // 初始化结果列表
        List<Integer> distribution = new ArrayList<>(Collections.nCopies(N, 0));

        // 计算最大可能的本地节点分配量
        int maxLocal = M - (N - 1);  // 其他节点至少各1个
        int finalLocal = -1;

        // 逆向寻找满足约束条件的最大本地分配值
        for (int candidate = maxLocal; candidate >= 1; candidate--) {
            int remaining = M - candidate;

            // 计算其他节点需要分配的最小最大值
            int minMax = (int) Math.ceil((double) remaining / (N - 1));

            // 检查约束条件：candidate < 2*minMax
            if (candidate < 2 * minMax) {
                finalLocal = candidate;
                break;
            }
        }

        // 没有找到合法解的情况（正常情况下应该总能找到）
        if (finalLocal == -1) {
            return new ArrayList<>();
        }

        // 设置本地节点分配值
        distribution.set(0, finalLocal);

        // 计算其他节点的分配方案
        int remainingTasks = M - finalLocal;
        int base = remainingTasks / (N - 1);
        int remainder = remainingTasks % (N - 1);

        // 给前remainder个节点各+1
        for (int i = 1; i < N; i++) {
            distribution.set(i, base + (i <= remainder ? 1 : 0));
        }

        return distribution;
    }

    public static void main(String[] args) {
        // 测试案例
        System.out.println(distributeTasks(3, 10));  // [5, 3, 2]
        System.out.println(distributeTasks(2, 3));   // [1, 2]
        System.out.println(distributeTasks(4, 10));  // [3, 3, 2, 2]
        System.out.println(distributeTasks(5, 8));   // [3, 2, 1, 1, 1]
        System.out.println(distributeTasks(2, 16));
    }
}
