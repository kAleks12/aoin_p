package org.genetic;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.genetic.alg.GeneticAlgorithm;
import org.genetic.alg.GreedyAlgorithm;
import org.genetic.alg.RandomAlgorithm;
import org.genetic.alg.entities.*;
import org.genetic.utils.OverviewWriter;
import org.genetic.utils.TspLoader;
import org.genetic.utils.entities.Algorithm;
import org.genetic.utils.entities.DistanceMatrix;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
//        var instanceList = List.of("berlin52.tsp", "kroA100.tsp", "kroA150.tsp", "kroA200.tsp", "ali535.tsp", "gr666.tsp");
        var instanceList = List.of("ali535.tsp", "gr666.tsp");

        Paths.get("results", "multi").toFile().mkdir();
        Paths.get("results", "genetic").toFile().mkdir();
        for (var instance : instanceList) {
            var graph = TspLoader.load(Paths.get("src", "main", "resources", "data", instance).toString());
            if (graph.isPresent()) {
                logger.info("Graph loaded successfully");

                var genetic = new GeneticAlgorithm.Builder()
                        .setInitializationType(InitializationType.Random)
                        .setMutationType(MutationType.Inverse)
                        .setCrossoverType(CrossoverType.PMX)
                        .setSelectionType(SelectionType.Tournament)
                        .setTournamentSize(10)
                        .setCrossoverProbability(0.7f)
                        .setMutationProbability(0.1f)
                        .setEliteSize(50)
                        .setPopulationSize(500)
                        .setGenerationLimit(1000)
                        .build();

//                testGenetic(genetic, graph.get(), instance);
                testMultiple(genetic, graph.get(), instance);
            } else {
                logger.error("Error while loading graph");
            }
        }
    }

    private static void testGenetic(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) throws IOException {
        for (int i = 0; i < 10; i++) {
            var timeBody = LocalDateTime.now()
                    .toString()
                    .replace(':', '_')
                    .replace('.', '_')
                    .replace('-', '_')
                    .substring(0, 19);

            var metricsPath = Paths.get("results", "genetic", instanceName + '_' + timeBody + ".csv").toString();
            var result = genetic.execute(graph, metricsPath);
            System.out.println(result);
        }
    }

    private static void testMultiple(GeneticAlgorithm genetic, DistanceMatrix graph,String instanceName) throws IOException {
        var timeBody = LocalDateTime.now()
                .toString()
                .replace(':', '_')
                .replace('.', '_')
                .replace('-', '_')
                .substring(0, 19);

        var metricsPath = Paths.get("results", "multi", instanceName + '_' + timeBody + ".csv").toString();

        var randomResults = RandomAlgorithm.execute(graph);
        OverviewWriter.saveMetrics(Algorithm.Random, randomResults, metricsPath, true);

        var greedyResults = GreedyAlgorithm.execute(graph);
        OverviewWriter.saveMetrics(Algorithm.Greedy, greedyResults, metricsPath, false);


        var geneticResults = new ArrayList<Path>();
        for (int i = 0; i < 10; i++) {
            var result = genetic.execute(graph, null);
            geneticResults.add(result);
        }
        OverviewWriter.saveMetrics(Algorithm.Genetic, geneticResults, metricsPath, false);
        logger.info("Instance {} finished", instanceName);
    }
}
