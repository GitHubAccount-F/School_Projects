package datastructures.worklists;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.worklists.PriorityWorkList;

import java.util.NoSuchElementException;

/**
 * See cse332/interfaces/worklists/PriorityWorkList.java
 * for method specifications.
 */
public class MinFourHeapComparable<E extends Comparable<E>> extends PriorityWorkList<E> {
    /* Do not change the name of this field; the tests rely on it to work correctly. */
    private E[] data; //stores our work

    private int size; //keeps track of how many work we have left

    @SuppressWarnings("unchecked")
    public MinFourHeapComparable() {
        data = (E[])new Comparable[10];
        //used -1 to represent us having no items at the start
        size = -1;
    }

    /**
     * Returns true iff this worklist has any remaining work
     *
     * @return true iff there is at least one piece of work in the worklist.
     */
    @Override
    public boolean hasWork() {
        return (this.size > -1);
    }

    /**
     * Adds work to the worklist. This method should conform to any additional
     * contracts that the particular type of worklist has.
     *
     * @param work
     *            the work to add to the worklist
     */
    @SuppressWarnings("unchecked")
    @Override
    public void add(E work) {
        //when we meet the capacity
        if (size == data.length - 1) {
            E[] temp = (E[])new Comparable[data.length*2];
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
        while(hole > 0 && value.compareTo(data[(hole-1)/4]) < 0) {
            data[hole] = data[(hole-1)/4]; //swaps parent and child
            hole = (hole-1)/4;
        }
        return hole;
    }

    /**
     * Returns a view to the next element of the worklist.
     *
     * @precondition hasWork() is true
     * @postcondition return(peek()) is return(next())
     * @postcondition the structure of this worklist remains unchanged.
     * @throws NoSuchElementException
     *             if hasWork() is false
     * @return the next element in this worklist
     */
    @Override
    public E peek() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        return data[0];
    }

    /**
     * Returns and removes the next element of the worklist
     *
     * @precondition hasWork() is true
     * @postcondition return(next()) + after(next()) == before(next())
     * @postcondition after(size()) + 1 == before(size())
     * @throws NoSuchElementException
     *             if hasWork() is false
     * @return the next element in this worklist
     */
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
            if(second <= size && data[second].compareTo(data[target]) < 0) {
                target = second;
            }
            if(third <= size && data[third].compareTo(data[target]) < 0) {
                target = third;
            }
            if(fourth <= size && data[fourth].compareTo(data[target]) < 0) {
                target = fourth;
            }
            if (data[target].compareTo(value) < 0) {
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

    /**
     * Returns the number of elements of work remaining in this worklist
     *
     * @return the size of this worklist
     */
    @Override
    public int size() {
        //since we are counting from index 0, and
        //the size variable represents the current spot
        //in the array that is filled, we need to add 1
        //to the size to get the total amount of work left
        return size + 1;
    }

    /**
     * Resets this worklist to the same state it was in right after
     * construction.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        data = (E[])new Comparable[10];
        size = -1;
    }
}
