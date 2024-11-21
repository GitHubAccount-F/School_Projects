package ckpt1;

import cse332.interfaces.worklists.WorkList;
import datastructures.worklists.ListFIFOQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ListFIFOQueueTests {

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_addPeekNext_manyNumbers_correctStructure() {
        WorkList<Integer> STUDENT_QUEUE = new ListFIFOQueue<>();

        // Add numbers 0 - 999 (inclusive) to the queue
        for (int i = 0; i < 1000; i++) {
            // Add the number
            STUDENT_QUEUE.add(i);
            // Checks if the front of the queue is 0
            assertEquals(0, STUDENT_QUEUE.peek());
            // Checks if the queue is not empty
            assertTrue(STUDENT_QUEUE.hasWork());
            // Checks if the size is correct
            assertEquals((i + 1), STUDENT_QUEUE.size());
        }

        // Empty out the queue
        for (int i = 0; i < 999; i++) {
            // Checks if the queue is not empty
            assertTrue(STUDENT_QUEUE.hasWork());
            // Checks if the top of the queue is the correct number
            assertEquals(i, STUDENT_QUEUE.peek());
            // Removing the top of the queue should be the correct number
            assertEquals(i, STUDENT_QUEUE.next());
            // Checks if the size is correct
            assertEquals(999 - i, STUDENT_QUEUE.size());
        }
    }

    @Test
    public void testHasWork() {
        WorkList<Integer> STUDENT_INT = new ListFIFOQueue<>();
        assertFalse(STUDENT_INT.hasWork());
    }

    @Test
    public void testHasWorkAfterAdd() {
        WorkList<Integer> STUDENT_INT = new ListFIFOQueue<>();
        STUDENT_INT.add(1);
        assertTrue(STUDENT_INT.hasWork());
    }

    @Test
    public void testHasWorkAfterAddRemove() {
        WorkList<Double> STUDENT_DOUBLE = new ListFIFOQueue<>();
        for (int i = 0; i < 1000; i++) {
            STUDENT_DOUBLE.add(Math.random());
        }
        for (int i = 0; i < 1000; i++) {
            STUDENT_DOUBLE.next();
        }
        assertFalse(STUDENT_DOUBLE.hasWork());
    }
    @Test
    public void testPeekHasException() {
        WorkList<Integer> STUDENT_INT = new ListFIFOQueue<>();
        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_INT.peek();
        });

        for (int i = 0; i < 10; i++) {
            STUDENT_INT.add(42);
        }
        for (int i = 0; i < 10; i++) {
            STUDENT_INT.next();
        }

        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_INT.peek();
        });
    }

    @Test
    public void testNextHasException() {
        WorkList<Integer> STUDENT_INT = new ListFIFOQueue<>();
        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_INT.next();
        });

        for (int i = 0; i < 10; i++) {
            STUDENT_INT.add(42);
        }
        for (int i = 0; i < 10; i++) {
            STUDENT_INT.next();
        }

        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_INT.next();
        });
    }
    @Test
    public void testClear() {
        WorkList<String> STUDENT_STR = new ListFIFOQueue<>();

        // Add all the words into the queue
        String[] words = {"Beware", "the", "Jabberwock", "my", "son!"};
        for (String value : words) {
            STUDENT_STR.add(value);
        }

        assertTrue(STUDENT_STR.hasWork());
        assertEquals(5, STUDENT_STR.size());

        STUDENT_STR.clear();
        assertFalse(STUDENT_STR.hasWork());
        assertEquals(0, STUDENT_STR.size());
        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_STR.peek();
        });
        assertThrows(NoSuchElementException.class, () -> {
            STUDENT_STR.next();
        });
    }
}
