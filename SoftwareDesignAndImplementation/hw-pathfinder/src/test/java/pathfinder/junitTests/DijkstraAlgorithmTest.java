package pathfinder.junitTests;

import org.junit.*;
import static org.junit.Assert.*;
import static pathfinder.DijkstraAlgorithm.*;

import graph.*;
import java.util.*;
import org.junit.Rule;
import org.junit.rules.Timeout;
import pathfinder.DijkstraAlgorithm;
import pathfinder.datastructures.Path;

public class DijkstraAlgorithmTest {
    @Rule public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    //Tests DijkstraAlgorithm.algorithm() when given null inputs
    @Test(expected = IllegalArgumentException.class)
    public void testNullInputs() {
        Graph<String,Double> graph = null;
        Path<String> path = DijkstraAlgorithm.algorithm(graph, "one","two");
    }

    //Tests when the graph doesn't have the nodes given
    @Test
    public void graphDoesntHaveNodes() {
        Graph<String,Double> graph = new Graph<>();
        graph.insertNode(new Node<>("one"));
        Path<String> path = DijkstraAlgorithm.algorithm(graph, "one","two");
        assertTrue(path == null);
    }

    //Tests when you have a node with an edge to itself
    @Test
    public void edgeToItself() {
        Graph<String,Double> graph = new Graph<>();
        graph.insertNode(new Node<>("one"));
        graph.insertEdge("one","one",2.00);
        Path<String> path = DijkstraAlgorithm.algorithm(graph, "one","one");
        assertTrue(path.getCost() == 0);
    }

    
}
