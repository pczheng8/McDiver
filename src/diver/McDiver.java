package diver;

import datastructures.PQueue;
import datastructures.SlowPQueue;
import game.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * This is the place for your implementation of the {@code SewerDiver}.
 */
public class McDiver implements SewerDiver {

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
                if(dfsWalk(state, vis)) return true;
                state.moveTo(cur);
            }
        }
        return false;
    }

    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
//        getCoins(state, new HashSet<>());
    }

    /**
     * Effect: Computes the best paths from a given source vertex, which can then be queried using
     * bestPath().
     */
    public void singleSourceDistances(ScramState state) { //WE NEED TO USE exit() AS WELL
//        PQueue<Node> frontier = new SlowPQueue<>();
//        HashMap<Node, Double> distances = new HashMap<>();
//        HashMap<Node, Edge> bestEdges = new HashMap<>();
//        frontier.add(state.currentNode(), 0.0);
//        distances.put(state.currentNode(), 0.0);
//        while (!frontier.isEmpty()) {
//            Node v = frontier.extractMin();
//            for (Edge e : graph.outgoingEdges(v)) {
//                Vertex neighbor = graph.dest(e);
//                double dist = distances.get(v) + graph.weight(e);
//                if (!distances.containsKey(neighbor)) {
//                    frontier.add(neighbor, dist);
//                    distances.put(neighbor, dist);
//                    bestEdges.put(neighbor, e);
//                } else if (dist < distances.get(neighbor)) {
//                    distances.put(neighbor, dist);
//                    frontier.changePriority(neighbor, dist);
//                    bestEdges.put(neighbor, e);
//                }
//            }
//        }
    }
}
