package org.example.graph.scc;


import org.example.Graph;
import org.example.Metrics;

import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components.
 * Time complexity: O(V + E)
 */
public class TarjanSCC {
    private final Graph graph;
    private final Metrics metrics;

    private int[] ids;
    private int[] low;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int id;
    private int sccCount;
    private List<List<Integer>> sccs;

    public TarjanSCC(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Finds all strongly connected components.
     * @return list of SCCs, each SCC is a list of vertex indices
     */
    public List<List<Integer>> findSCCs() {
        int n = graph.getVertexCount();
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();

        Arrays.fill(ids, -1);
        id = 0;
        sccCount = 0;

        metrics.startTimer();

        // Run DFS from all unvisited nodes
        for (int u = 0; u < n; u++) {
            if (ids[u] == -1) {
                dfs(u);
            }
        }

        metrics.stopTimer();

        return sccs;
    }

    private void dfs(int u) {
        ids[u] = low[u] = id++;
        stack.push(u);
        onStack[u] = true;
        metrics.incrementDfsVisits();

        // Explore neighbors
        for (Graph.Edge edge : graph.getNeighbors(u)) {
            int v = edge.to;
            metrics.incrementEdgesExplored();

            if (ids[v] == -1) {
                // Not visited
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // On current path (back edge)
                low[u] = Math.min(low[u], ids[v]);
            }
        }

        // Found SCC root
        if (ids[u] == low[u]) {
            List<Integer> scc = new ArrayList<>();
            while (true) {
                int v = stack.pop();
                onStack[v] = false;
                scc.add(v);
                if (v == u) break;
            }
            sccs.add(scc);
            sccCount++;
        }
    }

    /**
     * Builds condensation graph where each SCC becomes a single vertex.
     * @param sccs list of strongly connected components
     * @return condensation graph
     */
    public Graph buildCondensationGraph(List<List<Integer>> sccs) {
        int n = graph.getVertexCount();

        // Map each vertex to its SCC index
        int[] vertexToScc = new int[n];
        for (int i = 0; i < sccs.size(); i++) {
            for (int v : sccs.get(i)) {
                vertexToScc[v] = i;
            }
        }

        // Create condensation graph
        Graph condensation = new Graph(sccs.size(), true);
        Set<String> addedEdges = new HashSet<>();

        // Add edges between different SCCs
        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getNeighbors(u)) {
                int v = edge.to;
                int sccU = vertexToScc[u];
                int sccV = vertexToScc[v];

                if (sccU != sccV) {
                    String edgeKey = sccU + "->" + sccV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(sccU, sccV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    public int getSccCount() {
        return sccCount;
    }
}
