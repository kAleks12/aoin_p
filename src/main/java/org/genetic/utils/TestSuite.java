package org.genetic.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.genetic.alg.GeneticAlgorithm;
import org.genetic.alg.GreedyAlgorithm;
import org.genetic.alg.RandomAlgorithm;
import org.genetic.alg.entities.*;
import org.genetic.utils.entities.Algorithm;
import org.genetic.utils.entities.DistanceMatrix;
import org.genetic.utils.entities.Params;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestSuite {
    private static final Logger logger = LogManager.getLogger(TestSuite.class);

    public static void testMultiple(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) throws IOException {
        var timeBody = getCurrDate();

        var metricsPath = Paths.get("results", "multi", instanceName + '_' + timeBody + ".csv").toString();
        File csvFile = new File(metricsPath);
        try (var fileWriter = new FileWriter(csvFile, true)) {
            fileWriter.write("alg,best,worst,avg,mean\n");
        } catch (IOException e) {
            return;
        }

        Thread randomThread = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                var randomResults = RandomAlgorithm.execute(graph);
                OverviewWriter.saveMetrics(Algorithm.Random.toString() + i, randomResults, metricsPath);
            }
        });
        randomThread.start();

        Thread greedyThread = new Thread(() -> {
            var greedyResults = GreedyAlgorithm.execute(graph);
            OverviewWriter.saveMetrics(Algorithm.Greedy.toString(), greedyResults, metricsPath);
        });
        greedyThread.start();

        Thread geneticThread = new Thread(() -> {
            var geneticResults = new ArrayList<Path>();
            for (int i = 0; i < 10; i++) {
                var result = genetic.execute(graph, null);
                geneticResults.add(result);
            }
            OverviewWriter.saveMetrics(Algorithm.Genetic.toString(), geneticResults, metricsPath);
        });
        geneticThread.start();

        try {
            greedyThread.join();
            geneticThread.join();
            randomThread.join();
        } catch (InterruptedException e) {
            logger.error("Multiple test thread interrupted", e);
        }
    }

    public static void testRun(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) throws IOException {
        var timeBody = getCurrDate();

        var metricsPath = Paths.get("results", "run", instanceName + '_' + timeBody + ".csv").toString();
        var result = genetic.execute(graph, metricsPath);
        logger.info("Instance {} finished with best result: {}", instanceName, result.getCost());
    }

    public static void testParams(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) throws IOException {
        var timeBody = getCurrDate();

        var metricsPath = Paths.get("results", "params", instanceName + '_' + timeBody + ".csv").toString();
        File csvFile = new File(metricsPath);
        try (var fileWriter = new FileWriter(csvFile, true)) {
            fileWriter.write("value,best,worst,avg,mean\n");
        } catch (IOException e) {
            return;
        }

        var thread1 = new Thread(() -> {
            testMutationProbability(genetic, graph, metricsPath);
            testCrossoverProbability(genetic, graph, metricsPath);
        });

        var thread2 = new Thread(() -> {
            testPopSize(genetic, graph, metricsPath);
            testGenerations(genetic, graph, metricsPath);
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Params test thread interrupted", e);
        }

    }

    public static void testFeatures(GeneticAlgorithm genetic, DistanceMatrix graph, String instanceName) {
        var timeBody = getCurrDate();

        var metricsPath = Paths.get("results", "features", instanceName + '_' + timeBody + ".csv").toString();
        File csvFile = new File(metricsPath);
        try (var fileWriter = new FileWriter(csvFile, true)) {
            fileWriter.write("feature,best,worst,avg,mean\n");
        } catch (IOException e) {
            return;
        }

        var thread1 = new Thread(() -> {
            testMutation(genetic, graph, metricsPath);
            testCrossover(genetic, graph, metricsPath);
        });

        var thread2 = new Thread(() -> {
            testInit(genetic, graph, metricsPath);
            testSelect(genetic, graph, metricsPath);
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Feature test thread interrupted", e);
        }
    }

    private static void testMutation(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setMutType(MutationType.Swap);

        testGenetics(List.of(genetic, newGenetic1), graph, metricsPath, Params.Mutation);
    }

    private static void testCrossover(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setCrossoverType(CrossoverType.OX);

        testGenetics(List.of(genetic, newGenetic1), graph, metricsPath, Params.Crossover);
    }

    private static void testInit(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setInitType(InitializationType.Random);

        testGenetics(List.of(genetic, newGenetic1), graph, metricsPath, Params.Init);
    }

    private static void testSelect(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setSelType(SelectionType.Roulette);

        testGenetics(List.of(genetic, newGenetic1), graph, metricsPath, Params.Select);
    }

    private static void testPopSize(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setPopulationSize(100);
        newGenetic1.setEliteSize(10);
        var newGenetic2 = new GeneticAlgorithm(genetic);
        newGenetic2.setPopulationSize(1000);
        newGenetic2.setEliteSize(100);

        testGenetics(List.of(genetic, newGenetic1, newGenetic2), graph, metricsPath, Params.PopSize);
    }

    private static void testGenerations(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setGenerationLimit(300);
        var newGenetic2 = new GeneticAlgorithm(genetic);
        newGenetic2.setGenerationLimit(1500);

        testGenetics(List.of(genetic, newGenetic1, newGenetic2), graph, metricsPath, Params.Generations);
    }

    private static void testMutationProbability(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setMutationProbability(0.01f);
        var newGenetic2 = new GeneticAlgorithm(genetic);
        newGenetic2.setMutationProbability(0.4f);

        testGenetics(List.of(genetic, newGenetic1, newGenetic2), graph, metricsPath, Params.MutationProb);
    }

    private static void testCrossoverProbability(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath) {
        var newGenetic1 = new GeneticAlgorithm(genetic);
        newGenetic1.setCrossoverProbability(0.4f);
        var newGenetic2 = new GeneticAlgorithm(genetic);
        newGenetic2.setCrossoverProbability(0.9f);

        testGenetics(List.of(genetic, newGenetic1, newGenetic2), graph, metricsPath, Params.CrossoverProb);
    }

    private static void testGenetics(List<GeneticAlgorithm> algs, DistanceMatrix graph, String metricsPath, Params param) {
        List<Thread> threads = new ArrayList<>();
        for (var alg: algs) {
            threads.add(getGeneticThread(alg, graph, metricsPath, param));
        }

        for (var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static Thread getGeneticThread(GeneticAlgorithm genetic, DistanceMatrix graph, String metricsPath, Params param) {
        var thread = new Thread(() -> {
            var geneticResults = new ArrayList<Path>();
            for (int i = 0; i < 10; i++) {
                var result = genetic.execute(graph, null);
                geneticResults.add(result);
            }
            switch (param) {
                case PopSize -> OverviewWriter.saveMetrics(
                        "population" + genetic.getPopulationSize(), geneticResults, metricsPath
                );
                case MutationProb -> OverviewWriter.saveMetrics(
                        "mutationProb" + genetic.getMutationProbability(), geneticResults, metricsPath
                );
                case CrossoverProb -> OverviewWriter.saveMetrics(
                        "crossoverProb" + genetic.getCrossoverProbability(), geneticResults, metricsPath
                );
                case Generations -> OverviewWriter.saveMetrics(
                        "generations" + genetic.getGenerationLimit(), geneticResults, metricsPath
                );
                case Init ->  OverviewWriter.saveMetrics(
                        "init" + genetic.getInitType(), geneticResults, metricsPath
                );
                case Select ->  OverviewWriter.saveMetrics(
                        "select" + genetic.getSelType(), geneticResults, metricsPath
                );
                case Mutation ->  OverviewWriter.saveMetrics(
                        "mutation" + genetic.getMutType(), geneticResults, metricsPath
                );
                case Crossover ->  OverviewWriter.saveMetrics(
                        "crossover" + genetic.getCrossoverType(), geneticResults, metricsPath
                );
            }
        });
        thread.start();
        return thread;
    }


    private static String getCurrDate() {
        return LocalDateTime.now()
                .toString()
                .replace(':', '_')
                .replace('.', '_')
                .replace('-', '_')
                .substring(0, 19);
    }
}
