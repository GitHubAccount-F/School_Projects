package marvel.junitTests;
import marvel.MarvelPaths;
import org.junit.*;
import static org.junit.Assert.*;
import graph.*;
import java.util.*;
import org.junit.Rule;
import org.junit.rules.Timeout;

public class MarvelPathsTest {
    @Rule public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    //When using the marvelNetwork() method and giving invalid inputs
    @Test(expected = IllegalArgumentException.class)
    public void testMarvelNetwork1() {
        //When the value given is null
        Graph<String,String> graph1 = MarvelPaths.marvelNetwork(null);
    }
    //When the network variable given has a null value as a key
    @Test(expected = IllegalArgumentException.class)
    public void testMarvelNetwork2() {
        Map<String, List<String>> test = new HashMap<>();
        test.put(null,new ArrayList<>());
        test.get(null).add("one");
        Graph<String,String> graph1 = MarvelPaths.marvelNetwork(test);
    }

    //When the network variable given has a null value
    @Test(expected = IllegalArgumentException.class)
    public void testMarvelNetwork3() {
        Map<String, List<String>> test = new HashMap<>();
        test.put("one",null);
        Graph<String,String> graph1 = MarvelPaths.marvelNetwork(test);
    }

    //When using the shortestPath() method and giving invalid inputs
    @Test(expected = IllegalArgumentException.class)
    public void testShortestPath1() {
        //When the value given is null
       List<String> lst = MarvelPaths.shortestPath(null, null, null);
    }

}
