package smartcity;
import smartcity.algorithms.Dijkstra;
import smartcity.algorithms.MstAlgorithms;
import smartcity.bench.BenchmarkRunner;
import smartcity.bench.GraphGenerator;
import smartcity.graph.WeightedGraph;
import java.nio.file.Path;
import java.util.Arrays;
public final class Main {
    private Main() {}
    public static void main(String[] args) throws Exception {
        String command = args.length == 0 ? "demo" : args[0].toLowerCase();
        switch (command) {
            case "demo" -> demo();
            case "benchmark" -> BenchmarkRunner.run(Path.of("results/tables/benchmark-results.csv"), Path.
                    of("results/plots"));
            case "test" -> smartcity.AlgorithmTests.main(new String[0]);
            default -> {
                System.out.println("Usage: ./run.sh [demo|test|benchmark]");
                System.exit(2);
            }
        }
    }
    private static void demo() {
        WeightedGraph graph = new WeightedGraph(5, false);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 5);
        graph.addEdge(2, 3, 8);
        graph.addEdge(3, 4, 3);
        var kruskal = MstAlgorithms.kruskal(graph);
        var prim = MstAlgorithms.prim(graph);
        var shortest = Dijkstra.shortestPaths(graph, 0);
        System.out.println("Smart City Infrastructure — sample run");
        System.out.printf("Kruskal MST: weight=%d, connected=%s%n", kruskal.totalWeight(), kruskal.
                connected());
        System.out.printf("Prim MST: weight=%d, connected=%s%n", prim.totalWeight(), prim.connected());
        System.out.println("Dijkstra distances from district 0: " + Arrays.toString(shortest.distances()))
        ;
        System.out.println("Path 0 -> 4: " + shortest.pathTo(4));
        System.out.println("Run ./run.sh benchmark to reproduce measurements and plots.");
    }
}