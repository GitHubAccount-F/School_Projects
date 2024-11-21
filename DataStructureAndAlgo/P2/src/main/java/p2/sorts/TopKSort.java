package p2.sorts;

import cse332.exceptions.NotYetImplementedException;
import datastructures.worklists.MinFourHeap;

import java.util.Comparator;

public class TopKSort {
    public static <E extends Comparable<E>> void sort(E[] array, int k) {
        sort(array, k, (x, y) -> x.compareTo(y));
    }

    /**
     * Behaviour is undefined when k > array.length
     */
    public static <E> void sort(E[] array, int k, Comparator<E> comparator) {
        if(k > array.length) {
            k = array.length;
        }
        if(k >= 0 && k <= array.length) {
            MinFourHeap<E> heap = new MinFourHeap<>(comparator);
            for(int i = 0; i < k; i++) {
                heap.add(array[i]);
            }
            for(int i = k; i < array.length; i++) {
                if(comparator.compare(array[i], heap.peek()) > 0) {
                    heap.next();
                    heap.add(array[i]);
                }
            }
            for(int i = 0; i < array.length; i++) {
                if(i < k) {
                    array[i] = heap.next();
                } else {
                    array[i] = null;
                }
            }
        }

    }
}
