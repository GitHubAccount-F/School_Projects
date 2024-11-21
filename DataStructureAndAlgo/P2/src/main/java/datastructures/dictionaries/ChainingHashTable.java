package datastructures.dictionaries;

import cse332.datastructures.containers.Item;
import cse332.datastructures.trees.BinarySearchTree;
import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.misc.DeletelessDictionary;
import cse332.interfaces.misc.Dictionary;
import cse332.interfaces.misc.SimpleIterator;
import cse332.interfaces.worklists.WorkList;
import datastructures.worklists.ArrayStack;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * 1. You must implement a generic chaining hashtable. You may not
 * restrict the size of the input domain (i.e., it must accept
 * any key) or the number of inputs (i.e., it must grow as necessary).
 * 3. Your HashTable should rehash as appropriate (use load factor as
 * shown in class!).
 * 5. HashTable should be able to resize its capacity to prime numbers for more
 * than 200,000 elements. After more than 200,000 elements, it should
 * continue to resize using some other mechanism.
 * 6. You should use the prime numbers in the given PRIME_SIZES list to resize
 * your HashTable with prime numbers.
 * 7. When implementing your iterator, you should NOT copy every item to another
 * dictionary/list and return that dictionary/list's iterator.
 */
public class ChainingHashTable<K, V> extends DeletelessDictionary<K, V> {
    private Supplier<Dictionary<K, V>> newChain;
    private Dictionary<K, V>[] array;

    private int whichPrimeSize;

    private int tableSize;
    static final int[] PRIME_SIZES =
            {11, 23, 47, 97, 193, 389, 773, 1549, 3089, 6173, 12347, 24697, 49393, 98779, 197573, 395147};

    @SuppressWarnings("unchecked")
    public ChainingHashTable(Supplier<Dictionary<K, V>> newChain) {
        this.newChain = newChain;
        whichPrimeSize = 0;
        tableSize = PRIME_SIZES[whichPrimeSize];
        array = (Dictionary<K, V>[]) new Dictionary[tableSize];

    }

    @Override
    public V insert(K key, V value) {
        if(key == null || value == null) {
            throw new IllegalArgumentException();
        }
        if(this.size() > tableSize) {
            if(whichPrimeSize >= PRIME_SIZES.length - 1) {
                tableSize = (tableSize * 2) + 1;
            } else {
                whichPrimeSize++;
                tableSize = PRIME_SIZES[whichPrimeSize];
            }
            array = reSizingInsert();
        }
        int hash = key.hashCode();
        if(hash < 0) {
            hash = hash * -1;
        }
        int index = hash % tableSize;

        if(array[index] == null) {
            array[index] = newChain.get();
            array[index].insert(key,value);
            this.size++;
            return null;
        } else {
            V valueToReturn = array[index].find(key);
            if(valueToReturn == null) {
                this.size++;
            }
            array[index].insert(key,value);
            return valueToReturn;
        }
    }

    @SuppressWarnings("unchecked")
    private Dictionary<K, V>[] reSizingInsert() {
        Dictionary<K, V>[] temp = (Dictionary<K, V>[]) new Dictionary[tableSize];
        for(int i = 0; i < array.length; i++) {
            if(array[i] != null) {
                Iterator<Item<K,V>> itr = array[i].iterator();
                while(itr.hasNext()) {
                    Item<K,V> next = itr.next();
                    int hash = next.key.hashCode();
                    if(hash < 0) {
                        hash = hash * -1;
                    }
                    int index = hash % tableSize;
                    if (temp[index] == null) {
                        temp[index] = newChain.get();
                    }
                    temp[index].insert(next.key, next.value);
                }
            }
        }
        return temp;
    }

    @Override
    public V find(K key) {
        if(key == null) {
            throw new IllegalArgumentException();
        }
        int hash = key.hashCode();
        if(hash < 0) {
            hash = hash * -1;
        }
        int index = hash % tableSize;
        if(array[index] == null) {
            return null;
        }
        V valueToReturn = array[index].find(key);
        return valueToReturn;
    }

    @Override
    public Iterator<Item<K, V>> iterator() {
        return new ChainTableIterator();
    }

    private class ChainTableIterator extends SimpleIterator<Item<K, V>> {
        private Iterator<Item<K,V>> itr;

        private int index;

        private int totalElements;

        public ChainTableIterator() {
            totalElements = size();
            index = 0;
            if(array[index] != null) {
                itr = array[index].iterator();
            } else {
                while(index < array.length && array[index] == null) {
                    index++;
                }
                if(index < array.length && array[index] != null) {
                    itr = array[index].iterator();
                } else {
                    itr = null;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return (totalElements > 0);
        }

        @Override
        public Item<K, V> next() {
            if(totalElements == 0) {
                throw new NoSuchElementException();
            }
            if(itr.hasNext()) {
                totalElements--;
                return itr.next();
            } else {
                while(index < array.length) {
                    index++;
                    if(array[index] != null) {
                        itr = array[index].iterator();
                        break;
                    }
                }
                totalElements--;
                return itr.next();
            }
        }
    }
    /**
     * Temporary fix so that you can debug on IntelliJ properly despite a broken iterator
     * Remove to see proper String representation (inherited from Dictionary)
     */

    @Override
    public String toString() {
        return "ChainingHashTable String representation goes here.";
    }


}
