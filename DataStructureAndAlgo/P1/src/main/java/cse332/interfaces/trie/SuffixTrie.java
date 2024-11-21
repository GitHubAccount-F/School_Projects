package cse332.interfaces.trie;

import cse332.interfaces.worklists.FIFOWorkList;
import cse332.interfaces.worklists.FixedSizeFIFOWorkList;
import cse332.types.ByteString;
import datastructures.dictionaries.HashTrieMap;
import datastructures.worklists.CircularArrayFIFOQueue;
import datastructures.worklists.ListFIFOQueue;

/**
 * CSE 332 16wi (2016-01-25)
 * Michael Lee (mlee42@cs.washington.edu)
 * Added final reference solution after conclusion of p1.
 */
public class SuffixTrie extends HashTrieMap<Byte, ByteString, Boolean> {
    protected static final Byte TERMINATOR = null;

    private FixedSizeFIFOWorkList<Byte> currentMatch;
    private ListFIFOQueue<HashTrieNode> leaves;
    private HashTrieNode lastMatchedNode;
    private FixedSizeFIFOWorkList<Byte> window;

    public SuffixTrie(int size, int maxMatchLength) {
        super(ByteString.class);
        if (maxMatchLength <= 0) {
            throw new IllegalArgumentException();
        }

        // General setup
        this.currentMatch = new CircularArrayFIFOQueue<>(maxMatchLength);
        this.leaves = new ListFIFOQueue<>();
        this.lastMatchedNode = null;
        this.window = new CircularArrayFIFOQueue<>(size);

        // Start with empty string
        this.root = this.makeLeaf(new HashTrieNode());
        this.size = 1;
        this.leaves.add(this.getRoot());
    }

    /**
     * Finds the longest matching suffix in the trie for a prefix of buffer.
     * To do this, we gradually shift elements from buffer to match as we
     * determine that they are actually a match to a suffix in the trie.
     *
     * @param buffer the buffer to search with
     * @return the total number of bytes matched
     * @postcondition currentMatch == suffix + b for the longest possible _partial_
     * suffix in the trie and some single byte b
     * @postcondition the node representing the last matched character in the trie
     * is stored in this.lastMatchedNode (we might need this later)
     * <p>
     * Note that this method is not guaranteed to find a complete match -- it may,
     * in some cases, make a partial match.
     * <p>
     * We will find a COMPLETE match when we use the buffer to traverse the tree
     * from the root to any leaf. It indicates that the next segment of the buffer
     * is exactly one of the suffixes in the trie.
     * <p>
     * We will find a PARTIAL match when we use the buffer to traverse the tree from
     * the root, but do NOT reach a leaf.  For example, if...
     * buffer = ['a', 'b', 'c']
     * trie = {"abcde", "bcde", "cde", "de", "e"}
     * Then, the longest match is "abc", but this isn't a complete word in the trie.
     * There is definitely a match; it's just a partial one.
     * <p>
     * If you find a COMPLETE match, you should return the total number of bytes you've
     * matched against the buffer. Otherwise, you should return zero.
     * <p>
     * When implementing this method, you should start by resetting this.lastMatchedNode,
     * then start traversing from the root of the trie to try finding the new match. You
     * should not traverse starting from the old value of this.lastMatchedNode.  Make sure
     * to update this.lastMatchedNode after finishing traversing.
     */
    public int startNewMatch(FIFOWorkList<Byte> buffer) {
        this.lastMatchedNode = this.getRoot();
        while (this.matchesHasRoom() && buffer.hasWork() &&
                this.lastMatchedNode.pointers.containsKey(buffer.peek())) {
            Byte match = buffer.next();
            this.currentMatch.add(match);
            this.lastMatchedNode = this.lastMatchedNode.pointers.get(match);
        }
        if (this.lastMatchedNode.pointers.containsKey(TERMINATOR)) {
            return this.currentMatch.size();
        } else {
            return 0;
        }
    }

    /**
     * Extends this.currentMatch to handle duplicates.
     * <p>
     * Consider the case where the buffer is:
     * abcabcabcd
     * A good decomposition of this buffer would be:
     * abc abc abc d
     * LZ77 can capture this idea by using *the match itself* as part of the match.
     * On the first match, we will get just 'a'. Then, just 'b'.  Then, just 'c'.
     * Now, our suffix trie is populated with "abc", "bc", and "c".
     * When we next try to match, we clearly can find "abc":
     * abc|abcabcd
     * ***
     * ^--^
     * <p>
     * The interesting idea is that the match can *continue*.  Because the next
     * character in the buffer ('a') matches the next character in the already
     * consumed window ('a'), we can continue the match.  In fact, this can
     * continue indefinitely.
     * abc|abcabcd
     * ***
     * ^--^
     * ^--^
     * ^--^
     * abc|abcabcd
     * ^--x
     * <p>
     * Eventually, it will stop matching (see above at the 'd').  Then, we output the
     * entire match.
     * <p>
     * This special case of the LZ77 algorithm interestingly is where much of the
     * compression comes from.
     *
     * @param buffer the buffer to search against
     * @return the total number of bytes matched
     */
    public int extendMatch(FIFOWorkList<Byte> buffer) {
        // Note: this method has been provided for you. You should not make any
        // changes to this method.
        int numMatches = 0;
        while (this.matchesHasRoom() && buffer.hasWork() &&
                this.currentMatch.peek(numMatches).equals(buffer.peek())) {
            this.currentMatch.add(buffer.next());
            numMatches += 1;
        }
        return numMatches;
    }

    /**
     * Adds the given byte to this.currentMatch. This method should
     * NOT change this.lastMatchedNode.
     * <p>
     * If the client tries adding a byte after this.currentMatch is full,
     * you should do nothing.
     *
     * @param b the byte to add
     */
    public void addToMatch(byte b) {
        this.currentMatch.add(b);
    }

    /**
     * Returns a worklist representing the current match.  Clients of this tree
     * SHOULD NOT be able to modify this.currentMatch by modifying the returned
     * worklist.  So, this method should return a deep copy.
     *
     * @return a copy of the current match
     */
    public FIFOWorkList<Byte> getMatch() {
        FIFOWorkList<Byte> copy = new ListFIFOQueue<>();
        int size = this.currentMatch.size();
        for (int i = 0; i < size; i++) {
            byte b = this.currentMatch.next();
            currentMatch.add(b);
            copy.add(b);
        }
        return copy;
    }

    /**
     * Returns the distance from the end of the current match to some leaf
     *
     * @return the number of (non-terminator) characters between lastMatchedNode and the
     * closest leaf on an arbitrary path
     */
    public int getDistanceToLeaf() {
        HashTrieNode curr = this.lastMatchedNode;
        int dist = 0;
        while (!curr.pointers.containsKey(TERMINATOR)) {
            curr = curr.pointers.values().iterator().next();
            dist += 1;
        }
        return dist;
    }

    /**
     * Advances this trie by the found match.
     * <p>
     * For every byte b in match, you should do the following:
     * <p>
     * 1. If the contents of the suffixtrie are at full capacity,
     * shift off a byte and remove the whole word from the trie
     * 2. Append b to the end of every stored node
     * 3. Re-insert the empty string back into the trie
     * <p>
     * HINT: be sure to pay careful attention to how exactly you are updating
     * your various fields, and how exactly they interact with one another. See the
     * example and descriptions in the spec for more details about this method.
     */
    public void advance() {
        while (this.currentMatch.hasWork()) {
            Byte match = this.currentMatch.next();

            // Step 1: add to sliding window
            this.advanceSlidingWindow(match);

            // Step 2: grow every leaf in trie
            this.growTrie(match);

            // Step 3: re-insert empty string
            this.leaves.add(this.makeLeaf(this.getRoot()));
            this.size += 1;
        }
    }

    /**
     * Advances the sliding window by one character. Will
     * automatically compensate and adjust the window and
     * the trie if the window is full.
     */
    private void advanceSlidingWindow(Byte match) {
        // Step 1a: Shift if at full capacity
        if (this.window.isFull()) {
            // Invariant: front of this.leaves contains the leaf of
            // the longest path.
            HashTrieNode endOfLongestPath = this.leaves.next();
            this.makeNotLeaf(endOfLongestPath);

            // Remove longest path from trie
            this.delete(new ByteString(this.window));

            // Shift the sliding window by one
            this.window.next();
        }

        // Step 1b: add to sliding window
        this.window.add(match);
    }

    /**
     * Extends all leaves in the trie with a new leaf.
     */
    private void growTrie(Byte match) {
        int numLeaves = this.leaves.size();
        for (int i = 0; i < numLeaves; i++) {
            HashTrieNode leaf = this.leaves.next();

            // Update current node
            this.makeNotLeaf(leaf);
            leaf.value = null;

            // Create new leaf
            if (!leaf.pointers.containsKey(match)) {
                leaf.pointers.put(match, new HashTrieNode());
            }

            // Record new leaf
            HashTrieNode newLeaf = this.makeLeaf(leaf.pointers.get(match));
            this.leaves.add(newLeaf);
        }
    }

    /**
     * Clears the state of this trie to identical to initialization.
     */
    @Override
    public void clear() {
        super.clear();
        this.currentMatch.clear();
        this.leaves.clear();
        this.window.clear();
        this.lastMatchedNode = null;

        this.root = this.makeLeaf(new HashTrieNode());
        this.size = 1;
        this.leaves.add(this.getRoot());
    }

    /**
     * Returns a reference to the root of the tree, but as a HashTrieNode
     * instead of a TrieNode.
     */
    @SuppressWarnings("unchecked")
    private HashTrieNode getRoot() {
        return (HashTrieNode) this.root;
    }

    /**
     * Returns true if currentMatches has room to add a new item.
     */
    private boolean matchesHasRoom() {
        return this.currentMatch.size() < this.currentMatch.capacity() - 1;
    }

    /**
     * Takes a node, and marks it as a leaf by adding the terminal character.
     * Also returns the same node to facilitate chaining.
     */
    private HashTrieNode makeLeaf(HashTrieNode node) {
        node.pointers.put(TERMINATOR, new HashTrieNode(true));
        return node;
    }

    /**
     * Takes a node, and removes the terminal node, if it exists. Also marks the
     * value of this leaf as true.
     * <p>
     * Returns the same node to facilitate chaining.
     */
    private HashTrieNode makeNotLeaf(HashTrieNode leaf) {
        leaf.pointers.remove(TERMINATOR);
        leaf.value = true;
        return leaf;
    }
}