package smartcity.bench;
import smartcity.graph.WeightedGraph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
public final class GraphGenerator {
    private GraphGenerator() {}
    public static WeightedGraph connectedUndirected(int vertices, int edges, long seed) {
        validate(vertices, edges);
        if (edges < Math.max(0, vertices - 1)) throw new IllegalArgumentException("Connected
                graph needs at least V-1 edges");
        long maximum = (long) vertices * (vertices - 1) / 2;
        if (edges > maximum) throw new IllegalArgumentException("Too many unique undirected edges
                ");
                Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, false);
        Set<Long> pairs = new HashSet<>();
        for (int v = 1; v < vertices; v++) addUndirected(graph, pairs, v - 1, v, random);
        while (pairs.size() < edges) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);
            if (a != b) addUndirected(graph, pairs, a, b, random);
        }
        return graph;
    }
    public static WeightedGraph dag(int vertices, int edges, long seed) {
        validate(vertices, edges);
        if (edges < Math.max(0, vertices - 1)) throw new IllegalArgumentException("This DAG
                generator needs at least V-1 edges");
        long maximum = (long) vertices * (vertices - 1) / 2;
        if (edges > maximum) throw new IllegalArgumentException("DAG cannot contain that many
                unique edges");
                Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Set<Long> pairs = new HashSet<>();
        for (int v = 1; v < vertices; v++) addDirected(graph, pairs, v - 1, v);
        while (pairs.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from < to) addDirected(graph, pairs, from, to);
        }
        return graph;
    }
    public static WeightedGraph cyclicDirected(int vertices, int edges, long seed) {
        validate(vertices, edges);
        if (vertices == 0 && edges != 0) throw new IllegalArgumentException("Empty graph cannot
                have edges");
        if (vertices > 1 && edges < vertices) throw new IllegalArgumentException("Cyclic
                construction needs at least V edges");
        if (vertices == 1 && edges == 0) throw new IllegalArgumentException("A cyclic one-vertex
                graph needs a self-loop");
        long maximum = vertices <= 1 ? 1 : (long) (vertices - 1) * (vertices - 1) + (vertices ==
                2 ? 1 : 0);
        if (edges > maximum) throw new IllegalArgumentException("Too many unique directed edges
        for this construction");
        Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Set<Long> pairs = new HashSet<>();
        if (vertices == 1) {
            if (edges == 1) addDirected(graph, pairs, 0, 0);
            else if (edges != 0) throw new IllegalArgumentException("A one-vertex graph supports
                    at most one unique edge");
            return graph;
        }
        for (int v = 1; v < vertices; v++) addDirected(graph, pairs, v - 1, v);
        addDirected(graph, pairs, vertices - 1, vertices - 2);
        while (pairs.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from != to && to != 0) addDirected(graph, pairs, from, to);
        }
        return graph;
    }
    public static WeightedGraph stronglyConnectedDirected(int vertices, int edges, long seed) {
        validate(vertices, edges);
        if (vertices > 1 && edges < vertices) throw new IllegalArgumentException("Ring requires
                at least V directed edges");
        long maximum = (long) vertices * Math.max(0, vertices - 1);
        if (edges > maximum) throw new IllegalArgumentException("Too many unique directed edges")
                ;
        Random random = new Random(seed);
        WeightedGraph graph = new WeightedGraph(vertices, true);
        Set<Long> pairs = new HashSet<>();
        if (vertices == 1) {
            if (edges == 1) addDirected(graph, pairs, 0, 0);
            else if (edges != 0) throw new IllegalArgumentException("A one-vertex graph supports
                    at most one unique edge");
            return graph;
        }
        for (int v = 0; v < vertices; v++) addDirected(graph, pairs, v, (v + 1) % vertices);
        while (pairs.size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from != to) addDirected(graph, pairs, from, to);
        }
        return graph;
    }
    private static void addUndirected(WeightedGraph graph, Set<Long> pairs, int a, int b, Random
            random) {
        int lo = Math.min(a, b);
        int hi = Math.max(a, b);
        long key = pairKey(lo, hi);
        if (pairs.add(key)) graph.addEdge(lo, hi, 1 + random.nextInt(10_000));
    }
    private static void addDirected(WeightedGraph graph, Set<Long> pairs, int from, int to) {
        if (pairs.add(pairKey(from, to))) graph.addEdge(from, to, 1);
    }
    private static long pairKey(int from, int to) { return ((long) from << 32) ^ (to & 0
        xffffffffL); }
    private static void validate(int vertices, int edges) {
        if (vertices < 0 || edges < 0) throw new IllegalArgumentException("V and E must be nonnegative");
        if (vertices == 0 && edges != 0) throw new IllegalArgumentException("Empty graph cannot
                have edges");
    }
}
