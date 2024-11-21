package solvers;

import cse332.exceptions.NotYetImplementedException;
import cse332.graph.GraphUtil;
import cse332.interfaces.BellmanFordSolver;
import main.Parser;
import paralleltasks.ArrayCopyTask;
import paralleltasks.RelaxOutTaskBad;

import java.util.List;
import java.util.Map;

public class OutParallelBad implements BellmanFordSolver {

    public List<Integer> solve(int[][] adjMatrix, int source) {
        List<Map<Integer, Integer>> g = Parser.parse(adjMatrix);

        int[] pred = new int[g.size()];
        //computes distance from source for each vertex
        int[] dist = new int[g.size()];
        int[] dist_copy = new int[g.size()];
        for (int i = 0; i < g.size(); i++) {
            dist[i] = GraphUtil.INF;
            pred[i] = -1;
        }
        dist[source] = 0;
        for (int i = 0; i < g.size(); i++) {
            //parallel array copying
            dist_copy = ArrayCopyTask.copy(dist);


            //this below is for relaxing the edges,
            //but using parallelism to do each vertex separately

            RelaxOutTaskBad.parallel(g, dist, dist_copy, pred);
        }
        List<Integer> predArr = GraphUtil.getCycle(pred);
        return predArr;
    }
}
