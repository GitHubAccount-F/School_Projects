package ckpt1;

import cse332.interfaces.worklists.WorkList;
import datastructures.worklists.ArrayStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayStackTests {
    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_hasWorkSize_insertOne_updatesState() {
        WorkList<Integer> STUDENT_STACK = new ArrayStack<>();

        assertFalse(STUDENT_STACK.hasWork());
        STUDENT_STACK.add(1);
        assertTrue(STUDENT_STACK.hasWork());
    }

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_addNext_oneNumber_correctStructure() {
        WorkList<Integer> STUDENT_STACK = new ArrayStack<>();

        STUDENT_STACK.add(1);
        assertEquals(1, STUDENT_STACK.next());
    }

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_add_manyEntries_handlesArrayGrowth() {
        WorkList<Boolean> STUDENT_STACK = new ArrayStack<>();

        // Adds many elements into the
        for (int i = 0; i < 100000; i++) {
            assertDoesNotThrow(() -> {
                STUDENT_STACK.add(true);
            });
        }
    }

    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_addPeekNext_manyNumbers_correctStructure() {
        WorkList<Integer> STUDENT_STACK = new ArrayStack<>();

        // Add numbers 0 - 999 (inclusive) to the stack
        for (int i = 0; i < 1000; i++) {
            // Add the number
            STUDENT_STACK.add(i);
            // Checks if the top of the stack is the correct number
            assertEquals(i, STUDENT_STACK.peek());
            // Checks if the stack is not empty
            assertTrue(STUDENT_STACK.hasWork());
            // Checks if the size is correct
            assertEquals((i + 1), STUDENT_STACK.size());
        }

        // Empty out the stack
        for (int i = 999; i >= 0; i--) {
            // Checks if the stack is not empty
            assertTrue(STUDENT_STACK.hasWork());
            // Checks if the top of the stack is the correct number
            assertEquals(i, STUDENT_STACK.peek());
            // Removing the top of the stack should be the correct number
            assertEquals(i, STUDENT_STACK.next());
            // Checks if the size is correct
            assertEquals(i, STUDENT_STACK.size());
        }
    }
}
