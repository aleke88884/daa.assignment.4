package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DAGShortestPath {

    private final Graph graph;
    private final Metrics metrics;

    public static class PathResult {
        public final int[] distances;
        public final int[] parent;
        public final int source;

        public PathResult(int[] distances, int[] parent, int source) {
            this.distances = distances;
            this.parent = parent;
            this.source = source;
        }

        /**
         * Reconstructs path from source to target.
         */
        public List<Integer> reconstructPath(int target) {
            if (distances[target] == Integer.MAX_VALUE || distances[target] == Integer.MIN_VALUE) {
                return null; // No path exists
            }

            List<Integer> path = new ArrayList<>();
            int current = target;

            while (current != -1) {
                path.add(current);
                current = parent[current];
            }

            Collections.reverse(path);
            return path;
        }
    }

    public DAGShortestPath(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Computes shortest paths from source using topological ordering.
     * @param topoOrder topological order of the DAG
     * @param source source vertex
     * @return PathResult with distances and parent pointers
     */
    public PathResult shortestPaths(List<Integer> topoOrder, int source) {
        int n = graph.getVertexCount();
        int[] dist = new int[n];
        int[] parent = new int[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        boolean afterSource = false;
        for (int u : topoOrder) {
            if (u == source) afterSource = true;

            if (!afterSource || dist[u] == Integer.MAX_VALUE) continue;

            // Relax edges
            for (Graph.Edge edge : graph.getNeighbors(u)) {
                int v = edge.to;
                if (dist[u] != Integer.MAX_VALUE && dist[u] + edge.weight < dist[v]) {
                    dist[v] = dist[u] + edge.weight;
                    parent[v] = u;
                    metrics.incrementRelaxations();
                }
            }
        }

        metrics.stopTimer();

        return new PathResult(dist, parent, source);
    }

    /**
     * Computes longest paths (critical path) using topological ordering.
     * Uses negation approach or max-based DP.
     * @param topoOrder topological order of the DAG
     * @param source source vertex
     * @return PathResult with longest distances
     */
    public PathResult longestPaths(List<Integer> topoOrder, int source) {
        int n = graph.getVertexCount();
        int[] dist = new int[n];
        int[] parent = new int[n];

        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        boolean afterSource = false;
        for (int u : topoOrder) {
            if (u == source) afterSource = true;

            if (!afterSource || dist[u] == Integer.MIN_VALUE) continue;

            // Relax edges for maximum distance
            for (Graph.Edge edge : graph.getNeighbors(u)) {
                int v = edge.to;
                if (dist[u] != Integer.MIN_VALUE && dist[u] + edge.weight > dist[v]) {
                    dist[v] = dist[u] + edge.weight;
                    parent[v] = u;
                    metrics.incrementRelaxations();
                }
            }
        }


        metrics.stopTimer();

        return new PathResult(dist, parent, source);
    }

    /**
     * Finds the critical path (longest path in the entire DAG).
     * @param topoOrder topological order
     * @return PathResult with the critical path information
     */
    public PathResult findCriticalPath(List<Integer> topoOrder) {
        int n = graph.getVertexCount();
        int[] dist = new int[n];
        int[] parent = new int[n];

        Arrays.fill(dist, 0);
        Arrays.fill(parent, -1);

        metrics.startTimer();

        // Process all vertices in topological order
        for (int u : topoOrder) {
            for (Graph.Edge edge : graph.getNeighbors(u)) {
                int v = edge.to;
                if (dist[u] + edge.weight > dist[v]) {
                    dist[v] = dist[u] + edge.weight;
                    parent[v] = u;
                    metrics.incrementRelaxations();
                }
            }
        }

        metrics.stopTimer();

        // Find vertex with maximum distance
        int maxDist = Integer.MIN_VALUE;
        int endVertex = -1;
        for (int i = 0; i < n; i++) {
            if (dist[i] > maxDist) {
                maxDist = dist[i];
                endVertex = i;
            }
        }

        return new PathResult(dist, parent, endVertex);
    }
}
