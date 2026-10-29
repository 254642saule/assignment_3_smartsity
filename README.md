# Smart City Infrastructure and Route Planning

## Overview

This project models a city's infrastructure as a weighted graph. Vertices represent city districts or locations, and edges represent roads or other infrastructure connections. Edge weights represent construction costs or travel distances.

The application implements graph algorithms for infrastructure planning, connectivity analysis, and route planning. It compares minimum spanning tree algorithms, analyzes directed graph connectivity, and finds shortest paths from a selected source vertex.

## Implementation

The graph is represented using an adjacency list. `WeightedGraph` stores vertices and weighted edges and supports directed and undirected graphs.

The project includes:

- **Disjoint Set Union (DSU):** supports `find` and `union` operations with path compression and union by rank or size.
- **Kruskal's algorithm:** sorts edges by weight and uses DSU to avoid cycles while building a minimum spanning tree.
- **Prim's algorithm:** uses a priority queue to add the lowest-weight edge connecting the current tree to another vertex.
- **Topological sort:** uses Kahn's algorithm to produce an ordering for a directed acyclic graph and detect cycles.
- **Strongly connected components:** uses Kosaraju's algorithm to group mutually reachable vertices.
- **Dijkstra's algorithm:** calculates shortest distances and paths from a source vertex. It requires non-negative edge weights.
- **Benchmark tools:** generate graphs, record measurements in CSV, and create SVG plots.

## Complexity Analysis

The analysis assumes an adjacency-list graph representation and a binary heap for priority queues.

| Algorithm or operation | Time complexity | Additional space |
|---|---:|---:|
| DSU `find` and `union` | Amortized `O(α(V))` per operation with path compression and union by rank or size | `O(V)` |
| Kruskal | `O(E log E)` | `O(V + E)` |
| Prim | `O(E log V)` | `O(V + E)` |
| Topological sort (Kahn's algorithm) | `O(V + E)` | `O(V)` |
| Kosaraju's SCC algorithm | `O(V + E)` | `O(V + E)` |
| Dijkstra with a binary heap | `O((V + E) log V)` | `O(V + E)` |

Here, `V` is the number of vertices, `E` is the number of edges, and `α(V)` is the inverse Ackermann function, which grows very slowly.

Kruskal's runtime is mainly affected by sorting the edges. Prim's runtime depends on priority queue operations while processing vertices and edges. DSU makes connectivity checks efficient in Kruskal's algorithm. Topological sort and Kosaraju's algorithm process each vertex and edge a constant number of times. Dijkstra's runtime depends on the number of reachable vertices and edges and on priority queue operations.

## Experimental Setup

The experiments use the graph sizes specified in the assignment:

| Graph size | Vertices (`V`) | Edges (`E`) |
|---|---:|---:|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1,000 | 5,000 |

Graphs should be generated with a fixed random seed to make the experiments reproducible. Undirected graphs used for MST experiments must be connected. For directed graph experiments, document how edges are generated and whether cycles are possible.

Each experiment should be repeated five times. Execution time should be measured with `System.nanoTime()`. Report average execution time and relevant operation counts, such as edge examinations, comparisons, and DSU operations.

Before submission, make sure the following details match the actual benchmark implementation:

- Random seed: **[enter the seed used by the program]**
- Directed graph generation: **[describe how edges are generated and whether cycles are possible]**
- Connected undirected graph generation: **[describe how connectivity is guaranteed]**
- Number of repetitions: **[confirm the number used by the program]**
- Timing method: `System.nanoTime()`

## Results

### Benchmark measurements

Add the actual measurements from `BenchmarkRunner` after running the benchmark. Do not use estimated values.

| Graph size | Vertices | Edges | Kruskal average time (ns) | Prim average time (ns) | Dijkstra average time (ns) | SCC average time (ns) | Topological sort average time (ns) |
|---|---:|---:|---:|---:|---:|---:|---:|
| Small | 10 | 30 | [result] | [result] | [result] | [result] | [result] |
| Medium | 100 | 500 | [result] | [result] | [result] | [result] | [result] |
| Large | 1,000 | 5,000 | [result] | [result] | [result] | [result] | [result] |

If operation counts are available, include them here as well:

| Graph size | Algorithm | Comparisons | Edge examinations | DSU operations |
|---|---|---:|---:|---:|
| Small | [algorithm] | [result] | [result] | [result] |
| Medium | [algorithm] | [result] | [result] | [result] |
| Large | [algorithm] | [result] | [result] | [result] |

Expected benchmark output files:

- [Benchmark results CSV](results/tables/benchmark-results.csv)
- [Execution time vs. vertices](results/plots/time-vs-vertices.svg)
- [Execution time vs. edges](results/plots/time-vs-edges.svg)

### Test results

All 24 checks passed:

```text
PASS: Kruskal handles one vertex
PASS: Prim handles one vertex
PASS: Kruskal weight is zero
PASS: Prim weight is zero
PASS: Kruskal reports disconnected graph
PASS: Prim reports disconnected graph
PASS: Kruskal returns forest weight
PASS: Prim returns forest weight
PASS: Kruskal handles equal weights
PASS: Prim handles equal weights
PASS: Kruskal finds MST weight
PASS: Prim finds MST weight
PASS: Topological sort accepts a DAG
PASS: Topological sort includes every vertex
PASS: Topological order respects graph edges
PASS: Topological sort detects a cycle
PASS: Kosaraju finds SCCs
PASS: Dijkstra finds shortest distance
PASS: Dijkstra returns shortest path
PASS: Dijkstra marks unreachable vertex
PASS: Dijkstra rejects negative weights
PASS: Kruskal and Prim agree for V=10
PASS: Kruskal and Prim agree for V=100
PASS: Kruskal and Prim agree for V=1000
All 24 checks passed.
