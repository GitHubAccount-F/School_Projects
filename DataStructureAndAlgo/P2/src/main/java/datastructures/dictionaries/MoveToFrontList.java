package datastructures.dictionaries;

import cse332.datastructures.containers.Item;
import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.misc.DeletelessDictionary;
import cse332.interfaces.misc.SimpleIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 1. The list is typically not sorted.
 * 2. Add new items to the front of the list.
 * 3. Whenever find or insert is called on an existing key, move it
 * to the front of the list. This means you remove the node from its
 * current position and make it the first node in the list.
 * 4. You need to implement an iterator. The iterator SHOULD NOT move
 * elements to the front.  The iterator should return elements in
 * the order they are stored in the list, starting with the first
 * element in the list. When implementing your iterator, you should
 * NOT copy every item to another dictionary/list and return that
 * dictionary/list's iterator.
 */
public class MoveToFrontList<K, V> extends DeletelessDictionary<K, V> {

    //personal node class where key node has a
    //key-value pair
    public class ListNode {
        public K key;
        public V value;

        public ListNode next;

        public ListNode(K key, V value, ListNode next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private ListNode front;
    public MoveToFrontList() {
        front = null;
        super.size = 0;
    }
    /*
    Associates the specified value with the specified key in this map.
    If the map previously contained a mapping for the key, the old value is replaced.
    Params: key – key with which the specified value is to be
           associated value – value to be associated with the specified key
    Returns:the previous value associated with key, or null if
            there was no mapping for key.
    Throws: IllegalArgumentException – if either key or value is null.
     */
    @Override
    public V insert(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        //holds previous value for the given key if we find it
        V previousValue = null;
        //handles the front case
        if (front != null && front.key.equals(key)) {
            previousValue = front.value;
            front.value = value;
            return previousValue;
        }
        ListNode temp = front;
        ListNode holdNode = null; //holds previous key node if we find it
        while(temp != null && temp.next != null) {
            if(temp.next.key.equals(key)) {
                previousValue = temp.next.value;
                holdNode = temp.next;
                //handles middle case
                if(temp.next.next != null) {
                    temp.next = temp.next.next;
                    //handles end case
                } else {
                    temp.next = null;
                }
                holdNode.next = front;
                //we place the node with the key in the front of the list
                front = holdNode;
                front.value = value;
                return previousValue;
            }
            //moves onto next node if it isn't the key we want
            temp = temp.next;
        }
        //case when the key wasn't already in the list
        //or list was empty
        front = new ListNode(key, value, front);
        //size++ not included inside while loop because in there, a
        // copy of the key was found, so we don't need to make another
        // node again. We do it here when we know the key
        //doesn't exist
        size++;
        return null;
    }
    /*
    Returns the value to which the specified key is mapped, or null if this
    map contains no mapping for the key.
    Params: key – the key whose associated value is to be returned
    Returns: the value to which the specified key is mapped, or null if
             this map contains no mapping for the key
    Throws: IllegalArgumentException – if key is null.
    */
    @Override
    public V find(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        V valueOfKey = null;
        //handles front case
        if(front != null && front.key.equals(key)) {
            return front.value;
        }
        ListNode temp = front;
        while(temp != null && temp.next != null) {
            if(temp.next.key.equals(key)) {
                valueOfKey = temp.next.value; //holds the previous value for the key
                ListNode hold = temp.next; //holds the node of the key
                //handles middle case
                if(temp.next.next != null) {
                    temp.next = temp.next.next;
                    //handles end case
                } else {
                    temp.next = null;
                }
                //we place the node with the key in the front of the list
                hold.next = front;
                front = hold;
                break; //leaves loop because we found the key
            }
            temp = temp.next;
        }
        return valueOfKey;
    }

    @Override
    public Iterator<Item<K, V>> iterator() {
        return new FrontListIterator();
    }

    public class FrontListIterator extends SimpleIterator<Item<K,V>> {
        private ListNode current;

        public FrontListIterator() {
            //the same front used to store our linked list in our class
            current = front;
        }
        @Override
        public boolean hasNext() {
            return (current != null);
        }

        @Override
        public Item<K, V> next() {
            if(current == null) {
                throw new NoSuchElementException();
            }
            Item<K,V> value = new Item<>(this.current.key, this.current.value);
            current = current.next;
            return value;
        }
    }
}
