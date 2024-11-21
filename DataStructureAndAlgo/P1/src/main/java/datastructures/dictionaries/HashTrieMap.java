package datastructures.dictionaries;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.trie.TrieMap;
import cse332.types.BString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * See cse332/interfaces/trie/TrieMap.java
 * and cse332/interfaces/misc/Dictionary.java
 * for method specifications.
 */
public class HashTrieMap<A extends Comparable<A>, K extends BString<A>, V> extends TrieMap<A, K, V> {
    public class HashTrieNode extends TrieNode<Map<A, HashTrieNode>, HashTrieNode> {
        public HashTrieNode() {
            this(null);
        }

        public HashTrieNode(V value) {
            this.pointers = new HashMap<A, HashTrieNode>();
            this.value = value;
        }

        @Override
        public Iterator<Entry<A, HashTrieMap<A, K, V>.HashTrieNode>> iterator() {
            return pointers.entrySet().iterator();
        }
    }

    public HashTrieMap(Class<K> KClass) {
        super(KClass);
        this.root = new HashTrieNode();
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for the key, the old value is
     * replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
     * if there was no mapping for <tt>key</tt>.
     * @throws IllegalArgumentException if either key or value is null.
     */

    @SuppressWarnings("unchecked")
    @Override
    public V insert(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        V valueOfKey = null;
        HashTrieNode temp = (HashTrieNode)this.root;
        Iterator<A> itr = key.iterator();
        //goes through all the nodes to reach the spot where the
        //node representing the key should be at
        for(int i = 0; i < key.size(); i++) {
            A index = itr.next();
            //we are able to access or create the node that
            //represents the key
            if (!temp.pointers.containsKey(index)) {
                temp.pointers.put(index,new HashTrieNode());
            }
            temp = temp.pointers.get(index);
        }
        //once we have arrived at the node with the key,
        //we see if it had any previous values. If so,
        //we only just replace it's value with the new one.
        //But if it was null before, this means we increase
        //the size field to represent having a new key in our
        //TrieMap
        if (temp.value != null) {
            valueOfKey = temp.value;
        } else { //temp.value == null
            this.size++;
        }
        temp.value = value;
        return valueOfKey;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key
     * @throws IllegalArgumentException if key is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public V find(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Iterator<A> itr = key.iterator();
        HashTrieNode temp = (HashTrieNode)this.root;
        while (itr.hasNext()) {
            A index = itr.next();
            //if we don't find the exact keys we want
            //as we descend the nodes, we will return null
            if (!temp.pointers.containsKey(index)) {
                return null;
            } else {
                //shifts to next node
                temp = temp.pointers.get(index);
            }
        }
        //when we reached the node representing the key
        return temp.value;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for which the key
     * starts with the specified key prefix.
     *
     * @param key The prefix of a key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping whose key starts
     * with the specified key prefix.
     * @throws IllegalArgumentException if the key is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean findPrefix(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Iterator<A> itr = key.iterator();
        HashTrieNode temp = (HashTrieNode)this.root;
        //deals with case when we find an empty string when our map is empty
        if(!itr.hasNext() && temp.pointers.size() == 0 && temp.value == null) {
            return false;
        }
        while (itr.hasNext()) {
            A index = itr.next();
            if (!temp.pointers.containsKey(index)) {
                return false;
            }
            temp = temp.pointers.get(index);
        }
        return true;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @throws IllegalArgumentException if either key is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void delete(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Iterator<A> itr = key.iterator();
        HashTrieNode temp = (HashTrieNode)this.root;
        deleteHelper(itr, temp);
    }

    private void deleteHelper(Iterator<A> itr, HashTrieNode temp) {
        //we reached the node representing the key
        if (!itr.hasNext()) {
            //only remove from size when we remove a node with an key value
            if(temp.value != null) {
                this.size--;
            }
            temp.value = null;

        }
        else {
            A index = itr.next();
            //the if-block helps make sure we only search for keys in our root
            if (temp.pointers.containsKey(index)) {
                //keeps recursing until we reach our key
                deleteHelper(itr,temp.pointers.get(index));
                //removes any pointers that lead to a node being null, will also
                //move back up in each recursive call to make sure any null
                //nodes beforehand get eliminated.
                if(temp.pointers.get(index).pointers.isEmpty() && temp.pointers.get(index).value == null) {
                    temp.pointers.remove(index);
                }
            }
        }
    }

    @Override
    public void clear() {
        this.root = new HashTrieNode();
        this.size = 0;
    }
}
