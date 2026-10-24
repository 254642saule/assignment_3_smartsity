package smartcity.algorithms;
import smartcity.graph.WeightedGraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public final class StronglyConnectedComponents {
    public record Result(List<List<Integer>> components, AlgorithmStats stats) {}
    private StronglyConnectedComponents() {}
    public static Result kosaraju(WeightedGraph graph) {
        if (!graph.isDirected()) throw new IllegalArgumentException("SCC requires a directed graph");
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        List<Integer> finishOrder = new ArrayList<>(n);
        AlgorithmStats stats = new AlgorithmStats();
        for (int vertex = 0; vertex < n; vertex++) {
            if (!visited[vertex]) firstDfs(graph, vertex, visited, finishOrder, stats);
        }
        List<List<Integer>> reverse = new ArrayList<>(n);
        for (int i = 0; i < n; i++) reverse.add(new ArrayList<>());
        for (WeightedGraph.Edge edge : graph.edges()) reverse.get(edge.to()).add(edge.from());
        java.util.Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int vertex = finishOrder.get(i);
            if (visited[vertex]) continue;
            List<Integer> component = new ArrayList<>();
            reverseDfs(vertex, reverse, visited, component, stats);
            Collections.sort(component);
            components.add(List.copyOf(component));
        }
        return new Result(List.copyOf(components), stats);
    }
    private static void firstDfs(WeightedGraph graph, int vertex, boolean[] visited,
                                 List<Integer> finishOrder, AlgorithmStats stats) {
        visited[vertex] = true;
        for (WeightedGraph.Edge edge : graph.outgoing(vertex)) {
            stats.edgeExaminations++;
            if (!visited[edge.to()]) firstDfs(graph, edge.to(), visited, finishOrder, stats);
        }
        finishOrder.add(vertex);
    }
    private static void reverseDfs(int vertex, List<List<Integer>> reverse, boolean[] visited,
                                   List<Integer> component, AlgorithmStats stats) {
        visited[vertex] = true;
        component.add(vertex);
        for (int predecessor : reverse.get(vertex)) {
            stats.edgeExaminations++;
            if (!visited[predecessor]) reverseDfs(predecessor, reverse, visited, component, stats);
        }
    }
}