package diver;

import game.*;
import game.Tile.TileType;
import graph.ShortestPaths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Collection;

/**
 * This is the place for your implementation of the {@code SewerDiver}.
 */
public class McDiver implements SewerDiver {

    /**
     * This class stores a location (represented by a coordinate), and it's associated value
     */
    class Triple implements Comparable<Object> {

        /**
         * The x-coordinate of the location
         */
        private final int x;

        /**
         * The y-coordinate of the location
         */
        private final int y;

        /**
         * The value associated with the location
         */
        private final int value;

        /**
         * Constructor that takes the exact parameters of the fields
         */
        public Triple(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        /**
         * Implements comparisons among the fields, with value being highest priority, then x
         * coordinate, then y coordinate
         */
        public int compareTo(Object obj) {
            Triple other = (Triple) obj;

            if (this.value != other.value) {
                return (this.value - other.value);
            }

            if (this.x != other.x) {
                return (this.x - other.x);
            }

            return (this.y - other.y);
        }

        /**
         * Returns x
         */
        public int x() {
            return x;
        }

        /**
         * Returns y
         */
        public int y() {
            return y;
        }

        /**
         * Returns value
         */
        public int value() {
            return value;
        }
    }

    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void seek(SeekState state) {
        // TODO : Look for the ring and return.
        // DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
        // Instead, write your method (it may be recursive) elsewhere, with a
        // good specification, and call it from this one.
        //
        // Working this way provides you with flexibility. For example, write
        // one basic method, which always works. Then, make a method that is a
        // copy of the first one and try to optimize in that second one.
        // If you don't succeed, you can always use the first one.
        //
        // Use this same process on the second method, scram.
        dfsWalk(state, new HashSet<>());
    }

    private boolean dfsWalk(SeekState state, Set<Long> vis) {
        assert !vis.contains(state.currentLocation());
        if (state.distanceToRing() == 0) {
            return true;
        }
        long cur = state.currentLocation();
        vis.add(cur);
        for (NodeStatus node : state.neighbors()) {
            if (!vis.contains(node.getId())) {
                state.moveTo(node.getId());
                if (dfsWalk(state, vis)) {
                    return true;
                }
                state.moveTo(cur);
            }
        }
        return false;
    }

    /**
     * See {@code SewerDriver} for specification.
     * <p>
     * /** See {@code SewerDriver} for specification.
     */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.

        List<Edge> case1 = shortScram(state);
        List<Edge> case2 = fullScram(state);

        List<Edge> edges = case2;
        int total = 0;

        for (Edge edge : case2) {
            total += edge.length();
        }

        if (total > state.stepsToGo()) {
            // This block should never be entered, but it is there to make sure McDiver
            // survives just in case
            edges = case1;
        }

        for (Edge edg : edges) {
            state.moveTo(edg.destination());
        }
    }

    /**
     * Implementation of scram that goes to the exit as fast as possible
     */
    private List<Edge> shortScram(ScramState state) {
        ShortestPaths<Node, Edge> graph = new ShortestPaths<>(
                new Maze((Set<Node>) state.allNodes()));
        graph.singleSourceDistances(state.currentNode());
        return graph.bestPath(state.exit());
    }

    /**
     * An implementation of scram that goes to the best area in terms of coins and then does that
     * repeatedly while it has enough steps
     */
    private List<Edge> fullScram(ScramState state) {
        // Determines the side length of box to be checked for coins
        int size = 3;

        // Creates a maze from the nodes of our sewer system
        Maze ourMaze = new Maze((Set<Node>) state.allNodes());
        Object[] nodes = state.allNodes().toArray();

        // We can already access tiles from nodes, this data structure now allows us to access
        // nodes from tiles
        Map<Tile, Node> tileToNode = mapping(nodes);

        // Determines the number of rows and columns in our sewer system, rows at index 0 and
        // columns at index 1
        int[] rowsAndCols = rowsAndCols(nodes);

        // Places the tiles associated with each node in a two-dimensional array with the index
        // representing the location of the tile in the sewer system
        Tile[][] tiles = tiling(nodes, rowsAndCols);

        // Calculates the total number of coins in each "group" (n by n square)
        int[][] groups = grouping(tiles, size);

        // Associates group values with the location and returns a reverse sorted list of priorities
        List<Triple> values = listing(groups);

        // Will return this at the end
        List<Edge> edges = new LinkedList<>();

        Node current = state.currentNode();
        Node next = current;

        // values.get(0) has the highest value
        while (values.get(0).value() > 0) {

            // Location of where we want to go (once transformed)
            int iInd = values.get(0).x();
            int jInd = values.get(0).y();

            boolean first = true;

            // The edges we add in this iteration of the while loop
            List<Edge> edgesAdd = new LinkedList<>();

            ShortestPaths<Node, Edge> graph = new ShortestPaths<>(
                    new Maze((Set<Node>) state.allNodes()));
            graph.singleSourceDistances(current);

            for (int i = iInd * size; i < iInd * size + size; ++i) {
                for (int j = jInd * size; j < jInd * size + size; ++j) {
                    // We only care about getting to a tile if it has coins
                    if (tiles[i][j] != null && tiles[i][j].coins() > 0) {
                        Node node = tileToNode.get(tiles[i][j]);

                        // first means we aren't in the area yet
                        if (first) {
                            edgesAdd = graph.bestPath(node);

                            next = node;
                            first = false;
                        } else {
                            assert next != null;

                            if (next.getNeighbors().contains(node)) {
                                for (Edge edge : ourMaze.outgoingEdges(next)) {
                                    if (edge.destination().equals(node)) {
                                        edgesAdd.add(edge);
                                        next = node;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (next == null) {
                values.remove(0);
                continue;
            }

            // Makes sure that we can still get to the exit if we move along the edges we want to
            ShortestPaths<Node, Edge> testGraph = new ShortestPaths<>(
                    new Maze((Set<Node>) state.allNodes()));
            testGraph.singleSourceDistances(next);

            List<Edge> testEdges = testGraph.bestPath(state.exit());

            int total = 0;

            for (Edge edge : edges) {
                total += edge.length();
            }

            for (Edge edge : edgesAdd) {
                total += edge.length();
            }

            for (Edge edge : testEdges) {
                total += edge.length();
            }

            if (total > state.stepsToGo()) {
                graph = new ShortestPaths<>(
                        new Maze((Set<Node>) state.allNodes()));
                graph.singleSourceDistances(current);

                edgesAdd = graph.bestPath(state.exit());
                edges.addAll(edgesAdd);
                return edges;
            }

            edges.addAll(edgesAdd);
            current = next;

            values.remove(0);
        }

        // In case we have enough steps to pick up every single coin this finishes the run
        ShortestPaths<Node, Edge> graph = new ShortestPaths<>(
                new Maze((Set<Node>) state.allNodes()));

        graph.singleSourceDistances(current);

        List<Edge> rest = graph.bestPath(state.exit());
        edges.addAll(rest);

        return edges;
    }

    /**
     * Maps tiles to nodes
     */
    private Map<Tile, Node> mapping(Object[] nodes) {
        Map<Tile, Node> tileToNode = new HashMap<>();

        for (Object obj : nodes) {
            Node node = (Node) obj;

            tileToNode.put(node.getTile(), node);
        }

        return tileToNode;
    }

    /**
     * Determines the number of rows and columns by analyzing the maximum values
     * Returns rows at index 0 and columns at index 1
     */
    private int[] rowsAndCols(Object[] nodes) {
        int rows = 0;
        int cols = 0;

        for (Object obj : nodes) {
            Node node = (Node) obj;
            Tile til = node.getTile();

            if (til.row() > rows) {
                rows = til.row();
            }

            if (til.column() > cols) {
                cols = til.column();
            }
        }

        return new int[]{rows, cols};
    }

    /**
     * Places the tiles associated with each node in a two-dimensional array with the index
     * representing the location of the tile in the sewer system
     */
    private Tile[][] tiling(Object[] nodes, int[] rowsAndCols) {
        int rows = rowsAndCols[0];
        int cols = rowsAndCols[1];

        Tile[][] tiles = new Tile[rows + 1][cols + 1];

        for (Object obj : nodes) {
            Node node = (Node) obj;
            Tile til = node.getTile();
            tiles[til.row()][til.column()] = til;
        }

        return tiles;
    }

    /**
     * Calculates the total number of coins in each "group" (n by n square) and stores it
     * in a two-dimensional array where groups[i][j] is the total sum of coins with
     * x coordinate from size * i to size * i + size - 1 and y coordinate from size * j to
     * size * j + size -1
     */
    private int[][] grouping(Tile[][] tiles, int size) {
        int[][] groups = new int[tiles.length / size][tiles[0].length / size];

        for (int i = 0; i < tiles.length - size; i += size) {
            for (int j = 0; j < tiles[0].length - size; j += size) {
                int coinValue = 0;

                for (int k = i; k < i + size; ++k) {
                    for (int l = j; l < j + size; ++l) {
                        coinValue += tiles[k][l] != null ? tiles[k][l].coins() : 0;
                    }
                }

                groups[i / size][j / size] = coinValue;
            }
        }

        return groups;
    }

    /**
     * Associates group values with the location and returns a reverse sorted list of priorities
     */
    private List<Triple> listing(int[][] groups) {
        List<Triple> values = new LinkedList<>();

        for (int i = 0; i < groups.length; ++i) {
            for (int j = 0; j < groups[0].length; ++j) {
                values.add(new Triple(i, j, groups[i][j]));
            }
        }

        Collections.sort(values);
        Collections.reverse(values);
        return values;
    }
}