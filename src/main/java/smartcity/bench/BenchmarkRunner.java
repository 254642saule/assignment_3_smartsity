package smartcity.bench;

import smartcity.algorithms.AlgorithmStats;
import smartcity.algorithms.Dijkstra;
import smartcity.algorithms.MstAlgorithms;
import smartcity.algorithms.StronglyConnectedComponents;
import smartcity.algorithms.TopologicalSort;
import smartcity.graph.WeightedGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class BenchmarkRunner {
    private static final int REPEATS = 5;
    private static final int WARMUP_RUNS = 10;
    private static final long SEED = 20261015L;

    private record Measurement(double averageNanos, AlgorithmStats stats) {}

    private record Row(
            String algorithm,
            String graphType,
            int vertices,
            int edges,
            double averageNanos,
            AlgorithmStats stats
    ) {}

    private BenchmarkRunner() {}

    public static void run(Path csvPath, Path plotsDirectory) throws IOException {
        int[][] configurations = {
                {10, 30},
                {100, 500},
                {1_000, 5_000}
        };

        List<Row> rows = new ArrayList<>();
        List<SvgPlotWriter.Point> plotPoints = new ArrayList<>();

        for (int[] config : configurations) {
            int vertices = config[0];
            int edges = config[1];

            WeightedGraph undirected =
                    GraphGenerator.connectedUndirected(vertices, edges, SEED + vertices);

            add(rows, plotPoints, "Kruskal", "connected undirected",
                    vertices, edges,
                    () -> MstAlgorithms.kruskal(undirected).stats());

            add(rows, plotPoints, "Prim", "connected undirected",
                    vertices, edges,
                    () -> MstAlgorithms.prim(undirected).stats());

            add(rows, plotPoints, "Dijkstra", "connected undirected",
                    vertices, edges,
                    () -> Dijkstra.shortestPaths(undirected, 0).stats());

            WeightedGraph sccGraph =
                    GraphGenerator.stronglyConnectedDirected(
                            vertices, edges, SEED + 10_000L + vertices);

            add(rows, plotPoints, "SCC (Kosaraju)", "directed strongly connected",
                    vertices, edges,
                    () -> StronglyConnectedComponents.kosaraju(sccGraph).stats());

            WeightedGraph dag =
                    GraphGenerator.dag(vertices, edges, SEED + 20_000L + vertices);

            add(rows, plotPoints, "Topological (DAG)", "directed DAG",
                    vertices, edges,
                    () -> TopologicalSort.sort(dag).stats());

            WeightedGraph cyclic =
                    GraphGenerator.cyclicDirected(
                            vertices, edges, SEED + 30_000L + vertices);

            add(rows, plotPoints, "Topological (cyclic)", "directed cyclic",
                    vertices, edges,
                    () -> TopologicalSort.sort(cyclic).stats());
        }

        writeCsv(csvPath, rows);

        SvgPlotWriter.write(
                plotsDirectory.resolve("time-vs-vertices.svg"),
                "Algorithm execution time by graph size",
                "Vertices",
                false,
                plotPoints
        );

        SvgPlotWriter.write(
                plotsDirectory.resolve("time-vs-edges.svg"),
                "Algorithm execution time by edge count",
                "Edges",
                true,
                plotPoints
        );

        System.out.printf(
                "Wrote %d benchmark rows (%d repetitions each) to %s%n",
                rows.size(), REPEATS, csvPath
        );
        System.out.println("Wrote SVG plots to " + plotsDirectory);
    }

    private static void add(
            List<Row> rows,
            List<SvgPlotWriter.Point> plotPoints,
            String algorithm,
            String graphType,
            int vertices,
            int edges,
            Supplier<AlgorithmStats> operation
    ) {
        Measurement measurement = measure(operation);

        rows.add(new Row(
                algorithm,
                graphType,
                vertices,
                edges,
                measurement.averageNanos(),
                measurement.stats()
        ));

        plotPoints.add(new SvgPlotWriter.Point(
                algorithm,
                vertices,
                edges,
                measurement.averageNanos()
        ));

        System.out.printf(
                "%-24s V=%4d E=%5d avg=%10.0f ns%n",
                algorithm,
                vertices,
                edges,
                measurement.averageNanos()
        );
    }

    private static Measurement measure(Supplier<AlgorithmStats> operation) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            operation.get();
        }

        long elapsedNanos = 0;
        AlgorithmStats latestStats = null;

        for (int i = 0; i < REPEATS; i++) {
            long start = System.nanoTime();
            latestStats = operation.get();
            elapsedNanos += System.nanoTime() - start;
        }

        double averageNanos = (double) elapsedNanos / REPEATS;
        return new Measurement(averageNanos, latestStats);
    }

    private static void writeCsv(Path path, List<Row> rows) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder csv = new StringBuilder(
                "algorithm,graph_type,vertices,edges,repeats,avg_time_ns,"
                        + "edge_examinations,comparisons,dsu_find_calls,"
                        + "dsu_union_calls,heap_polls,heap_offers,relaxations\n"
        );

        for (Row row : rows) {
            AlgorithmStats stats = row.stats();

            csv.append(row.algorithm()).append(',')
                    .append(row.graphType()).append(',')
                    .append(row.vertices()).append(',')
                    .append(row.edges()).append(',')
                    .append(REPEATS).append(',')
                    .append(String.format(Locale.ROOT, "%.1f", row.averageNanos())).append(',')
                    .append(stats.edgeExaminations).append(',')
                    .append(stats.comparisons).append(',')
                    .append(stats.dsuFindCalls).append(',')
                    .append(stats.dsuUnionCalls).append(',')
                    .append(stats.heapPolls).append(',')
                    .append(stats.heapOffers).append(',')
                    .append(stats.relaxations).append('\n');
        }

        Files.writeString(path, csv.toString());
    }
}