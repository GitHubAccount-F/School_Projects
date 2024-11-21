package solvers;

import cse332.exceptions.NotYetImplementedException;
import cse332.graph.GraphUtil;
import cse332.interfaces.BellmanFordSolver;
import main.Parser;

import java.util.List;
import java.util.Map;

public class OutSequential implements BellmanFordSolver {

    @SuppressWarnings("ManualArrayCopy")
    public List<Integer> solve(int[][] adjMatrix, int source) {
        List<Map<Integer, Integer>> g = Parser.parse(adjMatrix);
        //keeps track of predecessor for each vertex
        int[] pred = new int[g.size()];
        //computes distance from source for each vertex
        int[] dist = new int[g.size()];
        int[] dist_copy = new int[g.size()];
        for(int i = 0; i < g.size(); i++) {
            dist[i] = GraphUtil.INF;
            pred[i] = -1;
        }
        dist[source] = 0;
        for(int i = 0; i < g.size(); i++) {
            for(int j = 0; j < g.size(); j++) {
                dist_copy[j] = dist[j];
            }
            //this below is for relaxing the edges
            for(int v = 0; v < g.size(); v++) {//all vertexes
                for(int w: g.get(v).keySet()) {//all outgoing edges for each vertex
                    int weight = g.get(v).get(w);
                    if(dist_copy[v] != GraphUtil.INF) {
                        if(dist_copy[v] + weight < dist[w]) {
                            //Found a shorter path to w
                            dist[w] = dist_copy[v] + weight;
                            pred[w] = v;
                        }
                    }
                }
            }
        }
        List<Integer> predArr = GraphUtil.getCycle(pred);
        return predArr;
    }

}
