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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        var instanceList = List.of("berlin52.tsp", "kroA100.tsp", "kroA150.tsp", "kroA200.tsp", "ali535.tsp", "gr666.tsp");
//        var instanceList = List.of("ali535.tsp", "gr666.tsp");

        Paths.get("results", "multi").toFile().mkdir();
        Paths.get("results", "genetic").toFile().mkdir();
        var genetic = new GeneticAlgorithm.Builder()
                .setInitializationType(InitializationType.Greedy)
                .setMutationType(MutationType.Inverse)
                .setCrossoverType(CrossoverType.PMX)
                .setSelectionType(SelectionType.Tournament)
                .setTournamentSize(100)
                .setCrossoverProbability(0.7f)
                .setMutationProbability(0.1f)
                .setEliteSize(50)
                .setPopulationSize(500)
                .setGenerationLimit(3000)
                .build();

        List<Thread> threads = getThreads(instanceList, genetic);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }
        }
    }

    private static List<Thread> getThreads(List<String> instanceList, GeneticAlgorithm genetic) {
        List<Thread> threads = new ArrayList<>();
        for (var instance : instanceList) {
            Thread thread = new Thread(() -> {
                var graph = TspLoader.load(Paths.get("src", "main", "resources", "data", instance).toString());
                if (graph.isPresent()) {
                    try {
//                        testMultiple(genetic, graph.get(), instance);
                        testGenetic(genetic, graph.get(), instance);
                    } catch (IOException e) {
                        logger.error("Error during testing", e);
                    }
                } else {
                    logger.error("Error while loading graph");
                }
            });
            threads.add(thread);
            thread.start();
        }
        return threads;
    }

    private static void testGenetic(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) throws IOException {
        var timeBody = LocalDateTime.now()
                .toString()
                .replace(':', '_')
                .replace('.', '_')
                .replace('-', '_')
                .substring(0, 19);

        var metricsPath = Paths.get("results", "genetic", instanceName + '_' + timeBody + ".csv").toString();
        var result = genetic.execute(graph, metricsPath);
        logger.info("Instance {} finished with best result: {}", instanceName, result.getCost());
    }

    private static void testMultiple(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) throws IOException {
        var timeBody = LocalDateTime.now()
                .toString()
                .replace(':', '_')
                .replace('.', '_')
                .replace('-', '_')
                .substring(0, 19);

        var metricsPath = Paths.get("results", "multi", instanceName + '_' + timeBody + ".csv").toString();
        File csvFile = new File(metricsPath);
        try (var fileWriter = new FileWriter(csvFile, true)) {
            fileWriter.write("alg,best,worst,avg,mean\n");
        } catch (IOException e) {
            return;
        }

        Thread randomThread = new Thread(() -> {
            var randomResults = RandomAlgorithm.execute(graph);
            OverviewWriter.saveMetrics(Algorithm.Random, randomResults, metricsPath);
        });
        randomThread.start();

        Thread greedyThread = new Thread(() -> {
            var greedyResults = GreedyAlgorithm.execute(graph);
            OverviewWriter.saveMetrics(Algorithm.Greedy, greedyResults, metricsPath);
        });
        greedyThread.start();

        Thread geneticThread = new Thread(() -> {
            var geneticResults = new ArrayList<Path>();
            for (int i = 0; i < 10; i++) {
                var result = genetic.execute(graph, null);
                geneticResults.add(result);
            }
            OverviewWriter.saveMetrics(Algorithm.Genetic, geneticResults, metricsPath);
            logger.info("Instance {} finished", instanceName);
        });
        geneticThread.start();

        try {
            randomThread.join();
            greedyThread.join();
            geneticThread.join();
        } catch (InterruptedException e) {
            logger.error("Multiple test thread interrupted", e);
        }
    }
}
