package org.genetic.alg;

import org.genetic.alg.entities.CrossoverType;
import org.genetic.alg.entities.InitializationType;
import org.genetic.alg.entities.MutationType;
import org.genetic.alg.entities.Path;
import org.genetic.alg.entities.SelectionType;
import org.genetic.utils.RandomGenerator;
import org.genetic.utils.entities.DistanceMatrix;
import org.genetic.utils.entities.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneticOperatorHelper {
    public static List<Path> initialize(InitializationType initType, DistanceMatrix graph, int size) {
        List<Path> paths = new ArrayList<>();
        while (paths.size() < size) {
            paths.add( switch (initType) {
                case Greedy -> createGreedyPath(graph);
                case Random -> createRandomPath(graph);
            });
        }
        return paths;
    }

    public static void mutate(MutationType mutType, Path path, DistanceMatrix graph) {
         switch (mutType) {
            case Swap -> swapMutation(path, graph);
            case Inverse -> inverseMutation(path, graph);
            default -> throw new UnsupportedOperationException("Unsupported MutationType: " + mutType);
        }
    }

    public static List<Path> crossover(CrossoverType crossType, Path parent1, Path parent2, DistanceMatrix graph) {
        return switch (crossType) {
            case OX -> oxCrossover(parent1, parent2, graph);
            case PMX -> pmxCrossover(parent1, parent2, graph);
            default -> throw new UnsupportedOperationException("Unsupported CrossoverType: " + crossType);
        };
    }

    public static Path select(SelectionType selType, List<Path> population, int size) {
        return switch (selType) {
            case Roulette -> rouletteSelection(population);
            case Tournament -> tournamentSelection(population, size);
            default -> throw new UnsupportedOperationException("Unsupported SelectionType: " + selType);
        };
    }


    private static Path createRandomPath(DistanceMatrix graph) {
        Path path = new Path();
        int currNode;
        List<Integer> usedNodes = new ArrayList<>();
        while (usedNodes.size() < graph.size()) {
            currNode = RandomGenerator.getRandomInt(graph.size(), usedNodes);
            usedNodes.add(currNode);
            path.addNode(currNode);
        }
        graph.setPathCost(path);
        return path;
    }

    private static Path createGreedyPath (DistanceMatrix graph) {
        Path path = new Path();
        int currNode = RandomGenerator.getRandomInt(graph.size(), Collections.emptyList());
        List<Integer> usedNodes = new ArrayList<>();
        usedNodes.add(currNode);
        path.addNode(currNode);

        while (usedNodes.size() < graph.size()) {
            currNode = graph.getShortestRoute(currNode, usedNodes);
            usedNodes.add(currNode);
            path.addNode(currNode);
        }
        graph.setPathCost(path);
        return path;
    }

    private static void swapMutation(Path path, DistanceMatrix graph) {
        Interval <Integer> interval = RandomGenerator.getRandomInterval(graph.size());
        int firstIndex = interval.min();
        int lastIndex = interval.max();
        
        var nodes = path.getNodes();
        int first = nodes.get(firstIndex);
        int second = nodes.get(lastIndex);
        
        nodes.remove(firstIndex);
        nodes.add(firstIndex, second);
        
        nodes.remove(lastIndex);
        nodes.add(lastIndex, first);
        
        path.setNodes(nodes);
        graph.setPathCost(path);
    }

    private static void inverseMutation(Path path, DistanceMatrix graph) {
        Interval <Integer> interval = RandomGenerator.getRandomInterval(graph.size());
        int firstIndex = interval.min();
        int lastIndex = interval.max();
        
        var nodes = path.getNodes();
        List<Integer> newNodes = new ArrayList<>(nodes.size());

        for (int i = 0; i < nodes.size(); i++) {
            if (i >= firstIndex && i <= lastIndex) {
                newNodes.add(firstIndex, nodes.get(i));
            } else {
                newNodes.add(nodes.get(i));
            }
        }
        
        path.setNodes(newNodes);
        graph.setPathCost(path);
    }

    private static List<Path> oxCrossover(Path parent1, Path parent2, DistanceMatrix graph) {
        Interval<Integer> interval = RandomGenerator.getRandomInterval(graph.size());
        int firstIndex = interval.min();
        int lastIndex = interval.max();

        var nodes = parent1.getNodes();
        ArrayList<Integer> unchanged = new ArrayList<>(nodes.size());
        ArrayList<Integer> newNodes = new ArrayList<>(nodes.size());
        for (int i = firstIndex; i <= lastIndex; i++) {
            unchanged.add(nodes.get(i));
        }
        nodes = parent2.getNodes();
        for(int i = 0; i < graph.size(); i++) {
            var currNode = nodes.get(i);
            if (unchanged.contains(currNode)) {
                continue;
            }
            newNodes.add(currNode);
        }
        newNodes.addAll(firstIndex, unchanged);
        var newPath = new Path(newNodes, 0);
        graph.setPathCost(newPath);
        return List.of(newPath);
    }

    private static List<Path> pmxCrossover(Path parent1, Path parent2, DistanceMatrix graph) {
        Interval<Integer> interval = RandomGenerator.getRandomInterval(graph.size());
        int firstIndex = interval.min();
        int lastIndex = interval.max();

        var nodes1 = parent1.getNodes();
        var nodes2 = parent2.getNodes();
        var mapOneTwo = new HashMap<Integer, Integer>();
        var mapTwoOne = new HashMap<Integer, Integer>();

        for (int i = firstIndex; i <= lastIndex; i++) {
            mapOneTwo.put(nodes1.get(i), nodes2.get(i));
            mapTwoOne.put(nodes2.get(i), nodes1.get(i));
        }

         var newPath1 = new Path(constructChild(nodes1, nodes2, graph.size(), mapTwoOne, interval), 0);
        graph.setPathCost(newPath1);

        var newPath2 = new Path(constructChild(nodes2, nodes1, graph.size(), mapOneTwo, interval), 0);
        graph.setPathCost(newPath2);

        return List.of(newPath1, newPath2);
    }

    private static ArrayList<Integer> constructChild(List<Integer> baseNodes, List<Integer> otherNodes, int size, HashMap<Integer, Integer> mapping, Interval<Integer> interval) {
        var child =  new ArrayList<Integer>();
        int firstIndex = interval.min();
        int lastIndex = interval.max();
        for (int i = 0; i < size; i++) {
            if (i >= firstIndex && i <= lastIndex) {
                child.add(otherNodes.get(i));
                continue;
            }

            var currNode = baseNodes.get(i);
            if (mapping.containsKey(currNode)) {
                var currValue = mapping.get(currNode);
                while (mapping.containsKey(currValue)) {
                    currValue = mapping.get(currValue);
                }
                child.add(currValue);
            } else {
                child.add(baseNodes.get(i));
            }
        }
        return child;
    }

    private static Path rouletteSelection(List<Path> population) {
        Map<Path, Interval<Double>> weights = new HashMap<>(population.size());
        var multiplier = 1 / population.stream().mapToDouble(Path::getCost).sum();
        var start = 0.0;
        for (Path path : population) {
            var length = path.getCost() * multiplier;
            var interval = new Interval<>(start, start + length);
            weights.put(path, interval);
            start += length;
        }

        var rand = RandomGenerator.randomDouble();
        var winner = weights.entrySet()
                .stream()
                .filter(e -> rand >= e.getValue().min() && rand <= e.getValue().max())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Winner found"));

        return winner.getKey();
    }

    private static Path tournamentSelection(List<Path> population, int tournamentSize) {
        List<Integer> usedNodes = new ArrayList<>(tournamentSize);
        List<Path> candidates = new ArrayList<>(tournamentSize);

        while (usedNodes.size() < tournamentSize) {
            var currNode = RandomGenerator.getRandomInt(population.size(), usedNodes);
            usedNodes.add(currNode);
            candidates.add(population.get(currNode));
        }

        return candidates.stream().min(Comparator.comparing(Path::getCost)).orElse(null);
    }
}

