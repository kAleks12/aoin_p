package org.genetic.alg;

import org.genetic.alg.entities.Path;
import org.genetic.utils.RandomGenerator;
import org.genetic.utils.entities.DistanceMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RandomAlgorithm {
    public static List<Path> execute(DistanceMatrix graph) throws IOException {
        var population = new ArrayList<Path>();
        for (int i = 0; i < 10000; i++) {
            Path path = new Path();
            int currNode;
            List<Integer> usedNodes = new ArrayList<>();
            while (usedNodes.size() < graph.size()) {
                currNode = RandomGenerator.getRandomInt(graph.size(), usedNodes);
                usedNodes.add(currNode);
                path.addNode(currNode);
            }
            graph.setPathCost(path);
            population.add(path);
        }
        return population;
    }
}
