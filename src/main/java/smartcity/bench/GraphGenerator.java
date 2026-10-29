package smartcity.bench;

import smartcity.graph.WeightedGraph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class GraphGenerator {
    private GraphGenerator() {}

    public static WeightedGraph connectedUndirected(
            int vertices, int edges, long seed
    ) {
        validate(vertices, edges);

        long maxEdges = (long) vertices * (vertices - 1) / 2;
        int minEdges = Math.max(0, vertices - 1);

        if (edges < minEdges || edges > maxEdges) {
            throw new IllegalArgumentException(
                    "A connected undirected graph needs between V-1 and V*(V-1)/2 edges"
            );
        }

        Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, false);
        Set<Long> usedEdges = new HashSet<>();

        // Сначала создаём связный путь через все вершины.
        for (int vertex = 1; vertex < vertices; vertex++) {
            addUndirectedEdge(graph, usedEdges, vertex - 1, vertex, random);
        }

        // Добавляем случайные рёбра, пока не достигнем нужного количества.
        while (usedEdges.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);

            if (from != to) {
                addUndirectedEdge(graph, usedEdges, from, to, random);
            }
        }

        return graph;
    }

    public static WeightedGraph dag(int vertices, int edges, long seed) {
        validate(vertices, edges);

        long maxEdges = (long) vertices * (vertices - 1) / 2;
        int minEdges = Math.max(0, vertices - 1);

        if (edges < minEdges || edges > maxEdges) {
            throw new IllegalArgumentException(
                    "A DAG needs between V-1 and V*(V-1)/2 edges"
            );
        }

        Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Set<Long> usedEdges = new HashSet<>();

        // Рёбра идут только от меньшей вершины к большей.
        // Поэтому цикл в графе образоваться не может.
        for (int vertex = 1; vertex < vertices; vertex++) {
            addDirectedEdge(graph, usedEdges, vertex - 1, vertex);
        }

        while (usedEdges.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);

            if (from < to) {
                addDirectedEdge(graph, usedEdges, from, to);
            }
        }

        return graph;
    }

    public static WeightedGraph cyclicDirected(
            int vertices, int edges, long seed
    ) {
        validate(vertices, edges);

        if (vertices == 0) {
            throw new IllegalArgumentException(
                    "A cyclic graph needs at least one vertex"
            );
        }

        long maxEdges = (long) vertices * (vertices - 1);

        if (vertices == 1) {
            if (edges != 1) {
                throw new IllegalArgumentException(
                        "A one-vertex cyclic graph needs exactly one self-loop"
                );
            }

            WeightedGraph graph = new WeightedGraph(1, true);
            graph.addEdge(0, 0, 1);
            return graph;
        }

        if (edges < vertices || edges > maxEdges) {
            throw new IllegalArgumentException(
                    "A cyclic directed graph needs between V and V*(V-1) edges"
            );
        }

        Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Set<Long> usedEdges = new HashSet<>();

        // Создаём цикл: 0 -> 1 -> ... -> V-1 -> 0.
        for (int vertex = 0; vertex < vertices; vertex++) {
            int next = (vertex + 1) % vertices;
            addDirectedEdge(graph, usedEdges, vertex, next);
        }

        addRandomDirectedEdges(graph, usedEdges, vertices, edges, random);
        return graph;
    }

    public static WeightedGraph stronglyConnectedDirected(
            int vertices, int edges, long seed
    ) {
        validate(vertices, edges);

        long maxEdges = (long) vertices * (vertices - 1);

        if (vertices == 0) {
            if (edges != 0) {
                throw new IllegalArgumentException(
                        "An empty graph cannot contain edges"
                );
            }
            return new WeightedGraph(0, true);
        }

        if (vertices == 1) {
            if (edges > 1) {
                throw new IllegalArgumentException(
                        "A one-vertex graph can have at most one unique edge"
                );
            }

            WeightedGraph graph = new WeightedGraph(1, true);
            if (edges == 1) {
                graph.addEdge(0, 0, 1);
            }
            return graph;
        }

        if (edges < vertices || edges > maxEdges) {
            throw new IllegalArgumentException(
                    "A strongly connected graph needs between V and V*(V-1) edges"
            );
        }

        Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Set<Long> usedEdges = new HashSet<>();

        // Направленный цикл обеспечивает достижимость всех вершин.
        for (int vertex = 0; vertex < vertices; vertex++) {
            int next = (vertex + 1) % vertices;
            addDirectedEdge(graph, usedEdges, vertex, next);
        }

        addRandomDirectedEdges(graph, usedEdges, vertices, edges, random);
        return graph;
    }

    private static void addRandomDirectedEdges(
            WeightedGraph graph,
            Set<Long> usedEdges,
            int vertices,
            int targetEdges,
            Random random
    ) {
        while (usedEdges.size() < targetEdges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);

            if (from != to) {
                addDirectedEdge(graph, usedEdges, from, to);
            }
        }
    }

    private static void addUndirectedEdge(
            WeightedGraph graph,
            Set<Long> usedEdges,
            int first,
            int second,
            Random random
    ) {
        int from = Math.min(first, second);
        int to = Math.max(first, second);
        long key = pairKey(from, to);

        if (usedEdges.add(key)) {
            graph.addEdge(from, to, 1 + random.nextInt(10_000));
        }
    }

    private static void addDirectedEdge(
            WeightedGraph graph,
            Set<Long> usedEdges,
            int from,
            int to
    ) {
        long key = pairKey(from, to);

        if (usedEdges.add(key)) {
            graph.addEdge(from, to, 1);
        }
    }

    private static long pairKey(int from, int to) {
        return ((long) from << 32) | (to & 0xffffffffL);
    }

    private static void validate(int vertices, int edges) {
        if (vertices < 0 || edges < 0) {
            throw new IllegalArgumentException(
                    "The number of vertices and edges cannot be negative"
            );
        }

        if (vertices == 0 && edges != 0) {
            throw new IllegalArgumentException(
                    "An empty graph cannot contain edges"
            );
        }
    }
}