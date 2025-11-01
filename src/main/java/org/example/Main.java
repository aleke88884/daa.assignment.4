package org.example;

import com.google.gson.*;
import org.example.graph.dagscp.DAGShortestPath;
import org.example.graph.scc.TarjanSCC;
import org.example.graph.topo.TopologicalSort;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Main application for Smart City/Campus Scheduling.
 * Works both from IDE (manual input) and terminal (argument mode).
 */
public class Main {

    public static void main(String[] args) {
        String filename;

        // Если аргумент передан — используем его
        if (args.length >= 1) {
            filename = args[0];
        } else {
            // Если запущено из IDE (или без аргументов), попросим вручную ввести путь
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter path to JSON file (e.g., data/tasks.json): ");
            filename = scanner.nextLine().trim();

            if (filename.isEmpty()) {
                System.out.println("No file path provided. Exiting.");
                return;
            }
        }

        try {
            processTaskGraph(filename);
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void processTaskGraph(String filename) throws IOException {
        System.out.println("\n=== Smart City/Campus Task Scheduler ===");
        System.out.println("Processing file: " + filename);

        // Load graph from JSON
        GraphData data = loadGraphFromJson(filename);
        Graph graph = data.graph;

        System.out.println("Graph loaded: " + graph.getVertexCount() + " vertices");
        System.out.println("Weight model: " + data.weightModel);
        System.out.println();

        // Step 1: Find Strongly Connected Components
        System.out.println("=== Step 1: Finding Strongly Connected Components ===");
        Metrics sccMetrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, sccMetrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        System.out.println("Found " + sccs.size() + " SCCs:");
        for (int i = 0; i < sccs.size(); i++) {
            System.out.println("  SCC " + i + ": " + sccs.get(i) + " (size: " + sccs.get(i).size() + ")");
        }
        System.out.println("Metrics: " + sccMetrics);
        System.out.println();

        // Step 2: Build Condensation Graph
        System.out.println("=== Step 2: Building Condensation DAG ===");
        Graph condensation = tarjan.buildCondensationGraph(sccs);
        System.out.println("Condensation graph has " + condensation.getVertexCount() + " vertices");
        System.out.println();

        // Step 3: Topological Sort
        System.out.println("=== Step 3: Topological Ordering ===");
        Metrics topoMetrics = new Metrics();
        TopologicalSort topoSort = new TopologicalSort(condensation, topoMetrics);
        List<Integer> sccOrder = topoSort.sortKahn();

        if (sccOrder == null) {
            System.out.println("ERROR: Cycle detected in condensation graph!");
            return;
        }

        System.out.println("Topological order of SCCs: " + sccOrder);
        List<Integer> taskOrder = TopologicalSort.deriveTaskOrder(sccOrder, sccs);
        System.out.println("Derived task execution order: " + taskOrder);
        System.out.println("Metrics: " + topoMetrics);
        System.out.println();

        // Step 4: Shortest Paths
        System.out.println("=== Step 4: Shortest Paths in Condensation DAG ===");
        int sourceScc = 0;
        if (data.source != -1) {
            for (int i = 0; i < sccs.size(); i++) {
                if (sccs.get(i).contains(data.source)) {
                    sourceScc = i;
                    break;
                }
            }
        }

        Metrics shortestMetrics = new Metrics();
        DAGShortestPath dagSP = new DAGShortestPath(condensation, shortestMetrics);
        DAGShortestPath.PathResult shortestResult = dagSP.shortestPaths(sccOrder, sourceScc);

        System.out.println("Shortest distances from SCC " + sourceScc + ":");
        for (int i = 0; i < shortestResult.distances.length; i++) {
            if (shortestResult.distances[i] != Integer.MAX_VALUE) {
                System.out.println("  To SCC " + i + ": " + shortestResult.distances[i]);
                List<Integer> path = shortestResult.reconstructPath(i);
                System.out.println("    Path: " + path);
            }
        }
        System.out.println("Metrics: " + shortestMetrics);
        System.out.println();

        // Step 5: Longest Path (Critical Path)
        System.out.println("=== Step 5: Longest Path (Critical Path) ===");
        Metrics longestMetrics = new Metrics();
        DAGShortestPath dagLP = new DAGShortestPath(condensation, longestMetrics);
        DAGShortestPath.PathResult criticalResult = dagLP.findCriticalPath(sccOrder);

        int maxDist = Integer.MIN_VALUE;
        int endVertex = -1;
        for (int i = 0; i < criticalResult.distances.length; i++) {
            if (criticalResult.distances[i] > maxDist) {
                maxDist = criticalResult.distances[i];
                endVertex = i;
            }
        }

        System.out.println("Critical path length: " + maxDist);
        if (endVertex != -1) {
            List<Integer> criticalPath = criticalResult.reconstructPath(endVertex);
            System.out.println("Critical path: " + criticalPath);
        }
        System.out.println("Metrics: " + longestMetrics);
        System.out.println();

        System.out.println("=== Analysis Complete ===");
    }

    static class GraphData {
        Graph graph;
        String weightModel;
        int source;
    }

    private static GraphData loadGraphFromJson(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        JsonObject json = JsonParser.parseString(content).getAsJsonObject();

        boolean directed = json.get("directed").getAsBoolean();
        int n = json.get("n").getAsInt();

        Graph graph = new Graph(n, directed);

        JsonArray edges = json.getAsJsonArray("edges");
        for (JsonElement edgeElement : edges) {
            JsonObject edge = edgeElement.getAsJsonObject();
            int u = edge.get("u").getAsInt();
            int v = edge.get("v").getAsInt();
            int w = edge.get("w").getAsInt();
            graph.addEdge(u, v, w);
        }

        GraphData data = new GraphData();
        data.graph = graph;
        data.weightModel = json.has("weight_model") ? json.get("weight_model").getAsString() : "edge";
        data.source = json.has("source") ? json.get("source").getAsInt() : -1;

        return data;
    }
}
