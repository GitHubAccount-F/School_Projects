package graph.junitTests;
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import graph.*;
import java.util.*;
import org.junit.Rule;
import org.junit.rules.Timeout;
/**
 * Unit tests for my Node ADT implementation
 */
public class NodeTest {

    @Rule public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested
    //Tests to make sure that the constructor
    // doesn't accept null as a label.
    @Test(expected = IllegalArgumentException.class)
    public void testImproperNodeCreation() {
        Node<String,String> first = new Node<String,String>(null);
    }

    //Tests the insertEdge() method in my Node adt implementation
    @Test
    public void testInsertEdge() {
        //Creates an edge between our original node a new one
        Node<String,String> one = new Node<String,String>("one");
        one.insertEdge("e1", new Node<String,String>("two"));
        List<String> list1 = one.allEdgeLabels();
        assertTrue((list1.size() == 1) && list1.contains("e1"));
        //Creates two edges between our original node and two other nodes
        Node<String,String> two = new Node<String,String>("two");
        two.insertEdge("e1", new Node<String,String>("three"));
        two.insertEdge("f1", new Node<String,String>("four"));
        List<String> list2 = two.allEdgeLabels();
        assertTrue((list2.size() == 2) && list2.contains("e1") && list2.contains("f1"));
        //Tests when the inputs are null
        Node<String,String> five = new Node<String,String>("five");
        Node<String,String> six = new Node<String,String>("six");
        five.insertEdge(null, six);
        assertTrue(five.allEdgeLabels().isEmpty() && five.allChildNodeLabels().isEmpty());
        //Same as previous test, but with both inputs as null
        //Tests when there is one child node inside, but then with null inputs given later
        Node<String,String> eight = new Node<String,String>("eight");
        Node<String,String> nine = new Node<String,String>("nine");
        eight.insertEdge("e1",nine);
        assertTrue((eight.allEdgeLabels().size() == 1) && eight.allEdgeLabels().contains("e1"));
        assertTrue((eight.allChildNodeLabels().size() == 1) && eight.allChildNodeLabels().contains("nine"));
        //Tests when given duplicate edges with same label between two nodes
        Node<String,String> ten = new Node<String,String>("ten");
        Node<String,String> eleven = new Node<String,String>("eleven");
        ten.insertEdge("e1",eleven);
        ten.insertEdge("e1",eleven);
        assertTrue((ten.getEdgeLabel("eleven").size() == 1) &&
                            ten.getEdgeLabel("eleven").contains("e1"));
        //Tests when given triples edges with same label between two nodes
        Node<String,String> twelve = new Node<String,String>("twelve");
        Node<String,String> thirteen = new Node<String,String>("thirteen");
        twelve.insertEdge("e1",thirteen);
        twelve.insertEdge("e1",thirteen);
        twelve.insertEdge("e1",thirteen);
        assertTrue((twelve.getEdgeLabel("thirteen").size() == 1) &&
                twelve.getEdgeLabel("thirteen").contains("e1"));
    }

    //Tests if we can create multiple edge's between two nodes
    @Test
    public void testMultipleEdgesBetweenNodes() {
        //Creates two edges between our first node and another node
        Node<String,String> one = new Node<String,String>("two");
        Node<String,String> two = new Node<String,String>("two");
        one.insertEdge("e1", two);
        one.insertEdge("f1", two);
        List<String> list1 = one.allEdgeLabels();
        List<String> list2 = one.getEdgeLabel("two");
        //I made two lists because they should return equivalent
        //lists so if one of their method implementation is incorrect,
        //I can rely on the other to tell the test is passed.
        assertTrue((list1.size() == 2) && list1.contains("e1") && list1.contains("f1"));
        assertTrue((list2.size() == 2) && list2.contains("e1") && list2.contains("f1"));
        //Creates three edges between our first node and another node
        Node<String,String> three = new Node<String,String>("three");
        Node<String,String> four = new Node<String,String>("four");
        three.insertEdge("e1", four);
        three.insertEdge("f1", four);
        three.insertEdge("g1", four);
        List<String> list3 = three.allEdgeLabels();
        List<String> list4 = three.getEdgeLabel("four");
        assertTrue((list3.size() == 3) && list3.contains("e1") && list3.contains("f1")
                                                && list3.contains("g1"));
        assertTrue((list4.size() == 3) && list4.contains("e1") && list4.contains("f1")
                                                && list4.contains("g1"));
        //Creates multiple edges between our first node(parent node) and two other nodes
        Node<String,String> five = new Node<String,String>("five");
        Node<String,String> six = new Node<String,String>("six");
        Node<String,String> seven = new Node<String,String>("seven");
        five.insertEdge("e1",six);
        five.insertEdge("e2",six);
        five.insertEdge("e3",six);
        five.insertEdge("e1",seven);
        five.insertEdge("e2",seven);
        List<String> list5 = five.getEdgeLabel("six");
        List<String> list6 = five.getEdgeLabel("seven");
        assertTrue((list5.size() == 3) && list5.contains("e1") && list5.contains("e2")
                                                && list5.contains("e3"));
        assertTrue((list6.size() == 2) && list6.contains("e1") && list6.contains("e2"));
    }

    //Tests to make sure our node doesn't have replica edges outgoing from it
    //towards the same child node.
    @Test
    public void testEdgesWithSameLabel() {
        //Tests to see that this won't add
        //two edges with the same label heading towards
        //the same child node
        Node<String,String> one = new Node<String,String>("one");
        Node<String,String> two = new Node<String,String>("two");
        one.insertEdge("e1",two);
        one.insertEdge("e1",two);
        List<String> list1 = one.getEdgeLabel("two");
        List<String> list2 = one.allEdgeLabels();
        assertTrue((list1.size() == 1) && list1.contains("e1"));
        assertTrue((list2.size() == 1) && list2.contains("e1"));
        //Does the same above, but with more edges
        Node<String,String> three = new Node<String,String>("three");
        Node<String,String> four = new Node<String,String>("four");
        three.insertEdge("e1",four);
        three.insertEdge("E1",four);
        three.insertEdge("e1",four);
        three.insertEdge("f1",four);
        List<String> list3 = three.getEdgeLabel("four");
        List<String> list4 = three.allChildNodeLabels();
        assertTrue((list3.size() == 3) && list3.contains("e1") && list3.contains("f1") && list3.contains("E1"));
        assertTrue((list4.size() == 1) && list4.contains("four"));
        //Tests if multiple edges can point to the first node, excluding repeat edge labels
        Node<String,String> five = new Node<String,String>("five");
        five.insertEdge("e1",five);
        five.insertEdge("E1",five);
        five.insertEdge("e1",five);
        five.insertEdge("e2",five);
        List<String> list5 = five.getEdgeLabel("five");
        List<String> list6 = five.allEdgeLabels();
        assertTrue((list5.size() == 3) && list5.contains("e1") && list5.contains("e2") && list5.contains("E1"));
        assertTrue((list6.size() == 3) && list6.contains("e1") && list6.contains("e2") && list5.contains("E1"));
    }

    //Tests to see if our parent node(the first node in this)
    //can have an edge to itself
    @Test
    public void testEdgeToItself() {
        //Tests if one edge can point to the first node
        Node<String,String> one = new Node<String,String>("one");
        one.insertEdge("e1",one);
        List<String> list1 = one.getEdgeLabel("one");
        List<String> list2 = one.allEdgeLabels();
        assertTrue((list1.size() == 1) && list1.contains("e1"));
        assertTrue((list2.size() == 1) && list2.contains("e1"));
        //Tests if multiple edges can point to the first node
        Node<String,String> two = new Node<String,String>("two");
        two.insertEdge("e1",two);
        two.insertEdge("e2",two);
        List<String> list3 = two.getEdgeLabel("two");
        List<String> list4 = two.allEdgeLabels();
        assertTrue((list3.size() == 2) && list3.contains("e1") && list3.contains("e2"));
        assertTrue((list4.size() == 2) && list4.contains("e1") && list4.contains("e2"));
    }

    //Tests the getEdgeLabel() method in my Node adt implementation
    @Test
    public void testGetEdgeLabel() {
        //Tests for when there is just the first
        //node by-itself(no edges pointing to it)
        Node<String,String> hello = new Node<String,String>("hello");
        List<String> lis = hello.getEdgeLabel("hello");
        assertTrue(lis.isEmpty());
        //Tests for when there is an edge outgoing from and incoming to the
        //first node
        Node<String,String> one = new Node<String,String>("one");
        one.insertEdge("e1",one);
        List<String> list1 = one.getEdgeLabel("one");
        assertTrue((list1.size() == 1) && list1.contains("e1"));
        //Tests for when there is a single edge between two nodes
        Node<String,String> two = new Node<String,String>("two");
        Node<String,String> three = new Node<String,String>("three");
        two.insertEdge("e1",three);
        List<String> list2 = two.getEdgeLabel("three");
        assertTrue((list2.size() == 1) && list2.contains("e1"));
        //Tests for multiple edges between the first node and some other node
        Node<String,String> five = new Node<String,String>("five");
        Node<String,String> six = new Node<String,String>("six");
        five.insertEdge("e1",six);
        five.insertEdge("e2",six);
        List<String> list3 = five.getEdgeLabel("six");
        assertTrue((list3.size() == 2) && list3.contains("e1") && list3.contains("e2"));
        //Tests if the target string is null
        Node<String,String> seven = new Node<String,String>("seven");
        Node<String,String> eight = new Node<String,String>("eight");
        seven.insertEdge("e1",eight);
        assertTrue(seven.getEdgeLabel(null).isEmpty());
        assertTrue((seven.getEdgeLabel("eight").size() == 1) &&
                            seven.getEdgeLabel("eight").contains("e1"));
    }

    //Tests the allNodeLabel() method in my Node adt implementation.
    @Test
    public void testAllChildNodeLabel() {
        //Tests for when there is just a single node in this
        Node<String,String> one = new Node<String,String>("one");
        List<String> list1 = one.allChildNodeLabels();
        assertTrue(list1.isEmpty());
        //Tests for when there are multiple nodes in this
        Node<String,String> two = new Node<String,String>("two");
        Node<String,String> three = new Node<String,String>("three");
        Node<String,String> four = new Node<String,String>("four");
        two.insertEdge("e1",three);
        two.insertEdge("e1",four);
        List<String> list2 = two.allChildNodeLabels();
        assertTrue((list2.size() == 2) && list2.contains("three") && list2.contains("four"));
    }

    //Tests the allEdgeLabel() method in my Node adt implementation
    @Test
    public void testAllEdgeLabels() {
        //Tests for when there is just a single node in this
        Node<String,String> one = new Node<String,String>("one");
        List<String> list1 = one.allEdgeLabels();
        assertTrue(list1.isEmpty());
        //Tests for when there is a single edge
        Node<String,String> hello = new Node<String,String>("Hello");
        Node<String,String> bye = new Node<String,String>("bye");
        hello.insertEdge("e1",bye);
        List<String> lis = hello.allEdgeLabels();
        assertTrue((lis.size() == 1) && lis.contains("e1"));
        //Tests for when there are multiple nodes in this with
        //one edge between them and the first node
        Node<String,String> two = new Node<String,String>("two");
        Node<String,String> three = new Node<String,String>("three");
        Node<String,String> four = new Node<String,String>("four");
        two.insertEdge("e1",three);
        two.insertEdge("e2",four);
        List<String> list2 = two.allEdgeLabels();
        assertTrue((list2.size() == 2) && list2.contains("e1") && list2.contains("e2"));
        //Tests for when there is multiple nodes with one or more
        //edges between them and the first node
        Node<String,String> five = new Node<String,String>("five");
        Node<String,String> six = new Node<String,String>("six");
        Node<String,String> seven = new Node<String,String>("seven");
        five.insertEdge("e1",six);
        five.insertEdge("e2",six);
        five.insertEdge("E1",six);
        five.insertEdge("e3",seven);
        List<String> list3 = five.allEdgeLabels();
        assertTrue((list3.size() == 4) && list3.contains("e1") && list3.contains("e2")
                && list3.contains("e3") && list3.contains("E1"));
        //Tests for the allEdgeLabels() method not
        //returning the same edge twice
        Node<String,String> eight = new Node<String,String>("eight");
        Node<String,String> nine = new Node<String,String>("nine");
        Node<String,String> ten = new Node<String,String>("ten");
        eight.insertEdge("e1",nine);
        eight.insertEdge("e1",ten);
        List<String> list4 = eight.allEdgeLabels();
        assertTrue((list4.size() == 1) && list4.contains("e1"));
        //Same as previous test, but more complex
        Node<String,String> eleven = new Node<String,String>("eleven");
        Node<String,String> twelve = new Node<String,String>("twelve");
        Node<String,String> thirteen = new Node<String,String>("thirteen");
        eleven.insertEdge("e1",twelve);
        eleven.insertEdge("e2",thirteen);
        eleven.insertEdge("e4",thirteen);
        eleven.insertEdge("e1",thirteen);
        List<String> list5 = eleven.allEdgeLabels();
        assertTrue((list5.size() == 3) && list5.contains("e1") && list5.contains("e2") && list5.contains("e4"));
    }

    //Tests the getFirstNodeLabel() in my adt implementation()
    @Test
    public void testGetFirstNodeLabel() {
        //Tests when there is just a node with no edges
        //in this
        Node<String,String> one = new Node<String,String>("one");
        assertTrue(one.getFirstNodeLabel().equals("one"));
        //Tests when there are multiple nodes in this
        Node<String,String> two = new Node<String,String>("two");
        Node<String,String> three = new Node<String,String>("three");
        two.insertEdge("e1",three);
        assertEquals(two.getFirstNodeLabel(),"two");
    }

    //Tests the getNodeLabel() method in my Node adt implementation
    @Test
    public void testGetNodeLabel() {
        //Tests when there one edge in this
        Node<String,String> one = new Node<String,String>("one");
        one.insertEdge("e1", new Node<String,String>("two"));
        List<String> list1 = one.getNodeLabel("e1");
        assertTrue(list1.contains("two") && (list1.size() == 1));
        //Tests when there is a shared label for two different edges
        //between two different child nodes
        Node<String,String> three = new Node<String,String>("three");
        Node<String,String> four = new Node<String,String>("four");
        Node<String,String> five = new Node<String,String>("five");
        three.insertEdge("e1", four);
        three.insertEdge("e1", five);
        List<String> list2 = three.getNodeLabel("e1");
        assertTrue((list2.size() == 2) && list2.contains("four") && list2.contains("five"));
        //Tests that multiple edges outgoing from the first node that is incoming
        //to the child node gives the same answers.
        Node<String,String> six = new Node<String,String>("six");
        Node<String,String> seven = new Node<String,String>("seven");
        six.insertEdge("e1", seven);
        six.insertEdge("e2", seven);
        List<String> list3 = six.getNodeLabel("e1");
        List<String> list4 = six.getNodeLabel("e2");
        assertTrue((list3.size() == 1) && list3.contains("seven"));
        assertTrue((list4.size() == 1) && list4.contains("seven"));
    }

    //Tests behavior when there is only a single node.
    @Test
    public void testSingleNode() {
        Node<String,String> one = new Node<String,String>("one");
        assertTrue(one.allEdgeLabels().isEmpty());
        assertTrue(one.getEdgeLabel("one").isEmpty());
        List<String> list1 = one.allChildNodeLabels();
        assertTrue(list1.isEmpty());
    }

    //Tests a complex case to see if Node implementation is correct
    @Test
    public void testComplexCase() {
        Node<String,String> one = new Node<String,String>("one");
        Node<String,String> two = new Node<String,String>("two");
        Node<String,String> three = new Node<String,String>("three");
        Node<String,String> four = new Node<String,String>("four");
        Node<String,String> five = new Node<String,String>("five");
        one.insertEdge("e1",two);
        one.insertEdge("e1",two);
        one.insertEdge("hi",two);
        one.insertEdge("hi",one);
        one.insertEdge("HI",one);
        one.insertEdge("nice",one);
        one.insertEdge("e3",three);
        one.insertEdge("e4",three);
        one.insertEdge("e1",four);
        one.insertEdge("e1",five);
        List<String> allNodes = one.allChildNodeLabels();
        //Assumption that other method-tests passed, so the calls below
        //return the correct content.
        assertTrue(allNodes.size() == 5);
        assertTrue(one.allEdgeLabels().size() == 6);
        assertTrue(one.getEdgeLabel("one").size() == 3);
        assertTrue(one.getEdgeLabel("two").size() == 2);
        assertTrue(one.getEdgeLabel("three").size() == 2);
        assertTrue(one.getEdgeLabel("four").size() == 1);
        assertTrue(one.getEdgeLabel("five").size() == 1);
        assertTrue(one.getNodeLabel("e1").size() == 3);
        assertTrue(one.getNodeLabel("nice").size() == 1);
    }

    //Tests my equals method in my Node adt
    @Test
    public void testEquals() {
        Node<String,String> test1 = new Node<String,String>("one");
        Node<String,String> test2 = new Node<String,String>("one");
        Node<String,String> test3 = new Node<String,String>("one");
        Node<String,String> test4 = new Node<String,String>("two");
        //tests reflectivity of equals method
        assertTrue(test1.equals(test1));
        //tests symmetry of equals method
        assertTrue(test1.equals(test2) && test2.equals(test1));
        assertTrue(!test1.equals(test4));
        assertTrue(!test1.equals(test4) && !test4.equals(test1));
        //Tests transitivity of equals method
        assertTrue(test1.equals(test2) && test2.equals(test3) && test1.equals(test3));
        assertTrue(test1.equals(test2) && !test2.equals(test4) && !test1.equals(test4));
        assertTrue(!test1.equals(test4) && test2.equals(test1) && !test2.equals(test4));
        //Tests if the equals method is consistent(as expected of it)
        Node<String,String> test5 = new Node<String,String>("one");
        Node<String,String> test6 = new Node<String,String>("one");
        Node<String,String> test7 = new Node<String,String>("two");
        assertTrue(test5.equals(test6) && test5.equals(test6));
        assertTrue(!test5.equals(test7) && !test5.equals(test7));
        //Tests that the equals method has a null uniqueness
        Node<String,String> test8 = new Node<String,String>("one");
        assertTrue(!test8.equals(null));
    }

    //Tests my hashCode() method in my Node adt
    @Test
    public void testHashCode() {
        Node<String,String> test1 = new Node<String,String>("one");
        Node<String,String> test2 = new Node<String,String>("one");
        Node<String,String> test3 = new Node<String,String>("two");
        assertTrue(test1.equals(test1) && test1.hashCode() == test1.hashCode());
        assertTrue(test1.equals(test2) && test1.hashCode() == test2.hashCode());
        //Tests when the hash codes shouldn't be equal
        assertTrue(!test1.equals(test3) && !(test1.hashCode() == test3.hashCode()));
        //Tests that the hashcode method is self-consistent
        Node<String,String> test4 = new  Node<String,String>("one");
        assertTrue(test4.hashCode() == test4.hashCode());
    }

    //Tests the InsertEdge() when given a node with a null label
    @Test(expected = IllegalArgumentException.class)
    public void testInsertingANodeWithNullLabel() {
        //When the label of the node given is null
        Node<String,String> one = new Node<>(null);
    }
}
