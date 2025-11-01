package org.example;

import java.util.*;

public class TopologicalSort {
    private final Graph graph;
    private final Metrics metrics;

    public TopologicalSort(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Computes topological order using Kahn's algorithm.
     * @return topological order as list of vertices, or null if cycle detected
     */
    public List<Integer> sortKahn() {
        int n = graph.getVertexCount();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getNeighbors(u)) {
                inDegree[edge.to]++;
            }
        }

        // Queue for vertices with in-degree 0
        Queue<Integer> queue = new LinkedList<>();
        for (int u = 0; u < n; u++) {
            if (inDegree[u] == 0) {
                queue.offer(u);
                metrics.incrementPushes();
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        metrics.startTimer();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.incrementPops();
            topoOrder.add(u);

            // Reduce in-degree for neighbors
            for (Graph.Edge edge : graph.getNeighbors(u)) {
                int v = edge.to;
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementPushes();
                }
            }
        }

        metrics.stopTimer();

        // Check if all vertices are included (no cycle)
        if (topoOrder.size() != n) {
            return null; // Cycle detected
        }

        return topoOrder;
    }

    /**
     * DFS-based topological sort (alternative implementation).
     * @return topological order or null if cycle detected
     */
    public List<Integer> sortDFS() {
        int n = graph.getVertexCount();
        boolean[] visited = new boolean[n];
        boolean[] recStack = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        metrics.startTimer();

        for (int u = 0; u < n; u++) {
            if (!visited[u]) {
                if (hasCycleDFS(u, visited, recStack, stack)) {
                    metrics.stopTimer();
                    return null; // Cycle detected
                }
            }
        }

        metrics.stopTimer();

        // Convert stack to list (reverse order)
        List<Integer> topoOrder = new ArrayList<>();
        while (!stack.isEmpty()) {
            topoOrder.add(stack.pop());
        }

        return topoOrder;
    }

    private boolean hasCycleDFS(int u, boolean[] visited, boolean[] recStack, Stack<Integer> stack) {
        visited[u] = true;
        recStack[u] = true;
        metrics.incrementDfsVisits();

        for (Graph.Edge edge : graph.getNeighbors(u)) {
            int v = edge.to;
            metrics.incrementEdgesExplored();

            if (!visited[v]) {
                if (hasCycleDFS(v, visited, recStack, stack)) {
                    return true;
                }
            } else if (recStack[v]) {
                return true; // Cycle detected
            }
        }

        recStack[u] = false;
        stack.push(u);
        return false;
    }

    /**
     * Derives task order from SCC components in topological order.
     * @param sccOrder topological order of SCCs
     * @param sccs list of SCCs
     * @return flattened task order
     */
    public static List<Integer> deriveTaskOrder(List<Integer> sccOrder, List<List<Integer>> sccs) {
        List<Integer> taskOrder = new ArrayList<>();
        for (int sccIdx : sccOrder) {
            taskOrder.addAll(sccs.get(sccIdx));
        }
        return taskOrder;
    }
}
