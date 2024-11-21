package duedate;

import cse332.types.AlphabeticString;
import datastructures.dictionaries.HashTrieMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class HashTrieMapTests {

    /**
     * Tests if insert, find, and findPrefix work in general.
     */
    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_addFindFindPrefix_fewWords_correct() {
        HashTrieMap<Character, AlphabeticString, String> STUDENT_TRIE = new HashTrieMap<>(AlphabeticString.class);

        // Add all the words into the trie
        String[] words = {"dog", "doggy", "doge", "dragon", "cat", "draggin"};
        for (String word : words) {
            STUDENT_TRIE.insert(toAlphabeticString(word), word.toUpperCase());
        }

        // Makes sure the trie can:
        // find each word we inserted
        // findPrefix each word we inserted
        // NOT find garbage words
        // findPrefix every possible prefix of each word we inserted
        for (String word : words) {
            containsPath(STUDENT_TRIE, word);
        }

        // Makes sure the trie CANNOT find each invalid word
        String[] invalid = {"d", "cataract", "", "do"};
        for (String invalidWord : invalid) {
            assertNull(STUDENT_TRIE.find(toAlphabeticString(invalidWord)),
                       "Was able to find " + invalidWord + " even though its not meant to be there");
        }
    }

    /**
     * Checks to see if basic delete functionality works.
     */
    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_delete_fewWords_correct() {
        HashTrieMap<Character, AlphabeticString, String> STUDENT_TRIE = new HashTrieMap<>(AlphabeticString.class);
        String[] words = {"dog", "doggy", "dreamer", "cat"};

        // Add all the words into the trie
        for (String word : words) {
            STUDENT_TRIE.insert(toAlphabeticString(word), word.toUpperCase());
        }

        // Makes sure the trie can:
        // find each word we inserted
        // findPrefix each word we inserted
        // NOT find garbage words
        // findPrefix every possible prefix of each word we inserted
        for (String word : words) {
            containsPath(STUDENT_TRIE, word);
        }

        // Delete something that doesn't exist (shouldn't do anything)
        STUDENT_TRIE.delete(toAlphabeticString("I don't exist"));
        // Delete a word that exists
        STUDENT_TRIE.delete(toAlphabeticString("dreamer"));
        // Check if the other words still exist (it should)
        containsPath(STUDENT_TRIE, "dog");
        containsPath(STUDENT_TRIE, "doggy");
        containsPath(STUDENT_TRIE, "cat");

        // Check if the prefixes of "dreamer" exist (it should NOT)
        for (String prefix : new String[]{"dreamer", "dreame", "dream", "drea", "dre", "dr"}) {
            assertFalse(STUDENT_TRIE.findPrefix(toAlphabeticString(prefix)),
                        "Was able to findPrefix " + prefix + " even though its not meant to be there");
        }

        // except "d" since it shares with "dog" and "doggy"
        assertTrue(STUDENT_TRIE.findPrefix(toAlphabeticString("d")),
                   "Could not findPrefix d");

        // Delete a word that exists
        STUDENT_TRIE.delete(toAlphabeticString("dog"));
        // Check if the other words still exist (it should)
        containsPath(STUDENT_TRIE, "doggy");
        containsPath(STUDENT_TRIE, "cat");

        // Delete a word that exists
        STUDENT_TRIE.delete(toAlphabeticString("doggy"));
        // Check if the other words still exist (it should)
        containsPath(STUDENT_TRIE, "cat");
    }

    // UTILITY METHODS

    /**
     * Converts a String into an AlphabeticString
     */
    private static AlphabeticString toAlphabeticString(String s) {
        return new AlphabeticString(s);
    }

    /**
     * Checks if the trie contains the word and the expected value, and that all prefixes of
     * the word exist in the trie.
     *
     * Assumes that the expected value of the key word is word.toUpperCase().
     */
    private static void containsPath(HashTrieMap<Character, AlphabeticString, String> trie, String word) {
        AlphabeticString key = toAlphabeticString(word);

        // null if this trie contains no mapping for the word
        assertEquals(word.toUpperCase(), trie.find(key),
                     "Could not find " + key);
        // findPrefix should return true on the key itself
        assertTrue(trie.findPrefix(key),
                   "Could not findPrefix " + key);
        // Should not be able to find garbage words
        assertNull(trie.find(toAlphabeticString(word + "$")),
                   "Somehow found " + word + "$ even though it should not exist in the trie");

        // Should be able to find every prefix
        String accumulatedWord = "";
        for (char c : word.toCharArray()) {
            accumulatedWord += c;
            // We should be able to find every prefix of the word
            assertTrue(trie.findPrefix(toAlphabeticString(accumulatedWord)),
                       "Could not find prefix " + accumulatedWord + " of the word " + word);
        }
    }
}
