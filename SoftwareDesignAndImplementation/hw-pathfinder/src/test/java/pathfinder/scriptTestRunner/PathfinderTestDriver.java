/*
 * Copyright (C) 2023 Hal Perkins.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 331 for use solely during Winter Quarter 2023 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

package pathfinder.scriptTestRunner;

import graph.Graph;
import graph.Node;
import pathfinder.DijkstraAlgorithm;
import pathfinder.datastructures.Path;

import java.io.*;
import java.util.*;

/**
 * This class implements a test driver that uses a script file format
 * to test an implementation of Dijkstra's algorithm on a graph.
 */
public class PathfinderTestDriver {
    private final Map<String, Graph<String,Double>> graphs = new HashMap<>();
    private final PrintWriter output;
    private final BufferedReader input;

    // Leave this constructor public
    public PathfinderTestDriver(Reader r, Writer w) {
        input = new BufferedReader(r);
        output = new PrintWriter(w);
    }

    // Leave this method public
    public void runTests() throws IOException {
        String inputLine;
        while((inputLine = input.readLine()) != null) {
            if((inputLine.trim().length() == 0) ||
                    (inputLine.charAt(0) == '#')) {
                // echo blank and comment lines
                output.println(inputLine);
            } else {
                // separate the input line on white space
                StringTokenizer st = new StringTokenizer(inputLine);
                if(st.hasMoreTokens()) {
                    String command = st.nextToken();

                    List<String> arguments = new ArrayList<>();
                    while(st.hasMoreTokens()) {
                        arguments.add(st.nextToken());
                    }

                    executeCommand(command, arguments);
                }
            }
            output.flush();
        }
    }
    private void executeCommand(String command, List<String> arguments) {
        try {
            switch(command) {
                case "CreateGraph":
                    createGraph(arguments);
                    break;
                case "AddNode":
                    addNode(arguments);
                    break;
                case "AddEdge":
                    addEdge(arguments);
                    break;
                case "ListNodes":
                    listNodes(arguments);
                    break;
                case "ListChildren":
                    listChildren(arguments);
                    break;
                case "FindPath":
                    findPath(arguments);
                    break;
                default:
                    output.println("Unrecognized command: " + command);
                    break;
            }
        } catch(Exception e) {
            String formattedCommand = command;
            formattedCommand += arguments.stream().reduce("", (a, b) -> a + " " + b);
            output.println("Exception while running command: " + formattedCommand);
            e.printStackTrace(output);
        }
    }

    private void createGraph(List<String> arguments) {
        if(arguments.size() != 1) {
            throw new CommandException("Bad arguments to CreateGraph: " + arguments);
        }

        String graphName = arguments.get(0);
        createGraph(graphName);
    }

    private void createGraph(String graphName) {
        Graph<String, Double> testGraph = new Graph<>();
        graphs.put(graphName, testGraph);
        output.println("created graph " + graphName);
    }

    private void addNode(List<String> arguments) {
        if(arguments.size() != 2) {
            throw new CommandException("Bad arguments to AddNode: " + arguments);
        }

        String graphName = arguments.get(0);
        String nodeName = arguments.get(1);

        addNode(graphName, nodeName);
    }

    private void addNode(String graphName, String nodeName) {
        Graph<String,Double> testGraph = graphs.get(graphName);
        testGraph.insertNode(new Node<String,Double>(nodeName));
        output.println("added node " + nodeName + " to " + graphName);
    }

    private void addEdge(List<String> arguments) {
        if(arguments.size() != 4) {
            throw new CommandException("Bad arguments to AddEdge: " + arguments);
        }

        String graphName = arguments.get(0);
        String parentName = arguments.get(1);
        String childName = arguments.get(2);
        double edgeLabel = Double.parseDouble(arguments.get(3));

        addEdge(graphName, parentName, childName, edgeLabel);
    }

    private void addEdge(String graphName, String parentName, String childName,
                         double edgeLabel) {
        Graph<String,Double> testGraph = graphs.get(graphName);
        testGraph.insertEdge(parentName,childName,edgeLabel);
        output.println("added edge " + String.format("%.3f",edgeLabel) +
                " from " + parentName + " to " + childName + " in " + graphName);
    }

    private void listNodes(List<String> arguments) {
        if(arguments.size() != 1) {
            throw new CommandException("Bad arguments to ListNodes: " + arguments);
        }

        String graphName = arguments.get(0);
        listNodes(graphName);
    }

    private void listNodes(String graphName) {
        Graph<String,Double> testGraph = graphs.get(graphName);
        Set<String> labels = new TreeSet<>();
        labels.addAll(testGraph.getAllNodes());
        String containsNodes = "";
        if (!labels.isEmpty()) {
            Iterator<String> itr = labels.iterator();
            while (itr.hasNext()) {
                containsNodes = containsNodes + " " + itr.next();
            }
        }
        output.println(graphName + " contains:" + containsNodes);

    }

    private void listChildren(List<String> arguments) {
        if(arguments.size() != 2) {
            throw new CommandException("Bad arguments to ListChildren: " + arguments);
        }

        String graphName = arguments.get(0);
        String parentName = arguments.get(1);
        listChildren(graphName, parentName);
    }

    private void listChildren(String graphName, String parentName) {
        Graph<String,Double> testGraph = graphs.get(graphName);
        Set<String> childNodes = new TreeSet<>();
        childNodes.addAll(testGraph.getChildNode(parentName));
        Map<String, Set<Double>> container = new TreeMap<>();
        Iterator<String> itr = childNodes.iterator();
        //Retrieves information on child nodes and their edges from 'parentName',
        // and stores this
        while (itr.hasNext()) {
            String nodeLabel = itr.next();
            container.put(nodeLabel, new TreeSet<>());
            List<Double> list1 = graphs.get(graphName).getEdgesBetweenNodes(parentName,nodeLabel);
            container.get(nodeLabel).addAll(list1);
        }
        //Begins to create a string that has the child nodes and their respective edges.
        //This will be in sorted order(both the nodes and their edges if a child has multiple nodes
        //going to it)
        String listTheChilds = "";
        Iterator<String> itr2 = container.keySet().iterator();
        while (itr2.hasNext()) {
            String childNode = itr2.next();
            Iterator<Double> itr3 = container.get(childNode).iterator();
            while (itr3.hasNext()) {
                listTheChilds = listTheChilds + " " + childNode +
                        "(" + String.format("%.3f", itr3.next()) + ")";
            }
        }
        output.println("the children of " + parentName + " in " + graphName + " are:" + listTheChilds);
    }

    private void findPath(List<String> arguments) {
        if(arguments.size() != 3) {
            throw new CommandException("Bad arguments to LoadGraph: " + arguments);
        }
        String graphName = arguments.get(0);
        String nodeA = arguments.get(1);
        String nodeB = arguments.get(2);
        findPath(graphName, nodeA, nodeB);
    }

    private void findPath(String graphName, String nodeA, String nodeB) {
        Graph<String,Double> dijkstra = graphs.get(graphName);
        Path<String> pathing = DijkstraAlgorithm.algorithm(dijkstra,nodeA,nodeB);
        if (!dijkstra.containsNode(nodeA)) {
            output.println("unknown: " + nodeA);
        }
        if (!dijkstra.containsNode(nodeB)) {
            output.println("unknown: " + nodeB);
        }
        else if (pathing == null && dijkstra.containsNode(nodeA) && dijkstra.containsNode(nodeB)) {
            output.println("path from " + nodeA + " to " + nodeB + ":");
            output.println("no path found");
        }
        else if (pathing != null && dijkstra.containsNode(nodeA) && dijkstra.containsNode(nodeB)){
            Iterator<Path<String>.Segment> itr = pathing.iterator();
            output.println("path from " + nodeA + " to " + nodeB + ":");
            while (itr.hasNext()) {
                Path<String>.Segment segment = itr.next();
                output.println(segment.getStart() + " to " +
                        segment.getEnd() + " with weight " + String.format("%.3f",segment.getCost()));
            }
            output.println(String.format("total cost: %.3f", pathing.getCost()));
        }
    }

    /**
     * This exception results when the input file cannot be parsed properly
     **/
    static class CommandException extends RuntimeException {

        public CommandException() {
            super();
        }

        public CommandException(String s) {
            super(s);
        }

        public static final long serialVersionUID = 3495;
    }
}
