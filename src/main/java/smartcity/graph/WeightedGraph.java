package smartcity.graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeightedGraph {
    public record Edge(int from, int to, long weight) {}

    private final int vertexCount;
    private final boolean directed;
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Edge>> adjacency;

    public WeightedGraph(int vertexCount, boolean directed) {
        if (vertexCount < 0) throw new IllegalArgumentException("Vertex count cannot be negative");
        this.vertexCount = vertexCount;
        this.directed = directed;
        this.adjacency = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) adjacency.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, long weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed && from != to) adjacency.get(to).add(new Edge(to, from, weight));
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException("Vertex " + vertex + " is outside [0, " + vertexCount + ")");
        }
    }

    public int vertexCount() { return vertexCount; }
    public boolean isDirected() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> outgoing(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }
}