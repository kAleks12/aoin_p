package org.genetic.utils.entities;

import org.genetic.alg.entities.Path;

import java.util.List;

public record DistanceMatrix(double[][] distanceMatrix, int size) {
    public double getDistance(int x, int y) {
        return this.distanceMatrix[x][y];
    }

    public void setPathCost(Path path) {
        double result = 0;
        int lastIndex = this.size() - 1;

        for (int i = 0; i < lastIndex; i++) {
            result += getDistance(path.getNode(i), path.getNode(i + 1));
        }
        result += getDistance(path.getNode(lastIndex), path.getNode(0));
        path.setCost(result);
    }

    public int getShortestRoute(int node, List<Integer> usedNodes) {
        double min = Double.MAX_VALUE;
        int bestNode = -1;
        double curr;
        for (int i = 0; i < this.size(); i++) {
            if (usedNodes.contains(i) || i == node) {
                continue;
            }
            curr = this.distanceMatrix[node][i];
            if (curr < min) {
                min = curr;
                bestNode = i;
            }
        }
        return bestNode;
    }
}
