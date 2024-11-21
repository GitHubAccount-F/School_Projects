package datastructures.worklists;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.worklists.PriorityWorkList;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * See cse332/interfaces/worklists/PriorityWorkList.java
 * for method specifications.
 */
public class MinFourHeap<E> extends PriorityWorkList<E> {
    /* Do not change the name of this field; the tests rely on it to work correctly. */
    private E[] data;

    private int size; //keeps track of how many work we have left

    private Comparator<E> comparator;

    @SuppressWarnings("unchecked")
    public MinFourHeap(Comparator<E> c) {
        comparator = c;
        data = (E[]) new Object[10];
        //used -1 to represent us having no items at the start
        size = -1;
    }

    @Override
    public boolean hasWork() {
        return (this.size > -1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(E work) {
        //when we meet the capacity
        if (size == data.length - 1) {
            E[] temp = (E[])new Object[data.length*2];
            for (int i = 0; i < data.length; i++) {
                temp[i] = data[i];
            }
            data = temp;
        }
        size++;
        //find the appropriate spot to put 'work' in  our arr
        int spotToPlaceValue = percolateUp(size, work);
        data[spotToPlaceValue] = work;
    }

    private int percolateUp(int hole, E value) {
        //keeps comparing parent and child until we find the
        //appropriate place to put our value
        //parent of an index is found using (index - 1)/4
        while(hole > 0 && comparator.compare(value,data[(hole-1)/4])  < 0) {
            data[hole] = data[(hole-1)/4]; //swaps parent and child
            hole = (hole-1)/4;
        }
        return hole;
    }

    @Override
    public E peek() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        return data[0];
    }

    @Override
    public E next() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        E answer = data[0];
        int hole = percolateDown(0, data[size]);
        data[hole] = data[size];
        size--;
        return answer;
    }

    private int percolateDown(int hole, E value) {
        while(hole*4+1 <= size) {
            int first = hole*4+1; //calculates the position of the 1st child
            int second = hole*4+2;//calculates the position of the 2nd child
            int third = hole*4+3; //calculates the position of the 3rd child
            int fourth = hole*4+4; //calculates the position of the 4th child
            int target = first;
            //these if blocks find which child of a parent(if it exists), has
            //the smallest priority
            if(second <= size && comparator.compare(data[second],data[target]) < 0) {
                target = second;
            }
            if(third <= size && comparator.compare(data[third],data[target]) < 0) {
                target = third;
            }
            if(fourth <= size && comparator.compare(data[fourth],data[target]) < 0) {
                target = fourth;
            }
            if (comparator.compare(data[target],value) < 0) {
                data[hole] = data[target];
                hole = target;
                //else block is when we found the right place
                //to put the value
            } else {
                break;
            }
        }
        return hole;
    }

    @Override
    public int size() {
        return size + 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        data = (E[])new Object[10];
        size = -1;
    }
}
