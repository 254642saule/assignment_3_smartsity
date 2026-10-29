package smartcity;

import smartcity.algorithms.Dijkstra;
import smartcity.algorithms.MstAlgorithms;
import smartcity.algorithms.StronglyConnectedComponents;
import smartcity.algorithms.TopologicalSort;
import smartcity.bench.GraphGenerator;
import smartcity.graph.WeightedGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AlgorithmTests {
    private static int passed;

    private AlgorithmTests() {}

    public static void main(String[] args) {
        testSingleVertexMst();
        testDisconnectedMst();
        testEqualWeightMst();
        testDagOrder();
        testCycleDetection();
        testSccPartition();
        testDijkstraPaths();
        testDijkstraRejectsNegativeWeights();
        testGeneratedGraphs();

        System.out.println("All " + passed + " checks passed.");
    }

    private static void testSingleVertexMst() {
        WeightedGraph graph = new WeightedGraph(1, false);

        MstAlgorithms.MstResult kruskal =
                MstAlgorithms.kruskal(graph);
        MstAlgorithms.MstResult prim =
                MstAlgorithms.prim(graph);

        check(kruskal.connected(), "Kruskal handles one vertex");
        check(prim.connected(), "Prim handles one vertex");
        check(kruskal.totalWeight() == 0, "Kruskal weight is zero");
        check(prim.totalWeight() == 0, "Prim weight is zero");
    }

    private static void testDisconnectedMst() {
        WeightedGraph graph = new WeightedGraph(3, false);
        graph.addEdge(0, 1, 7);

        MstAlgorithms.MstResult kruskal =
                MstAlgorithms.kruskal(graph);
        MstAlgorithms.MstResult prim =
                MstAlgorithms.prim(graph);

        check(!kruskal.connected(), "Kruskal reports disconnected graph");
        check(!prim.connected(), "Prim reports disconnected graph");
        check(kruskal.totalWeight() == 7, "Kruskal returns forest weight");
        check(prim.totalWeight() == 7, "Prim returns forest weight");
    }

    private static void testEqualWeightMst() {
        WeightedGraph graph = new WeightedGraph(4, false);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(0, 3, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        MstAlgorithms.MstResult kruskal =
                MstAlgorithms.kruskal(graph);
        MstAlgorithms.MstResult prim =
                MstAlgorithms.prim(graph);

        check(kruskal.connected(), "Kruskal handles equal weights");
        check(prim.connected(), "Prim handles equal weights");
        check(kruskal.totalWeight() == 3, "Kruskal finds MST weight");
        check(prim.totalWeight() == 3, "Prim finds MST weight");
    }

    private static void testDagOrder() {
        WeightedGraph graph = new WeightedGraph(5, true);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(2, 4, 1);

        TopologicalSort.Result result = TopologicalSort.sort(graph);

        check(result.acyclic(), "Topological sort accepts a DAG");
        check(result.order().size() == 5, "Topological sort includes every vertex");

        int[] position = new int[5];
        for (int index = 0; index < result.order().size(); index++) {
            position[result.order().get(index)] = index;
        }

        boolean correctOrder = graph.edges().stream()
                .allMatch(edge ->
                        position[edge.from()] < position[edge.to()]);

        check(correctOrder, "Topological order respects graph edges");
    }

    private static void testCycleDetection() {
        WeightedGraph graph = new WeightedGraph(3, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        check(
                !TopologicalSort.sort(graph).acyclic(),
                "Topological sort detects a cycle"
        );
    }

    private static void testSccPartition() {
        WeightedGraph graph = new WeightedGraph(6, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        graph.addEdge(4, 5, 1);

        StronglyConnectedComponents.Result result =
                StronglyConnectedComponents.kosaraju(graph);

        Set<Set<Integer>> expected = Set.of(
                Set.of(0, 1, 2),
                Set.of(3, 4),
                Set.of(5)
        );

        Set<Set<Integer>> actual = new HashSet<>();
        for (List<Integer> component : result.components()) {
            actual.add(Set.copyOf(component));
        }

        check(actual.equals(expected), "Kosaraju finds SCCs");
    }

    private static void testDijkstraPaths() {
        WeightedGraph graph = new WeightedGraph(5, false);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 8);

        Dijkstra.Result result = Dijkstra.shortestPaths(graph, 0);

        check(result.distanceTo(3) == 4, "Dijkstra finds shortest distance");
        check(
                result.pathTo(3).equals(List.of(0, 2, 1, 3)),
                "Dijkstra returns shortest path"
        );
        check(
                result.distanceTo(4) == Long.MAX_VALUE,
                "Dijkstra marks unreachable vertex"
        );
    }

    private static void testDijkstraRejectsNegativeWeights() {
        WeightedGraph graph = new WeightedGraph(2, true);
        graph.addEdge(0, 1, -1);

        boolean rejected = false;

        try {
            Dijkstra.shortestPaths(graph, 0);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }

        check(rejected, "Dijkstra rejects negative weights");
    }

    private static void testGeneratedGraphs() {
        int[][] configurations = {
                {10, 30},
                {100, 500},
                {1_000, 5_000}
        };

        for (int[] config : configurations) {
            int vertices = config[0];
            int edges = config[1];

            WeightedGraph graph =
                    GraphGenerator.connectedUndirected(
                            vertices, edges, 100L + vertices
                    );

            MstAlgorithms.MstResult kruskal =
                    MstAlgorithms.kruskal(graph);
            MstAlgorithms.MstResult prim =
                    MstAlgorithms.prim(graph);

            check(
                    kruskal.connected()
                            && prim.connected()
                            && kruskal.totalWeight() == prim.totalWeight(),
                    "Kruskal and Prim agree for V=" + vertices
            );
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }

        passed++;
        System.out.println("PASS: " + message);
    }
}