package datastructures.worklists;

import cse332.interfaces.worklists.FIFOWorkList;

import java.util.NoSuchElementException;

/**
 * See cse332/interfaces/worklists/FIFOWorkList.java
 * for method specifications.
 */
public class ListFIFOQueue<E> extends FIFOWorkList<E> {

    //stores the front of the queue
    private ListNode front;
    //stores the end of the queue
    private ListNode end;
    private int size;

    //My inner Node class
    class ListNode {
        public E data;
        public ListNode next;

        public ListNode(E data, ListNode next) {
            this.data = data;
            this.next = next;
        }
    }
    public ListFIFOQueue() {
        this.front = null;
        this.end = null;
        this.size = 0;
    }

    @Override
    public void add(E work) {
        if(this.front == null && this.end == null) {
            ListNode first = new ListNode(work,null);
            this.front = first;
            this.end = first;
            size++;
        } else {
            this.end.next = new ListNode(work, null);
            this.end = this.end.next;
            size++;
        }
    }

    @Override
    public E peek() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        return this.front.data;
    }

    @Override
    public E next() {
        if(!hasWork()) {
            throw new NoSuchElementException();
        }
        E data = this.front.data;
        this.front = front.next;
        //when 1 work left, that is considered both
        //the front and end of the queue
        if(size == 1) {
            this.end = this.front;
        }
        size--;
        return data;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.front = null;
        this.end = null;
    }
}
