package datastructures.worklists;

import cse332.interfaces.worklists.LIFOWorkList;

import java.util.NoSuchElementException;

/**
 * See cse332/interfaces/worklists/LIFOWorkList.java
 * for method specifications.
 */
public class ArrayStack<E> extends LIFOWorkList<E> {
    //Holds our array
    private E[] array;
    //Used for keeping track of how many elements are used
    //up in the array. The num it's currently on means all
    //positions before it have been filled
    private int currentNum;

    @SuppressWarnings("unchecked")
    public ArrayStack() {
        this.array = (E[])new Object[10];
        currentNum = 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(E work) {
        if (currentNum == array.length) {
            E[] temp = (E[])new Object[array.length * 2];
            for(int i = 0; i < array.length; i++) {
                temp[i] = array[i];
            }
            temp[array.length] = work;
            array = temp;
        } else {
            array[currentNum] = work;
        }
        currentNum++;
    }

    @Override
    public E peek() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        return array[currentNum - 1];
    }

    @Override
    public E next() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        E value = array[currentNum - 1];
        currentNum--;
        return value;
    }

    @Override
    public int size() {
        return currentNum;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        array = (E[])new Object[10];
        currentNum = 0;
    }
}
