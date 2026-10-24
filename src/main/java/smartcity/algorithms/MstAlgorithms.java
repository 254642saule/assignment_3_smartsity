package smartcity.algorithms;
import smartcity.dsu.DisjointSetUnion;
import smartcity.graph.WeightedGraph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
public final class MstAlgorithms {
    public record MstResult(long totalWeight, List<WeightedGraph.Edge> selectedEdges,
                            boolean connected, AlgorithmStats stats) {}
    private MstAlgorithms() {}
    public static MstResult kruskal(WeightedGraph graph) {
        requireUndirected(graph);
        AlgorithmStats stats = new AlgorithmStats();
        List<WeightedGraph.Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            stats.comparisons++;
            int byWeight = Long.compare(a.weight(), b.weight());
            if (byWeight != 0) return byWeight;
            int byFrom = Integer.compare(a.from(), b.from());
            return byFrom != 0 ? byFrom : Integer.compare(a.to(), b.to());
        });
        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertexCount());
        List<WeightedGraph.Edge> chosen = new ArrayList<>();
        long total = 0;
        for (WeightedGraph.Edge edge : sorted) {
            stats.edgeExaminations++;
            if (dsu.union(edge.from(), edge.to())) {
                chosen.add(edge);
                total += edge.weight();
                if (chosen.size() == graph.vertexCount() - 1) break;
            }
        }
        stats.dsuFindCalls = dsu.findCalls();
        stats.dsuUnionCalls = dsu.unionCalls();
        boolean connected = graph.vertexCount() <= 1 || chosen.size() == graph.vertexCount() - 1;
        return new MstResult(total, List.copyOf(chosen), connected, stats);
    }
    public static MstResult prim(WeightedGraph graph) {
        requireUndirected(graph);
        AlgorithmStats stats = new AlgorithmStats();
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        Comparator<WeightedGraph.Edge> order = Comparator
                .comparingLong(WeightedGraph.Edge::weight)
                .thenComparingInt(WeightedGraph.Edge::from)
                .thenComparingInt(WeightedGraph.Edge::to);
        PriorityQueue<WeightedGraph.Edge> queue = new PriorityQueue<>(order);
        List<WeightedGraph.Edge> chosen = new ArrayList<>();
        long total = 0;
        int visitedCount = 0;
        for (int root = 0; root < n; root++) {
            if (visited[root]) continue;
            visited[root] = true;
            visitedCount++;
            offerOutgoing(graph, root, visited, queue, stats);
            while (!queue.isEmpty()) {
                WeightedGraph.Edge edge = queue.poll();
                stats.heapPolls++;
                if (visited[edge.to()]) continue;
                visited[edge.to()] = true;
                visitedCount++;
                chosen.add(edge);
                total += edge.weight();
                offerOutgoing(graph, edge.to(), visited, queue, stats);
            }
        }
        boolean connected = n <= 1 || visitedCount == n && chosen.size() == n - 1;
        return new MstResult(total, List.copyOf(chosen), connected, stats);
    }
    private static void offerOutgoing(WeightedGraph graph, int vertex, boolean[] visited,
                                      PriorityQueue<WeightedGraph.Edge> queue, AlgorithmStats stats) {
        for (WeightedGraph.Edge edge : graph.outgoing(vertex)) {
            stats.edgeExaminations++;
            if (!visited[edge.to()]) {
                queue.offer(edge);
                stats.heapOffers++;
            }
        }
    }
}
private static void requireUndirected(WeightedGraph graph) {
    if (graph.isDirected()) throw new IllegalArgumentException("MST algorithms require an undirected
            graph");
}
}

