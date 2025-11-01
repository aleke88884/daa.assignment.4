# Smart City/Campus Task Scheduler - Assignment 4

## Overview
This project implements graph algorithms for scheduling city-service tasks with dependencies. It handles cyclic dependencies through Strongly Connected Components (SCC) detection and computes optimal execution orders using topological sorting and shortest/longest path algorithms on DAGs.

## Features

### 1. Strongly Connected Components (Tarjan's Algorithm)
- Detects cycles in task dependencies
- Compresses cyclic dependencies into single components
- O(V + E) time complexity
- Builds condensation graph (DAG of components)

### 2. Topological Sorting
- Kahn's algorithm (BFS-based) for topological ordering
- DFS-based alternative implementation
- Derives valid execution order for tasks
- Detects cycles in graphs

### 3. Shortest & Longest Paths in DAG
- Single-source shortest paths using topological ordering
- Critical path (longest path) computation
- Path reconstruction from source to any vertex
- O(V + E) time complexity

## Project Structure

```
.
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── graph/
│   │       │   ├── Graph.java          # Main graph data structure
│   │       │   ├── Metrics.java        # Performance metrics tracking
│   │       │   ├── scc/
│   │       │   │   └── TarjanSCC.java  # SCC detection
│   │       │   ├── topo/
│   │       │   │   └── TopologicalSort.java
│   │       │   └── dagsp/
│   │       │       └── DAGShortestPath.java
│   │       ├── Main.java               # Main application
│   │       └── DatasetGenerator.java   # Test data generator
│   └── test/
│       └── java/
│           └── GraphAlgorithmsTest.java # JUnit tests
├── data/                               # Generated test datasets
├── pom.xml                            # Maven configuration
└── README.md                          # This file
```

## Dependencies

- Java 11 or higher
- JUnit 5 (for testing)
- Gson 2.10.1 (for JSON parsing)

## Build & Run

### Using Maven

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Generate datasets
mvn exec:java -Dexec.mainClass="DatasetGenerator"

# Run main application
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="data/tasks.json"
```

### Using Java directly

```bash
# Compile
javac -cp "lib/*" -d bin src/main/java/**/*.java

# Generate datasets
java -cp "bin:lib/*" DatasetGenerator

# Run analysis
java -cp "bin:lib/*" Main data/tasks.json
```

## Dataset Format

JSON format for task graphs:

```json
{
  "directed": true,
  "n": 8,
  "edges": [
    {"u": 0, "v": 1, "w": 3},
    {"u": 1, "v": 2, "w": 2}
  ],
  "source": 0,
  "weight_model": "edge"
}
```

- `n`: Number of vertices
- `edges`: Array of directed edges with weights
- `source`: Starting vertex for shortest path queries
- `weight_model`: "edge" (edge weights) or "node" (node durations)

## Generated Datasets

### Small (6-10 nodes) - 3 datasets
- `small_dag_1.json`: 6 nodes, simple DAG
- `small_cyclic_1.json`: 8 nodes, contains cycles
- `small_dag_2.json`: 10 nodes, sparse DAG

### Medium (10-20 nodes) - 3 datasets
- `medium_mixed_1.json`: 12 nodes, mixed structure
- `medium_dag_1.json`: 15 nodes, DAG
- `medium_dense_1.json`: 18 nodes, dense with SCCs

### Large (20-50 nodes) - 3 datasets
- `large_sparse_1.json`: 25 nodes, sparse DAG
- `large_cyclic_1.json`: 35 nodes, multiple SCCs
- `large_dense_1.json`: 50 nodes, dense graph

## Algorithm Details

### Tarjan's SCC Algorithm
- Uses DFS with low-link values
- Maintains stack of vertices in current path
- Identifies SCC roots when `low[u] == id[u]`
- Single pass through the graph

### Topological Sort (Kahn's Algorithm)
- Maintains in-degree count for each vertex
- Processes vertices with in-degree 0
- Returns null if cycle detected
- O(V + E) using queue

### DAG Shortest/Longest Paths
- Processes vertices in topological order
- Relaxes edges for shortest path (min operation)
- Maximizes distances for longest path
- Linear time O(V + E) due to topological preprocessing

## Performance Metrics

The implementation tracks:
- **DFS visits**: Number of vertex visits during DFS
- **Edges explored**: Number of edges traversed
- **Relaxations**: Number of distance updates
- **Pushes/Pops**: Queue operations in Kahn's algorithm
- **Execution time**: Nanosecond precision timing

## Testing

JUnit tests cover:
- Simple DAG structures
- Cyclic graphs
- Mixed structures (some cycles, some DAG portions)
- Edge cases (empty graph, single vertex)
- Path reconstruction
- Topological ordering correctness
- Shortest and longest path accuracy

Run tests: `mvn test`

## Example Output

```
=== Smart City/Campus Task Scheduler ===

Processing file: data/tasks.json
Graph loaded: 8 vertices
Weight model: edge

=== Step 1: Finding Strongly Connected Components ===
Found 3 SCCs:
  SCC 0: [7] (size: 1)
  SCC 1: [4, 5, 6] (size: 3)
  SCC 2: [0, 1, 2, 3] (size: 4)
Metrics: time=0.123ms, dfsVisits=8, edges=7

=== Step 2: Building Condensation DAG ===
Condensation graph has 3 vertices

=== Step 3: Topological Ordering ===
Topological order of SCCs: [2, 1, 0]
Derived task execution order: [0, 1, 2, 3, 4, 5, 6, 7]
Metrics: time=0.045ms, pushes=1, pops=3

=== Step 4: Shortest Paths in Condensation DAG ===
Shortest distances from SCC 0:
  To SCC 0: 0
  To SCC 1: 8
  To SCC 2: 14
Metrics: time=0.032ms, relaxations=2

=== Step 5: Longest Path (Critical Path) ===
Critical path length: 14
Critical path: [0, 1, 2]
Metrics: time=0.028ms, relaxations=2
```

## Complexity Analysis

| Algorithm | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| Tarjan SCC | O(V + E) | O(V) |
| Topological Sort | O(V + E) | O(V) |
| DAG Shortest Path | O(V + E) | O(V) |
| DAG Longest Path | O(V + E) | O(V) |

## Practical Recommendations

1. **Use SCC detection** when dependencies may contain cycles
2. **Topological sorting** is essential for task scheduling in DAGs
3. **Longest path** identifies critical bottlenecks in project scheduling
4. **Condensation graph** simplifies analysis of complex dependency structures
5. For large graphs (>1000 nodes), consider parallel processing for independent SCCs

## Authors

Assignment 4 - Graph Algorithms
Course: Data Structures & Algorithms

## License

Educational use only.