package graph;
import java.util.*;
/**
 * <b>Node</b> represents a <b>mutable</b> node connected to one, multiple, itself, or
 * no other nodes. Nodes always have a label. In addition,
 * nodes can be connected to other nodes based on directional edges.
 * For example, A --&gt; B means node A is connected to node B, and ---&gt; is referred to as the edge;
 * Edge's always have a label as well.
 * <p><b>Terminology:</b> In A--&gt;B, node A is the source and the edge is outgoing from it,
 * while node B is the destination and the edge if incoming to it. A parent node is the
 * node where the edge leaves from, while a child node is the node the edge points to.
 * In A--&gt;B, A is the parent node and B is the child node.</p>
 * <p>The first created node
 * in this class will act as the parent node for all subsequent nodes connected to
 * it in <i>this</i>.</p>
 * <p>@param T1 represents the type of the edges in this class. If this is mutable, behavior is unspecified if
 * values of this type that are passed to this class are changed.</p>
 * <p>@param T2 represents the type of the edge(s) in this class. If this is mutable, behavior is unspecified if
 * values of this type that are passed to this class are changed</p>
 */
/*
 * Abstract Invariant:
 * -An edge must be connected to two nodes, or else there none.
 * -No two edge's outgoing from the same node and incoming to the exact same
 * node can share a label
 * -No node can be empty.
 * -the first node and it's subsequent child node(s) must not have the same label.
 * -All labels are case-sensitive. For example, a node labeled "hello"
 * and another node labeled "HELLo" is possible.
 */

public class Node<T1,T2> {

    /**
     * Used for maintenance of Node class
     */
    public static final boolean DEBUG = false;
    /**
     * Contains out child nodes and edges
     */
    private Map<T1, Set<T2>> nodeToEdge;
    /**
     * The label of our first node
     */
    private T1 firstNode;

    /*
    Representation Invariant:
    -firstNode != null
    -edgeToNode.keySet() doesn't contain duplicate nodes
    -edgeToNode.get(i) doesn't contain duplicate edge labels
    -edgeToNode.values() and edgeToNode.keySet() doesn't contain null elements
     */


    /*
    Abstract function:
        -Label of first node  = firstNode
        -edgeToNode.keySet() represents all child nodes connected to the first node
        -edgeToNode.values() represent all edge labels between the first node and the
            corresponding child node/key in edgeToNode.keySet()
        -edgeToNode.size() represents the number of child nodes
        -{x1 x2 . . . xn} where xi represents a child node that has information about its edges
        coming from the 'first node'
     */
    /**
     * Constructs a new node.
     * @param label the label we shall use to name our first/starting node
     * @throws IllegalArgumentException if label = null
     * @spec.modifies this
     * @spec.effects creates a node. This node will act as the
     * first node(parent node) for any subsequent nodes
     * that are added in <i>this</i>.
     */
    public Node(T1 label) {
        if (label == null) {
            throw new IllegalArgumentException();
        }
        this.firstNode = label;
        nodeToEdge = new HashMap<>();
        checkRep();
    }

    //Throws an exception if the representation invariant is violated.
    private void checkRep() {
        assert (firstNode != null);
        assert (!nodeToEdge.keySet().contains(null));
        //No duplicate node labels guaranteed by nature of Map
        //No duplicate edge labels guaranteed by nature of HashSet
        if (DEBUG) {
            assert (!nodeToEdge.values().contains(null));
            Iterator<T1> itr = nodeToEdge.keySet().iterator();
            while (itr.hasNext()) {
                assert (!nodeToEdge.get(itr.next()).contains(null));
            }
        }
    }
    /**
     * Creates an edge between two nodes.
     * @param edgeLabel the label of our edge.
     * @param childNode the node that will become the child node
     * when creating the edge; The first created node is the node
     * that this edge will be outgoing from.
     * @spec.requires childNode != null, edgeName != null, there not already
     * exists an edge label the same as 'edgeLabel' between the parent node and
     * childNode.
     * @spec.modifies this
     * @spec.effects creates an edge between the first node in <i>this</i>, being the
     * parent node, and childNode. If childNode is not in <i>this</i>, it will be added
     * with the edge. If it's already in <i>this</i>, then childNode will not be replaced
     * and only a new edge will be made.
     */
    public void insertEdge(T2 edgeLabel, Node<T1,T2> childNode) {
        checkRep();
        if(edgeLabel != null && childNode != null) {
            if (!nodeToEdge.containsKey(childNode.getFirstNodeLabel())) {
                nodeToEdge.put(childNode.getFirstNodeLabel(), new HashSet<>());
            }
            nodeToEdge.get(childNode.getFirstNodeLabel()).add(edgeLabel);
        }
        checkRep();
    }

    /**
     * Gets the name of the edge(s) that are incoming to a specific node.
     * @param target the label of the child node.
     * @spec.requires target != null and the node labeled 'target' is in <i>this</i>
     * @return all edge labels that are outgoing from the first node and incoming towards
     * the node labeled 'target'.
     */
    public List<T2> getEdgeLabel(T1 target) {
        checkRep();
        List<T2> result = new ArrayList<>();
        if(target != null) {
            if (nodeToEdge.containsKey(target)) {
                result.addAll(nodeToEdge.get(target));
            }
        }
        checkRep();
        return result;
    }

    /**
     * Gets the node(s) based on a given edge label.
     * @param edgeLabel the label of an edge.
     * @spec.requires edgeLabel != null and there must be
     * an edge called 'edgeLabel' in <i>this</i>.
     * @return the label of the node(s) found that have an edge incoming to
     * them that have the name 'edgeLabel'.
     */
    public List<T1> getNodeLabel(T2 edgeLabel) {
        checkRep();
        List<T1> result = new ArrayList<>();
        if(edgeLabel != null) {
            Iterator<T1> itr = nodeToEdge.keySet().iterator();
            while (itr.hasNext()) {
                T1 temp = itr.next();
                if(nodeToEdge.get(temp).contains(edgeLabel)) {
                    result.add(temp);
                }
            }
        }
        checkRep();
        return result;
    }

    /**
     * Gets all edges outgoing from the first node.
     * @return the labels of every edge that is outgoing from the
     * first node(the parent node). This doesn't include repeated edge
     * label names that are the incoming to two different nodes.
     */
    public List<T2> allEdgeLabels() {
        checkRep();
        Set<T2> containEdges = new HashSet<>();
        List<T2> result = new ArrayList<>();
        Iterator<T1> itr = nodeToEdge.keySet().iterator();
        while (itr.hasNext()) {
            containEdges.addAll(nodeToEdge.get(itr.next()));
            }
        result.addAll(containEdges);
        checkRep();
        return result;
    }

    /**
     * Get all nodes that have an edge incoming from the first node.
     * @return the labels of all node's that are child node's of the
     * first node(the parent node).
     */
    public List<T1> allChildNodeLabels() {
        checkRep();
        List<T1> result = new ArrayList<>();
        result.addAll(nodeToEdge.keySet());
        checkRep();
        return result;
    }

    /**
     * Gets the label of the first node in <i>this</i>.
     * @return the label of the first node in <i>this</i>.
     */
    public T1 getFirstNodeLabel() {
        checkRep();
        return firstNode;
    }

    /**
     * Standard equality operation.
     * @param o the object to be compared for equality
     * @return true if and only if 'o' is an instance of a Node and 'this' and 'o' have
     * the same label.
     */
    @Override
    public boolean equals(Object o) {
        checkRep();
        if (! (o instanceof Node<?,?>)) {
            return false;
        }
        Node<?,?> n = (Node<?,?>) o;
        return (this.getFirstNodeLabel().equals((n).getFirstNodeLabel()));
    }

    /**
     * Standard hashCode function.
     * @return an int that all objects equal to this will also return.
     */
    @Override
    public int hashCode() {
        checkRep();
        return this.getFirstNodeLabel().hashCode();
    }
}
