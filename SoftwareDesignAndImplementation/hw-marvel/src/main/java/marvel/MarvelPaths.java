package marvel;
import graph.*;

import java.util.*;
import java.io.*;
import java.util.Map;

/**
 * <b>MarvelPaths</b> models a social network among characters in Marvel comic
 * books. We can find the path between two Marvel characters based on
 * comics they have connections to.
 */
public class MarvelPaths {

    //This class has no RI or AF as it's not an ADT

    /**
     * Asks for a file containing Marvel characters, where you can then
     * try to find paths between the characters in the given file.
     * @param args not used
     */
    public static void main(String[] args) {

        Scanner console = new Scanner(System.in);
        System.out.println("Give a file containing Marvel characters: ");
        String fileName = console.nextLine();
        Map<String, List<String>> data;
        try {
            data = MarvelParser.parseData(fileName);
            Graph<String,String> graph = MarvelPaths.marvelNetwork(data);
            String input;
            do {
                System.out.println("You will now be asked to give names to find the");
                System.out.println("path between Marvel characters in the file you gave.");
                System.out.println();
                System.out.println("Please enter the exact name of the character you want to");
                System.out.println("start from: ");
                String first = console.nextLine();
                System.out.println("Please enter the exact name of the character you want to");
                System.out.println("find: ");
                String second = console.nextLine();
                List<String> path = MarvelPaths.shortestPath(graph,first,second);
                if (!graph.containsNode(first)) {
                    System.out.println("unknown: " + first);
                }
                if (!graph.containsNode(second)) {
                    System.out.println("unknown: " + second);
                }
                else if (path == null && graph.containsNode(first) && graph.containsNode(second)) {
                    System.out.println("path from " + first + " to " + second + ":");
                    System.out.println("no path found");
                }
                else if (path != null && graph.containsNode(first) && graph.containsNode(second)){
                    System.out.println("path from " + first + " to " + second + ":");
                    for (int i = 0; i < path.size() - 1; i = i + 2) {
                        System.out.println(path.get(i) + " to " + path.get(i+2) + " via " + path.get(i+1));
                    }
                }
                System.out.println();
                System.out.println("Enter in any key to continue. Enter in -1 to exit.");
                input = console.nextLine().trim();
            } while(!input.equals("-1"));
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a correct file name next time.");
            console.close();
        }
    }

    /**
     * Creates a marvel social network using Marvel characters and the comics they appear in
     * @param network contains comic books and the Marvel characters that appear
     *                in them.
     * @spec.requires network != null, network contains only valid comic names and
     * characters
     * @return a new social network between Marvel characters where they are
     * associated through the comic books they appear in
     */
    public static Graph<String,String> marvelNetwork(Map<String, List<String>> network) {
        if (network == null || network.containsKey(null) || network.containsValue(null)) {
            throw new IllegalArgumentException();
        }
        Graph<String,String> result = new Graph<>();
        //Looks through all characters inside a comic book
        for (String oneComic: network.keySet()) {
            List<String> characters = network.get(oneComic);
            addEdgesInsideAComic(result,characters,oneComic);
        }
        return result;
    }

    /*
     * Creates edges between all characters inside the same comic book
     * @param graph-the graph the edges will be added to
     * @param characters-contains all characters inside the comic book
     * @param comicName-the name of the comic book
     * @spec.modifies graph
     * @spec.effects adds new nodes, representing characters in the Marvel comic,
     * and edges between all characters inside that comic
     */
    private static void addEdgesInsideAComic(Graph<String,String> graph, List<String> characters, String comicName) {
       //starts off with the first character to create edges from
        int initial = 0;
        //creates edges between all characters in the same comic
        for (int j = 0; j < characters.size(); j++) {
            //adds all nodes inside our graph
            graph.insertNode(new Node<String,String>(characters.get(j)));
        }
        while (initial < characters.size()) {
            //creates edge from character at 'i' to all other characters beside itself
            for(int i = 0; i < characters.size(); i++) {
                if (i != initial) {
                    graph.insertEdge(characters.get(initial), characters.get(i),comicName);
                }
            }
            //updates to the next character to create the next set of edges
            initial++;
        }
    }

    /**
     * Finds the shortest path between two Marvel characters based on the comics they have
     * connections to.
     * @param graph the social network Marvel characters based on their comics
     * @param chara1 the Marvel character we are starting from
     * @param chara2 the Marvel character we are trying to find a connection with
     * @spec.requires graph != null, chara1 !=null, chara2 != null, and 'graph' contains
     * chara1 and chara2
     * @return the shortest path, alphabetically between two Marvel characters. If along
     * the path, two intermediate Marvel characters appear inside the same comic, then the
     * alphabetically least comic is marked between them. If there is no path between the two
     * characters, then null is returned. The path is formatted as such: "character 1", "edge
     * between character 1 and character2", "character 2", "edge between character2
     * and character 3", "character3",  . . . and repeats. Also, when one or both of the characters given
     * are not inside 'graph', then no path will be found.
     */
    public static List<String> shortestPath(Graph<String,String> graph, String chara1, String chara2) {
        if (graph == null || chara1 == null || chara2 == null) {
            throw new IllegalArgumentException();
        }
        Queue<String> charToVisit = new LinkedList<>();
        //Keeps track of all paths to a character starting from chara1
        Map<String, List<String>> charaPath = new HashMap<>();
        charToVisit.add(chara1);
        charaPath.put(chara1, new ArrayList<>());
        while(!charToVisit.isEmpty()) {
            String checkChara = charToVisit.poll();
            if (checkChara.equals(chara2)) {
                return charaPath.get(checkChara);
            }
            //Sorts all other marvel characters that appear in same comic as "checkChara"
            Set<String> childNodes = sortedList(graph.getChildNode(checkChara));
            for (String relatedChara: childNodes) {
                //if the related marvel character is not visited
                Set<String> edgesBetweenMarvelChara =
                        sortedList(graph.getEdgesBetweenNodes(checkChara,relatedChara));
                //Goes through all comics that a related character and "checkChara"
                //both appear in
                for (String edges: edgesBetweenMarvelChara) {
                    if (!charaPath.containsKey(relatedChara)) {
                        //Marks the related character as visited and creates it's path from chara1
                        charaPath.put(relatedChara,
                                newPath(checkChara,relatedChara,charaPath.get(checkChara),edges));
                        charToVisit.add(relatedChara);
                    }
                }

            }
        }
        return null;
    }

    /*
     * Creates the path between two people in a Marvel social network.
     * @param person1-a Marvel character we are starting from in a Marvel social network
     *                and using to help mark down the path to another marvel character.
     * @param person2-a Marvel character we are forming a path to
     * @param lst-current path to person1 in a Marvel social network, starting from a character
     *            given.
     * @param comicLabel the comic book connecting person1 and person2
     * @spec.requires person1,comicLabel, lst !=null
     * @return an updated path between one Marvel character and another. If person1 already contains
     * a path, then person2 will have that some path plus person1 included inside.
     */
    private static List<String> newPath(String person1, String person2, List<String> lst,
                                                    String comicLabel) {
      //creates a path similar to person1
      List<String> newPath = new ArrayList<>();
      if (lst.isEmpty()) {
          newPath.add(person1);
      }
      else {
         newPath.addAll(lst);
      }
      newPath.add(comicLabel);
      newPath.add(person2);
      //Finishes creating the path to the new marvel character
      return newPath;
    }

    /*
     * Takes in a list of words, and sorts then alphabetically
     * @param lst
     * @return lst but with all words sorted
     */
    private static Set<String> sortedList(List<String> lst) {
        Set<String> result = new TreeSet<>(lst);
        return result;
    }

}
