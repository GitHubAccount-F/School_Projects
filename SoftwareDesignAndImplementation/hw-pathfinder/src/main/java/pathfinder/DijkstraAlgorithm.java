package pathfinder;

import graph.*;
import pathfinder.datastructures.Path;

import java.util.*;
import java.lang.*;

/**
 * Class uses the Dijkstra Algorithm to find the least weighted path between two destinations on a
 * graph. This assumes the edges/distances between two destinations are type double.
 */
public class DijkstraAlgorithm {

    //This class has no RI or AF as it's not an ADT

    /**
     * Finds the least weighted path between two destinations on a graph.
     * @param graph the graph containing the two destinations
     * @param start where we are starting from in a graph
     * @param end where we are stopping at inside of a graph
     * @spec.requires 'graph' contains 'start' and 'end'. Also, 'graph', 'start', and
     * 'end' != null
     * @return the least weighted path between two destinations. If no path is found or
     * 'graph' doesn't contain 'start' and 'end', then null is returned
     * @param <T1> describes the type of the nodes in a graph
     */
    public static <T1>
    Path<T1> algorithm(Graph<T1,Double> graph, T1 start, T1 end) {
        if (graph == null || start == null || end == null) {
            throw new IllegalArgumentException();
        }
        if (graph.containsNode(start) && graph.containsNode(end)) {
            Queue<Path<T1>> active = new PriorityQueue<>(Comparator.comparingDouble(Path<T1>::getCost));
            //change to set
            Set<T1> finished = new HashSet<>();
            Path<T1> firstPath = new Path<>(start);
            active.add(firstPath);

            while (!active.isEmpty()) {
                Path<T1> minPath = active.remove();
                T1 minDest = minPath.getEnd();

                if (minDest.equals(end)) {
                    return minPath;
                }

                if (!finished.contains(minDest)) {
                    //gets all child nodes of minDest
                    List<T1> childNodes = graph.getChildNode(minDest);
                    Iterator<T1> itr = childNodes.iterator();
                    while (itr.hasNext()) {
                        T1 child = itr.next();
                        //If 'finished' doesn't contain the child node, meaning its
                        //least cost path wasn't found yet, then we add minDest with each
                        //of its edges to the child node to 'active'
                        if (!finished.contains(child)) {
                            //looks through all edges between minDest and each child node
                            List<Double> edgesBetween = graph.getEdgesBetweenNodes(minDest,child);
                            Iterator<Double> itr2 = edgesBetween.iterator();
                            while (itr2.hasNext()) {
                                Double cost = itr2.next();
                                //creates a new path to that child node
                                Path<T1> addingChildToAct = minPath.extend(child,cost);
                                active.add(addingChildToAct);
                            }
                            finished.add(minDest);
                        }
                    }
                }
            }
        }
        return null;
    }
}
