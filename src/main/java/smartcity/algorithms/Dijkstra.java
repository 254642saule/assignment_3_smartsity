package smartcity.algorithms;
// AI-assisted implementation; review and adapt before submission.
import smartcity.graph.WeightedGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
public final class Dijkstra {
    private record QueueItem(int vertex, long distance) {}
    public record Result(long[] distances, int[] previous, AlgorithmStats stats) {
        public long distanceTo(int vertex) { return distances[vertex]; }
        public List<Integer> pathTo(int target) {
            if (distances[target] == Long.MAX_VALUE) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int at = target; at != -1; at = previous[at]) path.add(at);
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }
    private Dijkstra() {}
    public static Result shortestPaths(WeightedGraph graph, int source) {
        if (source < 0 || source >= graph.vertexCount()) throw new IndexOutOfBoundsException("Invalid
                source vertex");
        for (WeightedGraph.Edge edge : graph.edges()) {
            if (edge.weight() < 0) throw new IllegalArgumentException("Dijkstra requires non-negative edge
                    weights");
        }
        int n = graph.vertexCount();
        long[] distance = new long[n];
        int[] previous = new int[n];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(previous, -1);
        distance[source] = 0;
        AlgorithmStats stats = new AlgorithmStats();
        PriorityQueue<QueueItem> queue = new PriorityQueue<>((a, b) -> {
            int byDistance = Long.compare(a.distance(), b.distance());
            return byDistance != 0 ? byDistance : Integer.compare(a.vertex(), b.vertex());
        });
        queue.offer(new QueueItem(source, 0));
        stats.heapOffers++;
        while (!queue.isEmpty()) {
            QueueItem current = queue.poll();
            stats.heapPolls++;
            if (current.distance() != distance[current.vertex()]) continue;
            for (WeightedGraph.Edge edge : graph.outgoing(current.vertex())) {
                stats.edgeExaminations++;
                long candidate = current.distance() + edge.weight();
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    previous[edge.to()] = current.vertex();
                    queue.offer(new QueueItem(edge.to(), candidate));
                    stats.heapOffers++;
                    stats.relaxations++;
                }
            }
        }
        return new Result(distance, previous, stats);
    }
}
