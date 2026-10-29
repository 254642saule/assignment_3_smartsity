package smartcity.algorithms;
import smartcity.graph.WeightedGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
public final class TopologicalSort {
    public record Result(List<Integer> order, boolean acyclic, AlgorithmStats stats) {
    }

    private TopologicalSort() {
    }

    public static Result sort(WeightedGraph graph) {
        requireDirected(graph);
        int n = graph.vertexCount();
        AlgorithmStats stats = new AlgorithmStats();
        int[] indegree = new int[n];
        for (int from = 0; from < n; from++) {
            for (WeightedGraph.Edge edge : graph.outgoing(from)) {
                indegree[edge.to()]++;
                stats.edgeExaminations++;
            }
        }
        Queue<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < n; vertex++) if (indegree[vertex] == 0) ready.offer(vertex);
        List<Integer> order = new ArrayList<>(n);
        while (!ready.isEmpty()) {
            int vertex = ready.remove();
            order.add(vertex);
            for (WeightedGraph.Edge edge : graph.outgoing(vertex)) {
                stats.edgeExaminations++;
                if (--indegree[edge.to()] == 0) ready.offer(edge.to());
            }
        }
        return new Result(List.copyOf(order), order.size() == n, stats);
    }

    private static void requireDirected(WeightedGraph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Topological ordering requires a directed graph"
            );
        }
    }
}