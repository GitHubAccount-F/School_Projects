package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.graph.GraphUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class RelaxOutTaskBad extends RecursiveAction {

    public static final ForkJoinPool pool = new ForkJoinPool();
    public static final int CUTOFF = 1;

    private List<Map<Integer, Integer>> src;
    private int[] dist;
    private int[]dist_copy;
    private int[]pred;

    private int hi, lo;

    //constructor
    public RelaxOutTaskBad(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy, int[]pred, int hi, int lo) {
        this.src = src;
        this.dist = dist;
        this.dist_copy = dist_copy;
        this.pred = pred;
        this.hi = hi;
        this.lo = lo;
    }

    //computing
    protected void compute() {
        if(hi - lo <= CUTOFF) {
            sequential(src, dist, dist_copy, pred, hi, lo);
        } else {
            int mid = lo + (hi - lo) / 2;
            RelaxOutTaskBad left = new RelaxOutTaskBad(src, dist, dist_copy, pred, mid, lo);
            RelaxOutTaskBad right = new RelaxOutTaskBad(src, dist, dist_copy, pred, hi, mid);
            left.fork();
            right.compute();
            left.join();
        }
    }

    //sequential case
    public static void sequential(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy,
                                  int[]pred, int hi, int lo) {
        for(int i = lo; i < hi; i++) {
            for(int w: src.get(i).keySet()) {
                int weight = src.get(i).get(w);
                if(dist_copy[i] != GraphUtil.INF) {
                    if(dist_copy[i] + weight < dist[w]) {
                        //Found a shorter path to w
                        dist[w] = dist_copy[i] + weight;
                        pred[w] = i;
                    }
                }
            }
        }
    }

    //where I call RelexOutTasBad and invoke
    public static void parallel(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy, int[]pred) {
        pool.invoke(new RelaxOutTaskBad(src, dist, dist_copy, pred, src.size(), 0));
    }

}
