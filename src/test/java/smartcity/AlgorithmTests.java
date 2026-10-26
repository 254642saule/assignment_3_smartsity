package smartcity;
import smartcity.algorithms.Dijkstra;
import smartcity.algorithms.MstAlgorithms;
import smartcity.algorithms.StronglyConnectedComponents;
import smartcity.algorithms.TopologicalSort;
import smartcity.bench.GraphGenerator;
import smartcity.graph.WeightedGraph;
import java.util.HashSet;
import java.util.Set;
public final class AlgorithmTests {
    private static int passed;
    private AlgorithmTests() {}
    public static void main(String[] args) {
        testSingleVertexMst();
        testDisconnectedMst();
        testMultipleMstsAndDuplicateWeights();
        testDagOrder();
        testCycleDetection();
        testSccPartition();
        testDijkstraPathsAndUnreachableVertex();
        testDijkstraRejectsNegativeWeights();
        testGeneratedGraphs();
        System.out.println("All " + passed + " checks passed.");
    }
    private static void testSingleVertexMst() {
        WeightedGraph graph = new WeightedGraph(1, false);
        check(MstAlgorithms.kruskal(graph).connected() && MstAlgorithms.kruskal(graph).
                totalWeight() == 0, "Kruskal handles one vertex");
        check(MstAlgorithms.prim(graph).connected() && MstAlgorithms.prim(graph).totalWeight() ==
                0, "Prim handles one vertex");
    }
    private static void testDisconnectedMst() {
        WeightedGraph graph = new WeightedGraph(3, false);
        graph.addEdge(0, 1, 7);
        var k = MstAlgorithms.kruskal(graph);
        var p = MstAlgorithms.prim(graph);
        check(!k.connected() && !p.connected(), "Disconnected graph is reported");
        check(k.totalWeight() == 7 && p.totalWeight() == 7, "Disconnected input returns a minimum
                spanning forest");
    }
    private static void testMultipleMstsAndDuplicateWeights() {
        WeightedGraph graph = new WeightedGraph(4, false);
        graph.addEdge(0, 1, 1); graph.addEdge(0, 2, 1); graph.addEdge(0, 3, 1);
        graph.addEdge(1, 2, 1); graph.addEdge(1, 3, 1); graph.addEdge(2, 3, 1);
        var k = MstAlgorithms.kruskal(graph);
        var p = MstAlgorithms.prim(graph);
        check(k.connected() && p.connected(), "Equal-weight graph is connected");
        check(k.totalWeight() == 3 && p.totalWeight() == 3, "Kruskal and Prim agree when multiple
                MSTs exist");
    }
    private static void testDagOrder() {
        WeightedGraph dag = new WeightedGraph(5, true);
        dag.addEdge(0, 2, 1); dag.addEdge(1, 2, 1); dag.addEdge(2, 3, 1); dag.addEdge(2, 4, 1);
        var result = TopologicalSort.sort(dag);
        check(result.acyclic() && result.order().size() == 5, "DAG receives a full topological
                ordering");
        int[] position = new int[5];
        for (int i = 0; i < result.order().size(); i++) position[result.order().get(i)] = i;
        check(dag.edges().stream().allMatch(edge -> position[edge.from()] < position[edge.to()]),
                "Returned ordering respects every edge");
    }
    private static void testCycleDetection() {
        WeightedGraph graph = new WeightedGraph(3, true);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 1); graph.addEdge(2, 0, 1);
        check(!TopologicalSort.sort(graph).acyclic(), "Directed cycle prevents topological
                ordering");
    }
    private static void testSccPartition() {
        WeightedGraph graph = new WeightedGraph(6, true);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 1); graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1); graph.addEdge(3, 4, 1); graph.addEdge(4, 3, 1); graph.addEdge(4,
                5, 1);
        var result = StronglyConnectedComponents.kosaraju(graph);
        Set<Set<Integer>> expected = Set.of(Set.of(0, 1, 2), Set.of(3, 4), Set.of(5));
        Set<Set<Integer>> actual = new HashSet<>();
        result.components().forEach(component -> actual.add(Set.copyOf(component)));
        check(actual.equals(expected), "Kosaraju identifies all SCCs");
        check(result.components().stream().mapToInt(java.util.List::size).sum() == graph.
                vertexCount(), "Every vertex belongs to one SCC");
    }
    private static void testDijkstraPathsAndUnreachableVertex() {
        WeightedGraph graph = new WeightedGraph(5, false);
        graph.addEdge(0, 1, 4); graph.addEdge(0, 2, 1); graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1); graph.addEdge(2, 3, 8);
        var result = Dijkstra.shortestPaths(graph, 0);
        check(result.distanceTo(3) == 4, "Dijkstra returns the shortest distance");
        check(result.pathTo(3).equals(java.util.List.of(0, 2, 1, 3)), "Dijkstra reconstructs the
                shortest path");
                check(result.distanceTo(4) == Long.MAX_VALUE && result.pathTo(4).isEmpty(), "Unreachable
                        vertex is represented explicitly");
    }
    private static void testDijkstraRejectsNegativeWeights() {
        WeightedGraph graph = new WeightedGraph(2, true);
        graph.addEdge(0, 1, -1);
        boolean rejected = false;
        try { Dijkstra.shortestPaths(graph, 0); } catch (IllegalArgumentException expected) {
            rejected = true; }
        check(rejected, "Dijkstra rejects negative edge weights");
    }
    private static void testGeneratedGraphs() {
        int[][] configurations = {{10, 30}, {100, 500}, {1_000, 5_000}};
        for (int[] c : configurations) {
            WeightedGraph undirected = GraphGenerator.connectedUndirected(c[0], c[1], 99 + c[0]);
            var k = MstAlgorithms.kruskal(undirected);
            var p = MstAlgorithms.prim(undirected);
            check(k.connected() && p.connected() && k.totalWeight() == p.totalWeight(), "
                    Generated MST instance agrees at V=" + c[0]);
                    check(TopologicalSort.sort(GraphGenerator.dag(c[0], c[1], 199 + c[0])).acyclic(), "
                            Generated DAG is acyclic at V=" + c[0]);
                            check(!TopologicalSort.sort(GraphGenerator.cyclicDirected(c[0], c[1], 299 + c[0])).
                                    acyclic(), "Generated cyclic graph is detected at V=" + c[0]);
        }
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED: " + message);
        passed++;
        System.out.println("PASS: " + message);
    }
}
