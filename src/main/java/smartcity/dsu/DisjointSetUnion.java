package smartcity.dsu;
public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private int components;
    private long findCalls;
    private long unionCalls;
    public DisjointSetUnion(int count) {
        if (count < 0) throw new IllegalArgumentException("Count cannot be negative");
        parent = new int[count];
        size = new int[count];
        components = count;
        for (int i = 0; i < count; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int item) {
        checkItem(item);
        findCalls++;
        int root = item;
        while (root != parent[root]) root = parent[root];
        while (item != root) {
            int next = parent[item];
            parent[item] = root;
            item = next;
        }
        return root;
    }

    public boolean union(int first, int second) {
        unionCalls++;
        int rootA = find(first);
        int rootB = find(second);
        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) {
            int swap = rootA;
            rootA = rootB;
            rootB = swap;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        components--;
        return true;
    }

    private void checkItem(int item) {
        if (item < 0 || item >= parent.length) throw new IndexOutOfBoundsException("Invalid DSU item: " + item);
    }

    public int components() { return components; }
    public long findCalls() { return findCalls; }
    public long unionCalls() { return unionCalls; }
}