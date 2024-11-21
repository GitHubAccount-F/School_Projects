package datastructures.worklists;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.worklists.FixedSizeFIFOWorkList;

import java.util.NoSuchElementException;

/**
 * See cse332/interfaces/worklists/FixedSizeFIFOWorkList.java
 * for method specifications.
 */
public class CircularArrayFIFOQueue<E> extends FixedSizeFIFOWorkList<E> {

    //front of where all our values are stored in the array
    private int front;

    //1 position off from the end of where our values are stored in array
    private int end;
    //stores our work in a list
    private E[] array;

    //stores number of work to be done
    private int workToBeDone;

    @SuppressWarnings("unchecked")
    public CircularArrayFIFOQueue(int capacity) {
        super(capacity);
        array = (E[])new Comparable[capacity];
        front = 0;
        end = 0;
        workToBeDone = 0;
    }

    @Override
    public void add(E work) {
        if(isFull()) {
            throw new IllegalStateException();
        }
        array[end] = work;
        end = (end + 1) % capacity();
        workToBeDone++;
    }

    @Override
    public E peek() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        return array[front];
    }

    @Override
    public E peek(int i) {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        if(i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException();
        }
        //accounts for if we need to loop back around to start of array
        return array[(front + i) % capacity()];
    }

    @Override
    public E next() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        E value = array[front];
        front = (front + 1) % capacity();
        workToBeDone--;
        return value;
    }

    @Override
    public void update(int i, E value) {
        if (!hasWork()) {
            throw new NoSuchElementException();
        }
        if(i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException();
        }
        array[(front + i) % capacity()] = value;
    }

    @Override
    public int size() {
        return workToBeDone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        array = (E[])new Comparable[capacity()];
        front = 0;
        end = 0;
        workToBeDone = 0;
    }

    @Override
    public int compareTo(FixedSizeFIFOWorkList<E> other) {
        // You will implement this method in project 2. Leave this method unchanged for project 1.
        throw new NotYetImplementedException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        // You will finish implementing this method in project 2. Leave this method unchanged for project 1.
        if (this == obj) {
            return true;
        } else if (!(obj instanceof FixedSizeFIFOWorkList<?>)) {
            return false;
        } else {
            // Uncomment the line below for p2 when you implement equals
            // FixedSizeFIFOWorkList<E> other = (FixedSizeFIFOWorkList<E>) obj;

            // Your code goes here

            throw new NotYetImplementedException();
        }
    }

    @Override
    public int hashCode() {
        // You will implement this method in project 2. Leave this method unchanged for project 1.
        throw new NotYetImplementedException();
    }
}
