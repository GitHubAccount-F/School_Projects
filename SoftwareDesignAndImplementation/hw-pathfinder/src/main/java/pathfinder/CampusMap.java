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

package pathfinder;
import graph.*;
import pathfinder.DijkstraAlgorithm.*;
import pathfinder.parser.*;
import pathfinder.datastructures.Path;
import pathfinder.datastructures.Point;
import pathfinder.textInterface.Pathfinder;


import java.util.*;

/**
 * A class used to find the shortest paths between two buildings on the University of Washington Seattle
 * campus.
 */
public class CampusMap implements ModelAPI {

    /*
    Representation invariant:
    -graph doesn't contain null nodes
    -no edges are null
    -container doesn't contain null building names and no null building objects
    -all edges in 'graph' are Double's
    -all nodes in 'graph' are type Point

    Abstraction function:
    -graph contains points that represent paths between the buildings
    -each <key,value> in container has a key that is the short name of a building, with a value that is an object
        containing more information about that building.
     */
    private Graph<Point, Double> graph;
    private Map<String,CampusBuilding> container;
    //private List<String>
    private static final boolean DEBUG = false;

    /**
     * Creates a new CampusMap that stores all the buildings and the paths in-between them
     * on the UW Seattle campus.
     */
    public CampusMap() {
        List<CampusBuilding> buildings = CampusPathsParser.parseCampusBuildings("campus_buildings.csv");
        List<CampusPath> allPaths = CampusPathsParser.parseCampusPaths("campus_paths.csv");
        graph = new Graph<>();
        for(CampusPath paths : allPaths) {
            Point pt1 = new Point(paths.getX1(), paths.getY1());
            Point pt2 = new Point(paths.getX2(), paths.getY2());
            graph.insertNode(new Node<>(pt1));
            graph.insertNode(new Node<>(pt2));
            graph.insertEdge(pt1,pt2,paths.getDistance());
        }
        container = new HashMap<>();
        for(CampusBuilding structures : buildings) {
            container.put(structures.getShortName(),structures);
        }
    }

    /**
     * Ensures that the representation invariant has not been violated. Returns normally if
     * there is no violation.
     */
    private void checkRep() {
        //all other aspects of RI are covered by the Graph class
        assert (!container.containsKey(null));
        if (DEBUG) {
            assert (!container.containsValue(null));
        }
    }
    @Override
    public boolean shortNameExists(String shortName) {
        checkRep();
        return container.containsKey(shortName);
    }

    @Override
    public String longNameForShort(String shortName) {
        checkRep();
        if (!container.containsKey(shortName)) {
            throw new IllegalArgumentException();
        }
        return container.get(shortName).getLongName();
    }

    @Override
    public Map<String, String> buildingNames() {
        checkRep();
        Map<String, String> names = new HashMap<>();
        Iterator<String> itr = container.keySet().iterator();
        while (itr.hasNext()) {
            String shortName = itr.next();
            names.put(shortName, container.get(shortName).getLongName());
        }
        checkRep();
        return names;
    }

    @Override
    public Path<Point> findShortestPath(String startShortName, String endShortName) {
        checkRep();
        if (startShortName == null || endShortName == null ||
                !container.containsKey(startShortName) || !container.containsKey(endShortName) ) {
            throw new IllegalArgumentException();
        }
        Double x1 = container.get(startShortName).getX();
        Double y1 = container.get(startShortName).getY();
        Double x2 = container.get(endShortName).getX();
        Double y2 = container.get(endShortName).getY();
        Point start = new Point(x1,y1);
        Point end = new Point(x2,y2);
        Path<Point> path = DijkstraAlgorithm.algorithm(graph,start, end);
        checkRep();
        return path;
    }

}
