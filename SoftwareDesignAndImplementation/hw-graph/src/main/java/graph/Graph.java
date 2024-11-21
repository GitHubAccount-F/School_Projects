package graph;
import java.util.*;


/**
 * <p><b>Graph</b> represents a <b>mutable</b> graph where we can have a single node or
 * multiple nodes. These nodes are connected to each other through edges.
 * For example, we can have nodes A and B be connected like A--&gt;B, the other way around, or a combination
 * of both. We can even have many edges between two nodes or nodes connect to themselves.
 * Applying this, we can create chains of nodes such as A--&gt;B--&gt;C--&gt;A. </p>
 *<p>@param T1 represents the type of the node in this class. If this is mutable, behavior is unspecified if
 * values of this type that are passed to this class are changed</p>
 *<p>@param T2 represents the type of the edges in this class. If this is mutable, behavior is unspecified if
 *  values of this type that are passed to this class are changed</p>
 */
/*
 * Abstract invariant:
 * -No nodes can have the same label.
 * -Edges must be between two nodes
 * -No node can be null.
 * -No node can have the same label.
 * -Must adhere to same rules as Node's(see Node's documentation)
 */
//See Node's documentation for more information on terminology.
public class Graph<T1,T2> {

    /**
     * Used for maintenance of Graph class
     */
    public static final boolean DEBUG = false;
    private Map<T1, Node<T1,T2>> nodesInGraph;
    /*
    Representation invariant:
    -nodesInGraph.keySet() doesn't contain null labels
    -nodesInGraph.values() doesn't contain null nodes
    -nodesInGraph elements in nodesInGraph.keySet() and
        nodesInGraph.value() doesn't contain duplicate nodes
    -Nodes in nodesInGraph.values() can't have null edge/node labels



    Abstraction Function:
    -elements in nodesInGraph.keySet() represent the label of our nodes, and their corresponding
        value represents the actual nodes
    -elements in nodesInGraph represent the nodes in our graph such that {x1 x2 . . . xn}, where xi is a node
        label that has a Node corresponding to it.
    -nodesInGraph.size() represents the number of unique nodes in our graph
     */


    /**
     * Constructs an empty graph
     * @spec.effects creates a graph with no nodes
     */
    public Graph() {
        nodesInGraph = new HashMap<>();
        checkRep();
    }

    //Throws an exception if the representation invariant is violated
    private void checkRep() {
        assert (!nodesInGraph.containsKey(null));
        //No need to check for duplicate node labels because that's guaranteed by Map
        if (DEBUG) {
            assert (!nodesInGraph.containsValue(null));
            Iterator<T1> itr = nodesInGraph.keySet().iterator();
            while (itr.hasNext()) {
                T1 nodeLabel = itr.next();
                assert (!nodesInGraph.get(nodeLabel).allChildNodeLabels().contains(null));
                assert (!nodesInGraph.get(nodeLabel).allEdgeLabels().contains(null));
                assert (nodesInGraph.get(nodeLabel).getFirstNodeLabel().equals(nodeLabel));
                assert (!nodesInGraph.get(nodeLabel).getFirstNodeLabel().equals(null));
            }
        }
    }

    /**
     * Adds a new node to our graph, with no edges connecting it
     * to other nodes
     * @param other the node we are introducing in <i>this</i>.
     * @spec.requires other != null and no another node in <i>this</i>
     * has the same label as 'other'. Also,
     * 'other' must not have a null label
     * @spec.modifies <i>this</i>
     * @spec.effects adds new node to <i>this</i>.
     */


    public void insertNode(Node<T1,T2> other) {
        checkRep();
        if(other != null && other.getFirstNodeLabel() != null &&
                !nodesInGraph.containsKey(other.getFirstNodeLabel())) {
            nodesInGraph.put(other.getFirstNodeLabel(),other);
        }
        checkRep();
    }

    /**
     * Creates an edge between two nodes and specifies
     * the edge's direction
     * @param parentNode the node label where the edge starts from
     * @param childNode the node label where the edge points towards
     * @param label the label of the new edge
     * @spec.modifies <i>this</i>
     * @spec.requires parentNode != null, childNode != null,
     * label != null, there is not already an existing edge between the
     * nodes represented by parentNode and childNode
     * with the same name as 'label',
     * and the nodes parentNode and childNode represent are in <i>this</i>.
     * @spec.effects creates an edge between two nodes.
     */
    public void insertEdge(T1 parentNode, T1 childNode, T2 label) {
        checkRep();
        if (parentNode != null && childNode != null && label != null
                && nodesInGraph.containsKey(parentNode) &&
                nodesInGraph.containsKey(childNode)) {
            nodesInGraph.get(parentNode).insertEdge(label, new Node<T1,T2>(childNode));
        }
        checkRep();
    }


    /**
     * Gets all child node's of a given node.
     * @param parentNode the node label where the edges connecting to the
     * child node(s) are outgoing from.
     * @spec.requires parentNode != null and the node
     * parentNode represents is in <i>this</i>
     * @return all node labels where there is an edge originating from parentNode
     * and point towards another node.
     */
    public List<T1> getChildNode(T1 parentNode) {
        checkRep();
        List<T1> result = new ArrayList<>();
        if (parentNode != null && nodesInGraph.containsKey(parentNode)) {
            result.addAll(nodesInGraph.get(parentNode).allChildNodeLabels());
        }
        checkRep();
        return result;
    }

    /**
     * Gets all parent nodes of a child node.
     * @param childNode the label of the child node.
     * @spec.requires childNode != null and node
     * childNode represents is in <i>this</i>
     * @return all node labels, unordered, that have an edge outgoing from
     * them that leads to the node represented by childNode.
     */
    public List<T1> getParentNode(T1 childNode) {
        checkRep();
        List<T1> result = new ArrayList<>();
        if (childNode != null && nodesInGraph.containsKey(childNode)) {
            Iterator<T1> itr = nodesInGraph.keySet().iterator();
            while (itr.hasNext()) {
                T1 nodeLabel = itr.next();
                if (nodesInGraph.get(nodeLabel).allChildNodeLabels().contains(childNode)) {
                    result.add(nodeLabel);
                }
            }
        }
        checkRep();
        return result;
    }


    /**
     * Returns all nodes inside this graph.
     * @return returns the label of all node inside <i>this</i>.
     */
    public List<T1> getAllNodes() {
        checkRep();
        List<T1> result = new ArrayList<>();
        result.addAll(nodesInGraph.keySet());
        checkRep();
        return result;
    }

    /**
     * Gets all edges outgoing from a node in this graph.
     * @param origin the label of the node that has edges that
     * are outgoing from it.
     * @spec.requires origin != null and node origin represents is in <i>this</i>.
     * @return all edge labels outgoing from 'origin', ignoring repeated edge
     * labels
     */
    public List<T2> getEdgesOutgoing(T1 origin) {
        checkRep();
        List<T2> result = new ArrayList<>();
        if (origin != null && nodesInGraph.containsKey(origin)) {
            result.addAll(nodesInGraph.get(origin).allEdgeLabels());
        }
        checkRep();
        return result;
    }

    /**
     * Gets all edge's incoming to a target node.
     * @param target the label of the node where we find all edge's incoming to it.
     * @spec.requires target != null and the node target represents is in <i>this</i>.
     * @return all edge labels incoming to 'target', ignoring any repeated edge labels.
     */
    public List<T2> getEdgesIncoming(T1 target) {
        checkRep();
        List<T2> result = new ArrayList<>();
        if(target != null && nodesInGraph.containsKey(target)) {
            Iterator<T1> itr = this.getParentNode(target).iterator();
            Set<T2> container = new HashSet<>();
            while (itr.hasNext()) {
                container.addAll(this.getEdgesBetweenNodes(itr.next(), target));
            }
            result.addAll(container);
        }
        checkRep();
        return result;
    }

    /**
     * Gets all edge labels outgoing from one node and incoming to another.
     * @param parentNode the node label where the edge(s) are outgoing from
     * @param childNode the node label where the edge(s) are incoming to
     * @spec.requires parentNode != null, childNode != null, and the nodes
     * parentNode and childNode represent are in <i>this</i>.
     * @return all edge labels that are outgoing from the node called parentNode
     * that are incoming to the node labeled childNode.
     */
    public List<T2> getEdgesBetweenNodes(T1 parentNode, T1 childNode) {
        checkRep();
        List<T2> result = new ArrayList<>();
        if (parentNode != null && childNode != null && nodesInGraph.containsKey(parentNode)
                && nodesInGraph.containsKey(childNode)) {
            result.addAll(nodesInGraph.get(parentNode).getEdgeLabel(childNode));
        }
        checkRep();
        return result;
    }

    /**
     * Checks if a graph contains a node
     * @param nodeLabel the node we are checking if it's inside the graph
     * @spec.requires nodeLabel != null
     * @return true iff nodeLabel is inside this
     */
    public boolean containsNode(T1 nodeLabel) {
        if (nodeLabel == null) {
            throw new IllegalArgumentException();
        }
        return nodesInGraph.containsKey(nodeLabel);
    }
}
