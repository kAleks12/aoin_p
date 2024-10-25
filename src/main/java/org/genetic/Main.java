package org.genetic;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.genetic.alg.GeneticAlgorithm;
import org.genetic.alg.entities.CrossoverType;
import org.genetic.alg.entities.InitializationType;
import org.genetic.alg.entities.MutationType;
import org.genetic.alg.entities.SelectionType;
import org.genetic.utils.TspLoader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.genetic.utils.TestSuite.*;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        var instanceList = List.of("berlin52.tsp", "kroA100.tsp", "kroa150.tsp", "kroA200.tsp", "ali535.tsp", "gr666.tsp");

        Paths.get("results", "multi").toFile().mkdir();
        Paths.get("results", "run").toFile().mkdir();
        Paths.get("results", "params").toFile().mkdir();
        Paths.get("results", "features").toFile().mkdir();
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
                .setGenerationLimit(1000)
                .build();

        for (var instance : instanceList) {
            var graph = TspLoader.load(Paths.get("src", "main", "resources", "data", instance).toString());
            if (graph.isPresent()) {
                try {
                    logger.info("Testing instance: " + instance);

//                    logger.info("Starting run tests");
//                    testRun(genetic, graph.get(), instance);
//
//                    logger.info("Starting comparison tests");
//                    testMultiple(genetic, graph.get(), instance);

                    logger.info("Starting params tests");
                    testParams(genetic, graph.get(), instance);

                    logger.info("Starting features tests");
                    testFeatures(genetic, graph.get(), instance);
                } catch (IOException e) {
                    logger.error("Error during testing", e);
                }
            } else {
                logger.error("Error while loading graph");
            }
        }
    }
}