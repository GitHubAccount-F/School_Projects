package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.graph.GraphUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class RelaxInTask extends RecursiveAction {

    public static final ForkJoinPool pool = new ForkJoinPool();
    public static final int CUTOFF = 1;
    private List<Map<Integer, Integer>> src;
    private int[] dist;
    private int[]dist_copy;
    private int[]pred;

    private int hi, lo;

    public RelaxInTask(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy, int[]pred, int hi, int lo) {
        this.src = src;
        this.dist = dist;
        this.dist_copy = dist_copy;
        this.pred = pred;
        this.hi = hi;
        this.lo = lo;
    }

    protected void compute() {
        if(hi - lo <= CUTOFF) {
            sequential(src, dist, dist_copy, pred, hi, lo);
        } else {
            int mid = lo + (hi - lo) / 2;
            RelaxInTask left = new RelaxInTask(src, dist, dist_copy, pred, mid, lo);
            RelaxInTask right = new RelaxInTask(src, dist, dist_copy, pred, hi, mid);
            left.fork();
            right.compute();
            left.join();
        }
    }

    public static void sequential(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy,
                                  int[]pred, int hi, int lo) {
        for(int i = lo; i < hi; i++) {
            for(int w: src.get(i).keySet()) { //all inward edges
                int inwardWeight = src.get(i).get(w); //edge going to w
                if(dist_copy[w] != GraphUtil.INF) {
                    if(dist_copy[w] + inwardWeight < dist[i]) {
                        //Found a shorter path to w
                        dist[i] = dist_copy[w] + inwardWeight;
                        pred[i] = w;
                    }
                }
            }
        }
    }

    public static void parallel(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy, int[]pred) {
        pool.invoke(new RelaxInTask(src, dist, dist_copy, pred, src.size(), 0));
    }

}
