package graph.junitTests;
import org.junit.*;
import static org.junit.Assert.*;
import graph.*;
import java.util.*;
import org.junit.Rule;
import org.junit.rules.Timeout;

/**
 * Unit tests for my graph ADT implementation
 */
public class GraphTest {

    @Rule public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested
   //This tests the getParentNode() method inside our graph ADT
    @Test
    public void testGetParentNode() {
        //tests a graph with one node
        Graph<String,String> graph1 = new Graph<String,String>();
        graph1.insertNode(new Node<String,String>("one"));
        assertTrue(graph1.getParentNode("one").isEmpty());
        //tests a graph with two nodes with an edge
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.insertNode(new Node<String,String>("two"));
        graph2.insertEdge("one","two", "e1");
        List<String> list = graph2.getParentNode("two");
        assertTrue((list.size() == 1) && list.contains("one"));
        //tests a graph with three nodes and two edges
        Graph<String,String> graph3 = new Graph<String,String>();
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        graph3.insertNode(new Node<String,String>("three"));
        graph3.insertEdge("one","two", "e1");
        graph3.insertEdge("three","two", "e1");
        List<String> list2 = graph3.getParentNode("two");
        assertTrue((list2.size() == 2) && list2.contains("one") && list2.contains("three"));
        //tests a graph with two nodes and no edge
        Graph<String,String> graph4 = new Graph<String,String>();
        graph4.insertNode(new Node<String,String>("one"));
        graph4.insertNode(new Node<String,String>("two"));
        List<String> list4 = graph4.getParentNode("two");
        assertTrue(list4.isEmpty());
        //Implementation test:
        //when input is null
        Graph<String,String> graph5 = new Graph<String,String>();
        graph5.insertNode(new Node<String,String>("one"));
        graph5.insertNode(new Node<String,String>("two"));
        graph5.insertEdge("one","two", "e1");
        assertTrue(graph5.getParentNode(null).isEmpty());
        //when input is a node that doesn't exist in this
        Graph<String,String> graph6 = new Graph<String,String>();
        graph6.insertNode(new Node<String,String>("one"));
        graph6.insertNode(new Node<String,String>("two"));
        graph6.insertEdge("one","two", "e1");
        assertTrue(graph5.getParentNode("three").isEmpty());
    }

    //Tests the getEdgesOutgoing() method in Graph adt
    @Test
    public void testGetEdgesOutgoing() {
        //Tests when there is only one node in the graph
        Graph<String,String> graph1 = new Graph<String,String>();
        graph1.insertNode(new Node<String,String>("one"));
        List<String> list1 = graph1.getEdgesOutgoing("one");
        assertTrue(list1.isEmpty());
        //Tests when there is two nodes with an edge between them
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.insertNode(new Node<String,String>("two"));
        graph2.insertEdge("one","two","e1");
        List<String> list2 = graph2.getEdgesOutgoing("one");
        List<String> list3 = graph2.getEdgesOutgoing("two");
        assertTrue((list2.size() == 1) && list2.contains("e1"));
        assertTrue(list3.isEmpty());
        //Tests when there is three nodes and two edges
        Graph<String,String> graph3 = new Graph<String,String>();
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        graph3.insertNode(new Node<String,String>("three"));
        graph3.insertEdge("one","two","e1");
        graph3.insertEdge("one","three","f1");
        List<String> list4 = graph3.getEdgesOutgoing("one");
        List<String> list5 = graph3.getEdgesOutgoing("two");
        List<String> list6 = graph3.getEdgesOutgoing("three");
        assertTrue((list4.size() == 2) && list4.contains("e1") && list4.contains("f1"));
        assertTrue(list5.isEmpty());
        assertTrue(list6.isEmpty());
        //Tests when there are two nodes, but with two edge labels
        //in-between them.
        Graph<String,String> graph4 = new Graph<String,String>();
        graph4.insertNode(new Node<String,String>("one"));
        graph4.insertNode(new Node<String,String>("two"));
        graph4.insertEdge("one","two","e1");
        graph4.insertEdge("one","two","E1");
        List<String> list7 = graph4.getEdgesOutgoing("one");
        assertTrue((list7.size() == 2) && list7.contains("e1") && list7.contains("E1"));
        //When there are two nodes, but no edges
        Graph<String,String> graph5 = new Graph<String,String>();
        graph5.insertNode(new Node<String,String>("one"));
        graph5.insertNode(new Node<String,String>("two"));
        List<String> list8 = graph5.getEdgesOutgoing("one");
        List<String> list9 = graph5.getEdgesOutgoing("two");
        assertTrue(list8.isEmpty() && list9.isEmpty());
        //Implementation test:
        //When an input is null
        Graph<String,String> graph6 = new Graph<String,String>();
        graph6.insertNode(new Node<String,String>("one"));
        graph6.insertNode(new Node<String,String>("two"));
        graph6.insertEdge("one","two","e1");
        List<String> list11 = graph2.getEdgesOutgoing(null);
        assertTrue(list11.isEmpty());
        //When a node we are calling is not present in this
        Graph<String,String> graph7 = new Graph<String,String>();
        graph7.insertNode(new Node<String,String>("one"));
        graph7.insertNode(new Node<String,String>("two"));
        graph7.insertEdge("one","two","e1");
        List<String> list12 = graph2.getEdgesOutgoing("thee");
        assertTrue(list12.isEmpty());
    }

    //Tests the getEdgesIncoming() method in Graph adt
    @Test
    public void testGetEdgesIncoming() {
        //Tests when there is only one node in the graph
        Graph<String,String> graph1 = new Graph<String,String>();
        graph1.insertNode(new Node<String,String>("one"));
        List<String> list1 = graph1.getEdgesIncoming("one");
        assertTrue(list1.isEmpty());
        //Tests when there is two nodes with an edge between them
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.insertNode(new Node<String,String>("two"));
        graph2.insertEdge("one","two","e1");
        List<String> list2 = graph2.getEdgesIncoming("one");
        List<String> list3 = graph2.getEdgesIncoming("two");
        assertTrue(list2.isEmpty());
        assertTrue((list3.size() == 1) && list3.contains("e1"));
        //Tests when there is three nodes and two edges
        Graph<String,String> graph3 = new Graph<String,String>();
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        graph3.insertNode(new Node<String,String>("three"));
        graph3.insertEdge("one","two","e1");
        graph3.insertEdge("one","three","f1");
        List<String> list4 = graph3.getEdgesIncoming("one");
        List<String> list5 = graph3.getEdgesIncoming("two");
        List<String> list6 = graph3.getEdgesIncoming("three");
        assertTrue(list4.isEmpty());
        assertTrue((list5.size() == 1) && list5.contains("e1"));
        assertTrue((list6.size() == 1) && list6.contains("f1"));
        //Tests when there are two nodes and no edges
        Graph<String,String> graph4 = new Graph<String,String>();
        graph4.insertNode(new Node<String,String>("one"));
        graph4.insertNode(new Node<String,String>("two"));
        assertTrue(graph4.getEdgesIncoming("one").isEmpty() &&
                    graph4.getEdgesIncoming("two").isEmpty());
        //Implementation test:
        //when input is null
        Graph<String,String> graph5 = new Graph<String,String>();
        graph5.insertNode(new Node<String,String>("one"));
        graph5.insertNode(new Node<String,String>("two"));
        graph5.insertEdge("one","two","e1");
        assertTrue(graph5.getEdgesIncoming(null).isEmpty());
        assertTrue(graph5.getEdgesIncoming("two").size() == 1);
        //when input is not in this
        Graph<String,String> graph6 = new Graph<String,String>();
        graph6.insertNode(new Node<String,String>("one"));
        graph6.insertNode(new Node<String,String>("two"));
        graph6.insertEdge("one","two","e1");
        assertTrue(graph5.getEdgesIncoming("three").isEmpty());
        assertTrue(graph5.getEdgesIncoming("two").size() == 1);
    }

    //Tests the getChildNode() method in Graph adt implementation
    @Test
    public void testGetChildNode() {
        //Tests when there is just a single node
        Graph<String,String> graph1 = new Graph<String,String>();
        graph1.insertNode(new Node<String,String>("one"));
        assertTrue(graph1.getChildNode("one").isEmpty());
        //Tests when there are two nodes and an edge between them
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.insertNode(new Node<String,String>("two"));
        graph2.insertEdge("one","two","e1");
        List<String> list1 = graph2.getChildNode("one");
        assertTrue((list1.size() == 1) && list1.contains("two"));
        //Tests when there are three nodes each has an edge to each other.
        // Ex: A--->B--->C--->A
        Graph<String,String> graph3 = new Graph<String,String>();
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        graph3.insertNode(new Node<String,String>("three"));
        graph3.insertEdge("one","two","e1");
        graph3.insertEdge("two","three","e1");
        graph3.insertEdge("three","one","e2");
        List<String> list2 = graph3.getChildNode("one");
        List<String> list3 = graph3.getChildNode("two");
        List<String> list4 = graph3.getChildNode("three");
        assertTrue((list2.size() == 1) && list2.contains("two"));
        assertTrue((list3.size() == 1) && list3.contains("three"));
        assertTrue((list4.size() == 1) && list4.contains("one"));
        //Implementation tests:
        //when the 'parentNode' node is null
        Graph<String,String> graph4 = new Graph<String,String>();
        graph4.insertNode(new Node<String,String>("one"));
        graph4.insertNode(new Node<String,String>("two"));
        graph4.insertEdge("one", "two","e1");
        assertTrue(graph4.getChildNode(null).isEmpty());
        assertTrue(graph4.getChildNode("one").size() == 1);
        //when the 'parentNode' is not in this
        Graph<String,String> graph5 = new Graph<String,String>();
        graph5.insertNode(new Node<String,String>("one"));
        graph5.insertNode(new Node<String,String>("two"));
        graph5.insertEdge("one", "two","e1");
        assertTrue(graph4.getChildNode("three").isEmpty());
    }

    //Tests the getEdgesBetweenNodes() method in my Graph adt implementation
    @Test
    public void testGetEdgesBetweenNodes() {
        //tests when there are two nodes with no
        //edge between them
        Graph<String,String> graph1 = new Graph<String,String>();
        graph1.insertNode(new Node<String,String>("one"));
        graph1.insertNode(new Node<String,String>("two"));
        assertTrue(graph1.getEdgesBetweenNodes("one","two").isEmpty());
        //tests when there are two nodes with an edge between them
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.insertNode(new Node<String,String>("two"));
        graph2.insertEdge("one","two","e1");
        List<String> list1 = graph2.getEdgesBetweenNodes("one","two");
        assertTrue((list1.size() == 1) && list1.contains("e1"));
        //tests when there are two nodes with two edges between them
        Graph<String,String> graph3 = new Graph<String,String>();
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        graph3.insertEdge("one","two","e1");
        graph3.insertEdge("one","two","e2");
        List<String> list2 = graph3.getEdgesBetweenNodes("one","two");
        assertTrue((list2.size() == 2) && list2.contains("e1") && list2.contains("e2"));
        //tests when there is a single node with an edge pointing to itself
        Graph<String,String> graph4 = new Graph<String,String>();
        graph4.insertNode(new Node<String,String>("one"));
        graph4.insertEdge("one","one","e1");
        List<String> list3 = graph4.getEdgesBetweenNodes("one","one");
        assertTrue((list3.size() == 1) && list3.contains("e1"));
        //Tests when inputs are null
        Graph<String,String> graph5 = new Graph<String,String>();
        graph5.insertNode(new Node<String,String>("one"));
        graph5.insertNode(new Node<String,String>("two"));
        graph5.insertEdge("one","two","e1");
        assertTrue(graph5.getEdgesBetweenNodes(null,"two").isEmpty());
        Graph<String,String> graph6 = new Graph<String,String>();
        graph6.insertNode(new Node<String,String>("one"));
        graph6.insertNode(new Node<String,String>("two"));
        graph6.insertEdge("one","two","e1");
        assertTrue(graph5.getEdgesBetweenNodes(null,null).isEmpty());
        //Tests when both nodes aren't in this
        Graph<String,String> graph8 = new Graph<String,String>();
        assertTrue(graph8.getEdgesBetweenNodes("one","two").isEmpty());
        //Tests when one node isn't in this
        Graph<String,String> graph9 = new Graph<String,String>();
        graph9.insertNode(new Node<String,String>("one"));
        assertTrue(graph9.getEdgesBetweenNodes("one","two").isEmpty());
    }


    //Tests the insertEdge() method in my graph adt
    @Test
    public void testInsertEdge() {
        //Implementation tests
        //when the inputs are null
        Graph<String,String> graph1  = new Graph<String,String>();
        graph1.insertNode(new Node<String,String>("one"));
        graph1.insertNode(new Node<String,String>("two"));
        graph1.insertEdge("one","two",null);
        assertTrue(graph1.getChildNode("one").isEmpty());
        graph1.insertEdge(null,null,null);
        assertTrue(graph1.getChildNode("one").isEmpty());
        //Tests when a node label does not represent a node inside this
        Graph<String,String> graph2  = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.insertNode(new Node<String,String>("two"));
        graph2.insertEdge("three","two","e1");
        assertTrue(graph2.getChildNode("one").isEmpty());
        graph2.insertEdge("one","four","e1");
        assertTrue(graph2.getChildNode("one").isEmpty());
        //Tests when there is already an edge present inside with
        //the same label as the given
        Graph<String,String> graph3  = new Graph<String,String>();
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        graph3.insertEdge("one","two","e1");
        graph3.insertEdge("one","two","e1");
        assertTrue(graph3.getChildNode("one").size() == 1);
        List<String> list = graph3.getEdgesOutgoing("one");
        assertTrue((list.size() == 1) && list.contains("e1"));
        //Checks to make sure that the edge labels that have the same letters
        //but not the same capitalization works.
        Graph<String,String> graph4  = new Graph<String,String>();
        graph4.insertNode(new Node<String,String>("one"));
        graph4.insertNode(new Node<String,String>("two"));
        graph4.insertEdge("one","two","e1");
        graph4.insertEdge("one","two","E1");
        List<String> list2 = graph4.getEdgesOutgoing("one");
        assertTrue((list2.size() == 2) && list2.contains("e1") && list2.contains("E1"));
    }

    //Tests the insertNode() when given a node with a null label
    @Test(expected = IllegalArgumentException.class)
    public void testInsertingANodeWithNullLabel() {
        //When the label of the node given is null
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>(null));
    }

    //Tests the containsNode() when given a value that is null
    @Test(expected = IllegalArgumentException.class)
    public void testContainsNode() {
        Graph<String,String> graph2 = new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph2.containsNode(null);
    }

    //Tests the containsNode()
    @Test
    public void testContainsNode2() {
        Graph<String,String> graph1= new Graph<String,String>();
        Graph<String,String> graph2= new Graph<String,String>();
        Graph<String,String> graph3= new Graph<String,String>();
        graph2.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("one"));
        graph3.insertNode(new Node<String,String>("two"));
        boolean test1 = graph1.containsNode("one");
        boolean test2 = graph2.containsNode("one");
        boolean test3 = graph2.containsNode("two");
        boolean test4 = graph3.containsNode("one");
        boolean test5 = graph3.containsNode("hello");
        assertTrue(test2);
        assertTrue(test4);
        assertFalse(test1);
        assertFalse(test3);
        assertFalse(test5);
    }

}
