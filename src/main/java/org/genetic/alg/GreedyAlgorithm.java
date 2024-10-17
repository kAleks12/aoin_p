package org.genetic.alg;

import org.genetic.alg.entities.Path;
import org.genetic.utils.entities.DistanceMatrix;

import java.util.ArrayList;
import java.util.List;

public class GreedyAlgorithm {
    public static List<Path> execute(DistanceMatrix graph) {
        var population = new ArrayList<Path>();
        for (int i = 0; i < graph.size(); i++) {
            Path path = new Path();
            int currNode = i;
            List<Integer> usedNodes = new ArrayList<>();
            usedNodes.add(currNode);
            path.addNode(currNode);

            while (usedNodes.size() < graph.size()) {
                currNode = graph.getShortestRoute(currNode, usedNodes);
                usedNodes.add(currNode);
                path.addNode(currNode);
            }
            graph.setPathCost(path);
            population.add(path);
        }
        return population;
    }
}
