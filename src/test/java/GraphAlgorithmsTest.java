import org.example.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmsTest {

    @Test
    public void testSimpleDAG() {
        // Simple DAG: 0->1->2->3
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 3);

        // Should have 4 SCCs (one per vertex)
        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        assertEquals(4, sccs.size(), "Simple DAG should have 4 SCCs");
    }


    @Test
    public void testMultipleSCCsConnected() {
        // (0->1->0), (2->3->2), 1->2
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);

        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        assertEquals(2, sccs.size(), "Should have 2 SCCs connected by edge");
    }



    @Test
    public void testSimpleCycle() {
        // Cycle: 0->1->2->0
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        assertEquals(1, sccs.size(), "Cycle should form 1 SCC");
        assertEquals(3, sccs.get(0).size(), "SCC should contain 3 vertices");
    }

    @Test
    public void testMixedGraph() {
        // Graph with one cycle (1->2->3->1) and isolated vertex 0
        Graph graph = new Graph(4, true);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 1, 1);

        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        assertEquals(2, sccs.size(), "Should have 2 SCCs");
    }

    @Test
    public void testTopologicalSortDAG() {
        // DAG: 0->1, 0->2, 1->3, 2->3
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        Metrics metrics = new Metrics();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        List<Integer> order = topoSort.sortKahn();

        assertNotNull(order, "DAG should have valid topological order");
        assertEquals(4, order.size(), "Order should contain all vertices");

        // Check ordering: 0 before 1,2 and 1,2 before 3
        int pos0 = order.indexOf(0);
        int pos1 = order.indexOf(1);
        int pos2 = order.indexOf(2);
        int pos3 = order.indexOf(3);

        assertTrue(pos0 < pos1);
        assertTrue(pos0 < pos2);
        assertTrue(pos1 < pos3);
        assertTrue(pos2 < pos3);
    }

    @Test
    public void testTopologicalSortCycle() {
        // Graph with cycle
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        Metrics metrics = new Metrics();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        List<Integer> order = topoSort.sortKahn();

        assertNull(order, "Cycle should result in null topological order");
    }

    @Test
    public void testShortestPathDAG() {
        // DAG with weights
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 6);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2, 3);

        Metrics metrics = new Metrics();
        DAGShortestPath dagSP = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagSP.shortestPaths(topoOrder, 0);

        assertEquals(0, result.distances[0]);
        assertEquals(5, result.distances[1]);
        assertEquals(3, result.distances[2]);
        assertEquals(7, result.distances[3]); // Via 0->1->3
    }

    @Test
    public void testLongestPathDAG() {
        // Same graph as above
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 6);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2, 3);

        Metrics metrics = new Metrics();
        DAGShortestPath dagSP = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagSP.longestPaths(topoOrder, 0);

        assertEquals(0, result.distances[0]);
        assertEquals(5, result.distances[1]);
        assertEquals(3, result.distances[2]);
        assertEquals(9, result.distances[3]); // Via 0->2->3
    }

    @Test
    public void testPathReconstruction() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2, 3);

        Metrics metrics = new Metrics();
        DAGShortestPath dagSP = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagSP.shortestPaths(topoOrder, 0);

        List<Integer> path = result.reconstructPath(3);
        assertNotNull(path);
        assertEquals(Arrays.asList(0, 1, 2, 3), path);
    }

    @Test
    public void testEmptyGraph() {
        Graph graph = new Graph(0, true);

        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        assertEquals(0, sccs.size());
    }

    @Test
    public void testSingleVertex() {
        Graph graph = new Graph(1, true);

        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.findSCCs();

        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
    }
}