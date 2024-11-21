package p2.sorts;

import cse332.exceptions.NotYetImplementedException;

import java.util.Comparator;

public class QuickSort {
    public static <E extends Comparable<E>> void sort(E[] array) {
        QuickSort.sort(array, (x, y) -> x.compareTo(y));
    }

    public static <E> void sort(E[] array, Comparator<E> comparator) {
        recursiveSort(array, comparator, 0, array.length - 1);
    }

    private static <E> void recursiveSort(E[] array, Comparator<E> comparator, int low, int hi) {
        if(low < hi) {
            //test to make sure we aren't swapping when there is 2 elements left that are in order
            if (!((hi - low) == 1 && comparator.compare(array[low],array[hi]) < 0)) {
                int pivot = selectingPivot(array, low, hi, comparator);
                //swaps the pivot to the front
                E temp = array[low];
                array[low] = array[pivot];
                array[pivot] = temp;
                int i = low + 1;
                int j = hi;
                while(i < j) {
                    if(comparator.compare(array[j], array[low]) > 0) {
                        j--;
                    } else if(comparator.compare(array[i], array[low]) <= 0) {
                        i++;
                    } else {
                        E temp2 = array[i];
                        array[i] = array[j];
                        array[j] = temp2;
                    }
                }
                //swapping pivot back to middle
                E temp3 = array[i];
                array[i] = array[low];
                array[low] = temp3;
                recursiveSort(array, comparator, low,i - 1);
                recursiveSort(array, comparator, i + 1,hi);
            }

        }
    }

    private static <E> int selectingPivot(E[] array, int low, int hi, Comparator<E> comparator) {
        E front = array[low];
        E end = array[hi];
        E middle = array[(hi + low) / 2];
        int lowest = low;
        int highest = hi;
        if(comparator.compare(middle,array[lowest]) < 0) {
            lowest = (hi + low) / 2;
        }
        if(comparator.compare(end,array[lowest]) < 0) {
            lowest = hi;
        }
        if(comparator.compare(front,array[highest]) > 0) {
            highest = low;
        }
        if(comparator.compare(middle,array[highest]) > 0) {
            highest = (hi + low) / 2;
        }
        if(highest == hi) {
            if(lowest == low) {
                return ((hi + low) / 2);
            } else {
                return low;
            }
        } else if(highest == ((hi + low) / 2)) {
            if(lowest == low) {
                return hi;
            } else {
                return low;
            }
        } else {
            if(lowest == hi) {
                return ((hi + low) / 2);
            } else {
                return hi;
            }
        }

    }
}
