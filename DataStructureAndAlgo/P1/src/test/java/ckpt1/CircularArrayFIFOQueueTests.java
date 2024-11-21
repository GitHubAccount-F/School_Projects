package ckpt1;

import cse332.interfaces.worklists.FixedSizeFIFOWorkList;
import datastructures.worklists.CircularArrayFIFOQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CircularArrayFIFOQueueTests {

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_clear_fewWords_emptyQueue() {
        int MAX_CAPACITY = 5;
        CircularArrayFIFOQueue<String> STUDENT_QUEUE = new CircularArrayFIFOQueue<>(MAX_CAPACITY);

        // Add all the words into the queue
        String[] words = {"Beware", "the", "Jabberwock", "my", "son!"};
        for (String value : words) {
            STUDENT_QUEUE.add(value);
        }

        // Checks to make sure the queue is in the correct state after adding a bunch of words
        assertTrue(STUDENT_QUEUE.hasWork());
        assertEquals(MAX_CAPACITY, STUDENT_QUEUE.size());
        assertTrue(STUDENT_QUEUE.isFull());
        assertEquals(MAX_CAPACITY, STUDENT_QUEUE.capacity());

        // Clear everything
        STUDENT_QUEUE.clear();

        // Checks to make sure the queue is in the correct state after clearing the queue
        assertFalse(STUDENT_QUEUE.hasWork());
        assertEquals(0, STUDENT_QUEUE.size());
        assertFalse(STUDENT_QUEUE.isFull());
        assertEquals(5, STUDENT_QUEUE.capacity());

        // Checks to make sure the queue throws NoSuchElementException when calling peek or next
        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_QUEUE.peek();
        });
        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_QUEUE.next();
        });
    }

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_update_fewNumbers_oddQueue() {
        int MAX_CAPACITY = 10;
        FixedSizeFIFOWorkList<Integer> STUDENT_QUEUE = new CircularArrayFIFOQueue<>(MAX_CAPACITY);

        // Add 8 elements to the queue
        for (int i = 0; i < 8; i++) {
            STUDENT_QUEUE.add(i);
            // Checks to make sure the queue has the correct size
            assertEquals(i + 1, STUDENT_QUEUE.size());
        }
        // Check the 8 elements are there
        for (int i = 0; i < 8; i++) {
            assertEquals(i, STUDENT_QUEUE.peek(i));
        }
        // Checks to make sure the queue has the correct size
        assertEquals(8, STUDENT_QUEUE.size());
        // Checks to make sure the queue is NOT full
        assertFalse(STUDENT_QUEUE.isFull());

        // Replace every other index with a negative of that value
        for (int i = 0; i < 8; i += 2) {
            STUDENT_QUEUE.update(i, -i);
        }
        // Checks to make sure every other index in the queue is a negative of that index
        for (int i = 0; i < 8; i++) {
            int expected = i;
            if (i % 2 == 0) { // if odd index,
                expected *= -1;
            }
            assertEquals(expected, STUDENT_QUEUE.peek(i));

        }
        // Checks to make sure the queue has the correct size
        assertEquals(8, STUDENT_QUEUE.size());
        // Checks to make sure every other index in the queue is a negative of that index
        // This time, slowly remove from the queue
        for (int i = 0; i < 8; i++) {
            int expected = i;
            if (i % 2 == 0) { // if odd index,
                expected *= -1;
            }
            assertEquals(expected, STUDENT_QUEUE.peek());
            assertEquals(expected, STUDENT_QUEUE.next());
        }
        // Checks to make sure the queue has the correct size
        assertEquals(0, STUDENT_QUEUE.size());
    }

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_add_manyNumbersInACycle_correctStructure() {
        testCycle(1);
        testCycle(2);
        testCycle(9);
        testCycle(10);
    }
    
    private void testCycle(int maxCapacity) {
        FixedSizeFIFOWorkList<Integer> STUDENT_QUEUE = new CircularArrayFIFOQueue<>(maxCapacity);
        
        // Fill up the queue
        for (int i = 0; i < maxCapacity; i++) {
            STUDENT_QUEUE.add(i);
            // Checks to make sure the queue has the correct size
            assertEquals(i + 1, STUDENT_QUEUE.size());
        }
        // Checks to make sure the queue has work and is full
        assertTrue(STUDENT_QUEUE.hasWork());
        assertTrue(STUDENT_QUEUE.isFull());

        // Removes and adds from the queue and checks to make sure it is correct
        for (int i = maxCapacity; i < 100000; i++) {
            assertEquals((i - maxCapacity), STUDENT_QUEUE.peek());
            assertEquals((i - maxCapacity), STUDENT_QUEUE.next());
            assertFalse(STUDENT_QUEUE.isFull());
            STUDENT_QUEUE.add(i);
            assertEquals(maxCapacity, STUDENT_QUEUE.size());
            assertTrue(STUDENT_QUEUE.isFull());
        }
    }

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_add_manyNumbers_correctStructure() {
        int MAX_CAPACITY = 1000;
        FixedSizeFIFOWorkList<Integer> STUDENT_QUEUE = new CircularArrayFIFOQueue<>(MAX_CAPACITY);

        // Fill up the queue and checks to make sure it is correct
        for (int i = 0; i < MAX_CAPACITY; i++) {
            assertFalse(STUDENT_QUEUE.isFull());
            STUDENT_QUEUE.add(i);
            assertEquals(0, STUDENT_QUEUE.peek());
            assertEquals(0, STUDENT_QUEUE.peek(0));
            assertEquals(i, STUDENT_QUEUE.peek(i));
            assertTrue(STUDENT_QUEUE.hasWork());
            assertEquals((i + 1), STUDENT_QUEUE.size());
        }
        assertEquals(MAX_CAPACITY, STUDENT_QUEUE.size());
        assertTrue(STUDENT_QUEUE.isFull());

        // Queue should throw IllegalStateException when full
        assertThrows(IllegalStateException.class, () -> {
            STUDENT_QUEUE.add(2000);
        });

        // Checks to make sure every element is correct
        for (int i = 0; i < MAX_CAPACITY; i++) {
            assertEquals(i, STUDENT_QUEUE.peek(i));
        }

        // Empty the queue and checks to make sure it is correct
        for (int i = 0; i < 999; i++) {
            assertTrue(STUDENT_QUEUE.hasWork());
            assertEquals(i, STUDENT_QUEUE.peek());
            assertEquals(i, STUDENT_QUEUE.peek(0));
            assertEquals(i, STUDENT_QUEUE.next());
            assertEquals(999 - i, STUDENT_QUEUE.size());
            assertFalse(STUDENT_QUEUE.isFull());
        }
    }
}
