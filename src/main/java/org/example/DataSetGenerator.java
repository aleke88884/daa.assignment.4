package org.example;

import com.google.gson.*;
import java.io.*;
import java.util.*;

public class DataSetGenerator {

    private static final Random random = new Random(42);

    public static void main(String[] args) {


        String outputDir = "data/";
        new File(outputDir).mkdirs();

        System.out.println("Generating datasets...\n");

        // Small datasets (6-10 nodes)
        generateDataset(outputDir + "small_dag_1.json", 6, 7, false, 0.3, "Small DAG");
        generateDataset(outputDir + "small_cyclic_1.json", 8, 12, true, 0.4, "Small with cycles");
        generateDataset(outputDir + "small_dag_2.json", 10, 15, false, 0.35, "Small sparse DAG");

        // Medium datasets (10-20 nodes)
        generateDataset(outputDir + "medium_mixed_1.json", 12, 20, true, 0.3, "Medium mixed");
        generateDataset(outputDir + "medium_dag_1.json", 15, 25, false, 0.25, "Medium DAG");
        generateDataset(outputDir + "medium_dense_1.json", 18, 45, true, 0.5, "Medium dense with SCCs");

        // Large datasets (20-50 nodes)
        generateDataset(outputDir + "large_sparse_1.json", 25, 40, false, 0.15, "Large sparse DAG");
        generateDataset(outputDir + "large_cyclic_1.json", 35, 80, true, 0.2, "Large with multiple SCCs");
        generateDataset(outputDir + "large_dense_1.json", 50, 150, true, 0.25, "Large dense");

        System.out.println("\nAll datasets generated successfully!");
    }

    private static void generateDataset(String filename, int n, int targetEdges,
                                        boolean allowCycles, double density, String description) {
        System.out.println("Generating: " + filename);
        System.out.println("  Description: " + description);
        System.out.println("  Nodes: " + n + ", Target edges: " + targetEdges);

        JsonObject graph = new JsonObject();
        graph.addProperty("directed", String.valueOf(true));
        graph.addProperty("n", String.valueOf(n));
        graph.addProperty("description", description);
        graph.addProperty("weight_model", "edge");

        JsonArray edges = new JsonArray();
        Set<String> edgeSet = new HashSet<>();

        // Create a connected structure first
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < n; i++) vertices.add(Integer.valueOf(i));
        Collections.shuffle(vertices, random);

        // Build initial structure
        if (allowCycles) {
            // Create some cycles by connecting vertices in small groups
            int groupSize = Math.min(4, n / 2);
            for (int i = 0; i < n - 1; i++) {
                int u = vertices.get(i);
                int v = vertices.get(i + 1);
                addEdge(edges, edgeSet, u, v);

                // Occasionally add back edges to create cycles
                if (i % groupSize == groupSize - 1 && i > 0) {
                    int backU = vertices.get(i);
                    int backV = vertices.get(i - groupSize + 1);
                    addEdge(edges, edgeSet, backU, backV);
                }
            }
        } else {
            // Create DAG structure (topological order maintained)
            for (int i = 0; i < n - 1; i++) {
                int u = i;
                int v = random.nextInt(n - i - 1) + i + 1;
                addEdge(edges, edgeSet, u, v);
            }
        }

        // Add remaining edges to reach target
        int attempts = 0;
        while (edges.size() < targetEdges && attempts < targetEdges * 10) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);

            if (u == v) {
                attempts++;
                continue;
            }

            // For DAG, ensure u < v
            if (!allowCycles && u > v) {
                int temp = u;
                u = v;
                v = temp;
            }

            addEdge(edges, edgeSet, u, v);
            attempts++;
        }

        graph.add("edges", edges);

        // Set source vertex (usually 0 or a vertex with low in-degree)
        graph.addProperty("source", String.valueOf(0));

        // Write to file
        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(graph, writer);
            System.out.println("  Written: " + edges.size() + " edges");
        } catch (IOException e) {
            System.err.println("  Error writing file: " + e.getMessage());
        }

        System.out.println();
    }

    private static void addEdge(JsonArray edges, Set<String> edgeSet, int u, int v) {
        String edgeKey = u + "->" + v;
        if (!edgeSet.contains(edgeKey)) {
            JsonObject edge = new JsonObject();
            edge.addProperty("u", String.valueOf(u));
            edge.addProperty("v", String.valueOf(v));
            edge.addProperty("w", String.valueOf(random.nextInt(10) + 1)); // Weight 1-10
            edges.add(edge);
            edgeSet.add(edgeKey);
        }
    }
}
