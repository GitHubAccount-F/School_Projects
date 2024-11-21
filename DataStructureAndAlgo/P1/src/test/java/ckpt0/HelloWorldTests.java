package ckpt0;

import datastructures.HelloWorld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;



public class HelloWorldTests {
    @Test()
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_returnsHelloWorld() {
        assertEquals(HelloWorld.helloWorld(), "Hello World");
    }
}