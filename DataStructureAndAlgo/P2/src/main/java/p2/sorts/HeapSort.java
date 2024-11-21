package p2.sorts;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.worklists.WorkList;
import datastructures.worklists.MinFourHeap;

import java.util.Comparator;

public class HeapSort {
    public static <E extends Comparable<E>> void sort(E[] array) {
        sort(array, (x, y) -> x.compareTo(y));
    }

    public static <E> void sort(E[] array, Comparator<E> comparator) {
        WorkList<E> sorting = new MinFourHeap<>(comparator);
        for(int i = 0; i < array.length; i++) {
            sorting.add(array[i]);
        }
        for(int i = 0; i < array.length; i++) {
            array[i] = sorting.next();
        }
    }
}
