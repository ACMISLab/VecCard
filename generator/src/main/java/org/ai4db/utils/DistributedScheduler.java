package org.ai4db.utils;

import java.util.*;
import java.util.stream.Collectors;

class Partition {
    int id;
    int rows;

    public Partition(int id, int rows) {
        this.id = id;
        this.rows = rows;
    }
}

class Node {
    int id;
    double totalCpu;
    double totalMem;
    double usedCpu;
    double usedMem;
    double cpuPer100;
    double memPer100;
    double x1;

    public Node(int id, double totalCpu, double totalMem, double usedCpu, double usedMem,
                double cpuPer100, double memPer100, double x1) {
        this.id = id;
        this.totalCpu = totalCpu;
        this.totalMem = totalMem;
        this.usedCpu = usedCpu;
        this.usedMem = usedMem;
        this.cpuPer100 = cpuPer100;
        this.memPer100 = memPer100;
        this.x1 = x1;
    }
}

public class DistributedScheduler {

    public static Map<Node, List<Partition>> schedule(List<Partition> partitions, List<Node> nodes, double x2, double x3) {
        if (nodes.isEmpty()) return Collections.emptyMap();

        Node localNode = nodes.get(0);
        List<Partition> remaining = new ArrayList<>(partitions);
        Map<Node, List<Partition>> assignment = new HashMap<>();

        // 处理本地节点
        assignToNode(localNode, remaining, assignment, true);

        // 处理远程节点（按x1升序排序）
        List<Node> remoteNodes = nodes.stream()
                .filter(n -> n.id != localNode.id)
                .sorted(Comparator.comparingDouble(n -> n.x1))
                .collect(Collectors.toList());

        for (Node node : remoteNodes) {
            if (remaining.isEmpty()) break;
            assignToNode(node, remaining, assignment, false);
        }

        return assignment;
    }

    private static void assignToNode(Node node, List<Partition> remaining,
                                     Map<Node, List<Partition>> assignment, boolean isLocal) {
        List<Partition> nodeParts = new ArrayList<>();
        double availableCpu = 0.8 * node.totalCpu - node.usedCpu;
        double availableMem = 0.8 * node.totalMem - node.usedMem;

        if (availableCpu <= 0 || availableMem <= 0) return;

        // 按分区大小降序排列以优化处理时间
        List<Partition> sorted = new ArrayList<>(remaining);
        sorted.sort((a, b) -> Integer.compare(b.rows, a.rows));

        double currentCpu = 0, currentMem = 0;
        List<Partition> toRemove = new ArrayList<>();

        for (Partition p : sorted) {
            double reqCpu = (p.rows * node.cpuPer100) / 100.0;
            double reqMem = (p.rows * node.memPer100) / 100.0;

            if (currentCpu + reqCpu <= availableCpu && currentMem + reqMem <= availableMem) {
                currentCpu += reqCpu;
                currentMem += reqMem;
                nodeParts.add(p);
                toRemove.add(p);
            }
        }

        if (!nodeParts.isEmpty()) {
            assignment.put(node, nodeParts);
            remaining.removeAll(toRemove);
        }
    }

    public static double calculateTotalTime(Map<Node, List<Partition>> assignment, double x2, double x3) {
        double maxTime = 0;
        for (Map.Entry<Node, List<Partition>> entry : assignment.entrySet()) {
            Node node = entry.getKey();
            List<Partition> parts = entry.getValue();
            if (parts.isEmpty()) continue;

            if (assignment.size() == 1) { // 仅本地节点
                int maxRows = parts.stream().mapToInt(p -> p.rows).max().orElse(0);
                maxTime = Math.max(maxTime, (maxRows * node.x1) / 100.0);
            } else { // 远程节点
                int sumRows = parts.stream().mapToInt(p -> p.rows).sum();
                int maxRows = parts.stream().mapToInt(p -> p.rows).max().orElse(0);
                double time = 4 * x2 + (maxRows * node.x1) / 100.0 + (sumRows * x3) / 100.0;
                maxTime = Math.max(maxTime, time);
            }
        }
        return maxTime;
    }

    public static void main(String[] args) {
        // 示例数据
        List<Node> nodes = Arrays.asList(
                new Node(0, 1000, 2000, 500, 800, 2, 4, 1.5), // 本地节点
                new Node(1, 1000, 2000, 300, 500, 3, 5, 1.0),
                new Node(2, 1000, 2000, 200, 400, 1, 2, 2.0)
        );

        List<Partition> partitions = Arrays.asList(
                new Partition(1, 100000),
                new Partition(2, 300),
                new Partition(3, 700),
                new Partition(4, 200)
        );

        double x2 = 10.0;
        double x3 = 5.0;

        Map<Node, List<Partition>> assignment = schedule(partitions, nodes, x2, x3);
        double totalTime = calculateTotalTime(assignment, x2, x3);

        // 打印分配结果
        for (Map.Entry<Node, List<Partition>> entry : assignment.entrySet()) {
            System.out.println("Node " + entry.getKey().id + " assigned partitions: " +
                    entry.getValue().stream().map(p -> String.valueOf(p.id)).collect(Collectors.joining(", ")));
        }
        System.out.println("Total minimum time: " + totalTime + " ms");
    }
}

