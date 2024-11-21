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

package campuspaths;

import campuspaths.utils.CORSFilter;
import pathfinder.CampusMap;
import pathfinder.ModelAPI;
import spark.Spark;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;
import pathfinder.datastructures.Path;
import pathfinder.datastructures.Point;
import java.util.*;

public class SparkServer {

    public static void main(String[] args) {
        CORSFilter corsFilter = new CORSFilter();
        corsFilter.apply();
        ModelAPI model = new CampusMap();
        // The above two lines help set up some settings that allow the
        // React application to make requests to the Spark server, even though it
        // comes from a different server.
        // You should leave these two lines at the very beginning of main().

        // TODO: Create all the Spark Java routes you need here.
        Spark.get("/path", new Route () {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String startString = request.queryParams("start");
                String endString = request.queryParams("end");
                Path<Point> shortestPath = model.findShortestPath(startString,endString);
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(shortestPath);
                return jsonResponse;
            }
        });

        Spark.get("/buildings", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                //Holds the short to long name of the buildings
                Map<String, String> shrtLngNam = model.buildingNames();
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(shrtLngNam);
                return jsonResponse;
            }
        });

        Spark.get("/hello", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                //Holds the short to long name of the buildings
                String hello = "Hello";
                Gson gson = new Gson();

                return gson.toJson(hello);
            }
        });
    }

}
