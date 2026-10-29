package smartcity.algorithms;

import smartcity.dsu.DisjointSetUnion;
import smartcity.graph.WeightedGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MstAlgorithms {
    public record MstResult(
            long totalWeight,
            List<WeightedGraph.Edge> selectedEdges,
            boolean connected,
            AlgorithmStats stats
    ) {}

    private MstAlgorithms() {}

    public static MstResult kruskal(WeightedGraph graph) {
        requireUndirected(graph);

        AlgorithmStats stats = new AlgorithmStats();
        List<WeightedGraph.Edge> sortedEdges = new ArrayList<>(graph.edges());

        sortedEdges.sort((first, second) -> {
            stats.comparisons++;

            int weightComparison =
                    Long.compare(first.weight(), second.weight());

            if (weightComparison != 0) {
                return weightComparison;
            }

            int fromComparison =
                    Integer.compare(first.from(), second.from());

            if (fromComparison != 0) {
                return fromComparison;
            }

            return Integer.compare(first.to(), second.to());
        });

        DisjointSetUnion dsu =
                new DisjointSetUnion(graph.vertexCount());

        List<WeightedGraph.Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;

        for (WeightedGraph.Edge edge : sortedEdges) {
            stats.edgeExaminations++;

            if (dsu.union(edge.from(), edge.to())) {
                selectedEdges.add(edge);
                totalWeight += edge.weight();

                if (selectedEdges.size() == graph.vertexCount() - 1) {
                    break;
                }
            }
        }

        stats.dsuFindCalls = dsu.findCalls();
        stats.dsuUnionCalls = dsu.unionCalls();

        boolean connected = graph.vertexCount() <= 1
                || selectedEdges.size() == graph.vertexCount() - 1;

        return new MstResult(
                totalWeight,
                List.copyOf(selectedEdges),
                connected,
                stats
        );
    }

    public static MstResult prim(WeightedGraph graph) {
        requireUndirected(graph);

        AlgorithmStats stats = new AlgorithmStats();
        int vertexCount = graph.vertexCount();

        boolean[] visited = new boolean[vertexCount];

        Comparator<WeightedGraph.Edge> edgeOrder =
                Comparator.comparingLong(WeightedGraph.Edge::weight)
                        .thenComparingInt(WeightedGraph.Edge::from)
                        .thenComparingInt(WeightedGraph.Edge::to);

        PriorityQueue<WeightedGraph.Edge> queue =
                new PriorityQueue<>(edgeOrder);

        List<WeightedGraph.Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;
        int visitedCount = 0;

        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            visitedCount++;

            addOutgoingEdges(graph, start, visited, queue, stats);

            while (!queue.isEmpty()) {
                WeightedGraph.Edge edge = queue.poll();
                stats.heapPolls++;

                if (visited[edge.to()]) {
                    continue;
                }

                visited[edge.to()] = true;
                visitedCount++;
                selectedEdges.add(edge);
                totalWeight += edge.weight();

                addOutgoingEdges(graph, edge.to(), visited, queue, stats);
            }
        }

        boolean connected = vertexCount <= 1
                || (visitedCount == vertexCount
                && selectedEdges.size() == vertexCount - 1);

        return new MstResult(
                totalWeight,
                List.copyOf(selectedEdges),
                connected,
                stats
        );
    }

    private static void addOutgoingEdges(
            WeightedGraph graph,
            int vertex,
            boolean[] visited,
            PriorityQueue<WeightedGraph.Edge> queue,
            AlgorithmStats stats
    ) {
        for (WeightedGraph.Edge edge : graph.outgoing(vertex)) {
            stats.edgeExaminations++;

            if (!visited[edge.to()]) {
                queue.offer(edge);
                stats.heapOffers++;
            }
        }
    }

    private static void requireUndirected(WeightedGraph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "MST algorithms require an undirected graph"
            );
        }
    }
}