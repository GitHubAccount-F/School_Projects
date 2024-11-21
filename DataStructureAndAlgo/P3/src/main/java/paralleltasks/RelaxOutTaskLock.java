package paralleltasks;

import cse332.exceptions.NotYetImplementedException;
import cse332.graph.GraphUtil;
import solvers.OutParallelLock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RelaxOutTaskLock extends RecursiveAction {

    public static final ForkJoinPool pool = new ForkJoinPool();
    public static final int CUTOFF = 1;

    private final List<Map<Integer, Integer>> src;
    private int[] dist;
    private int[]dist_copy;
    private int[]pred;

    private final int hi, lo;



    public RelaxOutTaskLock(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy, int[]pred, int hi, int lo) {
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
            RelaxOutTaskLock left = new RelaxOutTaskLock(src, dist, dist_copy, pred, mid, lo);
            RelaxOutTaskLock right = new RelaxOutTaskLock(src, dist, dist_copy, pred, hi, mid);
            left.fork();
            right.compute();
            left.join();
        }
    }

    public static void sequential(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy,
                                  int[]pred, int hi, int lo) {
        for(int i = lo; i < hi; i++) {
            OutParallelLock.arrLocks[i].lock();
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
            OutParallelLock.arrLocks[i].unlock();
        }
    }

    public static void parallel(List<Map<Integer, Integer>> src, int[] dist, int[]dist_copy, int[]pred) {
        pool.invoke(new RelaxOutTaskLock(src, dist, dist_copy, pred, src.size(), 0));
    }
}
