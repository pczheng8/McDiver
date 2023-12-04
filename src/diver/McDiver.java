package diver;

import game.*;
import graph.ShortestPaths;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;


/** This is the place for your implementation of the {@code SewerDiver}.
 */
public class McDiver implements SewerDiver {

    /** See {@code SewerDriver} for specification. */
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
                if(dfsWalk(state, vis)) return true;
                state.moveTo(cur);
            }
        }
        return false;
    }

    /**
     * See {@code SewerDriver} for specification.

    /** See {@code SewerDriver} for specification. */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
        ArrayList<Node> nodes = shortScram(state);

        for (Node n : nodes) {
            state.moveTo(n);
        }
    }

    public ArrayList<Node> shortScram(ScramState state) {
        ShortestPaths<Node, Edge> s = new ShortestPaths<>(
                new Maze((Set<Node>) state.allNodes()));
        s.singleSourceDistances(state.currentNode());

        ArrayList<Node> nodes = new ArrayList<>();
        List<Edge> edges = s.bestPath(state.exit());

        for (Edge e : edges) {
            nodes.add(e.destination());
        }

        return nodes;
    }
}
