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

    class Triple implements Comparable<Object> {

        private final int x;
        private final int y;
        private final int value;

        public Triple(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

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

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

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
            System.out.println("BIG ISSUE HERE");
            edges = case1;
        }

        for (Edge edg : edges) {
            state.moveTo(edg.destination());
        }
    }

    private List<Edge> shortScram(ScramState state) {
        ShortestPaths<Node, Edge> graph = new ShortestPaths<>(
                new Maze((Set<Node>) state.allNodes()));
        graph.singleSourceDistances(state.currentNode());
        return graph.bestPath(state.exit());
    }

    private List<Edge> fullScram(ScramState state) {
        // Determines the side length of box to be checked for coins
        int size = 3;
        Maze ourMaze = new Maze((Set<Node>) state.allNodes());

        Object[] nodes = state.allNodes().toArray();
        Map<Tile, Node> tileToNode = mapping(nodes);

        int[] rowsAndCols = rowsAndCols(nodes);
        Tile[][] tiles = tiling(nodes, rowsAndCols);
        int[][] groups = grouping(tiles, size);
        List<Triple> values = listing(groups);

        List<Edge> edges = new LinkedList<>();

        Node current = state.currentNode();
        Node next = current;

        while (values.get(0).value() > 0) {
            int iInd = values.get(0).x();
            int jInd = values.get(0).y();

            boolean first = true;
            List<Edge> edgesAdd = new LinkedList<>();

            ShortestPaths<Node, Edge> graph = new ShortestPaths<>(
                    new Maze((Set<Node>) state.allNodes()));
            graph.singleSourceDistances(current);

            for (int i = iInd * size; i < iInd * size + size; ++i) {
                for (int j = jInd * size; j < jInd * size + size; ++j) {
                    if (tiles[i][j] != null && tiles[i][j].coins() > 0) {
                        Node node = tileToNode.get(tiles[i][j]);

                        if (first) {
                            edgesAdd = graph.bestPath(node);

                            if (node == null) {
                                System.out.println("BIG ISSUE HERE: " + i + " " + j + " " +
                                        tiles[i][j].coins());
                            }

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

        ShortestPaths<Node, Edge> graph = new ShortestPaths<>(
                new Maze((Set<Node>) state.allNodes()));

        graph.singleSourceDistances(current);

        List<Edge> rest = graph.bestPath(state.exit());
        edges.addAll(rest);

        return edges;
    }

    private Map<Tile, Node> mapping(Object[] nodes) {
        Map<Tile, Node> tileToNode = new HashMap<>();

        for (Object obj : nodes) {
            Node node = (Node) obj;

            tileToNode.put(node.getTile(), node);
        }

        return tileToNode;
    }

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